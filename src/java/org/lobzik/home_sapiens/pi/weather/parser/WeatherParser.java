package org.lobzik.home_sapiens.pi.weather.parser;

import java.io.*;
import java.io.InputStream;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.lobzik.tools.Tools;
import org.lobzik.home_sapiens.pi.weather.entity.Forecast;
import org.lobzik.home_sapiens.pi.weather.entity.WeatherInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.lobzik.home_sapiens.pi.weather.client.WeatherClient;
import org.lobzik.home_sapiens.pi.weather.com.Constant;
import org.lobzik.home_sapiens.pi.weather.client.WeatherParameter;
import org.lobzik.home_sapiens.pi.weather.exception.WeatherClientException;

public class WeatherParser {

    private static final String TAG = WeatherParser.class.getName();

    private static final SimpleDateFormat TIME_FORMAT_FORECAST = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static Object parseXML(WeatherParameter param, InputStream input) throws WeatherClientException, IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
        Document doc = objDocumentBuilder.parse(input);

        switch (param) {
            case REGISTRATION:
                return getKey(doc);
            case GET_LOCATION_ID:
                return getLocationId(doc);
            case GET_FORECAST:
                return getWeather(doc);
            default:
                return null;
        }
    }

    private static boolean checkForRegistration(Document doc) {
        boolean res = false;
        NodeList result = doc.getElementsByTagName(Constant.Xml.RESULT);
        if (result != null && result.getLength() > 0) {
            NodeList list = result.item(0).getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node el = list.item(i);
                System.out.println(el.getNodeName() + " -> " + el.getTextContent());
                if (el.getNodeName().equals(Constant.Xml.ERROR_CODE)) {
                    if (el.getTextContent().equals(Constant.Xml.OK)) {
                        res = true;
                        break;
                    }
                } else if (el.getNodeName().equals(Constant.Xml.ERROR_MESSAGE)) {
                    if (!el.getTextContent().trim().isEmpty()) {
                        System.out.println("ERROR " + TAG + " " + el.getTextContent()); //TODO Можно записать в лог                        
                    }
                }
            }
        }
        return res;
    }

    private static boolean check(Document doc) throws WeatherClientException {
        boolean res = false;
        NodeList result = doc.getElementsByTagName(Constant.Xml.RESULT);
        if (result != null && result.getLength() > 0) {
            NodeList list = result.item(0).getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node el = list.item(i);
                System.out.println(el.getNodeName() + " -> " + el.getTextContent());
                if (el.getNodeName().equals(Constant.Xml.ERROR_CODE)) {
                    if (el.getTextContent().equals(Constant.Xml.OK)) {
                        res = true;
                        break;
                    } else if (el.getTextContent().equals(Constant.Xml.BAD_SERIAL)) {
                        break;
                    }
                } else if (el.getNodeName().equals(Constant.Xml.ERROR_MESSAGE)) {
                    if (!el.getTextContent().trim().isEmpty()) {
                        System.out.println("ERROR " + TAG + " " + el.getTextContent());
                        throw new WeatherClientException();
                    }
                }
            }
        }
        return res;
    }

    private static String getKey(Document doc) {
        if (checkForRegistration(doc)) {
            NodeList key = doc.getElementsByTagName(Constant.RegistrationXml.KEY);
            if (key != null && key.getLength() > 0) {
                return key.item(0).getTextContent();
            }
        }
        return null;
    }

    private static int getLocationId(Document doc) throws WeatherClientException {
        if (check(doc)) {
            NodeList list = doc.getElementsByTagName(Constant.LocationIdXml.LOCATION_INFO_SHORT);
            for (int i = 0; i < list.getLength(); i++) {
                Node el = list.item(i);
                NodeList childItems = el.getChildNodes();
                for (int j = 0; j < childItems.getLength(); ++j) {
                    if (childItems.item(j).getNodeName().equals(Constant.LocationIdXml.ID)) {
                        return Tools.parseInt(childItems.item(j).getTextContent(), -1);
                    }
                }
            }
        }
        return -1;
    }

    private static WeatherInfo getWeather(Document doc) throws WeatherClientException {
        if (check(doc)) {
            NodeList forecasts = doc.getElementsByTagName(Constant.ForecastXml.FORECAST);
            WeatherInfo.Builder builder = new WeatherInfo.Builder();
            for (int i = 0; i < forecasts.getLength(); i++) {
               // System.out.println(forecasts.item(i).getNodeName());
                NodeList forecastNodes = forecasts.item(i).getChildNodes();
                Forecast f = parseForecastNode(forecastNodes);
               // System.out.println(f.toString());
                builder.addForecast(f);
            }
            return builder.build();
        }
        return null;
    }

    private static Forecast parseForecastNode(NodeList forecastNode) {
        Forecast.Builder builder = new Forecast.Builder();
        for (int j = 0; j < forecastNode.getLength(); j++) {
            Node el = forecastNode.item(j);
            String elementTag = el.getNodeName();
            String elementValue = el.getTextContent();
            if (elementTag.equalsIgnoreCase(Constant.ForecastXml.TIME_TAG)) {
                try {
                    long time = TIME_FORMAT_FORECAST.parse(elementValue).getTime();
                    builder.setTime(new Date(time));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.TOD_TAG)) {
                builder.setTimeOfDay(Tools.parseInt(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.T_TAG)) {
                builder.setTemperature(Tools.parseDouble(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.P_TAG)) {
                builder.setPressure(Tools.parseDouble(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.CL_TAG)) {
                builder.setClouds(Tools.parseInt(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.PRC_TAG)) {
                builder.setPrecipitation(Tools.parseDouble(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.PRCT_TAG)) {
                builder.setPrecipitationType(Tools.parseInt(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.DD_TAG)) {
                builder.setWindDirection(Tools.parseDouble(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.FF_TAG)) {
                builder.setWindSpeed(Tools.parseDouble(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.ST_TAG)) {
                builder.setStorm(Tools.parseInt(elementValue, 0));
            } else if (elementTag.equalsIgnoreCase(Constant.ForecastXml.HUMIDITY_TAG)) {
                builder.setHumidity(Tools.parseInt(elementValue, 0));
            }
        }
        return builder.build();
    }

}
