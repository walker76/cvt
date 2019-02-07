void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  while (!Serial) {
    ; // wait for serial port to connect.
  }

  //Serial.print("Setup Complete. Data collection begin\n\n");

}
int hallPin1 = 6;
int hallPin2 = 7;

bool magDetect1 = 0;
bool magDetect2 = 0;

void loop() {

  magDetect1 = digitalRead(hallPin1);
  magDetect2 = digitalRead(hallPin2);

  Serial.print(magDetect1);
  Serial.print(magDetect2);

  delay(10);



}

// jserialcomm
// 
