// This #include statement was automatically added by the Particle IDE.
#include "RelayShield.h"

// Create an instance of the RelayShield library, so we have something to talk to
RelayShield myRelays;
int relayOn(String command);
int relayOff(String command);

int relayState[] = {0, 0, 0, 0, 0};

STARTUP(WiFi.selectAntenna(ANT_EXTERNAL)); // selects the u.FL antenna


void setup() {
    //.begin() sets up a couple of things and is necessary to use the rest of the functions
    myRelays.begin();
    Particle.function("relayOn", relayOn);
    Particle.function("relayOff", relayOff);

    Particle.variable("valve1", &relayState[1], INT);
    Particle.variable("valve2", &relayState[2], INT);
    Particle.variable("valve3", &relayState[3], INT);
    Particle.variable("valve4", &relayState[4], INT);
}

void loop() {
    // Nothing needed here, functions will just run when called
}

int relayOn(String command){
    Particle.publish("Sprinkler Valve On", command);
    // Ritual incantation to convert String into Int
    char inputStr[64];
    command.toCharArray(inputStr,64);
    int i = atoi(inputStr);

    // Turn the desired relay on
    myRelays.on(i);
    relayState[i] = 1;

    // Respond
    return 1;
}

int relayOff(String command){
    Particle.publish("Sprinkler Valve Off", command);
    // Ritual incantation to convert String into Int
    char inputStr[64];
    command.toCharArray(inputStr,64);
    int i = atoi(inputStr);

    // Turn the desired relay off
    myRelays.off(i);
    relayState[i] = 0;

    // Respond
    return 1;
}