package org.lobzik.home_sapiens.pi.weather.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

public class Forecast {

    public static final int UNDEFINED_TEMPERATURE = -9999;
    public static final String WEATHERINFO_CLOUDS = "clouds";
//	public static final String WEATHERINFO_CURRENTWEATHERCODE = "weather_code";
//	public static final String WEATHERINFO_FULLDESC = "fulldesc";
//	public static final String WEATHERINFO_GRADE = "grade";
    public static final String WEATHERINFO_HUMIDITY = "humidity";
    public static final String WEATHERINFO_PRECIPITATION = "precipitation";
    public static final String WEATHERINFO_PRECIPITATIONTYPE = "precipitation_type";
    public static final String WEATHERINFO_PRESSURE = "pressure";
//	public static final String WEATHERINFO_SHORTDESC = "shortdesc";
    public static final String WEATHERINFO_STORM = "storm";
    public static final String WEATHERINFO_TEMPERATURE = "temperature";
//	public static final String WEATHERINFO_TEMPERATURE_MAX = "temperature_max";
//	public static final String WEATHERINFO_TEMPERATURE_MIN = "temperature_min";
    public static final String WEATHERINFO_TIME = "time";
    public static final String WEATHERINFO_TIMEOFDAY = "timeofday";
    public static final String WEATHERINFO_WINDDIRECTION = "wind_direction";
    public static final String WEATHERINFO_WINDSPEED = "wind_speed";
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    int clouds, humidity, precipitationType, storm,
            timeOfDay;
    double temperature, pressure, precipitation, windDirection, windSpeed;
    //currentWeatherCode, grade, temperatureMax, temperatureMin,
//	String fullDesc, shortDesc;
    Date time;

    public static class Builder {

        int clouds, humidity, precipitationType, storm,
                timeOfDay;
        double temperature, pressure, precipitation, windDirection, windSpeed;
        //currentWeatherCode, grade, temperatureMax, temperatureMin,
//	String fullDesc, shortDesc;
        Date time;

        public Builder() {
            this.temperature = UNDEFINED_TEMPERATURE;
//			this.temperatureMin = UNDEFINED_TEMPERATURE;
//			this.temperatureMax = UNDEFINED_TEMPERATURE;
        }

        public void setClouds(int clouds) {
            this.clouds = clouds;
        }

//		public void setCurrentWeatherCode(int currentWeatherCode) {
//			this.currentWeatherCode = currentWeatherCode;
//		}
//		public void setGrade(int grade) {
//			this.grade = grade;
//		}
        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public void setPrecipitation(double precipitation) {
            this.precipitation = precipitation;
        }

        public void setPrecipitationType(int precipitationType) {
            this.precipitationType = precipitationType;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public void setStorm(int storm) {
            this.storm = storm;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

//		public void setTemperatureMax(int temperatureMax) {
//			this.temperatureMax = temperatureMax;
//		}
//		public void setTemperatureMin(int temperatureMin) {
//			this.temperatureMin = temperatureMin;
//		}
        public void setTimeOfDay(int timeOfDay) {
            this.timeOfDay = timeOfDay;
        }

        public void setWindDirection(double windDirection) {
            this.windDirection = windDirection;
        }

        public void setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
        }

//		public void setFullDesc(String fullDesc) {
//			this.fullDesc = fullDesc;
//		}
//		public void setShortDesc(String shortDesc) {
//			this.shortDesc = shortDesc;
//		}
        public void setTime(Date time) {
            this.time = time;
        }

        public Forecast build() {
            return new Forecast(this);
        }

    }

    private Forecast(Builder builder) {
        this.clouds = builder.clouds;
//		this.currentWeatherCode = builder.currentWeatherCode;
//		this.grade = builder.grade;
        this.humidity = builder.humidity;
        this.precipitation = builder.precipitation;
        this.precipitationType = builder.precipitationType;
        this.pressure = builder.pressure;
        this.storm = builder.storm;
        this.temperature = builder.temperature;
//		this.temperatureMax = builder.temperatureMax;
//		this.temperatureMin = builder.temperatureMin;
        this.timeOfDay = builder.timeOfDay;
        this.windDirection = builder.windDirection;
        this.windSpeed = builder.windSpeed;
//		this.fullDesc = builder.fullDesc;
//		this.shortDesc = builder.shortDesc;
        this.time = builder.time;
    }

    public JSONObject toJSONObject() {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put(WEATHERINFO_CLOUDS, this.clouds);
//		jSONObject.put(WEATHERINFO_CURRENTWEATHERCODE, this.currentWeatherCode);
//		jSONObject.put(WEATHERINFO_FULLDESC, this.fullDesc);
//		jSONObject.put(WEATHERINFO_GRADE, this.grade);
        jSONObject.put(WEATHERINFO_HUMIDITY, this.humidity);
        jSONObject.put(WEATHERINFO_PRECIPITATION, this.precipitation);
        jSONObject.put(WEATHERINFO_PRECIPITATIONTYPE, this.precipitationType);
        jSONObject.put(WEATHERINFO_PRESSURE, this.pressure);
//		jSONObject.put(WEATHERINFO_SHORTDESC, this.shortDesc);
        jSONObject.put(WEATHERINFO_STORM, this.storm);
        jSONObject.put(WEATHERINFO_TEMPERATURE, this.temperature);
//		jSONObject.put(WEATHERINFO_TEMPERATURE_MIN, this.temperatureMin);
//		jSONObject.put(WEATHERINFO_TEMPERATURE_MAX, this.temperatureMax);
        jSONObject.put(WEATHERINFO_TIME, this.time.getTime());
        jSONObject.put(WEATHERINFO_TIMEOFDAY, this.timeOfDay);
        jSONObject.put(WEATHERINFO_WINDDIRECTION, this.windDirection);
        jSONObject.put(WEATHERINFO_WINDSPEED, this.windSpeed);
        return jSONObject;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("\nTime: ").append(mDateFormat.format(this.time))
                .append("\nTime of day: ").append(this.timeOfDay)
                .append("\nTemperature: ").append(this.temperature)
                //				.append("\nTemperatureMin: ").append(this.temperatureMin)
                //				.append("\nTemperatureMax: ").append(this.temperatureMax)
                .append("\nPressure: ").append(this.pressure)
                .append("\nClouds: ").append(this.clouds)
                .append("\nPrecipitation: ").append(this.precipitation)
                .append("\nPrecipitation type: ").append(this.precipitationType)
                .append("\nWind direction: ").append(this.windDirection)
                .append("\nWind speed: ").append(this.windSpeed)
                .append("\nStorm: ").append(this.storm)
                //				.append("\nWeather code: ").append(this.currentWeatherCode)
                .append("\nHumidity: ").append(this.humidity)
                //				.append("\nGrade: ").append(this.grade)
                //				.append("\nShort description: ").append(this.shortDesc)
                //				.append("\nFull description: ").append(this.fullDesc)
                .toString();
    }

    public int getClouds() {
        return this.clouds;
    }

//	public int getCurrentWeatherCode() {
//		return this.currentWeatherCode;
//	}
//
//	public String getFullDesc() {
//		return this.fullDesc;
//	}
//
//	public int getGrade() {
//		return this.grade;
//	}
    public int getHumidity() {
        return this.humidity;
    }

    public double getPrecipitation() {
        return this.precipitation;
    }

    public int getPrecipitationType() {
        return this.precipitationType;
    }

    public double getPressure() {
        return this.pressure;
    }

//	public String getShortDesc() {
//		return this.shortDesc;
//	}
    public int getStorm() {
        return this.storm;
    }

    public double getTemperature() {
        return this.temperature;
    }

//	public int getTemperatureMax() {
//		return this.temperatureMax;
//	}
//
//	public int getTemperatureMin() {
//		return this.temperatureMin;
//	}
    public Date getTime() {
        return this.time;
    }

    public int getTimeOfDay() {
        int i = this.timeOfDay;
        if (i != -1) {
            return i;
        }
        int hours = this.time.getHours();
        return hours < 4 ? 0 : hours < 10 ? 1 : hours < 16 ? 2 : hours < 22 ? 3 : 0;
    }

    public double getWindDirection() {
        return this.windDirection;
    }

    public double getWindSpeed() {
        return this.windSpeed;
    }

}

//import java.util.ArrayList;
//import java.util.List;
//
//public class WeatherInfo {
//	
////	private List<Forecast> forecasts;
////
////    public WeatherInfo() {
////        forecasts = new ArrayList<Forecast>( 4 );
////    }
////
////    public WeatherInfo(List<Forecast> forecasts) {
////        this.forecasts = forecasts;
////    }
////
////    public void add( Forecast forecast ){
////        forecasts.add( forecast );
////    }
////
////    public Forecast get( int indexForecast ) {
////        return forecasts.get(indexForecast);
////    }
////
////    public List<Forecast> getList() {
////        return forecasts;
////    }
////
////	@Override
////	public String toString() {
////		String str = "";
////		for (Forecast forecast : forecasts) {
////			str += "<FORECAST>\n";
////			str += forecast.getDate() + "\n";
////			str += forecast.getTOD() + "\n";
////			str += forecast.getShortInf() + "\n";
////			str += "</FORECAST>\n";
////		}
////		return str;
////	}
//	
////	public String toJSONString() {
////		
////	}
//	
//}
