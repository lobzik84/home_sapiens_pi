package org.lobzik.tools.db.mysql;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Test {

	public static void main(String args[]) {
		Connection connection;
		try {
			// Название драйвера
			String driverName = "com.mysql.jdbc.Driver"; 

			Class.forName(driverName);

			// Create a connection to the database
			String serverName = "192.168.4.4";
			String mydatabase = "test";
			String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
			String username = "zmuser";
			String password = "zmpass";

			connection = DriverManager.getConnection(url, username, password);
			System.out.println("is connect to DB" + connection);

			String query = "Select * FROM MODULES WHERE URL = ?";
			/*Statement stmt = connection.createStatement();

			stmt.execute(query);
			ResultSet rs = stmt.getResultSet();
			//stmt.ge
			String dbtime;
			while (rs.next()) {
				dbtime = rs.getString(2);
				System.out.println(dbtime);
			} // end while*/
			
			String sSQL = query;
			ArrayList arg = new ArrayList();
			arg.add("dumb");
			List<HashMap> db = DBSelect.getRows(sSQL, arg, connection);
			for (HashMap h:db)
			{
				System.out.println(h.toString());
			}

			HashMap dbmap = new HashMap();
			dbmap.put("SUBJECT", "javatest");
			int id = DBTools.insertRow("BLOG", dbmap, connection);
			System.out.println("Successfully inserted at id " + id);
			
			dbmap.clear();
			dbmap.put("ID", id);
			dbmap.put("SUBJECT", "java-edited куку ёпта! ");
			DBTools.updateRow("BLOG", dbmap, connection);
			
			System.out.println("Successfully updated");
			
			 db = DBSelect.getRows("SELECT * FROM BLOG ", connection);
				for (HashMap h:db)
				{
					System.out.println(h.toString());
				}
			dbmap.clear();
			dbmap.put("ID", 5);
			DBTools.deleteRow("BLOG", dbmap, connection);
			System.out.println("Successfully deleted");
			connection.close();
		} // end try
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			// Could not find the database driver
		} catch (SQLException e) {
			e.printStackTrace();
			// Could not connect to the database
		}
	}
}