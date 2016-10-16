package org.lobzik.home_sapiens.pi.weather.entity.params;

public enum Cloudiness{
    CLEAR,
    PARTLY,
    RAIN,
    CLOUDY;

    @Override
    public String toString() {
        switch (this){
            case CLEAR:
                return "ЯСНО";
            case PARTLY:
                return "ЧАСТИЧНО ОБЛАЧНО";
            case RAIN:
                return "ДОЖДЛИВО";
            case CLOUDY:
                return "ОБЛАЧНО";
        }
        return null;
    }
}
