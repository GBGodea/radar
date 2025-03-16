#pragma once
#include <Arduino.h>
#include <Servo.h>

class ServoForRadar {
public:
  ServoForRadar(short int pin, short int min, short int max) {
    servo.attach(pin, min, max);
  }

  void doServo() {
    Serial.print("Servo: ");
    Serial.println(servoAngle);
    servo.write(servoAngle);


    if (flag) {
      servoAngle++;
    } else {
      servoAngle--;
    }


    if (servoAngle >= 180 || servoAngle <= 0) {
      flag = !flag;
    }
  }

private:
  bool flag = true;
  int servoAngle = 0;
  Servo servo;
};