package engine;

/*
* Classname:            Vertex2f.java
*
* Version information:  1.0
*
* Date:                 11/17/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

import java.io.Serializable;

public class Vertex2f implements Serializable {
	private static final long serialVersionUID = 4768609106467658952L;
	private Float x;
    private Float y;

    public Vertex2f(float x, float y) {
    	super();
    	this.x = x;
    	this.y = y;
    }

    public int hashCode() {
    	int hashFirst = x != null ? x.hashCode() : 0;
    	int hashSecond = y != null ? y.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
    	if (other instanceof Vertex2f) {
    		@SuppressWarnings("unchecked")
			Vertex2f otherPosition = (Vertex2f) other;
    		return 
    		((  this.x == otherPosition.x ||
    			( this.x != null && otherPosition.x != null &&
    			  this.x.equals(otherPosition.x))) &&
    		 (	this.y == otherPosition.y ||
    			( this.y != null && otherPosition.y != null &&
    			  this.y.equals(otherPosition.y))) );
    	}

    	return false;
    }

    public String toString()
    { 
           return "(" + x + ", " + y + ")"; 
    }

    public float getX() {
    	return x;
    }

    public void setX(float x) {
    	this.x = x;
    }

    public float getY() {
    	return y;
    }

    public void setY(float y) {
    	this.y = y;
    }
    
    public void put(float x, float y) {
    	this.x = x;
    	this.y = y;
    }
}
