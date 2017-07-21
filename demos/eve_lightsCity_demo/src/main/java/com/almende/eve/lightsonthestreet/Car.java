/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 * Author: Luis F. M. Cunha
 */
package com.almende.eve.lightsonthestreet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.JSONRpc;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.StringUtil;
import com.almende.util.TypeUtil;

@Access(AccessType.PUBLIC)
public class Car extends Agent {

    // Create a string type using TypeUtil. When the type is used a lot of times
    // it performs better declaring it this way
    private final static TypeUtil<String> STRINGTYPE = new TypeUtil<String>() {
    };

    private String carNumber;
    private int route = 0;

    private Position carPosition = new Position(0, 0);
    private int carPositionX = 55;
    private int carPositionY = 0;

    private String direction = "down"; // right, left, down, up
    private int rightLimit = 1105; // limit to change direction
    private int leftLimit = 55; // limit to change direction
    private int topLimit = 55; // limit to change direction
    private int bottomLimit = 855; // limit to change direction
    private int velocity = 80;

    // map info
    private static int verticalLine0 = 80;
    private static int verticalLine1 = 380;
    private static int verticalLine2 = 630;
    private static int verticalLine3 = 1080;

    private static int horizontalLine0 = 80;
    private static int horizontalLine1 = 430;
    private static int horizontalLine2 = 830;

    private static int handDistance = 25;

    private Interception interception01 = new Interception(
            new Position(verticalLine1 - handDistance, horizontalLine0 - handDistance),
            new Position(verticalLine1 + handDistance, horizontalLine0 - handDistance),
            new Position(verticalLine1 - handDistance, horizontalLine0 + handDistance),
            new Position(verticalLine1 + handDistance, horizontalLine0 + handDistance));

    private Interception interception02 = new Interception(
            new Position(verticalLine2 - handDistance, horizontalLine0 - handDistance),
            new Position(verticalLine2 + handDistance, horizontalLine0 - handDistance),
            new Position(verticalLine2 - handDistance, horizontalLine0 + handDistance),
            new Position(verticalLine2 + handDistance, horizontalLine0 + handDistance));

    private Interception interception03 = new Interception(
            new Position(verticalLine3 - handDistance, horizontalLine0 - handDistance),
            new Position(verticalLine3 + handDistance, horizontalLine0 - handDistance),
            new Position(verticalLine3 - handDistance, horizontalLine0 + handDistance),
            new Position(verticalLine3 + handDistance, horizontalLine0 + handDistance));

    private Interception interception10 = new Interception(
            new Position(verticalLine0 - handDistance, horizontalLine1 - handDistance),
            new Position(verticalLine0 + handDistance, horizontalLine1 - handDistance),
            new Position(verticalLine0 - handDistance, horizontalLine1 + handDistance),
            new Position(verticalLine0 + handDistance, horizontalLine1 + handDistance));

    private Interception interception12 = new Interception(
            new Position(verticalLine2 - handDistance, horizontalLine1 - handDistance),
            new Position(verticalLine2 + handDistance, horizontalLine1 - handDistance),
            new Position(verticalLine2 - handDistance, horizontalLine1 + handDistance),
            new Position(verticalLine2 + handDistance, horizontalLine1 + handDistance));

    private Interception interception13 = new Interception(
            new Position(verticalLine3 - handDistance, horizontalLine1 - handDistance),
            new Position(verticalLine3 + handDistance, horizontalLine1 - handDistance),
            new Position(verticalLine3 - handDistance, horizontalLine1 + handDistance),
            new Position(verticalLine3 + handDistance, horizontalLine1 + handDistance));

    private Interception interception21 = new Interception(
            new Position(verticalLine1 - handDistance, horizontalLine2 - handDistance),
            new Position(verticalLine1 + handDistance, horizontalLine2 - handDistance),
            new Position(verticalLine1 - handDistance, horizontalLine2 + handDistance),
            new Position(verticalLine1 + handDistance, horizontalLine2 + handDistance));

    // constructor
    public Car() {
    }

    public void openConnectionAndStart() throws IOException {
        System.out.println("Connection opened with " + getId());
        // start();
    }

    public void setCarProperties(@Name("carNumber") String carNumber, @Name("route") int route,
            @Name("carPositionX") int carPositionX, @Name("carPositionY") int carPositionY) {

        this.route = route;
        this.carNumber = carNumber;
        this.carPositionX = carPositionX;
        this.carPositionY = carPositionY;

    }

    public void start() {
        schedule("startDrive", null, DateTime.now().plusSeconds(2));
    }

    public void startDrive() throws IOException {

        if (this.route == 0) {
            // If car is on the limits of the road then change direction
            if ((this.carPositionY >= this.bottomLimit) && (this.carPositionX <= this.leftLimit)) {
                // car is on the left bottom corner

                // change direction
                this.direction = "right";

                // set car to the correct position to drive
                this.carPositionY = this.bottomLimit;
                this.carPositionX = this.leftLimit;

            } else if ((this.carPositionY >= this.bottomLimit) && (this.carPositionX >= this.rightLimit)) {
                // car is on the right bottom corner

                // change direction
                this.direction = "up";

                // set car to the correct position to drive
                this.carPositionY = this.bottomLimit;
                this.carPositionX = this.rightLimit;

            } else if ((this.carPositionY <= this.topLimit) && (this.carPositionX >= this.rightLimit)) {
                // car is on the right top corner

                // change direction
                this.direction = "left";

                // set car to the correct position to drive
                this.carPositionY = this.topLimit;
                this.carPositionX = this.rightLimit;

            } else if ((this.carPositionY <= this.topLimit) && (this.carPositionX <= this.leftLimit)) {
                // car is on the left top corner

                // change direction
                this.direction = "down";

                // set car to the correct position to drive
                this.carPositionY = this.topLimit;
                this.carPositionX = this.leftLimit;
            } else if (this.carPositionX <= this.leftLimit) {
                // car is between top left corner and bottom left corner
                this.direction = "down";
            } else if (this.carPositionY >= this.bottomLimit) {
                // car is between top left corner and bottom left corner
                this.direction = "right";
            } else if (this.carPositionX >= this.rightLimit) {
                // car is between top left corner and bottom left corner
                this.direction = "up";
            } else if (this.carPositionY <= this.topLimit) {
                // car is between top left corner and bottom left corner
                this.direction = "left";

            } else {
                // place car at a start position
                this.carPositionY = this.topLimit;
                this.carPositionX = this.leftLimit;
                // change direction
                this.direction = "down";
            }

        } else if (this.route == 1) {
            // the car will drive from top to bottom on the second horizontal street
            if ((this.carPositionY <= this.interception01.topLeft.y)
                    && (this.carPositionX <= this.interception01.topLeft.x)) {
                this.direction = "down";
                this.carPositionX = this.interception01.topLeft.x;
            } else if ((this.carPositionY >= this.interception21.bottomRight.y)
                    && (this.carPositionX >= this.interception21.bottomRight.x)) {
                this.direction = "up";
                this.carPositionX = this.interception21.bottomRight.x;
            } else if ((this.carPositionY >= this.interception21.bottomLeft.y)) {
                this.direction = "right";
                this.carPositionY = this.interception21.bottomLeft.y;
            } else if ((this.carPositionY <= this.interception01.topRight.y)) {
                this.direction = "left";
            } else {

            }

        } else if (this.route == 2) {
            // the car will drive on the middle horizontal street forward and back
            if ((this.carPositionY <= this.interception10.topLeft.y)
                    && (this.carPositionX <= this.interception10.topLeft.x)) {
                this.direction = "down";
                this.carPositionX = this.interception10.topLeft.x;
            } else if ((this.carPositionY >= this.interception13.bottomRight.y)
                    && (this.carPositionX >= this.interception13.bottomRight.x)) {
                this.direction = "up";
                this.carPositionX = this.interception13.bottomRight.x;
            } else if ((this.carPositionY >= this.interception10.bottomLeft.y)) {
                this.direction = "right";
                this.carPositionY = this.interception10.bottomLeft.y;
            } else if ((this.carPositionY <= this.interception13.topRight.y)) {
                this.direction = "left";
                this.carPositionY = this.interception10.topRight.y;
            } else {

            }

        } else if (this.route == 3) {
            // the car will drive in circles around the top 3th island
            if ((this.carPositionY <= this.interception02.bottomRight.y)
                    && (this.carPositionX <= this.interception02.bottomRight.x)) {
                this.direction = "right";
                this.carPositionY = this.interception02.bottomRight.y;
            } else if ((this.carPositionY <= this.interception03.bottomLeft.y)
                    && (this.carPositionX >= this.interception03.bottomLeft.x)) {
                this.direction = "down";
                this.carPositionX = this.interception03.bottomLeft.x;
            } else if ((this.carPositionY >= this.interception13.topLeft.y)
                    && (this.carPositionX >= this.interception13.topLeft.x)) {
                this.direction = "left";
                this.carPositionY = this.interception13.topLeft.y;
            } else if ((this.carPositionY >= this.interception12.topRight.y)
                    && (this.carPositionX <= this.interception12.topRight.x)) {
                this.direction = "up";
                this.carPositionX = this.interception12.topRight.x;
            } else {
                
            }

        } else {

        }

        // Move car position
        if (direction.equals("down")) {
            this.carPositionY += 5;
        } else if (direction.equals("up")) {
            this.carPositionY -= 5;
        } else if (direction.equals("right")) {
            this.carPositionX += 5;
        } else if (direction.equals("left")) {
            this.carPositionX -= 5;
        } else {
            // don't do anything
        }

        updatePositionToBrowser(this.carPositionX, this.carPositionY);

        // Call this function again
        schedule("startDrive", null, DateTime.now().plusMillis(this.velocity));

    }

    // ----------------------------------------------------------------------------------------
    // Stupid implementation until I figure out how to return a object in just
    // one function
    public int getCarPositionX() {
        return this.carPositionX;
    }

    public int getCarPositionY() {
        return this.carPositionY;
    }

    public Position getCarPosition() {
        /*
         * final ObjectNode envelop = JOM.createObjectNode(); envelop.put("x",
         * this.carPosition.x); envelop.put("y", this.carPosition.y); return
         * envelop;
         * 
         * JSONRpc.invoke(destination, request,
         * auth).stringify(this.carPosition, null, 2); InputStream abc = new
         * InputStream(); StringUtil.streamToString(new InputStream
         * (this.carPosition).toString()); System.out.println("CAR: (" +
         * this.carPosition.x + ", " + this.carPosition.y + ")");
         */
        return (this.carPosition);
    }
    // ----------------------------------------------------------------------------------------

    public void updatePositionToBrowser(int carPositionX, int carPositionY) throws IOException {
        // System.out.println("Updating car position to: " + carPosition);

        URI url = URI.create("wsclient:carBrowser" + this.carNumber);
        String method = "updatePosition";
        Params params = new Params();
        params.add("x", carPositionX);
        params.add("y", carPositionY);

        caller.callSync(url, method, params, STRINGTYPE);
    }

    public String getCarDirection() {
        return this.direction;
    }

    public void setVelocity(@Name("vel") int vel) {
        this.velocity = vel;
    }

    // ------ Classes ------
    class Position {
        public int x = 0;
        public int y = 0;

        // constructor
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class Interception {
        public Position topLeft;
        public Position topRight;
        public Position bottomLeft;
        public Position bottomRight;

        public Interception(Position topLeft, Position topRight, Position bottomLeft, Position bottomRight) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
        }
    }
}
