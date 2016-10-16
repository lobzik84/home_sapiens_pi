package org.lobzik.home_sapiens.pi.weather.entity.params;

public enum Precipation{
    RAIN,
    RAINFALL,
    SNOW,
    SNOW2,
    STORM,
    UNKNOWN,
    WITHOUT;

    @Override
    public String toString() {
        switch (this){
            case RAIN:
                return "Дождь";
            case RAINFALL:
                return "Ливень";
            case SNOW:
            case SNOW2:
                return "Снег";
            case STORM:
                return "Шторм";
            case UNKNOWN:
                return "Не известно";
            case WITHOUT:
                return "Без осадков";
        }
        return null;
    }
}
