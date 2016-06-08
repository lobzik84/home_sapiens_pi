package org.lobzik.tools;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

public class Convert
{
  public static String replaceTags(String source)
  {
    if (source == null) return null;
    byte[] asciiToReplace = {34,39,60,62}; //символы " ' < >
    for (int i=0; i<asciiToReplace.length; i++)
      {
        byte[] ascii = {asciiToReplace[i]};
        source = source.replaceAll(new String(ascii),"&#"+String.valueOf(asciiToReplace[i]));
      }
    return(source);
  }
  
  /** Простенький метод заменяет все символы <, >, " их аналогами.
   * для защиты от XSS. Работает с приехавшими POST-параметрами.
   * Автор Лабозин А.В.
   * @return
   */
  public static HashMap replaceTags(HashMap postParams)
  {
    Object key, value;
    for (Iterator itr = postParams.keySet().iterator(); itr.hasNext(); )
    {
      key = itr.next();
      value = postParams.get(key);
      if (value instanceof String)
        {
          value = replaceTags((String)value);
          postParams.put(key, value);
        }
    }
    return (postParams);
  }

  public static boolean checkRightName(String source)
  {
    if (source == null) return true;
    Pattern p = Pattern.compile("^[_a-zA-Z]([_0-9a-zA-Z])*$");
    Matcher m = p.matcher(source);
    boolean b = m.matches();
    return b;
  }

  public static Vector ArrayToVector(Object[] arr)
  {
    Vector resVector = new Vector(arr.length);
    for (int index = 0; index < arr.length; index++)
    {
      resVector.add(arr[index]);
    }
    return resVector;
  } 
  
  public static int parseInt(Object o, int defaultVal)
  {
    try
    {
      return Integer.parseInt(o.toString());
    }
    catch (Exception e)
    {
      return defaultVal;
    }
  }

  public static boolean parseBoolean(Object o, boolean defaultVal)
  {
    try
    {
      return Boolean.parseBoolean(o.toString());
    }
    catch (Exception e)
    {
      return defaultVal;
    }
  }

  public static Boolean parseBoolean(Object o)
  {
    try
    {
      return Boolean.valueOf(Boolean.parseBoolean(o.toString()));
    }
    catch (Exception e)
    {
      return null;
    }
  }

  public static float parseFloat(Object o, float defaultVal)
  {
    try
    {
      return Float.parseFloat(o.toString());
    }
    catch (Exception e)
    {
      return defaultVal;
    }
  }

  public static double parseDouble(Object o, double defaultVal)
  {
    try
    {
      return Double.parseDouble(o.toString());
    }
    catch (Exception e)
    {
      return defaultVal;
    }
  }

  public static Date parseDate(String sDateTime)
  {
    if (sDateTime == null) return null;
    StringTokenizer st = new StringTokenizer(sDateTime, " ");
    String sDate = "", sTime = "";
    if (st.hasMoreTokens()) sDate = st.nextToken();
    if (st.hasMoreTokens()) sTime = st.nextToken();
    if (sDate.length() == 0)return null;
    
    st = new StringTokenizer(sDate, ".-/");
    if (st.countTokens() != 3) return null;
    String sDay = st.nextToken();
    String sMonth = st.nextToken();
    String sYear = st.nextToken();
    if (sDay.length() == 1) sDay = "0" + sDay;
    if (sMonth.length() == 1) sMonth = "0" + sMonth;
    if (sYear.length() == 2) sYear = "20" + sYear;
    
    sDate = sDay + "." + sMonth + "." +sYear;
    
    String sTimeT = sTime;
    sTime = "00:00";
    st = new StringTokenizer(sTimeT,":");
    if (st.countTokens() >= 2)
    {
       String sHour = st.nextToken();
       String sMin = st.nextToken();
       if (sHour.length() == 1) sHour = "0" + sHour;
       if (sMin.length() == 1) sMin = "0" + sMin;
       sTime = sHour + ":" + sMin;
    }
    
    sDateTime = sDate + " " + sTime;
    String pattern = "dd.MM.yyyy HH:mm";
    try
    {
      Locale loc = new Locale("ru","RU");
      SimpleDateFormat dateFormat = new SimpleDateFormat(pattern,loc);
      return dateFormat.parse(sDateTime);
    }
    catch(Exception e)
    {
      return null;
    }
  }

  public static String getFormatedFloat(float val, int fraction)
  {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(fraction);
    nf.setMinimumFractionDigits(fraction);
    String resStr = nf.format(val);
    return resStr;
  }

  public static String getFormatedDate(java.util.Date date)
  {
    return getFormatedDate(date, "dd MMMM yyyy");
  }
	
	public static String getFormatedNewsDate(java.util.Date date)
	{
		if (date  == null) return "";
		String result = "";

	  Calendar calendar1 = Calendar.getInstance(new Locale("ru","RU"));
	  Calendar calendar2 = Calendar.getInstance(new Locale("ru","RU"));
	  calendar2.setTime(date);

	  int dateDiff =  calendar2.get(Calendar.DATE) - calendar1.get(Calendar.DATE);
	  switch (dateDiff)
	  {
	    case -1: result = "Вчера, ";break;
	    case  0: result = "Сегодня, ";break;
	    case  1: result = "Завтра, ";break;
	  }
		
	  return result = result + reformDate(getFormatedDate(date));
	}

	private static String reformDate(String strDate)
	{
		strDate = strDate.replace("Январь","января");
		strDate = strDate.replace("Февраль","февраля");
		strDate = strDate.replace("Март","марта");
		strDate = strDate.replace("Апрель","апреля");
		strDate = strDate.replace("Май","мая");
		strDate = strDate.replace("Июнь","июня");
		strDate = strDate.replace("Июль","июля");
		strDate = strDate.replace("Август","августа");
		strDate = strDate.replace("Сентябрь","сентября");
		strDate = strDate.replace("Октябрь","октября");
		strDate = strDate.replace("Ноябрь","ноября");
		strDate = strDate.replace("Декабрь","декабря");
		
		if(strDate.startsWith("0"))
			strDate = strDate.substring(1);
			
		return strDate;
	}
	
  public static String getFormatedDate(java.util.Date date, String pattern)
  {
    if (pattern == null)
    {
      //"HH:mm E','dd MMMM yyyy";
      pattern = "dd.MM.yyyy";
    }
    String sDate = "";
    if (date == null) return "";
    try
    {
      Locale loc = new Locale("ru","RU");
      SimpleDateFormat dateFormat = new SimpleDateFormat(pattern,loc);
      sDate = dateFormat.format(date);
    }
    catch(Exception e)
    {
      return "Invalid date format";
    }
    return sDate;
  }
  
  public static java.sql.Date UtilToSqlDate(java.util.Date utilDate)
  {
    return new java.sql.Date(utilDate.getTime());
  }

	 public static String ListToCSV(List list)
	 {
		 String str = "";
		 for (int i = 0; i < list.size(); i++)
		 {
			 Object object = list.get(i);
			 str += i == 0 ? object : ("," + object);
		 }
		 return str;
	 }
	 
	 public static byte[] createThumbnail(ByteArrayOutputStream in_baos, int maxx, int maxy)
	 {
		 Image inImage = new ImageIcon(in_baos.toByteArray()).getImage();
		 double scale; 
		 
		 if (((double) maxx / (double) inImage.getWidth(null)) < 
					 ((double) maxy / (double) inImage.getHeight(null))) //если коэффициент шкалы по Х меньше, чем по Y
			 {
				 scale = (double) maxx / (double) inImage.getWidth(null); // считаем шкалу по X
			 } 
			 else
			 {
				 scale =(double) maxy / (double) inImage.getHeight(null); //иначе считаем по Y
			 }
			 
		 if (scale>=1) scale =1;
		 
		 int scaledW = (int) (scale * inImage.getWidth(null));
		 int scaledH = (int) (scale * inImage.getHeight(null)); //считаем размеры выходного изображения

		 BufferedImage outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
		 AffineTransform tx = new AffineTransform();
		 
		 if (scale < 1.0d)
		 {
			 tx.scale(scale, scale);
		 }
		 Graphics2D g2d = outImage.createGraphics();
		 g2d.drawImage(inImage, tx, null);
		 g2d.dispose();
		 
	   ByteArrayOutputStream out_baos = new ByteArrayOutputStream();
		 try 
		 {
			 // Jimi.putImage("image/jpeg", Jimi.createRasterImage(outImage.getSource()), out_baos);
		 } 
		 catch (Exception ex) 
		 {
			 ex.printStackTrace();
		 } 
		 return out_baos.toByteArray();
	 } 
}