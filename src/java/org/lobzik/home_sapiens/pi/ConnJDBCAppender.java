/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

/**
 *
 * @author lobzik
 */

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.jdbc.JDBCAppender;
import org.lobzik.tools.db.mysql.DBTools;

public class ConnJDBCAppender extends JDBCAppender
{

	private static DataSource ds = null;

	private ConnJDBCAppender(DataSource dsc)
	{
		super();
		ds = dsc;
		
	}
	
	protected  Connection getConnection() throws SQLException
	{

		return ds.getConnection();
	}
	
	protected  void	closeConnection(Connection con) 
	{
		DBTools.closeConnection(con);
	}
	
	public static Appender getAppenderInstance(DataSource dsc, String moduleName)
	{
		ConnJDBCAppender jdbcAppender = new ConnJDBCAppender(dsc);
  		jdbcAppender.setLayout(new PatternLayout());
  		jdbcAppender.setSql("insert into logs values (0,'" + moduleName + "','%d{yyyy-MM-dd HH:mm:ss.SSS}','%p','%m') \n");
		AsyncAppender asyncAppender = new AsyncAppender();
  		asyncAppender.setBufferSize(1000);
  		asyncAppender.addAppender(jdbcAppender);
                asyncAppender.setThreshold(Level.INFO);
  		return asyncAppender;
	}
}