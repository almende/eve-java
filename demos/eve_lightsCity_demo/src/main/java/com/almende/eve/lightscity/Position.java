package com.almende.eve.lightscity;

import java.io.Serializable;

public class Position implements Serializable {

    private static final long serialVersionUID = 3573450486335054935L;
    public int x = 0;
    public int y = 0;

    public Position() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    // constructor
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void incX(int x){
    	this.x += x;
    }
    public void decX(int x){
    	this.x -= x;
    }
    public void incY(int y){
    	this.y += y;
    }
    public void decY(int y){
    	this.y -= y;
    }
}