package org.eclipse.ptp.rm.core.rmsystem;

public class AbstractEffectiveTollRMConfiguration {
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

	public AbstractEffectiveTollRMConfiguration(AbstractToolRMConfiguration configuration) {
		this.configuration = configuration;
		this.capabilities = configuration.getCapabilities();
	}

	protected void applyValues(
			String launchCmd,
			String debugCmd,
			String discoverCmd,
			String periodicMonitorCmd,
			int periodicMonitorTime,
			String continuousMonitorCmd,
			String remoteInstallPath
	) {
		this.launchCmd = launchCmd;
		this.debugCmd = debugCmd;
		this.discoverCmd = discoverCmd;
		this.periodicMonitorCmd = periodicMonitorCmd;
		this.periodicMonitorTime = periodicMonitorTime;
		this.continuousMonitorCmd = continuousMonitorCmd;
		this.remoteInstallPath = remoteInstallPath;
	}

	public String getLaunchCmd() {
		return launchCmd;
	}
	public String getDebugCmd() {
		return debugCmd;
	}
	public String getDiscoverCmd() {
		return discoverCmd;
	}
	public String getPeriodicMonitorCmd() {
		return periodicMonitorCmd;
	}
	public int getPeriodicMonitorTime() {
		return periodicMonitorTime;
	}
	public String getContinuousMonitorCmd() {
		return continuousMonitorCmd;
	}
	public String getRemoteInstallPath() {
		return remoteInstallPath;
	}

	public boolean hasDiscoverCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_DISCOVER) != 0 && discoverCmd != null
		&& !discoverCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasLaunchCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_LAUNCH) != 0 && launchCmd != null
		&& !launchCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasDebugCmd() {
		return (capabilities & AbstractToolRMConfiguration.CAP_LAUNCH) != 0 && debugCmd != null
		&& !debugCmd.trim().equals(EMPTY_STRING);
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
