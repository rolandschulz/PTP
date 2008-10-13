package org.eclipse.ptp.rm.core.rmsystem;

public class AbstractEffectiveTollRMConfiguration {
	private String launchCmd;
	private String debugCmd;
	private String discoverCmd;
	private String periodicMonitorCmd;
	private int periodicMonitorTime;
	private String continuousMonitorCmd;
	private String remoteInstallPath;

	private AbstractToolRMConfiguration configuration;

	public AbstractEffectiveTollRMConfiguration(AbstractToolRMConfiguration configuration) {
		this.configuration = configuration;
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


}
