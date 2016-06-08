package org.lobzik.tools.db.mysql;
import java.sql.*;
import java.util.*;
/**
 * Класс содержит статические методы для выборки данных из базы данных
 */
public class DBSelect 
{

  
  public static int getCount(String sSQL, String cntField, List argsList, Connection conn) throws Exception
  {
    List<HashMap> resList = getRows(sSQL, argsList, conn);
    if (resList.size() == 0) return 0;
    if (resList.size() > 1) throw new Exception("Multiple count result");
    return (Integer) resList.get(0).get(cntField);
  }
  
  public static List<HashMap> getRows(String sSQL, List argsList, Connection conn) throws SQLException
  {
    return getRows(sSQL, argsList,  conn,false);
  }
  
  public static List<HashMap> getRows(String sSQL, Connection conn) throws SQLException
  {
    return getRows(sSQL, null, conn, false);
  }

  
  public static List<HashMap> getRows(String sSQL, List argsList, Connection conn, boolean forHtml) throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    ArrayList res = new ArrayList();
    try
    {
      ps = conn.prepareStatement(sSQL);
      if (argsList != null)
      {
          for (int i = 0; i < argsList.size(); i++) ps.setObject(i + 1, argsList.get(i));
      }
      rs = ps.executeQuery();
      
      while (rs.next()) res.add(getResultSetDataMap(rs, forHtml));
      
      rs.close(); rs = null;
      ps.close(); ps = null;
    }
    catch (SQLException e)
    {
      try 
      {
        if (rs != null) rs.close();
        if (ps != null) ps.close();
      }catch (SQLException ee){}
       throw e;
    }
    return res;
  }
  
   public static void executeStatement(String sSQL, List argsList, Connection conn) throws SQLException
   {
    PreparedStatement ps = null;
    try
    {
      ps = conn.prepareStatement(sSQL);
      if (argsList != null)
      {
          for (int i = 0; i < argsList.size(); i++) ps.setObject(i + 1, argsList.get(i));
      }
      ps.execute();
      
      ps.close(); ps = null;
    }
    catch (SQLException e)
    {
      try 
      {
        if (ps != null) ps.close();
      }catch (SQLException ee){}
       throw e;
    }
  }
  
  /**
   * Метод преобразовывает данные строки ResultSet в HashMap, где ключи имена
   * столбцов, а значения - значения столбцов в формате объектов 
   * @throws java.sql.SQLException
   * @return HashMap
   * @param rs - ResultSet
   */
  public static HashMap getResultSetDataMap(ResultSet rs, boolean replaceHtmlCtrSymbols) throws SQLException
  {
    HashMap dataMap = new HashMap();
    ResultSetMetaData rsmd = rs.getMetaData();
    int colNum = rsmd.getColumnCount();
    for (int i = 1; i <= colNum; i++)
    {
      String colName = rsmd.getColumnName(i);
      if (dataMap.containsKey(colName)) continue;
       
      switch (rsmd.getColumnType(i))
      {
          case Types.VARCHAR:
            String sData = rs.getString(colName);
            /*if (replaceHtmlCtrSymbols && sData != null) dataMap.put(colName,prepareTextForHtml(sData));
            else*/  
            	dataMap.put(colName,sData);
          break;
          /*case Types.CLOB:
            dataMap.put(colName, DBTools.readClob( (CLOB) rs.getClob(colName)));
          break;
          case Types.BLOB:
            dataMap.put(colName, DBTools.readBlob( (BLOB) rs.getBlob(colName)));
          break;*/
          case Types.DATE:
          /*
          java.sql.Date sqlDate = rs.getDate(colName);
          if (sqlDate != null) dataMap.put(colName,new java.util.Date(sqlDate.getTime()));
          else dataMap.put(colName,null);
          break;
          */
          
          case Types.TIMESTAMP:
          Timestamp sqlTimestamp = rs.getTimestamp(colName);
          if (sqlTimestamp != null) dataMap.put(colName,new java.util.Date(sqlTimestamp.getTime()));
          else  dataMap.put(colName,null);
          break;
          case Types.BIGINT:
            long lVal = rs.getLong(colName);
            dataMap.put(colName, new Long(lVal));
          break;
          case Types.NUMERIC:
          case Types.SMALLINT:
          case Types.DECIMAL:
            int scale = rsmd.getScale(i);
            if (scale > 0)
            {
              dataMap.put(colName, rs.getDouble(colName));
            }
            else if(rsmd.getPrecision(i) <= 10)
            {
              int iVal = rs.getInt(colName);
              dataMap.put(colName, new Integer(iVal));
            }
            else if(rsmd.getPrecision(i) > 10)
            {
              lVal = rs.getLong(colName);
              dataMap.put(colName, new Long(lVal));
            }
            
          break; 
          
          default:
          dataMap.put(colName,rs.getObject(colName));
      }
    }
    return dataMap;
  }

}