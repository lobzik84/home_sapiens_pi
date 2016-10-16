/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.weather;

import org.lobzik.home_sapiens.pi.weather.entity.WeatherInfo;
import org.lobzik.home_sapiens.pi.weather.exception.WeatherClientException;

/**
 *
 * @author dmitry
 */
public class WeatherTestScript {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws WeatherClientException {
        // TODO code application logic here
        String[] coordinats = {"55.768987", "37.609407"};

        //int locationId = (int) new WeatherClient(WeatherParameter.GET_LOCATION_ID).setLocation(coordinats).execute();
       //System.out.println("ID MESTA: " + locationId);
        
        //new WeatherClient(WeatherParameter.GET_FORECAST).setLocationId(locationId).execute();
        
        WeatherInfo forcast = WeatherGetter.getWeatherInfo(54.9700491, 82.6692267);
        System.out.println(forcast.toString());
        
       
        
    }

}
