package mow.thm.de.sleeptracker3;

public class History {

    // Einschlafen & Aufwachen (Nur Uhrzeit o. Uhrzeit + Datum?):
    private String startingTime;
    private String endingTime;
    private float durationHrs; // Schlafdauer (endingTime minus startingTime):

    public History() {

    }

    public History(String startingTime, String endingTime, float durationHrs) {
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.durationHrs = durationHrs;
    }

    //TODO: später...
    //int numAwake; // Wie oft man "leichten Schlaf" hatte
    //String[] timeOfNumAwake; // Zu welchen Uhrzeiten man "leichten Schlaf" hatte
//

    public String getStartingTime() {
        return startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public float getDuration() {
        return durationHrs;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }
    public void setEndingTime(String endingTime) {
        this.endingTime = endingTime;
    }

    public void setDuration(float durationHrs) {
        this.durationHrs = durationHrs;
    }

}