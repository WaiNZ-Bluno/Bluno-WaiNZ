#include <JsonGenerator.h>
#include <JsonParser.h>

/** This is the code we used to test our app using a Bluno, as the water quality 
/*  devices were not yet ready. This functions pretty much identically to the 
/*  real devices except for the generation of random values rather than real
/*  river quality measurements. **/

double temperature,ECcurrent; 
int messageID;
//These can't be strings due to some complication in the JSON parser
char* statusByte; 
char* device_id;
char* mobile_dev;
char* cmd;
char* session_id;
char* gps_location;
char* time_stamp;



void setup() {
  // initialize serial communication with computer:
  Serial.begin(115200);
  //Initialise stuff so that it's known when no test has been run
  messageID = 0;
  statusByte = "none";
  device_id = "001";
  mobile_dev = "none";
  cmd = "status";
  session_id = "0";
  gps_location = "none";
  time_stamp = "none";
  
  
}

void loop() {
  readSerial(); 
  if (strcmp(cmd,"test")==0){
    //If test make some random values, somewhat in line with actual probablities
    temperature = random(0, 45);
    ECcurrent = random(0, 2500);
    //delay to simulate test time
    delay(1000);
    //Complete status
    statusByte = "complete";
    cmd = "status";
    //Ugly string json formatting, but it breaks if you use the library
    String str;
    str +="{\"gps\":\"";
    str+=gps_location;
    str+="\",\"time\":\"";
    str+=time_stamp;
    //status now included in this packet to say test complete, makes life easier for us THIS IS NEW AND NOT IN API
    str+="\",\"status\":\"";
    str+=statusByte;
    str+="\",\"session\":\"";
    str+=session_id;
    char buf[10];
    dtostrf(temperature,2,2,buf);
    str+="\",\"temp\":\"";
    //numbers to string are fun in Arduino land
    str+=(String)buf;
    dtostrf(ECcurrent,2,2,buf);
    str+="\",\"ec\":\"";
    str+=(String)buf;
    str+="\"}";
    //send the data
    Serial.println(str);
    messageID++;
    statusByte = "idle";
    
  }
  //dunno what to do for calibration yet
  else if (strcmp(cmd,"cali")==0){
    
  }
  //this is all random status probing so we can test stuff
  else if (strcmp(cmd,"status")==0){
    int statusProb = random(1, 100);
    if(strcmp(cmd,"busy")==0){
      if (statusProb<40){
        statusByte = "idle";}
      else if(statusProb<50){
        statusByte = "fatal";}
      else if(statusProb<60){
        statusByte = "bt4le";}
      else if(statusProb<70){
        statusByte = "temp";}
      else if(statusProb<80){
        statusByte = "ec";}
      else if(statusProb<90){
        statusByte = "ph";}
      else if(statusProb<100){
        statusByte = "water";}
     }
  }
}

//generating short JSON messages do work, but longer ones get scrambled as the stack fills
void writeStatusJsonToSerial(){
   using namespace ArduinoJson::Generator;
   JsonObject<1> root; 
   root.add("status", statusByte);
   Serial.println(root); 
}

void readSerial(){
   if (Serial.available()){
       // Serial has a 10 second timeout
       char json[150]; // Allocate some space for the string
       char inChar; // Where to store the character read
       byte index = 0; // Index into array; where to store the character
       unsigned long start_time;
       start_time = millis();
       while(true){
         if(Serial.available() > 0){
           if(index < 149){ // One less than the size of the array
             inChar = Serial.read(); // Read a character
             json[index] = inChar; // Store it
             index++; // Increment where to write next
             json[index] = '\0'; // Null terminate the string
           }
         }
         // if the last character is a char(13) or greater than ten seconds have passed, break out and return.
         if(inChar ==char(125) || ((millis() - start_time) > 10000)){
           break;
         }
       }
       writeSerialToJson(json);
  }
}

void writeSerialToJson(char* json){
      using namespace ArduinoJson::Parser;
      JsonParser<16> parser;
      JsonObject root = parser.parse(json);
  
      if (!root.success())
      {
          Serial.println("JsonParser.parse() failed");
          return;
      }
  
      cmd = root["cmd"];
      if(strcmp(cmd,"init")==0){
          mobile_dev = root["dev"];
          // variable success rate of initialisation
          int success = random(0, 9);
           if (success != 9){
             statusByte = "idle";}
           else{
              statusByte = "fatal";}
      }else if(strcmp(cmd,"test")==0){
        //start a test if initialised
        if (strcmp(mobile_dev,"none")){
          statusByte = "busy";
          session_id = root["session"];
          gps_location = root["gps"];
          time_stamp = root["time"];
        }

        
      }else if(strcmp(cmd,"cali")==0){
        //this is a remnant from the bluetooth team, might be helpful?
          char* time_stamp = root["time"];
          double min_temp = root["temp"][0];
          double max_temp = root["temp"][1];
          double min_ec = root["ec"][0];
          double max_ec = root["ec"][1];
      }else if(strcmp(cmd,"status")==0){
      }else{
        Serial.println("cmd error");
        Serial.println(cmd);
      }
      writeStatusJsonToSerial() ;
}

