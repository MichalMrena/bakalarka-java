package utils;

public class ValueStabilizer {

    private final int stableThresholdLenght;

    private int valueCount;
    private int stableInRow;
    private double lastAvg;
    
    public ValueStabilizer(int stableLenght) {
        this.stableThresholdLenght = stableLenght;
        this.valueCount  = 0;
        this.stableInRow = 0;
        this.lastAvg     = 0.0;
    }

    public void addVal(long val) {
        double newPriemer;

        if (this.valueCount == 0) {
            newPriemer = (double)val;
        } else {
            newPriemer = (this.valueCount * this.lastAvg + val) / (this.valueCount + 1);
        }

        ++this.valueCount;
        this.checkStability(newPriemer);
    }
    
    public boolean isStable() {
        return this.stableInRow >= this.stableThresholdLenght;
    }

    public double getLastAvg() {
        return this.lastAvg;
    }

    private void checkStability(double newPriemer) {
        if (this.areStableLastDigit(newPriemer, this.lastAvg)) {
            ++this.stableInRow;
        } else {
            this.stableInRow = 0;
        }

        this.lastAvg = newPriemer;
    }

    private boolean areStableLastDigit(double newPriemer, double oldPriemer) {
        return this.getLastDigit(newPriemer) == this.getLastDigit(oldPriemer);
    }
    
    private boolean areStabRelativeDiff(double newPriemer, double oldPriemer) {
        double threshold = 1.0 / 300.0;
        double diff = Math.abs(newPriemer - oldPriemer);
        double relativeChange = diff / oldPriemer;

        return relativeChange < threshold;
    }

    private int getLastDigit(double newPriemer) {
        return ((int)newPriemer) % 10;
    }
    
}
