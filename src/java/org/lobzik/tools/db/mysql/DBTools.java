package org.lobzik.tools.db.mysql;
import java.sql.*;
import java.util.*;
import javax.naming.*;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBTools {

	static HashMap openConnectionsMap = new HashMap();
	
	public static Connection openConnection(String resourceName) throws Exception
	{
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		BasicDataSource ds = (BasicDataSource)envCtx.lookup(resourceName);
		Connection conn = ds.getConnection();
	    registerConnection(conn);
	    return conn;
	}
	
	public static void closeConnection(Connection conn)
	{
		try
		{
			conn.close();
		}
		catch (Exception e) {}
	}
	/**
	   * Метод возвращает массив имен столбцов составляющих Первичный ключ таблицы
	   * @throws java.sql.SQLException
	   * @return String[]
	   * @param conn - SQL соединение
	   * @param tableName - имя таблицы
	   */
	  static public String[] getPKFieldNameArray(String tableName, Connection conn) throws SQLException
	  {
	    ResultSet rs = null;
	    ArrayList arr = new ArrayList();
	    rs = conn.getMetaData().getPrimaryKeys(null,null,tableName);
	    while (rs.next())
	    {
	      arr.add(rs.getString("COLUMN_NAME"));
	    }
	    try {if (rs != null) rs.close();} catch (SQLException e){throw e;}
	    String res[] = new String[arr.size()];
	    for (int i = 0; i < arr.size(); i++) res[i] = (String) arr.get(i);
	    return res;
	  }
	  /**
	   * Метод возвращает массив ВСЕХ имен столбцов втаблице
	   * @throws java.sql.SQLException
	   * @return String[]
	   * @param conn - SQL соединение
	   * @param tableName
	   */
	  static public String[] getFieldNameArray(String tableName, Connection conn) throws SQLException
	  {
	    ResultSet rs = null;
	    ArrayList arr = new ArrayList();
	    rs = conn.getMetaData().getColumns(null, null, tableName, null);
	    while (rs.next())
	    {
	      arr.add(rs.getString("COLUMN_NAME"));
	    }
	    try {if (rs != null) rs.close();} catch (SQLException e){throw e;}
	    String res[] = new String[arr.size()];
	    for (int i = 0; i < arr.size(); i++) res[i] = (String) arr.get(i);
	    return res;
	  }
	  /**
	   * Метод возвращает Map содержащий данные в виде экземпляра класса ColumnData для
	   * каждого столбца таблицы, ключами являются имена столбцов
	   * @throws java.sql.SQLException
	   * @return Map
	   * @param conn - SQL соединение 
	   * @param tableName - имя таблицы
	   */
	  static public java.util.Map getColumnDataMap(String tableName, Connection conn) throws SQLException
	  {
	    Hashtable ht = new Hashtable();
	    ResultSet rs = null;
	    ArrayList arr = new ArrayList();
	    rs = conn.getMetaData().getColumns(null, null, tableName, null);
	    while (rs.next())
	    {
	      ColumnData colData = new ColumnData(rs);
	      ht.put(colData.columnName,colData);
	    }
	    try {if (rs != null) rs.close();} catch (SQLException e){throw e;}
	    return ht;
	  }
	   /**
	   * Метод возвращает массив ВСЕХ имен столбцов втаблице
	   * @throws java.sql.SQLException
	   * @return String[]
	   * @param columnMap (java.util.Map)
	   */
	  static public String[] getFieldNameArray(java.util.Map columnMap)
	  {
	    Set keySet = columnMap.keySet();
	    String  res[] = new String[keySet.size()];
	    
	    for (Iterator it = keySet.iterator(); it.hasNext();) 
	    {
	      String fieldName = (String) it.next();
	      int columnPos = ((ColumnData) columnMap.get(fieldName)).ordinalPosition;
	      res[columnPos - 1] = fieldName;
	    }
	    return res;
	   
	  }
	  
	  public static void deleteRow(String tableName, HashMap paramsMap, Connection conn) throws SQLException
	  {
	    String sSQL = "DELETE FROM " + tableName;
	    String sWhere = " WHERE ";
	   
	    ArrayList whereValsArr = new ArrayList();
	    ArrayList whereSQLTypesArr = new ArrayList();
	    
	    String columnNameArr[] = getFieldNameArray(tableName, conn);
	    String PKFieldNameArr[] = getPKFieldNameArray(tableName, conn);
	    Map columnDataMap = getColumnDataMap(tableName, conn);
	  
	    if (columnNameArr.length == 0) throw new SQLException("No columns");
	    if (PKFieldNameArr.length == 0) throw new SQLException("No PK columns");
	  
	    
	    for (int i = 0; i < PKFieldNameArr.length; i++)
	    {
	      if (!paramsMap.containsKey(PKFieldNameArr[i]) || paramsMap.get(PKFieldNameArr[i]) == null)
	          throw new SQLException("PK Value not SET"); 
	      String columnName = PKFieldNameArr[i];
	      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
	           
	      if (whereValsArr.size() > 0) sWhere += " AND ";
	      sWhere += columnName + "=?";
	    
	      whereValsArr.add(paramsMap.get(columnName));
	      whereSQLTypesArr.add(new Integer(columnData.SQLTypeId));
	    }
	  
	    sSQL += sWhere;
	    //System.out.println(sSQL);
	    PreparedStatement ps = conn.prepareStatement(sSQL);
	    setVals(ps,whereValsArr, whereSQLTypesArr);
	    ps.execute();
	    try {if (ps != null) ps.close();}catch (SQLException e){}
	  }
	  
	  public static void updateRow(String tableName, HashMap paramsMap, Connection conn) throws SQLException
	  {
	    boolean doCommit = false;
	    if (conn.getAutoCommit()) 
	    {
	      conn.setAutoCommit(false);
	      doCommit = true;
	    }
	    String sSQL = "UPDATE " + tableName + " SET ";
	    String sWhere = " WHERE ";
	    ArrayList setValsArr = new ArrayList();
	    ArrayList setSQLTypesArr = new ArrayList();
	    ArrayList whereValsArr = new ArrayList();
	    ArrayList whereSQLTypesArr = new ArrayList();
	    
	    String columnNameArr[] = getFieldNameArray(tableName, conn);
	    String PKFieldNameArr[] = getPKFieldNameArray(tableName, conn);
	    Map columnDataMap = getColumnDataMap(tableName, conn);
	    //int venderId = getDBVender(conn);
	  
	    if (columnNameArr.length == 0) throw new SQLException("No columns");
	    if (PKFieldNameArr.length == 0) throw new SQLException("No PK columns");
	  
	    Hashtable PKKeysTbl = new Hashtable();
	    ArrayList<String> setFieldsList = new ArrayList<String>();
	    for (int i = 0; i < PKFieldNameArr.length; i++)
	    {
	      if (!paramsMap.containsKey(PKFieldNameArr[i]) || paramsMap.get(PKFieldNameArr[i]) == null)
	          throw new SQLException("PK Value not SET"); 
	      String columnName = PKFieldNameArr[i];
	      if (setFieldsList.contains(columnName))continue;
	      setFieldsList.add(columnName);
	      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
	      PKKeysTbl.put(columnName, "");
	      
	      if (whereValsArr.size() > 0) sWhere =sWhere + " AND ";
	      sWhere =sWhere +  columnName + "=?";
	    
	      whereValsArr.add(paramsMap.get(columnName));
	      whereSQLTypesArr.add(new Integer(columnData.SQLTypeId));
	    }
	  
	    int cnt = 0;
	    ArrayList LobFieldList = new ArrayList();
	    
	    for (int i = 0; i < columnNameArr.length; i++)
	    {
	      String columnName = columnNameArr[i];
	      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
	      int columnType = columnData.SQLTypeId;
	      if (setFieldsList.contains(columnName))continue;
	      if (!paramsMap.containsKey(columnName)) continue;
	      if (PKKeysTbl.containsKey(columnName)) continue;
	      Object data = paramsMap.get(columnName);
	      if (data == null)
	      {
	        if (cnt > 0) sSQL += ",";
	        sSQL += columnName + "=NULL";
	      }
	      else
	      {
	        if (cnt > 0) sSQL += ",";
	        sSQL += columnName + "=?";
	        setValsArr.add(data);
	        setSQLTypesArr.add(new Integer(columnData.SQLTypeId));
	        if (columnType == Types.CLOB || columnType == Types.BLOB) LobFieldList.add(columnName);
	      }
	      setFieldsList.add(columnName);
	      cnt++;
	    }
	    int nUpdatedFields = cnt;
	    setValsArr.addAll(whereValsArr);
	    setSQLTypesArr.addAll(whereSQLTypesArr);
	    sSQL += sWhere;
	    //System.out.println(sSQL);
	    PreparedStatement ps = conn.prepareStatement(sSQL);
	    ResultSet rs = null;
	    setVals(ps,setValsArr, setSQLTypesArr);
	    if (nUpdatedFields > 0)
	    {
	      try 
	      {
	        ps.execute();
	        if (ps != null) { ps.close(); ps = null;}
	      } 
	      catch (SQLException e)
	      {
	        try 
	        {
	          if (doCommit) 
	          {
	            conn.rollback();
	            conn.setAutoCommit(true);
	          }
	          if (ps != null) ps.close();
	          if (rs != null) rs.close();
	        }
	        catch (SQLException ee){}
	        throw e;
	      }
	    }
	    if(doCommit)
	    {
	      conn.commit();
	      conn.setAutoCommit(true);
	    }
	  }
	
	public static int insertRow(String tableName, HashMap paramsMap, Connection conn) throws SQLException
	  {
	    boolean doCommit = false;
	    boolean connCommitState = conn.getAutoCommit();
	    if (conn.getAutoCommit()) 
	    {
	      conn.setAutoCommit(false);
	      doCommit = true;
	    }
	    String sSQL = "INSERT INTO " + tableName + " (";
	    String sVals = " VALUES (";
	    ArrayList paramValsArr = new ArrayList();
	    ArrayList SQLTypesArr = new ArrayList();
	    
	    String columnNameArr[] = getFieldNameArray(tableName, conn);
	    String PKFieldNameArr[] = getPKFieldNameArray(tableName, conn);
	    Map columnDataMap = getColumnDataMap(tableName, conn);
	    int venderId = 3;//getDBVender(conn);
	    String PKFieldName = "";
	    if (PKFieldNameArr.length == 1) PKFieldName = PKFieldNameArr[0];
	    
	    if (columnNameArr.length == 0) throw new SQLException("No columns");
	    
	    boolean useIdentity = true;;
	   // boolean useSequence = false;
	    boolean useOraLOB = false;
	    int newId = 0;
	    
	   /* if ((PKFieldNameArr.length == 1)
	     && (((ColumnData) columnDataMap.get(PKFieldNameArr[0])).typeName.indexOf("identity") > -1))
	    {
	      useIdentity = true;
	    }
	    else if (PKFieldName.length() > 0 && venderId == 2 && !paramsMap.containsKey(PKFieldName))
	    {
	      //newId = getSequenceNextVal(tableName + "_SEQ", conn);
	      paramsMap.put(PKFieldName, new Integer(newId));
	    }*/
	    
	    ArrayList LobFieldList = new ArrayList();
	    
	    for (int i = 0; i < columnNameArr.length; i++)
	    {
	      String columnName = columnNameArr[i];
	      if (!paramsMap.containsKey(columnName)) continue;
	      if (paramsMap.get(columnName) == null) continue;
	      ColumnData columnData = (ColumnData) columnDataMap.get(columnName);
	      int columnType = columnData.SQLTypeId;
	      if (paramValsArr.size() > 0)
	      {
	        sSQL += ",";
	        sVals += ",";
	      }
	      
	      sSQL += columnName;
	      sVals += "?";
	      paramValsArr.add(paramsMap.get(columnName));
	      SQLTypesArr.add(new Integer(columnData.SQLTypeId));
	      if (columnType == Types.CLOB || columnType == Types.BLOB) LobFieldList.add(columnName);
	    }
	    sSQL += ") " + sVals + ")";
	   // if (useIdentity) sSQL +=  "; \n SELECT LAST_INSERT_ID();\n";
	    PreparedStatement ps = conn.prepareStatement(sSQL);
	    ResultSet rs = null;
	    setVals(ps, paramValsArr, SQLTypesArr);
	    try
	    {
	      if (!useIdentity) ps.execute();
	      else
	      {
	        /*rs = ps.executeQuery();
	        if (rs.next()) newId = rs.getInt(1);
	        paramsMap.put(PKFieldName, new Integer(newId));*/
	    	ps.execute();
	    	rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
	    	if (rs.next()) newId = rs.getInt(1);
	        paramsMap.put(PKFieldName, new Integer(newId));
	      }
	      
	      if (rs != null) rs.close();
	      if (ps != null) ps.close();
	      
	    }
	    catch (SQLException e)
	    { 
	      try 
	      {
	        if (doCommit) 
	        {
	          conn.rollback();
	          conn.setAutoCommit(false);
	        }
	        if (rs != null) rs.close();
	        if (ps != null) ps.close();
	      }
	      catch (SQLException ee){}
	      throw e;
	    }
	    if(doCommit)
	    {
	      conn.commit();
	      conn.setAutoCommit(connCommitState);
	    }
	    //jdenis: стераем из карты ключ к PK полю
	    if(paramsMap.containsKey(PKFieldName)) paramsMap.remove(PKFieldName);
	    
	    //if (useIdentity) return newId;
	    return newId;
	  }
	  private static void setVals(PreparedStatement ps, ArrayList valArray, ArrayList typeArray) throws SQLException
	  {
	    if (valArray == null || typeArray == null || valArray.size() != typeArray.size()) throw new SQLException("Invalid params or SQL types array");
	    Connection conn = ps.getConnection();
	    for (int i = 0; i < valArray.size(); i++)
	    {
	      int typeId = ((Integer) typeArray.get(i)).intValue();
	      Object data = valArray.get(i);
	      if (data == null)
	      {
	        ps.setNull(i + 1, typeId);
	        continue;
	      }
	      switch (typeId)
	      {
	        case Types.BINARY:
	        case Types.VARBINARY:
	        case Types.LONGVARBINARY: 
	        if (data instanceof byte[]) ps.setBytes(i + 1, (byte[]) data);
	        else ps.setBytes(i + 1, data.toString().getBytes());
	        break;
	        
	        case Types.DATE:
	        case Types.TIMESTAMP:
	        if (data instanceof String) ps.setString(i + 1, (String) data);
	        else if (data instanceof java.util.Date) 
	          ps.setTimestamp(i + 1, new java.sql.Timestamp(((java.util.Date) data).getTime() ));
	     /*   else if (data instanceof java.sql.Date) 
	          ps.setTimestamp(1 + 1, (java.sql.Date) data); */
	        break;
	        case Types.CLOB:
	        /*CLOB clob = CLOB.empty_lob();
	        ps.setClob(i + 1, clob);
	        break;
	        case Types.BLOB:
	        BLOB blob = BLOB.empty_lob();
	        ps.setBlob(i + 1, blob);
	        break;*/
	        default:
	        if (data instanceof String) ps.setString(i + 1, (String) data);
	        if (data instanceof Integer) ps.setInt(i + 1, ((Integer) data).intValue());
	        if (data instanceof Long) ps.setLong(i + 1, ((Long) data).longValue());
	        if (data instanceof Float) ps.setFloat(i + 1, ((Float) data).floatValue());
	        if (data instanceof Double) ps.setDouble(i + 1, ((Double) data).doubleValue());
	      }
	    }
	  }

	  private static void registerConnection(Connection conn)
	  {
	      Thread thisThread = Thread.currentThread();
	      Map stackTraceMap = Thread.getAllStackTraces();
	      StackTraceElement stackTraceElement[] = (StackTraceElement[]) stackTraceMap.get(thisThread);
	      StackTraceElement callerElement = null;
	      if ( stackTraceElement != null )
	      {
	        boolean flag = false;
	        for (int i = 0; i < stackTraceElement.length; i++ )
	        {
	          if (stackTraceElement[i].getClassName().equals(DBTools.class.getName()))
	          {
	            flag = true;
	          }
	          else if (flag)
	          {
	            callerElement = stackTraceElement[i];
	            break;
	          }
	        }
	      }
	      String callerData = "";
	      if (callerElement != null) 
	      {
	        callerData = callerElement.getClassName() +" : " + callerElement.getMethodName() + "[" + callerElement.getLineNumber() + "]";
	        //System.out.println(callerData);
	        openConnectionsMap.put(conn,callerData);
	      }
	  }
	    
	 
	  public static java.util.List getOpenConnData()
	  {
	    clearConnectionMap();
	    ArrayList dataArr = new ArrayList();
	    Set keySet = openConnectionsMap.keySet();
	    for (Iterator itr = keySet.iterator(); itr.hasNext();)  dataArr.add(openConnectionsMap.get(itr.next()));
	    return dataArr;
	  }
	  
	  private static void clearConnectionMap()
	  {
	    ArrayList removeArr = new ArrayList();
	    Set keySet = openConnectionsMap.keySet();
	    for (Iterator itr = keySet.iterator(); itr.hasNext();)
	    {
	      Connection conn = (Connection) itr.next();
	      try
	      {
	        if (conn.isClosed()) removeArr.add(conn);
	      }
	      catch(Exception e)
	      {
	        System.out.println(e.getMessage());
	        removeArr.add(conn);
	      }
	    }
	    for (Iterator itr = removeArr.iterator(); itr.hasNext();) openConnectionsMap.remove(itr.next());
	  }
	  
}
