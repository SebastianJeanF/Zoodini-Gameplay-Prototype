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
import edu.cornell.gdiac.physics.lights.ConeSource;
import edu.cornell.gdiac.physics.lights.LightSource;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.b2lights.Guard;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.b2lights.SecurityCamera;

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

	private boolean garAtDoor = false;
	private boolean ottoAtDoor = false;


	// --- Patrol Path Variables for Guard ---
	private Vector2[] patrolPoints;
	private int currentPatrolIndex = 0;
	private static final float PATROL_THRESHOLD = 0.5f; // Distance to switch patrol points





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
				new Vector2(1,8),
				new Vector2(14,8),

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

		DudeModel avatar = level.getAvatar();
		// Check if the ability (meow) is pressed.
		// For a Gar, this sets a flag to indicate a meow event.
		if (input.isAbilityPressed()) {
			switch (avatar.getPlayerType()) {
				case GAR:
					((Gar) avatar).setMeowed(true);
					break;
				case OTTO:
					((Otto) avatar).setInked(true);
					break;
			}
		}
		
		return true;
	}
	
	private Vector2 angleCache = new Vector2();


	/** Invariant: Guard's state must be updated before calling this method
	 * i.e) target, agroed, meowed
	 * */
	void moveGuard() {

		Vector2 targetPos = level.getGuard().getTarget();
		Guard guard = level.getGuard();
		Vector2 guardPos = guard.getPosition();
		Vector2 direction = new Vector2(targetPos).sub(guardPos);
		if (direction.len() > 0) {
			direction.nor().scl(guard.getForce());
			if (guard.isMeowed()) {
				direction.scl(0.5f);
			}
			else if (guard.isAgroed()){
				direction.scl(1.1f);
			}
			else if (guard.isCameraAlerted()) {
				direction.scl(1.5f);
			}

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


	private void updateGuardBehavior() {
		Guard guard = level.getGuard();
		DudeModel avatar = level.getAvatar();
		SecurityCamera securityCamera = level.getSecurityCamera();


		// Check for meow alert (Gar) or inked alert (Otto)
		if (avatar.getPlayerType() == DudeModel.DudeType.GAR) {
			Gar gar = (Gar) avatar;
			if (gar.getMeowed()) {
				// Make guard go after the meow
				guard.setMeow(true);
				guard.setTarget(gar.getPosition().cpy());
				guard.setChaseTimer(Guard.MAX_CHASE_TIME);
				System.out.println("Guard alerted by meow, moving to meow position");
			}
		} else if (avatar.getPlayerType() == DudeModel.DudeType.OTTO) {
			Otto otto = (Otto) avatar;
			if (otto.getInked()) {
				securityCamera.setBlind(true);
			}
		}


		// Handle camera alert logic
		if (level.isAvatarInSecurityLight()) {
			guard.setMeow(false);
			guard.setAgroed(true);
			guard.setCameraAlerted(true);
			guard.setTarget(avatar.getPosition().cpy());

			// Guard should be extra aggressive in chasing player
			guard.setChaseTimer(Guard.MAX_CHASE_TIME * 2);
			System.out.println("Guard alerted by security camera light!");
		}


		// Reset camera alert when the guard reaches its target
//		if ((guard.isCameraAlerted() && guard.getPosition().dst(guard.getTarget()) < 0.1f)
//				|| guard.getChaseTimer() <= 0) {
//			guard.setCameraAlerted(false);
//		}

		// Reset meow alert when the guard reaches its target
		if ((guard.isMeowed() && guard.getPosition().dst(guard.getTarget()) < 0.1f)
				) {
			System.out.println("Guard is no longer following the meow");
			guard.setMeow(false);
		}

		// Check Field-of-view (FOV), making guard agroed if they see a player
		processGuardFOV(guard);

		if (guard.isMeowed()) {
			System.out.println("Guard is meowed, moving to meow position");
		}


		// Now that guard's state is updated, move the guard
		if (!guard.isAgroed() && !guard.isMeowed()) {
			updateGuardPatrol();
		}
		else {
			// Guard is agroed or meowed
			moveGuard();
		}

	}

	private void processGuardFOV(Guard guard) {
		DudeModel avatar = level.getAvatar();
		DudeModel avatarAFK = level.getAvatarAFK();

		LightSource guardLight = level.getGuardLight();
		if(guardLight.contains(avatar.getX(), avatar.getY())){
				// Guard is now chasing active player
				guard.setAgroed(true);
				guard.setMeow(false);
				guard.setTarget(avatar.getPosition().cpy());
				guard.setChaseTimer(Guard.MAX_CHASE_TIME);
				System.out.println("Guard alerted by FOV, moving to avatar position");
		}
		else if (guardLight.contains(avatarAFK.getX(), avatarAFK.getY())) {
			// Guard is now chasing afk player
			guard.setAgroed(true);
			guard.setMeow(false);
			guard.setTarget(avatarAFK.getPosition().cpy());
			guard.setChaseTimer(Guard.MAX_CHASE_TIME);
			System.out.println("Guard alerted by FOV, moving to avatarAFK position");
		}
		else {
			guard.setChaseTimer(guard.getChaseTimer() - 1);
			if (guard.getChaseTimer() <= 0) {
				// Guard is not chasing player anymore
				guard.setAgroed(false);
				guard.setCameraAlerted(false);
			}
		}

	}

	private void updateGuardPatrol() {
		Guard guard = level.getGuard();
		if (patrolPoints != null && patrolPoints.length > 0) {
			Vector2 patrolTarget = patrolPoints[currentPatrolIndex];
			if (guard.getPosition().dst(patrolTarget) < PATROL_THRESHOLD) {
				currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.length;
				patrolTarget = patrolPoints[currentPatrolIndex];
			}
			guard.setTarget(patrolTarget);
			moveGuard();
		} else {
			guard.setMovement(0, 0);
		}
	}



	private void updateAvatarMovement() {
		DudeModel avatar = level.getAvatar();
		InputController input = InputController.getInstance();
		Vector2 angleCache = new Vector2(input.getHorizontal(), input.getVertical());

		if (angleCache.len2() > 0.0f) {
			if (angleCache.len() > 1.0f) {
				angleCache.nor();
			}
			float angle = angleCache.angle();
			angle = (float)Math.PI * (angle - 90.0f) / 180.0f;
			avatar.setAngle(angle);
		}

		angleCache.scl(avatar.getForce());
		avatar.setMovement(angleCache.x, angleCache.y);
		avatar.applyForce();
	}


	private void updateSecurityCamera() {
		SecurityCamera securityCamera = level.getSecurityCamera();
		if (securityCamera.isBlinded() && securityCamera.getBlindTimer() >= 0) {
			if (securityCamera.getBlindTimer() == 0) {
				securityCamera.setBlind(false);
				level.unBlindCamera();
			} else {
				securityCamera.setBlindTimer(securityCamera.getBlindTimer() - 1);
				level.blindCamera();
			}
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

		// Process input-dependent events
		// (e.g., light switching could also be extracted if needed)
		InputController input = InputController.getInstance();
		if (input.didForward()) {
			level.activateNextLight();
		} else if (input.didBack()){
			level.activatePrevLight();
		}
		// Update avatar movement
		updateAvatarMovement();

		// Update guard and camera behavior
		updateGuardBehavior();
		updateSecurityCamera();


		// Apply forces for AFK avatar and guard to prevent sliding
		level.getAvatarAFK().applyForce();
		level.getGuard().applyForce();

		// Manage stamina and ability resets
		updateStamina();
		resetAbility();

		// Update the physics simulation
		level.update(dt);
	}


	void updateStamina() {
		// Constants: adjust these values as needed.
		float ACTIVE_DRAIN = 0.1f;
		final float ABILITY_DRAIN = 50.0f;
		final float AFK_RECOVERY = 0.2f;

		DudeModel activeAvatar = level.getAvatar();
		DudeModel afkAvatar = level.getAvatarAFK();

		// If the player didn't move, set drain to zero
		InputController input = InputController.getInstance();
		if (input.getHorizontal() == 0 && input.getVertical() == 0) {
			ACTIVE_DRAIN = 0.0f;
		} else {
			ACTIVE_DRAIN = 0.1f;
		}

	// If the active character uses an ability, subtract extra stamina.
		if (activeAvatar.getPlayerType() == DudeModel.DudeType.GAR) {
			Gar gar = (Gar) activeAvatar;
			if (gar.getMeowed()) {
				activeAvatar.setStamina(Math.max(0, activeAvatar.getStamina() - ABILITY_DRAIN));
			}
		} else if (activeAvatar.getPlayerType() == DudeModel.DudeType.OTTO) {
			Otto otto = (Otto) activeAvatar;
			if (otto.getInked()) {
				activeAvatar.setStamina(Math.max(0, activeAvatar.getStamina() - ABILITY_DRAIN));
			}
		}

	// Drain stamina for the active character every frame.
		activeAvatar.setStamina(Math.max(0, activeAvatar.getStamina() - ACTIVE_DRAIN));

	// Recover stamina for the inactive (AFK) character,
	// capping it at its maximum value.
		afkAvatar.setStamina(Math.min(afkAvatar.getMaxStamina(),
				afkAvatar.getStamina() + AFK_RECOVERY));

		// If the active character has run out of stamina,
		// and the inactive character still has some, swap them.
		if (activeAvatar.getStamina() <= 0 && afkAvatar.getStamina() > 0) {

			// Stop the active character's movement.
			activeAvatar.setMovement(0, 0);


			level.swap();
			// Exit the stamina update early to avoid further drain in this frame.
			return;
		}


	}

	/***
	 * Sets the abilities for the characters to false
	 */
	void resetAbility() {
		DudeModel avatar = level.getAvatar();
		DudeModel afkAvatar = level.getAvatarAFK();
		switch (avatar.getPlayerType()) {
			case GAR:
				((Gar) avatar).setMeowed(false);
				break;
			case OTTO:
				((Otto) avatar).setInked(false);
				break;
		}

		switch (afkAvatar.getPlayerType()) {
			case GAR:
				((Gar) afkAvatar).setMeowed(false);
				break;
			case OTTO:
				((Otto) afkAvatar).setInked(false);
				break;
		}
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
		drawStaminaBar();

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

			DudeModel afkAvatar = level.getAvatarAFK();

			ExitModel door   = level.getExit();

			boolean avatarAtDoor = (bd1 == avatar && bd2 == door  ) ||
					(bd1 == door   && bd2 == avatar);

			// Check for win condition
			if (avatarAtDoor && avatar.getPlayerType() == DudeModel.DudeType.GAR) {
				garAtDoor = true;
			}
			if (avatarAtDoor && avatar.getPlayerType() == DudeModel.DudeType.OTTO) {
				ottoAtDoor = true;
			}

			if (garAtDoor && ottoAtDoor) {
				setComplete(true);
				garAtDoor = false;
				ottoAtDoor = false;
			}

			// Check for failure condition
			// You lose if one of the characters touches the guards
			Guard guard = level.getGuard();
			if ((bd1 == guard && (bd2 == avatar || bd2 == afkAvatar)) ||
					(bd2 == guard && (bd1 == avatar || bd1 == afkAvatar))) {
				setFailure(true);
			}



		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void drawStaminaBar() {
		// Assume:
// - 'level.getAvatar()' returns the active character.
// - 'level.getAvatarAFK()' returns the inactive character.
// - Each DudeModel now has getStamina() and getMaxStamina() methods.
// - 'whitePixel' is a Texture (1x1 white pixel) loaded in your asset directory.

// Example constants – adjust positions, sizes, and colors as needed.
		final float barWidth = 200;   // full width of the stamina bar
		final float barHeight = 20;   // height of the bar
		final float xPos = 0;        // x-coordinate of the bar (for active character)
		final float yPos = 0; // y-coordinate (from top)
//		final float xPos = 50;        // x-coordinate of the bar (for active character)
//		final float yPos = canvas.getHeight() - 50; // y-coordinate (from top)

		// Active character's stamina
		DudeModel active = level.getAvatar();
		float activeRatio = active.getStamina() / active.getMaxStamina();
		float activeFilledWidth = activeRatio * barWidth;

		// Inactive character's stamina
		DudeModel afk = level.getAvatarAFK();
		float afkRatio = afk.getStamina() / afk.getMaxStamina();
		float afkFilledWidth = afkRatio * barWidth;

		// Now get the texture from the AssetManager singleton
		JsonValue json = levelFormat.getChild("stamina");
		String key = json.get("texture").asString();
		TextureRegion whitePixel = new TextureRegion(directory.getEntry(key, Texture.class));
//		setTexture(texture);


		// Begin drawing with the canvas.
		canvas.begin();
		// Draw active character stamina background (e.g., dark gray)
		// canvas.draw(whitePixel, Color.DARK_GRAY, xPos, yPos, barWidth, barHeight);
		// Draw active character stamina (e.g., green)
		canvas.draw(whitePixel, Color.GREEN, xPos, yPos, activeFilledWidth, barHeight);

		float afkXPos = 50;
		float afkYPos = yPos - 30; // 30 pixels below active bar
		canvas.draw(whitePixel, Color.DARK_GRAY, afkXPos, afkYPos, barWidth, barHeight);
		canvas.draw(whitePixel, Color.GREEN, afkXPos, afkYPos, afkFilledWidth, barHeight);
		canvas.end();

	}

	/** Unused ContactListener method */
	public void endContact(Contact contact) {}
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}