package org.eclipse.ptp.bluegene.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class BGCommands {
	private final String sDriver = "com.ibm.db2.jcc.DB2Driver";
	private final String sURL = "jdbc:db2://${host}/bgdb0";
	private final String sUsername = "";
	private final String sPassword = "";

	private Connection connection;
	
	public boolean init() {

		try {
			Class.forName(sDriver).newInstance();
		} catch (Exception e) {
			System.err.println("Failed to load current driver.");
			return false;
		}

		System.out.println("driver loaded");

		try {
			connection = DriverManager.getConnection(sURL, sUsername, sPassword);
		} catch (Exception e) {
			System.err.println("problems connecting to " + sURL + ":");
			System.err.println(e.getMessage());

			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e2) {
				}
			}

			return false;
		}

		System.out.println("connected to db");
		return true;
	}
	
	public void command() {
		Statement stmt = null;
		
		if (connection != null) {
			try {
				stmt = connection.createStatement();
				
				ResultSet result = stmt
						.executeQuery("SELECT jobid, username, blockid, status "
								+ "FROM bglsysdb.tbgljob");
		
				while (result.next()) {
					int jobid = result.getInt("jobid");
					String username = result.getString("username");
					String blockid = result.getString("blockid");
					String status = result.getString("status");
		
					System.out.println(jobid + " " + username + " " + blockid + " "
							+ status);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
				} catch (Exception e) {
				}
				try {
					connection.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
