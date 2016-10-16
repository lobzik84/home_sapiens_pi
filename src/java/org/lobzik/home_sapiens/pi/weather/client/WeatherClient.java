package org.lobzik.home_sapiens.pi.weather.client;

import org.lobzik.home_sapiens.pi.weather.com.CommonData;
import org.lobzik.home_sapiens.pi.weather.com.Constant;
import org.lobzik.home_sapiens.pi.weather.parser.WeatherParser;
import org.lobzik.home_sapiens.pi.weather.exception.WeatherClientException;
import java.io.BufferedInputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class WeatherClient {

    final WeatherParameter param;
    String[] location;
    int locationId = -1;

    public WeatherClient(WeatherParameter param) {
        this.param = param;
    }

    public WeatherClient setLocation(String[] location) {
        this.location = location;
        return this;
    }

    public WeatherClient setLocationId(int locationId) {
        this.locationId = locationId;
        return this;
    }

    public Object execute() throws WeatherClientException {
        String urlString;
        switch (param) {
            case REGISTRATION:
                String name = "",
                 email = "",
                 deviceId = "";
                urlString = String.format(Constant.Url.WS_REGISTER,
                        name,
                        email,
                        deviceId);
                break;
            case GET_LOCATION_ID:
                if (location == null || location.length != 2) {
                    throw new WeatherClientException();
                }
                urlString = String.format(Constant.Url.WS_GET_CITY,
                        CommonData.KEY,
                        location[0],
                        location[1],
                        CommonData.COUNT,
                        CommonData.LANGUAGE);
                break;
            case GET_FORECAST:
                if (locationId < 0) {
                    throw new WeatherClientException();
                }
                urlString = String.format(Constant.Url.WS_GET_FORECAST,
                        CommonData.KEY,
                        locationId);
                break;
            default:
                return null;
        }
        return get(urlString);
    }

    private Object get(String urlString) throws WeatherClientException {
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            inputStream = new BufferedInputStream(connection.getInputStream());
            return WeatherParser.parseXML(param, inputStream);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            System.out.println("Ошибка при создании соединения к URL или в XML парсере");
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /*public static String inputStreamToString(InputStream input) throws Exception {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (IOException e) {
            throw e;
        }
    }*/
}
