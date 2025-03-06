package edu.cornell.gdiac.b2lights;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class B2dSteeringEntity implements Steerable<Vector2> {

    Body body;
    boolean tagged;
    float boundingRadius;
    float maxLinearSpeed, maxLinerAcceleration;
    float maxAngularSpeed, maxAngularAcceleration;

    public B2dSteeringEntity(Body body, float boundingRadius) {
        this.body = body;
        this.boundingRadius = boundingRadius;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean b) {
        this.tagged = b;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float v) {

    }

    @Override
    public float getMaxLinearSpeed() {
        return 0;
    }

    @Override
    public void setMaxLinearSpeed(float v) {

    }

    @Override
    public float getMaxLinearAcceleration() {
        return 0;
    }

    @Override
    public void setMaxLinearAcceleration(float v) {

    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float v) {
        this.maxAngularSpeed = v;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float v) {
        this.maxAngularAcceleration = v;
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public float getOrientation() {
        return body.getAngle();
    }

    @Override
    public void setOrientation(float v) {
        
    }

    @Override
    public float vectorToAngle(Vector2 vector2) {
        return 0;
    }

    @Override
    public Vector2 angleToVector(Vector2 vector2, float v) {
        return null;
    }

    @Override
    public Location<Vector2> newLocation() {
        return null;
    }
}
