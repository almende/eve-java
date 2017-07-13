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

@Access(AccessType.PUBLIC)
public class Car extends Agent {
    
    // Create a string type using TypeUtil. When the type is used a lot of times it performs better declaring it this way
    private final static TypeUtil<String> STRINGTYPE =  new TypeUtil<String>(){};
    
    private String direction = "right"; // right or left
    
    private int carPosition = 0;
    private int rightLimit = 900;   // limit to change direction
    private int leftLimit = 30;     // limit to change direction
    private int velocity = 50;
    
    // constructor
    public Car(){
    }
    
    public void updatePositionToBrowser(int carPosition) throws IOException{
        //System.out.println("Updating car position to: " + carPosition);
        
        URI url = URI.create("wsclient:carBrowser");
        String method = "updatePosition";
        Params params = new Params();
        params.add("x", carPosition);

        caller.callSync(url, method, params, STRINGTYPE);
    }
    
    public void openConnectionAndStart() throws IOException{
        System.out.println("Connection opened with " + getId());
        start();
    }
    
    public void start(){
        schedule("startDrive",null,DateTime.now().plusSeconds(2));
    }
    
    public void startDrive() throws IOException{
        
        // If car is on the limits of the road then change direction
        if(carPosition >= rightLimit){
            direction = "left";
        }else if(carPosition <= leftLimit){
            direction = "right";
        }
        
        // Move car position
        if(direction.equals("right") ){
            carPosition+=5;
        }else{
            carPosition-=5;
        }
        
        updatePositionToBrowser(carPosition);
        
        // Call this function again
        schedule("startDrive", null, DateTime.now().plusMillis(this.velocity));
    }
    
    public int getCarPosition(){
        //System.out.println("Car agent: getCarPosition: " + this.carPosition);
        return this.carPosition;
    }
    
    public String getCarDirection(){
        return this.direction;
    }
    
    public void setVelocity(@Name("vel") int vel){
        this.velocity = vel;
    }
}
