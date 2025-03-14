/*
 * DesktopLauncher.java
 * 
 * LibGDX is a cross-platform development library. You write all of your code in 
 * the core project.  However, you still need some extra classes if you want to
 * deploy on a specific platform (e.g. PC, Android, Web).  That is the purpose
 * of this class.  It deploys your game on a PC/desktop computer.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015d
 */
package edu.cornell.gdiac.b2lights.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import edu.cornell.gdiac.b2lights.GDXRoot;

/**
 * The main class of the game.
 * 
 * This class sets the window size and launches the game.  Aside from modifying
 * the window size, you should almost never need to modify this class.
 */
public class DesktopLauncher {
	
	/**
	 * Classic main method that all Java programmers know.
	 * 
	 * This method simply exists to start a new LwjglApplication.  For desktop games,
	 * LibGDX is built on top of LWJGL (this is not the case for Android).
	 * 
	 * @param arg Command line arguments
	 */
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
//		config.setWindowedMode(800, 600);
//		config.setWindowedMode(1600, 1200);
		config.setWindowedMode(1200, 800);

		config.setForegroundFPS(60);
		config.setResizable(false);
		new Lwjgl3Application(new GDXRoot(), config);
	}
}