package mow.thm.de.sleeptracker3;

import java.util.ArrayList;

public class Analytics {

    // Wie oft man "leichten Schlaf" hatte:
    int numAwakeX;
    int numAwakeY;
    int numAwakeZ;
    int numAwakeAll;

    // Zu welchen Uhrzeiten man "leichten Schlaf" hatte:
    ArrayList<String> timeOfNumAwakeX;
    ArrayList<String> timeOfNumAwakeY;
    ArrayList<String> timeOfNumAwakeZ;
    ArrayList<String> timeOfNumAwakeAll;

    public Analytics() {

    }

    public Analytics(int numAwakeX, ArrayList<String> timeOfNumAwakeX,
                     int numAwakeY, ArrayList<String> timeOfNumAwakeY,
                     int numAwakeZ, ArrayList<String> timeOfNumAwakeZ,
                     int numAwakeAll, ArrayList<String> timeOfNumAwakeAll) {
        this.numAwakeX = numAwakeX;
        this.timeOfNumAwakeX = timeOfNumAwakeX;
        this.numAwakeY = numAwakeY;
        this.timeOfNumAwakeY = timeOfNumAwakeY;
        this.numAwakeZ = numAwakeZ;
        this.timeOfNumAwakeZ = timeOfNumAwakeZ;
        this.numAwakeAll = numAwakeAll;
        this.timeOfNumAwakeAll = timeOfNumAwakeAll;
    }

    public int getNumAwakeX() {
        return numAwakeX;
    }
    public int getNumAwakeY() {
        return numAwakeY;
    }
    public int getNumAwakeZ() {
        return numAwakeZ;
    }
    public int getNumAwakeAll() { return numAwakeAll; }

    public ArrayList<String> getTimeOfNumAwakeX() {
        return timeOfNumAwakeX;
    }
    public ArrayList<String> getTimeOfNumAwakeY() {
        return timeOfNumAwakeY;
    }
    public ArrayList<String> getTimeOfNumAwakeZ() {
        return timeOfNumAwakeZ;
    }
    public ArrayList<String> getTimeOfNumAwakeAll() { return timeOfNumAwakeAll; }

    public void setNumAwakeX(int numAwakeX) {
        this.numAwakeX = numAwakeX;
    }
    public void setNumAwakeY(int numAwakeY) {
        this.numAwakeY = numAwakeY;
    }
    public void setNumAwakeZ(int numAwakeZ) {
        this.numAwakeZ = numAwakeZ;
    }
    public void setNumAwakeAll(int numAwakeAll) { this.numAwakeAll = numAwakeAll; }

    public void setTimeOfNumAwakeX(ArrayList<String> timeOfNumAwakeX) {
        this.timeOfNumAwakeX = timeOfNumAwakeX;
    }
    public void setTimeOfNumAwakeY(ArrayList<String> timeOfNumAwakeY) {
        this.timeOfNumAwakeY = timeOfNumAwakeY;
    }
    public void setTimeOfNumAwakeZ(ArrayList<String> timeOfNumAwakeZ) {
        this.timeOfNumAwakeZ = timeOfNumAwakeZ;
    }
    public void setTimeOfNumAwakeAll(ArrayList<String> timeOfNumAwakeAll) {
        this.timeOfNumAwakeAll = timeOfNumAwakeAll;
    }

}
