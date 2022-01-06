package mow.thm.de.sleeptracker3;

import java.util.ArrayList;

public class Analytics {

    //TODO:
    int numAwake; // Wie oft man "leichten Schlaf" hatte
    ArrayList<String> timeOfNumAwake; // Zu welchen Uhrzeiten man "leichten Schlaf" hatte

    public Analytics() {

    }

    public Analytics(int numAwake, ArrayList<String> timeOfNumAwake) {
        this.numAwake = numAwake;
        this.timeOfNumAwake = timeOfNumAwake;
    }

    public int getNumAwake() {
        return numAwake;
    }

    public ArrayList<String> getTimeOfNumAwake() {
        return timeOfNumAwake;
    }

    public void setNumAwake(int numAwake) {
        this.numAwake = numAwake;
    }
    public void setTimeOfNumAwake(ArrayList<String> timeOfNumAwake) {
        this.timeOfNumAwake = timeOfNumAwake;
    }
}
