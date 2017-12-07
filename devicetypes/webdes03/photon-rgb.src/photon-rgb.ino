// define the pins used on the photon
#define REDPIN D0
#define GREENPIN D1
#define BLUEPIN D2
 
// variables to store the current color
int red = 0;
int green = 0;
int blue = 0;

// variables to store the previous color
int _red = 0;
int _green = 0;
int _blue = 0;

// define the fade speed for color changes
// turning on, and turning off (ms)
int fadeSpeed = 10;

// setup is run when the device boots and
// connects to the internet
void setup() {
  
  // set the photon pin modes
  pinMode(REDPIN, OUTPUT);
  pinMode(GREENPIN, OUTPUT);
  pinMode(BLUEPIN, OUTPUT);
  Particle.publish("Setup", "Device booted");
  
  // wire up the inbound functions
  Particle.function("color", setColor);
  Particle.function("on", turnOn);
  Particle.function("off", turnOff);
  Particle.function("status", getStatus);
  
  // update the strip with 000,000,000 just
  // to make sure it's off at boot
  updateStrip();
  
}
 
 
void loop() {
    // nothing needed here
}

void updateStrip() {
    
    // variables to track individual channel states
    // throughout the fade
    int c_red = _red;
    int c_green = _green;
    int c_blue = _blue;
    
    // placeholder of the color before the fade started
    int p_red = _red;
    int p_green = _green;
    int p_blue = _blue;
    
    // as long as at least one of the colors is not yet
    // what it needs to be, loop through the fade
    while ( (c_red != red) || (c_green != green) || (c_blue != blue) ) {
        
        // if current red is not target red; if it's less
        // than it needs to be, add one and paint; if it's
        // more than it needs to be, subtract one and paint
        if (c_red != red) {
            if (c_red < red) {
                c_red++;
                analogWrite(REDPIN, c_red);
            } else {
                c_red--;
                analogWrite(REDPIN, c_red);
            }
        }
        
        // if current green is not target green; if it's less
        // than it needs to be, add one and paint; if it's
        // more than it needs to be, subtract one and paint
        if (c_green != green) {
            if (c_green < green) {
                c_green++;
                analogWrite(GREENPIN, c_green);
            } else {
                c_green--;
                analogWrite(GREENPIN, c_green);
            }
        }
        
        // if current blue is not target blue; if it's less
        // than it needs to be, add one and paint; if it's
        // more than it needs to be, subtract one and paint
        if (c_blue != blue) {
            if (c_blue < blue) {
                c_blue++;
                analogWrite(BLUEPIN, c_blue);
            } else {
                c_blue--;
                analogWrite(BLUEPIN, c_blue);
            }
        }
        
        // wait the fade delay time before running the loop
        // again if needed
        delay(fadeSpeed);
    }
    
    // current colors now match the target colors; store
    // the color later so that we can revert back to the last
    // color used when the strip is turned back on again;
    // only do this if all colors are greater than 0, meaning
    // the strip is not off, otherwise use the color from the
    //beginning of this method
    if ( (red > 0) && (green > 0) && (blue > 0) ) {
        Particle.publish("Debug", "Storing current color, not off.");
        _red = red; _green = green; _blue = blue;
    } else {
        Particle.publish("Debug", "Storing previous color.");
        _red = p_red; _green = p_green; _blue = p_blue;
    }
    
}

// called when the 'on' command is sent to the api
int turnOn(String command) {
    
    Particle.publish("Command", "On");
    
    // is there a previously stored color?
    if (_red > 0 || _green > 0 || _blue > 0) {
        
        // set the current color to the previous color
        // and update the strip
        red = _red; green = _green; blue = _blue;
        _red = 0; _green = 0; _blue = 0;
        updateStrip();
        
    } else {
        
        // no previous color, so set to white and update
        // the strip
        red = 255; green = 255; blue = 255;
        updateStrip();    
    }
    
    // return 1 tells SmartThings that the call succeeded
    return 1;
    
}

// called when the 'off' command is sent to the api
int turnOff(String command) {
    
    Particle.publish("Command", "Off");
    
    // set RGB to 0, but don't erase the previous color
    // so that we can use that when we turn back on
    red = 0; green = 0; blue = 0;
    
    updateStrip();
    
    // return 1 tells SmartThings that the call succeeded
    return 1;
    
}

// called when the 'color' command is sent to the api
// expects command to be a RRR,GGG,BBB formatted string
int setColor(String command) {
    
    Particle.publish("Command", command);
    
    // parse the RRR,GGG,BBB value that comes back to
    // separate each color
    red = command.substring(0,2).toInt();
    green = command.substring(4,6).toInt();
    blue = command.substring(8,10).toInt();
    
    // update the strip to display the new color
    updateStrip();
    
    // return 1 tells SmartThings that the call succeeded
    return 1;
    
}

// called when the 'status' command is sent to the api
// returns 1 if any RGB value is greater than 0 which
// indicates that the strip is on
int getStatus(String command) {
    
    Particle.publish("Command", "Status");
    
    // check to see if any color values are greater
    // than zero
    if (red > 0 || green > 0 || blue > 0) {
        
        // return 1 to tell SmartThings that the strip
        // is on
        return 1;
        
    } else {
        
        // return 0 to tell SmartThings that the strip
        // is off
        return 0;
        
    }
    
}