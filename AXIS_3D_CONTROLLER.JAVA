import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import oscP5.*; 
import netP5.*; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class AXIS_3D_CONTROLLER extends PApplet {


//--------------------------------------------------------------------------GLOBAL VARS

  

    //----------import controlP5 library

ControlP5 cp5;   //----------------controlP5 object

OscP5 oscP5;
NetAddress R;
NetAddress R1;
NetAddress R2;
NetAddress R3;
NetAddress R4;
NetAddress R5;
NetAddress R6;
NetAddress R7;
NetAddress R8;
NetAddress R9;
NetAddress R10;
NetAddress R11;

static final int BAUDS = 9600;

Serial myPort;  //-----------------Create object from Serial class
int connection;      //------------Data received from the serial port
int i = 0;               //--------frame count

int knobRadius = 45;
int knobX = 427; int knobY = 367;
int ledX = 630; int ledY = 50;
int ledRadius = 25;
int textX = 140; int m = 15;

int padding = 30;

int sensorSizeX = 300; int sensorSizeY = 440;

boolean serialState = false;
boolean comState = false;

String[] comPort;


int[] serialInArray = new int[11]; //--------Where we'll put what we receive
int serialCount = 0;     //------------------A count of how many bytes we receive
int x, y, z, p1, p2 ,p3, b1, b2, b3, z1, z2;//----------Sensor Values
boolean firstContact = false;  //-----------------------Whether we've heard from the
                               //-----------------------microcontroller
boolean connectionEstablished = false;

int intervalTime = 30;
int prevTime = 0;

int f = color(0);
int c = color(0);
int d = color (20,50,255);
int e = color (20,50,255);
int g = color (20,50,255);

int w;

PFont ibm;

boolean toggleValue = false;
int col = color(255);

float t;
float r=1;

float xx ,yy ,zz , ppp1, ppp2, ppp3, bbb1, bbb2, bbb3, zzz1, zzz2;

//-------------------------------------------------------------------------------------------------//
public void setup(){
  
  
  background(255);
  
  comCheck();
  comSelect();
  
  oscP5 = new OscP5(this,8001);
  R = new NetAddress("127.0.0.1",100);
  R1 = new NetAddress("127.0.0.1",101);
  R2 = new NetAddress("127.0.0.1",102);
  R3 = new NetAddress("127.0.0.1",103);
  R4 = new NetAddress("127.0.0.1",104);
  R5 = new NetAddress("127.0.0.1",105);
  R6 = new NetAddress("127.0.0.1",106);
  R7 = new NetAddress("127.0.0.1",107);
  R8 = new NetAddress("127.0.0.1",108);
  R9 = new NetAddress("127.0.0.1",109);
  R10 = new NetAddress("127.0.0.1",110);
  R11 = new NetAddress("127.0.0.1",120);
  
  ibm = createFont("IBM.ttf", 32);
  
  cp5 = new ControlP5(this);
  //--------------------------------------------Create a toggle and change the default look to a (on/off) switch look
          cp5.addToggle("toggle")
               .setPosition(407,85)
               .setSize(40,10)
               .setValue(true)
               .setMode(ControlP5.SWITCH)
               ;

}

//-----------------------------------------------------------------LOOP
public void draw(){
      
      //----------------------IF SERIAL DATA IS COMING THOURGH LAUNCH AXIS
      if ( serialState) {
        runAxis();        
      }
      else {
        fill(0);
        rect(0,0,width,height);   
      } 

      sendDataOSC();
      
      //------------------RANDOM NOISE   
      t = noise(random(r));
      
      //------------------MAPPING OSC VALUES
      xx = map(PApplet.parseFloat(x), 0,255,0,1);
      yy = map(PApplet.parseFloat(y), 0,255,0,1);
      zz = map(PApplet.parseFloat(z), 0,255,0,1);
      ppp1 = map(PApplet.parseFloat(p1), 0,255,0,1);
      ppp2 = map(PApplet.parseFloat(p2), 0,255,0,1);
      ppp3 = map(PApplet.parseFloat(p3), 0,255,0,1);
      bbb1 = map(PApplet.parseFloat(b1), 0,255,0,1);
      bbb2 = map(PApplet.parseFloat(b2), 0,255,0,1);
      bbb3 = map(PApplet.parseFloat(b3), 0,255,0,1);
      zzz1 = map(PApplet.parseFloat(z1), 0,255,0,1);
      zzz2 = map(PApplet.parseFloat(z2), 0,255,0,1);
      
      
}
//------------------------------------------------------------------------

//---------------------------------LISTEN TO ALL AVAILABLE SERIAL PORTS AND ASIGN [0] TO comPort
//---------------------------------
public void comCheck(){
     
          printArray("Serial ports available: "); 
          printArray(Serial.list());
      
          comPort = Serial.list();
          
}
    
//----------------------------------------------------ASSIGN THE CORRECT PORT FOR THE ARDUINO
public void comSelect(){
      
        comState = false;
        while(!serialState)
          {
          for (int i = 0; i< (Serial.list()).length; i++)
            {
            if ((Serial.list()[i]).equals(comPort[0]))
              {
              comState = true;  
              }
            }
            
          if (comState)
            {
            println("Axis UNO is connected");
            println("sending OSC: 12001");
            myPort = new Serial (this, comPort[0], 9600);
            //myPort.bufferUntil(':');
 
            serialState = true;
            }
          else
            {
            //println("Connect Arduino");
            serialState = false;
            fill(0);
            rect(0,0,width,height);  
            }
          }
        }

//-----------------------------------------LISTEN TO SERIAL DATA AND ASSIGN TO VAR
public void serialEvent(Serial myPort) {
  
         // read a byte from the serial port:
         int inByte = myPort.read();
         
         // if this is the first byte received, and it's an A,
         // clear the serial buffer and note that you've
         // had first contact from the microcontroller.
         // Otherwise, add the incoming byte to the array:
      if (firstContact == false) {
         if (inByte == 'A') {
          myPort.clear();   // clear the serial port buffer
          firstContact = true;  // you've had first contact from the microcontroller
          myPort.write('A');  // ask for more
         }
         }
         
      else {
         // Add the latest byte from the serial port to array:
         serialInArray[serialCount] = inByte;
         serialCount++;
         
         // AXIS IS SENDING 11 VALUES
         if (serialCount > 10) {
          x = serialInArray[0];
          y = serialInArray[1];
          z = serialInArray[2];
          p1 = serialInArray[3];
          p2 = serialInArray[4];
          p3 = serialInArray[5];
          b1 = serialInArray[6];
          b2 = serialInArray[7];
          b3 = serialInArray[8];
          z1 = serialInArray[9];
          z2 = serialInArray[10];
          
          // print the values (for debugging purposes only):
          // println(x + "-" + y + "-"+ "-" + z + "-"+ "-" + p1 + "-"+ "-" + p2 + "-" + "-" + p3 + "-"+ "-" + b1 + "-"+ "-" + b2 + "-"+ "-" + b3 + "-"+ "-" + z1 + "-"+ "-" + z2 + ".");
          // Send a capital A to request new sensor readings:
          //println(x);
          myPort.write('A');
          // Reset serialCount:
          serialCount = 0;
         }
     }
}


//--------------------------------------RUN PROGRAM
public void runAxis(){
      
      //--------LOADING SCREEN
      fill(255);
      rect(0,0,width,height);
      w = 350;
      i++;
      textSize(40);
      fill(c);
      strokeWeight(0);
      textFont(ibm);
      textAlign(CENTER);
      text("AXIS CONNECTED",w,height/2);
      
      
      //-------------------------AFTER CONNECTION ESTABLISHED LAUNCH AXIS UNO GUI
      if( i > 50 ){
          
          c = color(0,0);
          w --;
          
          //-------BG
          pushMatrix();
          fill(78, 0, 55);
          stroke(0);
          strokeWeight(12);
          rectMode(CORNER);
          rect(0,0,width,height);
          popMatrix();
          
          //------------------ SKYWRITER SENSOR POS
          pushMatrix();
          fill(0,180);
          stroke(20,50,255);
          strokeWeight(3);
          rectMode(CORNER);
          rect(padding,padding,sensorSizeX,sensorSizeY);
          popMatrix();
          
          //---------KNOBS
          knob(knobX,knobY);
          knob1(knobX + 100, knobY);
          knob2(knobX + 200, knobY);
          
          
          //---------POWER LED
          pushMatrix();
          fill(0,255,100);
          stroke(45);
          strokeWeight(3);
          ellipse(ledX, ledY, ledRadius, ledRadius);
          popMatrix();
          
          //---------BLUE LED CONFIRMS SERIAL DATA IS COMING FROM THE AXIS
          pushMatrix();
          stroke(45);
          fill(f);
          if (x > 0){
          f = color(20,50,255);
          }
          ellipse(ledX, ledY + 30, ledRadius, ledRadius);
          popMatrix();
          
          //--------ELLIPSE INDICATING HAND LOCATION OVER SENSOR, STROKE BRIGHTNESS REVEALS Z POSITION
          pushMatrix();
          fill(x,y,x);
          stroke(z);
          ellipse(map(PApplet.parseFloat(y),0,255,40,310),map(PApplet.parseFloat(x),0,255,40,450),25,25);
          popMatrix();
          
          //---------BUTTONS ON THE AXIS CHANGING COLOR ON PRESS
          pushMatrix();
          stroke(0,10,50);
          strokeWeight(10);
          rectMode(CENTER);
          fill(d);
          if(b1 == 1){ d = color(15);}
          if(b1 == 0){ d = color(20,50,255);}
          rect(knobX,knobY+75, knobRadius, knobRadius);
          if(b2 == 1){ e = color(15);}
          if(b2 == 0){ e = color(20,50,255);}
          fill(e);
          rect(knobX + 100,knobY+75, knobRadius, knobRadius);
          if(b3 == 1){ g = color(15);}
          if(b3 == 0){ g = color(20,50,255);}
          fill(g);
          rect(knobX + 200,knobY+75, knobRadius, knobRadius);
          popMatrix();
          
        
          //-------------------TOGGLE MONITOR AND CHANGE ELLIPSE COLOR 
          pushMatrix();
          if(toggleValue==true) {
            fill(255,255,220);
          } else {
            fill(128,128,110);
          }
          fill(col);
          ellipse(427,50,40,40);
          popMatrix();
          
          pushMatrix();
          textSize(10);
          fill(255);
          text(t,knobX,textX);
          text(x,knobX,textX + m);
          text(y,knobX,textX + m*2);
          text(z,knobX,textX + m*3);
          text(p1,knobX,textX + m*4);
          text(p2,knobX,textX + m*5);
          text(p3,knobX,textX + m*6);
          text(b1,knobX,textX + m*7);
          text(b2,knobX,textX + m*8);
          text(b3,knobX,textX + m*9);
          text(z1,knobX,textX + m*10);
          text(z2,knobX,textX + m*11);
          
          popMatrix();
          
          
          
          
         }
      
      
         //print(i + "-");
      
}

//------------------------------------------------------KNOBS

public void knob(float x, float y){

          pushMatrix();
          
          fill(20,50,255);
          stroke(0,10,50);
          strokeWeight(10);
          ellipseMode(CENTER);
          
          ellipse(x,y,knobRadius,knobRadius);
          fill(255);
          translate(x,y);
          rotate(map(PApplet.parseFloat(p1),0,255,-1,2.5f));
          ellipse(-10,-10,knobRadius -30, knobRadius-30);
          popMatrix();

}

public void knob1(float x, float y){

          pushMatrix();
          
          fill(20,50,255);
          stroke(0,10,50);
          strokeWeight(10);
          ellipseMode(CENTER);
          
          ellipse(x,y,knobRadius,knobRadius);
          fill(255);
          translate(x,y);
          rotate(map(PApplet.parseFloat(p2),0,255,-1,2.5f));
          ellipse(-10,-10,knobRadius -30, knobRadius-30);
          popMatrix();

}
public void knob2(float x, float y){

          pushMatrix();
          
          fill(20,50,255);
          stroke(0,10,50);
          strokeWeight(10);
          ellipseMode(CENTER);
          
          ellipse(x,y,knobRadius,knobRadius);
          fill(255);
          translate(x,y);
          rotate(map(PApplet.parseFloat(p3),0,255,-1,2.5f));
          ellipse(-10,-10,knobRadius -30, knobRadius-30);
          popMatrix();

}

//-------------------SEND OSC OVER PORT 12001
public void sendDataOSC(){
  
      //------assignement
      OscMessage xPos = new OscMessage("/x");
      OscMessage yPos = new OscMessage("/y");
      OscMessage zPos = new OscMessage("/z");
      OscMessage pp1 = new OscMessage("/p1");
      OscMessage pp2 = new OscMessage("/p2");
      OscMessage pp3 = new OscMessage("/p3");
      OscMessage bb1 = new OscMessage("/b1");
      OscMessage bb2 = new OscMessage("/b2");
      OscMessage bb3 = new OscMessage("/b3");
      OscMessage zz1 = new OscMessage("/z1");
      OscMessage zz2 = new OscMessage("/z2");
      OscMessage random = new OscMessage("/random");
      
      //-------add the sensor values to the message
      xPos.add(xx);
      yPos.add(yy);
      zPos.add(zz);
      pp1.add(ppp1);
      pp2.add(ppp2);
      pp3.add(ppp3);
      bb1.add(bbb1);
      bb2.add(bbb2);
      bb3.add(bbb3);
      zz1.add(zzz1);
      zz2.add(zzz2);
      random.add(t);
    
      //------send the message
      oscP5.send(xPos, R); 
      oscP5.send(yPos, R1);
      oscP5.send(zPos, R2); 
      oscP5.send(pp1, R3);
      oscP5.send(pp2, R4); 
      oscP5.send(pp3, R5);
      oscP5.send(bb1, R6); 
      oscP5.send(bb2, R7);
      oscP5.send(bb3, R8);
      oscP5.send(zz1, R9); 
      oscP5.send(zz2, R10);
      oscP5.send(random, R11);

}

//----------------------------------MONITOR TOGGLE
public void toggle(boolean theFlag) {
      
      //-------changing the ellipse color on toggle
      if(theFlag==true) {
        col = color(0,255,100);;
        r = 1;
      } else {
        col = color (78, 0, 55);;
        r = 0;
      }
      //println("a toggle event.");
      
}
  public void settings() {  size(700, 500);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "AXIS_3D_CONTROLLER" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
