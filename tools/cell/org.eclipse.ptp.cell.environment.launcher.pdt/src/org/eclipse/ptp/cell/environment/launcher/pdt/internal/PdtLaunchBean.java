/**
 * 
 */
package org.eclipse.ptp.cell.environment.launcher.pdt.internal;

/**
 * Keep all PDT-relative data for use in the launch
 * 
 * @author Richard Maciel
 *
 */
public class PdtLaunchBean {
	private boolean copyXmlFile;
	private String remoteXmlDirPath;
	private String localXmlFilePath;
	private String remoteXmlFile;
	private String remoteTraceDirPath;
	private String traceFilePrefix;
	
	
	public boolean isCopyXmlFile() {
		return copyXmlFile;
	}
	public void setCopyXmlFile(boolean copyXmlFile) {
		this.copyXmlFile = copyXmlFile;
	}
	public String getRemoteXmlDirPath() {
		return remoteXmlDirPath;
	}
	public void setRemoteXmlDirPath(String remoteXmlDirPath) {
		this.remoteXmlDirPath = remoteXmlDirPath;
	}
	public String getLocalXmlFilePath() {
		return localXmlFilePath;
	}
	public void setLocalXmlFilePath(String localXmlFilePath) {
		this.localXmlFilePath = localXmlFilePath;
	}
	public String getRemoteXmlFile() {
		return remoteXmlFile;
	}
	public void setRemoteXmlFile(String remoteXmlFile) {
		this.remoteXmlFile = remoteXmlFile;
	}
	public String getRemoteTraceDirPath() {
		return remoteTraceDirPath;
	}
	public void setRemoteTraceDirPath(String remoteTraceDirPath) {
		this.remoteTraceDirPath = remoteTraceDirPath;
	}
	public String getTraceFilePrefix() {
		return traceFilePrefix;
	}
	public void setTraceFilePrefix(String traceFilePrefix) {
		this.traceFilePrefix = traceFilePrefix;
	}
	
	
}
