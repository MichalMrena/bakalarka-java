package utils;

public class Stopwatch {

    private final long beginTime;

    public Stopwatch() {
        this.beginTime = System.currentTimeMillis();
    }
    
    public long getTime() {
        return System.currentTimeMillis() - this.beginTime;
    }
    
}
