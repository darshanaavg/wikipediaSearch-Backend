package wikipedia.search.daos;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import wikipedia.search.models.WikipediaMetaData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MicrosoftSql {

	private static Connection sqlConn;

	private static final String serverName = "D";
	private static final String instanceName = "MSSQLSERVER";

	private static final String userName = "darshanaa";
	private static final String password = "vinayagar";

	public Connection enableSqlCConnection() throws SQLException {

		String dbUrl = "jdbc:sqlserver://" + serverName + ";instanceName=" + instanceName;

		sqlConn = DriverManager.getConnection(dbUrl, userName, password);
		
		if(sqlConn != null) {
			System.out.println("Connection to ms sql established");
		}
		return sqlConn;

	}
	
	public int updateTitleAndCount(String title, int count) throws SQLException {
		WikipediaMetaData w = new WikipediaMetaData();

		String dbName = w.getDbname();
		String tableName = w.getTablename();

		String query = "UPDATE [" + dbName + "].[dbo].[" + tableName + "] SET count=" + count + " WHERE title='"
				+ title + "'";
		return insertData(query);
		
	}

	public int insertData(String query) throws SQLException {

		Statement statement = sqlConn.createStatement();

		return statement.executeUpdate(query);

	}

	public ResultSet selectData(String query) throws SQLException {

		Statement statement = sqlConn.createStatement();

		return statement.executeQuery(query);
	}

}
