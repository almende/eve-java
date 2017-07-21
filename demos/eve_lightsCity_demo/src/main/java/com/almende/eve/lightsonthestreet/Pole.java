/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 * Author: Luis F. M. Cunha
 */
package com.almende.eve.lightsonthestreet;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.lightsonthestreet.Car.Position;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.TypeUtil;
import com.almende.util.URIUtil;

/**
 * The Class MyFirstAgent.  This class extends Agent to obtain agent capabilities. 
 */
@Access(AccessType.PUBLIC)
public class Pole extends Agent {
    
    // Create a string type using TypeUtil. When the type is used a lot it performs better declaring it this way
    private final static TypeUtil<String>   STRINGTYPE      =  new TypeUtil<String>(){};
    private final static TypeUtil<Integer>  INTTYPE         =  new TypeUtil<Integer>(){};
    private final static TypeUtil<Boolean>  BOOLEANTYPE     =  new TypeUtil<Boolean>(){};
    private final static TypeUtil<Position> POSITIONTYPE    =  new TypeUtil<Position>(){};
    
    private static int numberOfCars = 4;
    
    // Properties
    private int polePositionX = 0; // Change it to a object Position
    private int polePositionY = 0;
    
    private String poleNumber;
    private List<String> neighbors = new ArrayList<String>();
    private Map<String, Integer> polesToAffect = new HashMap<String, Integer>();
    
    private static int poleRange = 25;
    private boolean previousCarDetectedFlag = false;
    /* States strings
        "myLightState"      (int: 0 - 100)  // current light state
        "carDetected"
    */
    
    // The browser will call this method just to create a WS communication 
    public void openConnectionAndStart() throws IOException{
        System.out.println("WS Connection opened with " + getId());
    }
    
    /* Set properties about the pole:
     * poleNumber       -> Number of the pole (e.g.: "2")
     * polePosition     -> Space x position (e.g.: 150)
     * neighbors        -> IDs of the poles that make affect on the light of this pole 
     * polesToAffect    -> Poles IDs with light percentage levels. It will make sure that those poles have AT LEAST that percentage of light
     *                  when a this poles detect a car
    */
    public void setPoleProperties(@Name("poleNumber") String poleNumber, 
            @Name("polePositionX") int polePositionX,
            @Name("polePositionY") int polePositionY,
            //@Name("neighbors") List<String> neighbors,
            @Name("polesToAffect") Map<String, Integer> polesToAffect
            ){
      
        // Set properties
        this.poleNumber = poleNumber;
        //this.polePosition = polePosition;
        this.polePositionX = polePositionX;
        this.polePositionY = polePositionY;
        //this.neighbors = neighbors;
        this.polesToAffect = polesToAffect;
        
        // print pole info
        String poleText = "Pole: " + getId() + 
                "\n\t poleNumber: " + this.poleNumber +
                "\n\t polePositionX: " + this.polePositionX +
                "\n\t polePositionY: " + this.polePositionY +
                "\n\t neighbors: " + this.neighbors +
                "\n\t polesToAffect: " + this.polesToAffect;
                ;
        
        
        //for (Map.Entry<String,Integer> entry : this.polesToAffect.entrySet()) {
        //    String key = entry.getKey();
        //    int value = entry.getValue();
        //    nb = nb + "\n\t\t" + key + ": level = " + value;
        //}
        
        System.out.println(poleText);
    }
    
    /*
     * Start program
     * */
    public void start() throws IOException{
    
        // ---- state initializations ----
        getState().put("myLightState",      100);   // current light intensity of this pole
        getState().put("carDetected",       false); // flag to know if car was detected or not
        
        // set initial state of neighbors memory (Not needed anymore)
        //for(int i = 0; i < this.neighbors.size(); i++){
        //    getState().put(this.neighbors.get(i), 0);
        //    System.out.println("neighbor: " + getId() + " set to: " + getState().get(this.neighbors.get(i), INTTYPE));
        //}
        
        //for (Map.Entry<String,Integer> entry : this.neighbors.entrySet()) {
        //    String key = entry.getKey();
        //    getState().put(key, 0);
        //    System.out.println(getId() + " --> key: " + key + " set to: " + getState().get(key, INTTYPE));
        //}
        
        
        System.out.println("Pole: " + getId() + " is starting to detect!");
        // start detecting
        startDetect();
        
        // After 5 seconds (to make sure all poles were started already) this pole should start to check its own intensity
        schedule("checkLightIntensity", null, DateTime.now().plusSeconds(6));
        
    }
    
    /*
     * Start detection
     * 
     * ( To reduce the amount of messages sent to other poles we could check if the states
     * did change compared with the previous detection then send information just when it really changed!
     * But for now it is just a prove of concept! )
     * */
    public void startDetect() throws IOException{
        
        boolean carDetectedFlag = false;
        for(int i = 0; (i < Pole.numberOfCars); i++){
            int carX = getCarPositionX("carServer" + i);   // Stupid implementation for now
            int carY = getCarPositionY("carServer" + i);            
          //Position carX = getCarPosition();
            
            carDetectedFlag = carDetectedFlag | checkPoleLimits(carX, carY);
            if(carDetectedFlag){
                //System.out.println(getId() + " detected carServer" + i);
                break;
            }
        }
         
        
        // if flag changed then perform a task
        if(previousCarDetectedFlag != carDetectedFlag){
            previousCarDetectedFlag = carDetectedFlag; // change flag
            
            if(carDetectedFlag){
                // change state
                getState().put("carDetected", true);
              
                // send messages poles to turn on
                alertNeighbors("ON");
                
            }else{
                
                getState().put("carDetected", false);
    
                // send messages poles to turn off  
                alertNeighbors("OFF");            
            }
        }
        
        // call the function itself
        schedule("startDetect", null, DateTime.now().plusMillis(100));
    }
    
    /* 
     * This function checks and updates the intensity of this pole
     * */
    public void checkLightIntensity() throws IOException{
       

              
        // get pole light state
        int myLight = getState().get("myLightState", INTTYPE);
        int myNewLight = myLight;
        
        // get carDetected state
        boolean carDetected = getState().get("carDetected", BOOLEANTYPE);        
        
        if(!carDetected){ // if car not detected
                
            int intensity,
            maxIntensity = 0;
            String aux;
            for(int i = 0; i < this.neighbors.size(); i++) {
                
                aux = this.neighbors.get(i);
                
                //System.out.println(getId() + ":"
                //        + "\n\tnbArrayStrings size: " + nbArrayStrings.size()
                //        + "\n\ti: " + i
                //        + "\n\tnbArrayStrings.get(i): " + nbArrayStrings.get(i)
                //        + "\n\tgetState().get(aux, INTTYPE) = " + getState().get(aux, INTTYPE));
                
                intensity = getState().get(aux, INTTYPE);
                maxIntensity = Math.max(maxIntensity, intensity);
            }
          
            // reduce 30%
            myNewLight = myNewLight - 15; 
            
            // get the highest value
            myNewLight = Math.max(maxIntensity, myNewLight);
            
        }else{
            // keep light to 100%
            myNewLight = 100;
        }
         
        // set new state with new information
        getState().put("myLightState", myNewLight);
        updateLightOnBrowser(myNewLight);
        
        // call this function again after a while
        schedule("checkLightIntensity", null, DateTime.now().plusMillis(200));
        
    }
    
    /* This function will send information to the neighbor poles about how much 
     * light intensity they should have at least
     * */
    private void alertNeighbors(String alert) throws IOException {
        
        // list of poles to have affect
        //for (Map.Entry<String,Integer> entry : this.neighbors.entrySet()) {
        //    String key = entry.getKey();
        //    getState().put(key, 0);
        //    System.out.println(getId() + " --> key: " + key + " set to: " + getState().get(key, INTTYPE));
        //}
        
        Params params = new Params();
        URI url;
        String method = "setLightInfo";
        
        // set light intensities
        if(alert.equals("ON")){
            
            for (Map.Entry<String,Integer> entry : this.polesToAffect.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();
                
                // send message to pole to have affect
                params.add("lightIntensity", value);
                params.add("poleInfo", getId());
                url = URIUtil.create("http://localhost:8084/poleagents/" + key);        
                call(url, method, params, null);  
            }
            
            
        }else{

            for (Map.Entry<String,Integer> entry : this.polesToAffect.entrySet()) {
                String key = entry.getKey();
                
                // send message to pole to have affect
                params.add("lightIntensity", 0);
                params.add("poleInfo", getId());
                url = URIUtil.create("http://localhost:8084/poleagents/" + key);        
                call(url, method, params, null);  
            }
        }
        
    }
    
    /* 
     * Neighbors will use this method to inform if they detected a car and how much intensity this pole should set
     * */
    public void setLightInfo(@Name("poleInfo") String poleInfo, @Name("lightIntensity") int lightIntensity){
        
        // save state in memory
        getState().put(poleInfo, lightIntensity);
        
        // if pole doesn't exist in neighbors then add it 
        if(!this.neighbors.contains(poleInfo)){
            this.neighbors.add(poleInfo);
        }
        
    }

    /* 
     * Send intensity message to the pole agent in the browser
     * */
    public void updateLightOnBrowser(@Name("light") int light) throws IOException {
        
        String wsName = "wsclient:poleBrowser" + this.poleNumber;
        URI url = URI.create(wsName);
        String method = "updateLight";
        Params params = new Params();
        params.add("intensity", light);    
        
        caller.callSync(url, method, params, STRINGTYPE);       
    }
    
    /*
     * Check if car is in the range of the pole detection
     * */
    private boolean checkPoleLimits(int carX, int carY) {
        boolean carDetectedFlag = false;
        
        int leftLimit   = this.polePositionX - Pole.poleRange;
        int rightLimit  = this.polePositionX + Pole.poleRange;
        int topLimit    = this.polePositionY - Pole.poleRange;
        int bottomLimit = this.polePositionY + Pole.poleRange;
        
        if( (carX >= leftLimit ) && 
            (carX <= rightLimit) && 
            (carY >= topLimit)   && 
            (carY <= bottomLimit) ){
            carDetectedFlag = true;
        }
        
        return carDetectedFlag;
    }
    
    
    //--------------------------------------------------------------------------------------
    // Stupid implementation until I figure out how to return a serialized object
    /*
     * Ask to the car his position
     * */
    public int getCarPositionX(String carName) throws IOException{
            
        // send a sync message to carServer
        URI url = URIUtil.create("http://localhost:8084/caragents/" + carName);
        String method = "getCarPositionX";
        int x = caller.callSync(url, method, null, INTTYPE);
        return x;
    }   
    
    public int getCarPositionY(String carName) throws IOException{
        
        // send a sync message to carServer
        URI url = URIUtil.create("http://localhost:8084/caragents/" + carName);
        String method = "getCarPositionY";
        int y = caller.callSync(url, method, null, INTTYPE);
        return y;
    }       
    
    //NOT WORKING!! WHY???? HOW CAN I RETURN A STRINGFYED OBJECT???
    public Position getCarPosition() throws IOException{
        //Position newP = new Position(5, 5);
        // send a sync message to carServer
        URI url = URIUtil.create("http://localhost:8084/caragents/carServer0");
        String method = "getCarPosition";
        Position pos = caller.callSync(url, method, null, POSITIONTYPE);
        
        return pos;
    }      
    //--------------------------------------------------------------------------------------
}


