package org.lobzik.home_sapiens.pi.weather.entity.params;

public class Date{
    private int day, month, year;
    private TOD tod;

    private enum TOD{
        NIGHT,
        MORNING,
        DAY,
        EVENING;

        @Override
        public String toString() {
            switch (this){
                case NIGHT:
                    return "Ночь";
                case MORNING:
                    return "Утро";
                case DAY:
                    return "День";
                case EVENING:
                    return "Вечер";
            }
            return null;
        }
    }

    public Date(int day, int month, int year, int tod) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.tod = TOD.values()[ tod ];
    }

    @Override
    public String toString(){
        return "" + day + "." + month + "." + year;
    }

    public String todToString(){
        return tod.toString();
    }
}
