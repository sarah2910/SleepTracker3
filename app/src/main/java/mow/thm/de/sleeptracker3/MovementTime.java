package mow.thm.de.sleeptracker3;

public class MovementTime {


    // Start & Ende vom Schlaf:
    private String startingTime;
    private String endingTime;

    public MovementTime() {
        // Default constructor
    }

    public MovementTime(String startingTime, String endingTime) {
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

    public String getStartingTime() { return startingTime; }
    public String getEndingTime() { return endingTime; }


    public void setStartingTime(String startingTime) { this.startingTime = startingTime; }
    public void setEndingTime(String endingTime) { this.endingTime = endingTime; }

}
