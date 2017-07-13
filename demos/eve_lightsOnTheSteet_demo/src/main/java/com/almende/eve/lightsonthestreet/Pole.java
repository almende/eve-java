/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 * Author: Luis F. M. Cunha
 */
package com.almende.eve.lightsonthestreet;

import java.io.IOException;
import java.net.URI;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
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
    private final static TypeUtil<String> STRINGTYPE =  new TypeUtil<String>(){};
    private final static TypeUtil<Integer> INTTYPE =  new TypeUtil<Integer>(){};
    private final static TypeUtil<Boolean> BOOLEANTYPE =  new TypeUtil<Boolean>(){};
    
    // Properties
    private int polePosition = 30;
    private String poleNumber;
    private String left2PoleID;
    private String left1PoleID;
    private String right1PoleID;
    private String right2PoleID;
    
    private int poleRange = 80;

    
    /* States strings
        "myLightState"      (int: 0 - 100)  // current light state
        "left2PoleInfo";    (int: 0 - 100)  // info dropped from left2Pole
        "left1PoleInfo";    (int: 0 - 100)  // info dropped from left1Pole
        "right1PoleInfo";   (int: 0 - 100)  // info dropped from right1Pole
        "right2PoleInfo";   (int: 0 - 100)  // info dropped from right2Pole
        "carDetected"
    */
    
    // The browser will call this method just to create a WS communication 
    public void openConnectionAndStart() throws IOException{
        System.out.println("Connection opened with " + getId());
    }
    
    /* Set properties about the pole:
     * poleNumber       -> Number of the pole (e.g.: "2")
     * polePosition     -> Space x position (e.g.: 150)
     * left2PoleID      -> ID of the pole located two positions on the left side of this one (e.g.: "pole2" or "NoPole") 
     * left1PoleID      -> ID of the pole located one position on the left side of this one (e.g.: "pole2" or "NoPole")
     * right1PoleID     -> ID of the pole located one positions on the right side of this one (e.g.: "pole2" or "NoPole")
     * right2PoleID     -> ID of the pole located two positions on the right side of this one (e.g.: "pole2" or "NoPole")
    */
    public void setPoleProperties(@Name("poleNumber") String poleNumber, 
            @Name("polePosition") int polePosition,
            @Name("left2PoleID") String left2PoleID, 
            @Name("left1PoleID") String left1PoleID,
            @Name("right1PoleID") String right1PoleID,
            @Name("right2PoleID") String right2PoleID
            ){
        
        // Set properties
        this.polePosition = polePosition;
        this.poleNumber = poleNumber;
        this.left2PoleID = left2PoleID;
        this.left1PoleID = left1PoleID;
        this.right1PoleID = right1PoleID;
        this.right2PoleID = right2PoleID;
        
        String poleText = "Pole: " + getId() + 
                "\n\t poleNumber: " + this.poleNumber +
                "\n\t polePosition: " + this.polePosition +
                "\n\t left2PoleID: " + this.left2PoleID + 
                "\n\t left1PoleID: " + this.left1PoleID + 
                "\n\t right1PoleID: " + this.right1PoleID +
                "\n\t right2PoleID: " + this.right2PoleID;
        System.out.println(poleText);
        
    }
    
    /*
     * Start program
     * */
    public void start() throws IOException{
             
        // state initializations
        getState().put("left2PoleInfo",     0);     // info from the second pole on the left 
        getState().put("left1PoleInfo",     0);     // info from the first pole on the left
        getState().put("myLightState",      100);   // current light intensity of this pole
        getState().put("right1PoleInfo",    0);     // info from the first pole on the right
        getState().put("right2PoleInfo",    0);     // info from the second pole on the right
        getState().put("carDetected",       false); // flag to know if car was detected or not
        
        System.out.println("Pole: " + getId() + " is starting to detect!");
        // start detecting
        startDetect();
        
        // After 5 seconds (to make sure all poles were started already) this pole should start to check its own intensity
        schedule("checkLightIntensity", null, DateTime.now().plusSeconds(5));
    }
    
    /*
     * Start detection
     * 
     * ( To reduce the amount of messages sent to other poles we could check if the states
     * did change compared with the previous detection then send information just when it really changed!
     * But for now it is just a prove of concept! )
     * */
    public void startDetect() throws IOException{
                
        int carPosition = getCarPosition();
        boolean carDetectedFlag = checkPoleLimits(carPosition);
        
        if(carDetectedFlag){
            // change state
            getState().put("carDetected", true);
          
            // send messages to 1st and 2nd left and right poles to turn on
            alertNeighbors("ON");
            
        }else{
            
            getState().put("carDetected", false);

            // send messages to 1st and 2nd left and right poles to turn off  
            alertNeighbors("OFF");            
        }
        
        // call the function itself
        schedule("startDetect", null, DateTime.now().plusMillis(100));
    }
    
    /* 
     * This function checks and updates the intensity of this pole
     * */
    public void checkLightIntensity() throws IOException{
       
        // get info sent by other poles
        int left2PoleInfo = getState().get("left2PoleInfo", INTTYPE);
        int left1PoleInfo = getState().get("left1PoleInfo", INTTYPE);
        int right1PoleInfo = getState().get("right1PoleInfo", INTTYPE);
        int right2PoleInfo = getState().get("right2PoleInfo", INTTYPE);
        
        // get pole light state
        int myLight = getState().get("myLightState", INTTYPE);
        int myNewLight = myLight;
        
        // get carDetected state
        boolean carDetected = getState().get("carDetected", BOOLEANTYPE);        
        
        if(!carDetected){ // if car not detected
                
            // reduce 30%
            myNewLight = myNewLight - 30; 
            
            // Check max intensity given by neighbors
            int maxTemp = Math.max(left2PoleInfo, left1PoleInfo);
            maxTemp = Math.max(maxTemp, right1PoleInfo);
            maxTemp = Math.max(maxTemp, right2PoleInfo);
            
            // get the highest value
            myNewLight = Math.max(maxTemp, myNewLight);
            
        }else{
            // keep light to 100%
            myNewLight = 100;
        }
        
        //System.out.println("" + getId() + " --> myLight: " + myLight + " || carDetected: " + carDetected
        //        + "\n\tleft2PoleInfo: " + left2PoleInfo + " left1PoleInfo: " + left1PoleInfo 
        //        + " right1PoleInfo: " + right1PoleInfo + " right2PoleInfo:" + right2PoleInfo
        //        + "\n\tmyNewLight: " + myNewLight);
        
        // set new state with new information
        getState().put("myLightState", myNewLight);
        updateLightOnBrowser(myNewLight);
        
        // call this function again after a while
        schedule("checkLightIntensity", null, DateTime.now().plusMillis(500));
    }
    
    /* This function will send information to the neighbor poles about how much 
     * light intensity they should have at least
     * */
    private void alertNeighbors(String alert) throws IOException {
        
        int light2Pole;
        int light1Pole;
        Params params = new Params();
        URI url;
        String method = "setLightInfo";
        
        // set light intensities
        if(alert.equals("ON")){
            light2Pole = 40;
            light1Pole = 70;
        }else{
            light2Pole = 0;
            light1Pole = 0;
        }
        
        // If there is a pole
        if( !this.left2PoleID.equals("NoPole") ){
            // send message to left2Pole
            params.add("lightIntensity", light2Pole);
            params.add("poleInfo", "right2PoleInfo");
            url = URIUtil.create("http://localhost:8084/poleagents/" + this.left2PoleID);        
            call(url, method, params, null);  
        }
        
        // If there is a pole
        if( !this.left1PoleID.equals("NoPole") ){
            // send message to left1Pole
            params = new Params();
            params.add("lightIntensity", light1Pole);
            params.add("poleInfo", "right1PoleInfo");
            url = URIUtil.create("http://localhost:8084/poleagents/" + this.left1PoleID);        
            call(url, method, params, null);
        }
        
        // If there is a pole
        if( !this.right1PoleID.equals("NoPole") ){
            // send message to right1Pole
            params = new Params();
            params.add("lightIntensity", light1Pole);
            params.add("poleInfo", "left1PoleInfo");
            url = URIUtil.create("http://localhost:8084/poleagents/" + this.right1PoleID);        
            call(url, method, params, null);
        }
        
        // If there is a pole
        if( !this.right2PoleID.equals("NoPole") ){
            // send message to right2Pole
            params = new Params();
            params.add("lightIntensity", light2Pole);
            params.add("poleInfo", "left2PoleInfo");
            url = URIUtil.create("http://localhost:8084/poleagents/" + this.right2PoleID);        
            call(url, method, params, null);
        }
    }
    
    /* 
     * Neighbors will use this method to inform if they detected a car and how much intensity this pole should set
     * */
    public void setLightInfo(@Name("poleInfo") String poleInfo, @Name("lightIntensity") int lightIntensity){
        getState().put(poleInfo, lightIntensity);
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
    private boolean checkPoleLimits(int carPosition) {
        boolean carDetectedFlag = false;
        
        int leftLimit = this.polePosition - this.poleRange;
        int rightLimit = this.polePosition + this.poleRange;
        if( (carPosition > leftLimit ) && (carPosition < rightLimit) ){
            carDetectedFlag = true;
        }
        
        return carDetectedFlag;
    }
    
    /*
     * Ask to the car his position
     * */
    private int getCarPosition() throws IOException{
        
        // send a sync message to carServer
        URI url = URIUtil.create("http://localhost:8084/caragents/carServer/");
        String method = "getCarPosition";

        // Get car position
        int carPosition = caller.callSync(url, method, null, INTTYPE);
                
        return carPosition;
    }   
    
}


