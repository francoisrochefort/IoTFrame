void setup() {

  Serial.begin(115200);
  while (!Serial);

}

int angle = 0;

void loop() {

  Serial.printf("<AD38%d>", angle);
  delay(250);
  angle++;

}
