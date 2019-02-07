import com.fazecast.jSerialComm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CVTTesting {

    private static String PORT_KEY = "PORT_NUMBER";
    private static String TIME_KEY = "TIME_FOR_RUN";
    private static String SENSOR_KEY = "NUMBER_OF_SENSORS";

    public static void main(String[] args) throws IOException {

        // Create and load properties
        Properties props = new Properties();
        FileInputStream propsIn = new FileInputStream("config.yaml");
        props.load(propsIn);
        propsIn.close();

        // Initialize sensor objects
        ArrayList<Sensor> sensors = new ArrayList<>();
        int sensorCount = 2;
        if(props.containsKey(SENSOR_KEY)){
            sensorCount = Integer.parseInt((String)props.get(SENSOR_KEY));
        }
        for(int i = 0; i < sensorCount; i++){
            Sensor hall = new Sensor(i);
            sensors.add(hall);
        }

        System.out.println("CVT Testing - ");

        // Display all of the ports
        ArrayList<SerialPort> ports = new ArrayList<>();
        ports.addAll(Arrays.stream(SerialPort.getCommPorts()).collect(Collectors.toList()));

        for(int i = 0; i < ports.size(); i++){
            System.out.println(i + " : " + ports.get(i));
        }

        // Load port from config or prompt user
        int portNum = -1;
        if(props.containsKey(PORT_KEY)){
            portNum = Integer.parseInt((String)props.get(PORT_KEY));
            System.out.println("Opening on port " + portNum + " - " + ports.get(portNum));
        } else {
            System.out.print("Please enter which port you would like - ");
            Scanner in = new Scanner(System.in);

            portNum = in.nextInt();
        }

        if(portNum < 0 || portNum >= ports.size()){
            System.out.println("Unknown port");
            System.exit(1);
        }

        // Load and open port
        SerialPort sp = ports.get(portNum);
        sp.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
        sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0); // block until bytes can be written

        if (sp.openPort()) {
            System.out.println("Port is open");
        } else {
            System.out.println("Failed to open port");
            System.exit(1);
        }

        // Add data listener and init StringBuilder
        StringBuilder sb = new StringBuilder();

        sp.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }

                System.out.println("SerialEvent Received");

                try {
                    byte[] newData = new byte[sp.bytesAvailable()];

                    // Should be 32 bytes long of data on alternating reads
                    int numRead = sp.readBytes(newData, newData.length);

                    if(numRead > 0) {
                        sb.append(new String(newData, 0, numRead));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        System.out.println("Testing can begin...");

        // Load time to sleep and sleep
        int sleepSeconds = 10;
        if(props.containsKey(TIME_KEY)){
            sleepSeconds = Integer.parseInt((String)props.get(TIME_KEY));
        }

        try {
            TimeUnit.SECONDS.sleep(sleepSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Done retrieving data");
        sp.removeDataListener();

        String[] lines = sb.toString().split("\r\n");

        for(String s : lines){
            String[] tokens = s.split(":");
            int key = Integer.parseInt(tokens[0]);
            long value = Long.parseLong(tokens[1]);

            boolean validKey = false;
            for(Sensor _sensor : sensors){
                if(_sensor.getId() == key){
                    _sensor.update(value);
                    validKey = true;
                }
            }

            if(!validKey) {
                System.err.println("Invalid key");
                //System.exit(1);
            }
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH_mm_ss_MM_dd_YYYY");
        Date _date = now.getTime();
        String dateString = format.format(_date);

        for(Sensor _sensor : sensors){
            _sensor.save(dateString);
        }

    }
}
