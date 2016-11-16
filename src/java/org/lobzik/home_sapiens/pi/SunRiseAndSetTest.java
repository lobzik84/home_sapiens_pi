/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import java.util.Calendar;
import java.util.Locale;

/**
 *
 * @author lobzik
 */
public class SunRiseAndSetTest {

    public static void main(String[] args) {
        Location location = new Location("55.768987", "37.609407");
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, Locale.getDefault().getDisplayName());

        Calendar officialSunrise = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance());
        Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
        
        System.out.println("Восход " + officialSunrise.getTime());
        System.out.println("Закат " + officialSunset.getTime());
                
    }
}
