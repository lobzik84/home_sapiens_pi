/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.weather;

import org.lobzik.home_sapiens.pi.weather.client.WeatherClient;
import org.lobzik.home_sapiens.pi.weather.client.WeatherParameter;
import org.lobzik.home_sapiens.pi.weather.com.CommonData;
import org.lobzik.home_sapiens.pi.weather.entity.WeatherInfo;
import org.lobzik.home_sapiens.pi.weather.exception.WeatherClientException;

/**
 *
 * @author konstantin
 */
public class WeatherGetter {

    private static String userKey = CommonData.KEY;
    private static double latitude = 99999;
    private static double longitude = 99999;
    private static int locationId = 4368;

    private static String getKey() throws WeatherClientException {
        return new WeatherClient(WeatherParameter.REGISTRATION).execute().toString();
    }

    private static void getRegistration() throws WeatherClientException {
        String newUserKey = getKey();
        userKey = newUserKey;
        CommonData.KEY = newUserKey;
        int countOfAttempts = 0;
        while (newUserKey == null) {
            newUserKey = getKey();
            ++countOfAttempts;
            if (countOfAttempts < 3) {
                System.out.println("Ошибка при регистрации устройства в сервисе"); //TODO сделать запись в лог
                return;
            }
        }
    }

    private static int getLocationId(double latitude, double longitude) throws WeatherClientException {
        String[] coordinats = {String.valueOf(latitude), String.valueOf(longitude)};
        int locationId = (int) new WeatherClient(WeatherParameter.GET_LOCATION_ID).setLocation(coordinats).execute();
        return locationId;
    }

    private static WeatherInfo getForcast(int locationId) throws WeatherClientException {
        return (WeatherInfo) new WeatherClient(WeatherParameter.GET_FORECAST).setLocationId(locationId).execute();
    }

    public static WeatherInfo getWeatherInfo(double latitude, double longitude) {
        WeatherInfo forcast = null;
        try {
            if (latitude != WeatherGetter.latitude || longitude != WeatherGetter.longitude) { // TODO сделать метод сравнения даблов
                int newLocationId = getLocationId(latitude, longitude);
                int countOfAttemps = 0;
                while (newLocationId == -1) {
                    getRegistration();
                    newLocationId = getLocationId(latitude, longitude);
                    ++countOfAttemps;
                    if (countOfAttemps == 3) {
                        System.out.println("Ошибка при регистрации устройства в сервисе во время получения locationId"); //TODO сделать запись в лог
                        return null;  //Может сделать null'ом работающий прогноз. те если при обновлении случилась ошибка на сервере, 
                        //то занулит существующий прогноз, а лучше оставить старый тогда. Поэтому прежде чем присваивать в модуле,
                        //нужно проверить на null.                  
                    }

                }
                locationId = newLocationId;
            }
            forcast = getForcast(locationId);
            int countOfAttems = 0;
            while (forcast == null) {
                getRegistration();
                forcast = getForcast(locationId);
                ++countOfAttems;
                if (countOfAttems == 3) {
                    System.out.println("Ошибка при регистрации устройства в сервисе во время получения forcast");
                    return null;//Может сделать null'ом работающий прогноз. те если при обновлении случилась ошибка на сервере, 
                    //то занулит существующий прогноз, а лучше оставить старый тогда. Поэтому прежде чем присваивать в модуле,
                    //нужно проверить на null.
                }

            }

        } catch (WeatherClientException e) {
            System.out.println("В полученом файле XML есть запись об ошибке");
            e.printStackTrace();
        }
        return forcast;
    }
}
