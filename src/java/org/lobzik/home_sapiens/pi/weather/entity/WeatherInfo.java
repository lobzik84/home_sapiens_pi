package org.lobzik.home_sapiens.pi.weather.entity;

import java.util.ArrayList;
import java.util.List;

public class WeatherInfo {

    private List<Forecast> list = new ArrayList(4);

    public static class Builder {

        List<Forecast> list = new ArrayList(4);

        public Builder addForecast(Forecast forecast) {
            if (list.size() < 4) {
                list.add(forecast);
            }
            return this;
        }

        public WeatherInfo build() {
            return new WeatherInfo(this);
        }

    }

    private WeatherInfo(Builder builder) {
        this.list = builder.list;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Forecast currentElement : this.list) {
            str.append(currentElement.toString());
        }
        return str.toString();
    }
    
    public List<Forecast> getList() {
        return list;
    }
}
