import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

class Sensor {

    private State state;
    private int id;
    private ArrayList<Long> lows;

    private Sensor() {
        this.id = -1;
        state = State.HIGH;
        lows = new ArrayList<>();
    }

    Sensor(int id) {
        this.id = id;
        state = State.HIGH;
        lows = new ArrayList<>();
    }

    int getId() {
        return id;
    }

    ArrayList<Long> getLows() {
        return lows;
    }

    void update(long time){
        lows.add(time);
    }

    void save(String _date){

        String filename = "output/" + _date + "_hall_" + this.id + ".csv";

        try{

            PrintWriter pw = new PrintWriter(filename);

            for(long l : this.lows){
                pw.println(l);
            }
            pw.close();
            System.out.println("Data exported to " + filename);
        } catch (Exception e){
            System.err.println("Error exporting to " + filename);
        }

    }
}

enum State {HIGH, LOW}
