package mow.thm.de.sleeptracker3;

import java.util.ArrayList;

public class History {

    // Einschlafen & Aufwachen (Nur Uhrzeit o. Uhrzeit + Datum?):
    private String startingTime;
    private String endingTime;
    private String durationHrs; // Schlafdauer (endingTime minus startingTime):

//    //TODO:
//    int numAwake; // Wie oft man "leichten Schlaf" hatte
//    ArrayList<String> timeOfNumAwake; // Zu welchen Uhrzeiten man "leichten Schlaf" hatte

    public History() {
    }

    public History(String startingTime, String endingTime, String durationHrs) {
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.durationHrs = durationHrs;
//        this.numAwake = numAwake;
//        this.timeOfNumAwake = timeOfNumAwake;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public String getDuration() {
        return durationHrs;
    }

//    public int getNumAwake() {
//        return numAwake;
//    }
//
//    public ArrayList<String> getTimeOfNumAwake() {
//        return timeOfNumAwake;
//    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }
    public void setEndingTime(String endingTime) {
        this.endingTime = endingTime;
    }

    public void setDuration(String durationHrs) {
        this.durationHrs = durationHrs;
    }
//
//    public void setNumAwake(int numAwake) {
//        this.numAwake = numAwake;
//    }
//
//    public void setTimeOfNumAwake(ArrayList<String> timeOfNumAwake) {
//        this.timeOfNumAwake = timeOfNumAwake;
//    }

}
