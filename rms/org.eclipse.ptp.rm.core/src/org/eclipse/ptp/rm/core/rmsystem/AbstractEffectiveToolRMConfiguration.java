package org.eclipse.ptp.rm.core.rmsystem;

/**
 * Stores the tool configuration attributes that shall be used, according the
 * the "useDefault" attribute from the resource manager configuration, and
 * appends the remoteInstallPath to the command, if applicable.
 * <p>
 * The selection of the values for the configuration MUST be implemented by the
 * constructor. Typically, if "useDefaults" is of, then configuration is copied
 * from the resource manager configuration. Else, if "useDefaults" is true, then
 * the constructor usually copies the configuration from default values or from
 * the preferences. The constructor may use
 * {@link #applyValues(String, String, String, String, int, String, String)} to
 * set all configuration attributes.
 * <p>
 * All command attributes do automatically add the remoteInstallPath in front to
 * the command string, if remoteInstallPath is not null and not empty.
 * <p>
 * The class provides getters to read the configuration attributes.
 * 
 * @author Daniel Felix Ferber
 */
public class AbstractEffectiveToolRMConfiguration {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private String launchCmd;
	private String debugCmd;
	private String discoverCmd;
	private String periodicMonitorCmd;
	private int periodicMonitorTime;
	private String continuousMonitorCmd;
	private String remoteInstallPath;
	int capabilities;

	private AbstractToolRMConfiguration configuration;

	public AbstractEffectiveToolRMConfiguration(
			AbstractToolRMConfiguration configuration) {
		this.configuration = configuration;
		this.capabilities = configuration.getCapabilities();
	}

	protected void applyValues(String launchCmd, String debugCmd,
			String discoverCmd, String periodicMonitorCmd,
			int periodicMonitorTime, String continuousMonitorCmd,
			String remoteInstallPath) {
		this.launchCmd = launchCmd;
		this.debugCmd = debugCmd;
		this.discoverCmd = discoverCmd;
		this.periodicMonitorCmd = periodicMonitorCmd;
		this.periodicMonitorTime = periodicMonitorTime;
		this.continuousMonitorCmd = continuousMonitorCmd;
		this.remoteInstallPath = remoteInstallPath;
	}

	protected String completeCommand(String command) {
		if (remoteInstallPath == null) return command;
		if (remoteInstallPath.length() == 0) return command;
		// TODO: Remove this hard-coded path calculation!
		return remoteInstallPath+"/"+command.trim(); //$NON-NLS-1$
	}

	public String getLaunchCmd() {
		return completeCommand(launchCmd);
	}

	public String getDebugCmd() {
		return completeCommand(debugCmd);
	}

	public String getDiscoverCmd() {
		return completeCommand(discoverCmd);
	}

	public String getPeriodicMonitorCmd() {
		return completeCommand(periodicMonitorCmd);
	}

	public int getPeriodicMonitorTime() {
		return periodicMonitorTime;
	}

	public String getContinuousMonitorCmd() {
		return completeCommand(continuousMonitorCmd);
	}

	public String getRemoteInstallPath() {
		return remoteInstallPath;
	}

	public boolean hasDiscoverCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_DISCOVER) != 0
		&& discoverCmd != null
		&& !discoverCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasLaunchCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_LAUNCH) != 0
		&& launchCmd != null && !launchCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasDebugCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_LAUNCH) != 0
		&& debugCmd != null && !debugCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasContinuousMonitorCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_CONTINUOUS_MONITOR) != 0
		&& continuousMonitorCmd != null
		&& !continuousMonitorCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasPeriodicMonitorCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_PERIODIC_MONITOR) != 0
		&& periodicMonitorCmd != null
		&& !periodicMonitorCmd.trim().equals(EMPTY_STRING);
	}

	protected int getCapabilities() {
		return capabilities;
	}
}
