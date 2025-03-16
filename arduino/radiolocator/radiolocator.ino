#include "ServoForRadar.h"
#include "RadarMain.h"

void setup() {
  Serial.begin(115200);
}

ServoForRadar servoRadar(D2, 544, 2600);
RadarMain radarMain(D6, D5);
int timerServo = 0;
int timerRadar = 0;

//D6 Trig
//D5 Echo
void loop() {
  if (millis() - timerServo >= 100) {
    timerServo = millis();
    servoRadar.doServo();
  }

  if (millis() - timerRadar >= 50) {
    timerRadar = millis();
    radarMain.scan();
  }
}