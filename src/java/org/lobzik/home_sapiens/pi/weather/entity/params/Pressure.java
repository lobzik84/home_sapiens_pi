package org.lobzik.home_sapiens.pi.weather.entity.params;

public class Pressure {
	
    private final int min, max;

    public Pressure(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "" + (max + min) / 2;
    }
	
}
