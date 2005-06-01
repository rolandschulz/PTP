/*
 * Created on Mar 15, 2005
 *
 */
package org.eclipse.ptp.debug.external;


/**
 * @author donny
 *
 */
public class DebugConfig {
	/* MPI */
	private String mpirunPath = null;
	private String mpirunMasterHost = null;
	private String mpirunMasterPort = null;
	
	private String userName = null;
	private String userHome = null;
	
	private String[] debuggerPath = null;
	
	public DebugConfig() {
	}
	
	/**
	 * @return Returns the mpirunMasterHost.
	 */
	public String getMpirunMasterHost() {
		return mpirunMasterHost;
	}
	/**
	 * @param mpirunMasterHost The mpirunMasterHost to set.
	 */
	public void setMpirunMasterHost(String mpirunMasterHost) {
		this.mpirunMasterHost = mpirunMasterHost;
	}
	/**
	 * @return Returns the mpirunMasterPort.
	 */
	public String getMpirunMasterPort() {
		return mpirunMasterPort;
	}
	/**
	 * @param mpirunMasterPort The mpirunMasterPort to set.
	 */
	public void setMpirunMasterPort(String mpirunMasterPort) {
		this.mpirunMasterPort = mpirunMasterPort;
	}
	/**
	 * @return Returns the mpirunPath.
	 */
	public String getMpirunPath() {
		return mpirunPath;
	}
	/**
	 * @param mpirunPath The mpirunPath to set.
	 */
	public void setMpirunPath(String mpirunPath) {
		this.mpirunPath = mpirunPath;
	}
	/**
	 * @return Returns the userHome.
	 */
	public String getUserHome() {
		return userHome;
	}
	/**
	 * @param userHome The userHome to set.
	 */
	public void setUserHome(String userHome) {
		this.userHome = userHome;
	}
	/**
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName The userName to set.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * @return Returns the debuggerPath.
	 */
	public String[] getDebuggerPath() {
		return debuggerPath;
	}
	/**
	 * @param debuggerPath The debuggerPath to set.
	 */
	public void setDebuggerPath(String[] debuggerPath) {
		this.debuggerPath = debuggerPath;
	}
}
