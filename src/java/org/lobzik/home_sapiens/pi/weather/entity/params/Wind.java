package org.lobzik.home_sapiens.pi.weather.entity.params;

public class Wind{
    private int min, max;
    private DIRECTION dir;

    // ����������� ����� � ������, 0 - ��������, 1 - ������-���������,  � �.�.
    private enum DIRECTION{
        NORTH,
        NORTHEAST,
        EAST,
        SOUTHEAST,
        SOUTH,
        SOUTHWEST,
        WEST,
        NORTHWEST;

        @Override
        public String toString() {
             switch (this){
                case NORTH:
                    return "Северный";
                case NORTHEAST:
                    return "Северо-восточный";
                case EAST:
                    return "Восточный";
                case SOUTHEAST:
                    return "Юго-восточный";
                case SOUTH:
                    return "Южный";
                case SOUTHWEST:
                    return "Юго-западный";
                case WEST:
                    return "Западный";
                case NORTHWEST:
                    return "Северо-западный";
            }
            return null;
        }
    }

    public Wind(int min, int max, int dir) {
        this.min = min;
        this.max = max;
        this.dir = DIRECTION.values()[dir];
    }

    public String  getRangeString(){
        return dir.toString() + ", " + min + "-" + max + " м/с";
    }
}
