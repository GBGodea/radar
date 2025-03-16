//d1 - trig pin
//d2 - echo pin

#ifndef RadarMain_h
#define RadarMain_h
#include <Arduino.h>

class RadarMain {
  public:
    RadarMain(short int trigPin, short int echoPin
    // , short int lamp
    ) 
    : trigPin(trigPin), echoPin(echoPin)
    // , lamp(lamp)
     {
      pinMode(trigPin, OUTPUT);
      pinMode(echoPin, INPUT);
      
      // pinMode(lamp, OUTPUT);
    }

    void scan() {
        int duration, cm;

        digitalWrite(trigPin, LOW);
        delayMicroseconds(2);

        digitalWrite(trigPin, HIGH);
        delayMicroseconds(10);
        digitalWrite(trigPin, LOW);

        duration = pulseIn(echoPin, HIGH);

        cm = duration / 58;

        // blink(cm);

        Serial.print("Radar: ");
        Serial.println(cm);
    }

    private:
      short int trigPin;
      short int echoPin;

      // int lamp;
      // void blink(int cm) {
      //   if(cm <= 100) {
      //     digitalWrite(lamp, HIGH);
      //   } else {
      //     digitalWrite(lamp, LOW);
      //   }
      // }
};

#endif