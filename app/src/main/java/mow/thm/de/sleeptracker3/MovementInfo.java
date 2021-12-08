package mow.thm.de.sleeptracker3;

public class MovementInfo {

    // Mittelwerte Achsen:
    private float x;
    private float y;
    private float z;

    // Relativ vergangener Zeitraum:
//    private long delta;

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }

//    public long getDelta() {
//        return delta;
//    }

    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setZ(float z) {
        this.z = z;
    }

//    public void setDelta(long delta) {
//        this.delta = delta;
//    }

}
