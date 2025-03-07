package edu.cornell.gdiac.b2lights;


public class SecurityCamera extends DudeModel {
    public static final int MAX_BLIND_TIME = 180; //Maximum time a camera can be blind for

    private boolean blinded;
    private int blindTimer;


    public SecurityCamera(String type) {
        super(type);
        blinded = false;
        blindTimer = 0;
    }

    public boolean isBlinded() {return blinded;}

    public void setBlind(boolean blindState) {
        blinded = blindState;
        if (blindState) {
            setBlindTimer(MAX_BLIND_TIME);
        }
    }

    public int getBlindTimer(){
        return blindTimer;
    }

    public void setBlindTimer(int value){blindTimer = value;}


}
