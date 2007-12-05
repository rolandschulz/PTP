package org.eclipse.ptp.bluegene.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BGCommands {
	private final static int		POLL_INTERVAL = 5000; // milliseconds

	private final List<IBGEventListener>	listeners = new ArrayList<IBGEventListener>();
	private final Map<Integer, JobInfo>		jobs = new HashMap<Integer, JobInfo>();
	
	private final String			dbDriver = "com.ibm.db2.jcc.DB2Driver";
	private final String			dbURLFormat = "jdbc:db2://{0}/bgdb0";

	private final Timer				pollTimer = new Timer("BG Poll Timer");
	
	private String					dbHost = "localhost";
	private String					dbUsername = "";
	private String					dbPassword = "";
	private String					lastErrorMessage = "";
	private int						lastErrorCode = 0;
	private Connection				connection = null;
	private TimerTask				pollTask = null;
	private boolean					polling = false;
	
	/**
	 * @param listener
	 */
	public synchronized void addListener(IBGEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * @return
	 */
	public int getErrorCode() {
		return lastErrorCode;
	}
	
	/**
	 * @return
	 */
	public String getErrorMessage() {
		return lastErrorMessage;
	}
	
	/**
	 * @return
	 */
	public boolean init(String[] args) {
		try {
			Class.forName(dbDriver).newInstance();
		} catch (Exception e) {
			System.err.println("Failed to load current driver.");
			return false;
		}

		System.out.println("driver loaded");
		
		if (args.length >= 3) {
			dbHost = args[0];
			dbUsername = args[1];
			dbPassword = args[2];
		}
		
		String dbURL = MessageFormat.format(dbURLFormat, dbHost);

		try {
			connection = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
		} catch (Exception e) {
			System.err.println("problems connecting to " + dbURL + ":");
			System.err.println(e.getMessage());

			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e2) {
				}
			}

			setError(0, e.getMessage());
			return false;
		}

		System.out.println("connected to db");
		return true;
	}
	
	/**
	 * @param listener
	 */
	public synchronized void removeListener(IBGEventListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * @param message
	 */
	public void setError(int code, String message) {
		lastErrorCode = code;
		lastErrorMessage = message;
	}
	
	/**
	 * 
	 */
	public synchronized void startEvents() {
		if (pollTask == null) {
			pollTask = new TimerTask() {
				public void run() {
					checkSystemStatus();
					checkJobStatus();
				}
			};
		}
		
		if (!polling) {
			pollTimer.schedule(pollTask, 0, POLL_INTERVAL);
		}
	}
	
	/**
	 * 
	 */
	public synchronized void stopEvents() {
		if (polling) {
			pollTimer.cancel();
			polling = false;
		}
	}
	
	/**
	 * @param args
	 */
	public void submitJob(String[] args) {
		
	}
	
	/**
	 * @param args
	 */
	public void terminateJob(String[] args) {
		
	}
	
	/**
	 * 
	 */
	private void checkSystemStatus() {
		checkMachineStatus();
		checkNodeStatus();
	}
	
	/**
	 * 
	 */
	private void checkMachineStatus() {
	}

	/**
	 * 
	 */
	private void checkNodeStatus() {
	}

	/**
	 * 
	 */
	private void checkJobStatus() {
		if (connection != null) {
			Statement stmt = null;
			List<JobInfo> jobsToUpdate = new ArrayList<JobInfo>();
			
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
					
					JobInfo savedJob = jobs.get(jobid);
					if (savedJob == null) {
						// FIXME: error no job found
					} else if (!savedJob.getStatus().equals(status)) {
						savedJob.setStatus(status);
						jobsToUpdate.add(savedJob);
						/* 
						 * FIXME: find out terminated code
						 * 
						if (status.equals(TERMINATED)) {
							jobs.remove(jobid);
						}
						 */
					}
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
			
			for (IBGEventListener listener : listeners) {
				listener.handleJobChangedEvent(jobsToUpdate);
			}
		}
	}
}
