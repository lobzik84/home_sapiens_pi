package org.lobzik.home_sapiens.pi.weather.entity.params;

public class Relwet {
    
	private final int min, max;

    public Relwet(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "" + (max + min) / 2;
    }
	
}
