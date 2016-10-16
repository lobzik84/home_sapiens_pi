package org.lobzik.home_sapiens.pi.weather.exception;

public class WeatherClientException extends Exception {

	public WeatherClientException() {
		super("В ответном XML файле есть запись об ошибке");
	}
	
}
