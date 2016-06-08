package org.lobzik.tools;


import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.lobzik.tools.Convert;

public class CommonTools
{
	public static final String loglevels[] = {"DEBUG", "INFO", "WARN", "ERROR", "FATAL"}; 

  private static final String allBodyTags[] = {
  "a","applet",  "b",  "big",  "blockquote",  "br", "code",
  "center",  "div",  "em",  "font",  "frame",
  "frameset",  "h[n]",  "hr",  "href",  "i",  "img",
  "li",  "nobr",  "ol",  "p",  "pre",  "small",
  "strike",  "strong",   "span",  "textarea",  "sub",  "sup",
  "table",  "tbody",  "th",  "tr",  "td",
  "tt",  "ul",  "u"
  };
  /**
   * Âîçâðàùàåò "÷åëîâå÷åñêîå" ïðåäñòàâëåíèå îáúžìà äàííûõ.
   * Íàïðèìåð: 123,4 ÌÁàéò èëè: 1,23 Êáàéò
   * @param length
   * @return
   */
  public static String humanBytes(int length)
  {
    String[] prefixes = {"Áàéò","ÊÁàéò","ÌÁàéò","ÃÁàéò"};
    float num = length;
    final int step = 1024; 
    for (int i = 0; i < prefixes.length; i++)
    {
      String val = prefixes[i];
      if (num<step) return (((num>=2)?Convert.getFormatedFloat(num,1):Convert.getFormatedFloat(num,(i==0)?0:2)) + " " + val);
      num = num/step;
    }
    return (length + " " + prefixes[0]);
  }
  
  public static int  parseInt(Object o, int defaultVal)
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
  
  public static java.util.Date parseDate(String sDateTime, String pattern)
  {
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
  
  public static Object isNull(Object o, Object defaultObject)
  {
      return (o == null) ? defaultObject : o;
  }
  
  public static String getShortText(String text, int maxLength)
  {
    if ((text == null) || (text.length() <= maxLength))
      return text;
    int pos = maxLength - 1;
    while (pos >= 0 && Character.isLetter(text.charAt(pos))) pos--;
    if (pos <= 0) pos  = maxLength - 1;
    return text.substring(0, pos) + " ...";
  }
  
  public static String listId2WhereElelement(List<Integer> idList, String fieldName)
  {
    StringBuffer where = new StringBuffer();
    where.append(" (");
    for(int i = 0; i < idList.size(); i++)
    {
      if (i > 0) where.append(" OR ");
      where.append(fieldName + "=" + idList.get(i));
    }
    where.append(") "); 
    return where.toString();
  }
  
  public static String listId2WhereElelement(int []  idArr, String fieldName)
  {
    StringBuffer where = new StringBuffer();
    where.append(" (");
    for(int i = 0; i < idArr.length; i++)
    {
      if (i > 0) where.append(" OR ");
      where.append(fieldName + "=" + idArr[i]);
    }
    where.append(") "); 
    return where.toString();
  }
  
  public static ArrayList<HashMap> filerListOfHashMap(List<HashMap> inpList, String key, ArrayList<Integer> idList)
  {
    ArrayList<HashMap> outList = new ArrayList<HashMap>();
    for (HashMap hm: inpList)
    {
      if (!hm.containsKey(key) || !(hm.get(key) instanceof Integer) || !idList.contains(hm.get(key))) continue;
      outList.add(hm);
    }
    return outList;
  }
  
  public static ArrayList<HashMap> leftOuterJoinLists(ArrayList<HashMap> inpList, List<HashMap> joinList, String key, String joinKey)
  {
    for(HashMap hm: inpList)
    {
      for (HashMap joinHm: joinList)
      {
        if (hm.get(key).equals(joinHm.get(joinKey))) hm.putAll(joinHm);
      }
    }
    return inpList;
  }
  
  public static String removeHtmlBodyTags(String htmlFragment)
  {
    return  removeHtmlBodyTags(htmlFragment, allBodyTags);
  }
  
  public static String removeHtmlBodyTags(String htmlFragment, String bodyTags[])
  {
    StringBuffer sb = new StringBuffer("");
    int currPos = 0;
    while (currPos < htmlFragment.length() && currPos >= 0)
    {
     //if (currPos % 1000 == 0) System.out.println(currPos + " of " + htmlFragment.length());
      boolean findTag = false;
      char currChar = htmlFragment.charAt(currPos);
      if (currChar == '<')
      {
        
        for (int i = 0; i < bodyTags.length; i++)
        {
          String tag =  bodyTags[i];
          String tagB = tag.toUpperCase();
          if (htmlFragment.indexOf("<" + tag,currPos) == currPos)
          {
            findTag = true;
            break;
          }
          else  if (htmlFragment.indexOf("<" + tagB,currPos) == currPos)
          {
            findTag = true;
            break;
          }
          else if (htmlFragment.indexOf("</" + tag,currPos) == currPos)
          {
            findTag = true;
            break;
          }
          else  if (htmlFragment.indexOf("</" + tagB,currPos) == currPos)
          {
            findTag = true;
            break;
          }
        }
      }
     
      if (findTag)
      {
        int currPosNew = htmlFragment.indexOf(">", currPos) + 1;
        if (currPosNew <= currPos) break;
        currPos = currPosNew;
        if ((sb.length() > 0) && !(sb.charAt(sb.length() - 1) == ' ')) sb.append(' ');
      }
      else if (htmlFragment.indexOf("&nbsp;",currPos) == currPos) currPos=currPos + "&nbsp;".length();
      else if (htmlFragment.indexOf("&raquo;",currPos) == currPos) currPos=currPos + "&raquo;".length();
      else if (htmlFragment.indexOf("&laquo;",currPos) == currPos) currPos=currPos + "&laquo;".length();
      else if (htmlFragment.indexOf("&quot;",currPos) == currPos) currPos=currPos + "&quot;".length();
      else if (currChar == '\n' || currChar == '\r' || currChar == '\t' || currChar == ' ')
      {
        if ((sb.length() > 0) && !(sb.charAt(sb.length() - 1) == ' ')) sb.append(' ');
        currPos++;
      }
      else
      {
        sb.append(htmlFragment.charAt(currPos));
        currPos++;
      }
    }
    return sb.toString();
  }
  
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
  
  public static String replaceTagsOut(String source)
  {
    if (source == null) return null;
    byte[] asciiToReplace = {34,39,60,62}; //символы " ' < >
    for (int i=0; i<asciiToReplace.length; i++)
      {
        byte[] ascii = {asciiToReplace[i]};
        source = source.replaceAll(new String(ascii),"");
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
  
  public static void rewriteStreams(InputStream is, OutputStream os) throws Exception
  {
    long bufferLength = 1048576;
    byte[] buff = new byte[(int) bufferLength];
    int count = 0;
    while ((count = is.read(buff)) != -1)
    {
      os.write(buff, 0, count);
    }
  }
}
