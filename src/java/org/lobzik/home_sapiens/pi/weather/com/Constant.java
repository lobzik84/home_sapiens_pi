/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.weather.com;

/**
 *
 * @author dmitry
 */
public class Constant {

	public static class Url {

		private static final String WS_BASE = "http://ws.gismeteo.ru";
		public static final String WS_REGISTER = WS_BASE + "/Registration/Register.asmx/RegisterHHUser?name=%s&email=%s&deviceid=%s";
		public static final String WS_GET_CITY = WS_BASE + "/Locations/Locations.asmx/FindByCoords?user=%s&lat=%s&lng=%s&count=%d&language=%s";
		public static final String WS_GET_FORECAST = WS_BASE + "/Weather/Weather.asmx/GetHHForecast?serial=%s&location=%d";
	}

	public static class Xml {

		public static final String RESULT = "result";
		public static final String ERROR_CODE = "errorCode";
		public static final String OK = "OK";
		public static final String ERROR_MESSAGE = "errorMessage";
                public static final String BAD_SERIAL = "BadSerial";
                        
                        
	}

	public static class RegistrationXml {

		public static final String KEY = "key";
	}

	public static class LocationIdXml {

		public static final String LOCATION_INFO_SHORT = "LocationInfoShort";
		public static final String ID = "id";
	}

	public static class ForecastXml {

		public static final String FORECAST = "HHForecast";
		public static final String CL_TAG = "cl";
		public static final String DD_TAG = "dd";
		public static final String FF_TAG = "ff";
		public static final String HUMIDITY_TAG = "humidity";
		public static final String PRCT_TAG = "prct";
		public static final String PRC_TAG = "prc";
		public static final String P_TAG = "p";
		public static final String ST_TAG = "st";
		public static final String TIME_TAG = "time";
		public static final String TOD_TAG = "tod";
		public static final String T_TAG = "t";
	}

}
