// This #include statement was automatically added by the Particle IDE.
#include "RelayShield.h"

// Create an instance of the RelayShield library, so we have something to talk to
RelayShield sprinklers;
int relayOn(String command);
int relayOff(String command);

// Create an array to track the states of relays for polling purposes
int relayState[] = {0, 0, 0, 0, 0};


void setup() {
    //.begin() sets up a couple of things and is necessary to use the rest of the functions
    sprinklers.begin();

    // ensure that on initial boot all of the sprinkler valves are closed
    sprinklers.allOff();

    // define the on and off functions for the relays so that SmartThings can remotely control them
    Particle.function("relayOn", relayOn);
    Particle.function("relayOff", relayOff);
    
    // define variables to be queryable through the api to track relay states
    Particle.variable("relay1", &relayState[1], INT);
    Particle.variable("relay2", &relayState[2], INT);
    Particle.variable("relay3", &relayState[3], INT);
    Particle.variable("relay4", &relayState[4], INT);
}

void loop() {
    // Nothing needed here, functions will just run when called
}

int relayOn(String command){
    Particle.publish("Relay On", command);
    // convert String into Int
    char inputStr[64];
    command.toCharArray(inputStr,64);
    int i = atoi(inputStr);

    // turn the desired relay on
    myRelays.on(i);

    // update the relay state so it can be tracked
    relayState[i] = 1;

    // Respond
    return 1;
}

int relayOff(String command){
    Particle.publish("Relay Off", command);
    // convert String into Int
    char inputStr[64];
    command.toCharArray(inputStr,64);
    int i = atoi(inputStr);

    // turn the desired relay off
    myRelays.off(i);

    // update the relay state so it can be tracked
    relayState[i] = 0;

    // Respond
    return 1;
}