/*
 * GameController.java
 *
 * This combines the WorldController with the mini-game specific PlatformController
 * in the last lab.  With that said, some of the work is now offloaded to the new
 * LevelModel class, which allows us to serialize and deserialize a level. 
 * 
 * This is a refactored version of WorldController from Lab 4.  It separate the 
 * level out into a new class called LevelModel.  This model is, in turn, read
 * from a JSON file.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * B2Lights version, 3/12/2016
 */
package edu.cornell.gdiac.b2lights;

import box2dLight.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.b2lights.Guard;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Gameplay controller for the game.
 *
 * This class does not have the Box2d world.  That is stored inside of the
 * LevelModel object, as the world settings are determined by the JSON
 * file.  However, the class does have all of the controller functionality,
 * including collision listeners for the active level.
 *
 * You will notice that asset loading is very different.  It relies on the
 * singleton asset manager to manage the various assets.
 */
public class GameController implements Screen, ContactListener {
	// ASSETS
	/** Need an ongoing reference to the asset directory */
	protected AssetDirectory directory;
	/** The JSON defining the level model */
	private JsonValue  levelFormat;
	/** The font for giving messages to the player */
	protected BitmapFont displayFont;
	
	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
    /** How many frames after winning/losing do we continue? */
	public static final int EXIT_COUNT = 120;

	/** Reference to the game canvas */
	protected ObstacleCanvas canvas;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** Reference to the game level */
	protected LevelModel level;
		
	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Countdown active for winning or losing */
	private int countdown;


	// --- Guard Agro Variables ---
	private boolean guardAgro = false;
	private DudeModel guardTarget = null;
	/** Countdown for how long the guard will chase the player */
	private int guardChaseTimer = 0;
	private static final int MAX_CHASE_TIME = 180;
	final float GUARD_FOV_DISTANCE = 7.0f;  // Maximum detection distance.
	final float GUARD_FOV_ANGLE = 45.0f;       // Total cone angle in degrees.


	// --- Patrol Path Variables for Guard ---
	private Vector2[] patrolPoints;
	private int currentPatrolIndex = 0;
	private static final float PATROL_THRESHOLD = 0.5f; // Distance to switch patrol points



	// Variables (probably) needed to implement the guard going after a meow
	private boolean meowAlert = false;
	private Vector2 meowPos = null;


	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete( ) {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		failed = value;
	}
	
	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @param the canvas associated with this controller
	 */
	public ObstacleCanvas getCanvas() {
		return canvas;
	}
	
	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param value the canvas associated with this controller
	 */
	public void setCanvas(ObstacleCanvas canvas) {
		this.canvas = canvas;
	}




	/**
	 * Creates a new game world 
	 *
	 * The physics bounds and drawing scale are now stored in the LevelModel and
	 * defined by the appropriate JSON file.
	 */
	public GameController() {
		level = new LevelModel();
		complete = false;
		failed = false;
		active = false;
		countdown = -1;

		setComplete(false);
		setFailure(false);

		// Default patrol path for the guard (adjust coordinates as needed)
		patrolPoints = new Vector2[] {
//				new Vector2(2, 2),
//				new Vector2(8, 2),
//				new Vector2(8, 8),
//				new Vector2(2, 8)
				new Vector2(14,8),
				new Vector2(1,8)

		};
		currentPatrolIndex = 0;
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		level.dispose();
		level  = null;
		canvas = null;
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		// Access the assets used directly by this controller
		this.directory = directory;
		displayFont = directory.getEntry( "display", BitmapFont.class );

		// This represents the level but does not BUILD it
		levelFormat = directory.getEntry( "level1", JsonValue.class );
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the level and creates a new one. It will 
	 * reread from the JSON file, allowing us to make changes on the fly.
	 */
	public void reset() {
		level.dispose();
		
		setComplete(false);
		setFailure(false);
		countdown = -1;
		
		// Reload the json each time
		level.populate(directory, levelFormat);
		level.getWorld().setContactListener(this);
	}
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param delta Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		InputController input = InputController.getInstance();
		input.readInput();
		if (listener == null) {
			return true;
		}

		// Toggle debug
		if (input.didDebug()) {
			level.setDebug(!level.getDebug());
		}
		
		// Handle resets
		if (input.didReset()) {
			reset();
		}

		// Handle swapping characters
		if (input.didSwap()){
			// Set current avatar's velocity to 0
			DudeModel avatar = level.getAvatar();
			avatar.setMovement(0,0 );

			// Swap the two avatars
			level.swap();
		}

		// Now it is time to maybe switch screens.
		if (input.didExit()) {
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (countdown > 0) {
			countdown--;
		} else if (countdown == 0) {
			reset();
		}
		
		return true;
	}
	
	private Vector2 angleCache = new Vector2();



	void moveGuard(Vector2 targetPos) {
		Guard guard = level.getGuard();
		Vector2 guardPos = guard.getPosition();
		Vector2 direction = new Vector2(targetPos).sub(guardPos);
		if (direction.len() > 0) {
			direction.nor().scl(guard.getForce());
			guard.setMovement(direction.x, direction.y);
			// Update guard orientation to face the target.
			guard.setAngle(direction.angleRad());
		}

		// Update the guard's orientation to face the direction of movement.
		Vector2 movement = guard.getMovement();
		if (movement.len2() > 0.0001f) {  // Only update if there is significant movement
			guard.setAngle(movement.angleRad() - (float)Math.PI/2);
		}
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Process actions in object model
		DudeModel avatar = level.getAvatar();
		InputController input = InputController.getInstance();


		// Press N or P to switch light modes
		if (input.didForward()) {
			level.activateNextLight();
		} else if (input.didBack()){
			level.activatePrevLight();
		}

		// Check if the ability (meow) is pressed.
		// For a Gar, this sets a flag to indicate a meow event.
		if (input.isAbilityPressed()) {
			switch (avatar.getPlayerType()) {
				case GAR:
					((Gar) avatar).setMeowed(true);
					break;
				default:
					break;
			}
		}


		// --- Meow Alert Section ---
		// If the Gar meows, immediately alert the guard.
		Guard guard = level.getGuard();
		switch (avatar.getPlayerType()) {
			case GAR:{
				Gar gar = (Gar) avatar;
				if (gar.getMeowed()) {
					// When the guard hears a meow, its target is set to the Gar's position.
					guard.setAgroed(gar);
					guardChaseTimer = 180; // Reset the chase timer (adjust as needed)
					System.out.println("Guard alerted by meow, moving to meow position");
					gar.setMeowed(false);  // Reset the meow flag
				}
				break;
			}
			default:
				break;
		}


		// --- Guard Field-of-View (FOV) Logic Section ---
		// Only check FOV if guard is not already alerted by a meow.
		if (!guardAgro) {


			Vector2 guardPos = guard.getPosition();
			Vector2 avatarPos = avatar.getPosition();
			Vector2 toAvatar = new Vector2(avatarPos).sub(guardPos);
			float distance = toAvatar.len();

			if (distance <= GUARD_FOV_DISTANCE) {
				Vector2 toAvatarNorm = new Vector2(toAvatar).nor();
				// Assume guard's forward direction is defined by its current angle (0 rad = up)
				float guardAngle = guard.getAngle();
				Vector2 guardFacing = new Vector2(0, 1).setAngleRad(guardAngle + (float)Math.PI/2);
				float dot = guardFacing.dot(toAvatarNorm);
				// Calculate half-angle in radians.
				float halfAngleRad = (GUARD_FOV_ANGLE / 2.0f) * MathUtils.degreesToRadians;
				if (dot >= Math.cos(halfAngleRad)) {
					// The avatar is within the guard's field of view.
					guardAgro = true;
					guardTarget = avatar;
					guardChaseTimer = MAX_CHASE_TIME;
				}
			}
		} else {
			// If already agro, decrement the chase timer.
			guardChaseTimer--;
			if (guardChaseTimer <= 0) {
				guardAgro = false;
				guardTarget = null;
			}
		}

		// --- Guard Movement Update ---
		if (guardAgro && guardTarget != null) {
			// If alerted, chase the target.
			Vector2 targetPos = guardTarget.getPosition();
			moveGuard(targetPos);
		} else {
			// Patrol behavior: follow the defined patrol path.
			if (patrolPoints != null && patrolPoints.length > 0) {
				Vector2 patrolTarget = patrolPoints[currentPatrolIndex];
				if (guard.getPosition().dst(patrolTarget) < PATROL_THRESHOLD) {
					currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.length;
					patrolTarget = patrolPoints[currentPatrolIndex];
				}
				moveGuard(patrolTarget);
			} else {
				guard.setMovement(0, 0);
			}
		}

		// --- Avatar Movement ---
		// Rotate the avatar to face the direction of movement
		angleCache.set(input.getHorizontal(),input.getVertical());
		if (angleCache.len2() > 0.0f) {

			// Stops you from moving faster if you press two keys at once
			// instead of just one
			if (angleCache.len() > 1.0f) {
				angleCache.nor();
			}

			float angle = angleCache.angle();
			// Convert to radians with up as 0
			angle = (float)Math.PI*(angle-90.0f)/180.0f;
			avatar.setAngle(angle);
		}
		angleCache.scl(avatar.getForce());
		avatar.setMovement(angleCache.x,angleCache.y);
		avatar.applyForce();



		// Additional avatar-specific logic (e.g., Gar's meow ability)
		switch (avatar.getPlayerType()) {
			case GAR:
				Gar gar = (Gar) avatar;
				if (gar.getMeowed() == true) {
					// code for updating the nearest guard goes here
					System.out.println("Gar meowed a guard to his location");
					gar.setMeowed(false);
				}
			default:
				break;
		}

		// System.out.println("Character grid");

		// Stops afk avatar from continuously sliding, if he got hit by something
		DudeModel afkAvatar = level.getAvatarAFK();
		afkAvatar.applyForce();
		// Stops guard from continuously sliding, if they got hit by something
		guard.applyForce();

		// Turn the physics engine crank.
		level.update(dt);
	}



	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(float delta) {
		canvas.clear();
		
		level.draw(canvas);
				
		// Final message
		if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
			canvas.end();
		}
	}
	
	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				update(delta);
			}
			draw(delta);
		}
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use 
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();
		
		try {
			Obstacle bd1 = (Obstacle)body1.getUserData();
			Obstacle bd2 = (Obstacle)body2.getUserData();

			DudeModel avatar = level.getAvatar();
			ExitModel door   = level.getExit();
			
			// Check for win condition
			if ((bd1 == avatar && bd2 == door  ) ||
				(bd1 == door   && bd2 == avatar)) {
				setComplete(true);
			}

			// Check for failure condition
			// You lose if one of the characters touches the guards
			Guard guard = level.getGuard();
			DudeModel afkAvatar = level.getAvatarAFK();
			if ((bd1 == guard && (bd2 == avatar || bd2 == afkAvatar)) ||
					(bd2 == guard && (bd1 == avatar || bd1 == afkAvatar))) {
				setFailure(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** Unused ContactListener method */
	public void endContact(Contact contact) {}
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}