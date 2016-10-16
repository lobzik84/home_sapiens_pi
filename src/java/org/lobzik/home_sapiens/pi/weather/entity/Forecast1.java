/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.weather.entity;

import org.lobzik.home_sapiens.pi.weather.entity.params.Date;
import org.lobzik.home_sapiens.pi.weather.entity.params.Cloudiness;
import org.lobzik.home_sapiens.pi.weather.entity.params.Precipation;
import org.lobzik.home_sapiens.pi.weather.entity.params.Pressure;
import org.lobzik.home_sapiens.pi.weather.entity.params.Relwet;
import org.lobzik.home_sapiens.pi.weather.entity.params.Temperature;
import org.lobzik.home_sapiens.pi.weather.entity.params.Wind;

/**
 *
 * @author dmitry
 */
public class Forecast1 {
	private Date date;
    private Temperature temp;
    private Cloudiness cloudiness;
    private Precipation precipitation;
    private Wind wind;
    private Pressure pressure;
    private Relwet relwet;
    // ���������� �����������
    private Temperature tempHeat;

    // �����������
    public Forecast1(Date date, Temperature temp,
                    Cloudiness cloudiness, Precipation precipitation,
                    Wind wind, Pressure pressure, Relwet relwet, Temperature tempHeat) {
        this.date = date;
        this.temp = temp;
        this.cloudiness = cloudiness;
        this.precipitation = precipitation;
        this.wind = wind;
        this.pressure = pressure;
        this.relwet = relwet;
        this.tempHeat = tempHeat;
    }

    public String getDate(){
        return date.toString();
    }

    public String getTOD(){
        return date.todToString();
    }

    public String getShortInf(){
        return temp.getAverageString() + ", " + cloudiness.toString() + ", " + precipitation.toString();
    }

    public String getPressure() {
        return pressure.toString() + " мм.рт.ст.";
    }

    // TODO: ��������
    public String getWind() {
        return wind.getRangeString();
    }

    public String getRelwet() {
        return relwet.toString() + "%";
    }

    public String getHeatTemp() {
        return tempHeat.getRangeString();
    }

    public String  getShortInfFullTemp() {
        return temp.getRangeString() + ", " + cloudiness.toString() + ", " + precipitation.toString();
    }
	
//	public String toJSONObject() {
//		JSONObject jo = new JSONObject();
//		jo.put(jo, date)
//	}
}
