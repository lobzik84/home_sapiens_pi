package org.lobzik.home_sapiens.pi.weather.entity.params;

public class Temperature{
    private int min, max;

    public Temperature(int min, int max) {
        this.min = min;
        this.max = max;
    }

    // �������� ������� ����������� ��� ������� ����������
    public String getAverageString(){
        int average = (min + max) / 2;
        return sign( average ) + average;
    }

    // �������� �������� �����������
    public String getRangeString(){
        return sign( min ) + min + "..." + sign( max ) + max;
    }

    // ���������� ���� �����������
    private String sign( int temp ){
        if( temp > 0 ){
            return "+";
        } else{
            if( temp == 0 ){
                return "";
            } else{
                return "-";
            }
        }
    }
}
