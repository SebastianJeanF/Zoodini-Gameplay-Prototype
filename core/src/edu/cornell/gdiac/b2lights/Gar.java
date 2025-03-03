/*
 * DudeModel.java
 *
 * This is a refactored version of DudeModel that allows us to read its properties
 * from a JSON file.  As a result, it has a lot more getter and setter "hooks" than
 * in lab.
 *
 * While the dude can support lights, these are completely decoupled from this object.
 * The dude is not aware of any of the lights. These are attached to the associated
 * body and move with the body.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * B2Lights version, 3/12/2016
 */
package edu.cornell.gdiac.b2lights;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little. The true initialization happens
 * by reading the JSON value.
 */
public class Gar extends DudeModel {
	/// Whether or not this Gar instance has triggered the meow action
	private boolean meowed;

	public Gar() {
		super("Gar");
	}

	/**
	 * Gets the current value of <code>meowed</code>.
	 * 
	 * @return Whether this Gar instance has meowed
	 */
	public boolean getMeowed() {
		return meowed;
	}

	/**
	 * Update the value of <code>meowed</code>.
	 * 
	 * @param value What to set the new value of <code>meowed</code> to
	 */
	public void setMeowed(boolean value) {
		meowed = value;
	}
}