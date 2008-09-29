/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.simulator.core;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ptp.cell.simulator.conf.AttributeNames;
import org.eclipse.ptp.cell.simulator.conf.Parameters;


/**
 * A typical implementation of {@link ISimulatorParameters} for running the simulator
 * on a Linux host.
 * Does not assume any default for most of the parameters.
 * The attributed of this class are directly mapped to the parameters 
 * implemented in the org.eclipse.ptp.cell.simulator.profile.default plug-in.
 * @author Daniel Felix Ferber
 */
public abstract class AbstractSimulatorConfiguration implements ISimulatorParameters {

	String simulatorBaseDirectory = null; // mandatory
	
	/**
	 * Working directory. The simulator will be launched inside this directory. This attribute must be set.
	 */
	String workDirectory = null; // mandatory
	
	/**
	 * A string of TCL commands that create the machine configuration.
	 * Separated by newline or semicolons.
	 */
	String architectureTclString = null;
	
	/**
	 * Amount of memory for the simulator, in megabytes.
	 */
	int memorySize = 0;
	
	/**
	 * The kernel image to boot in the simulator.
	 */
	String kernelImagePath = null;
	
	/**
	 * The image for root file system .
	 */
	String  rootImagePath = null;
	
	/**
	 * The write policy for the root file system.
	 */
	int rootImagePersistence = FS_UNKNOWN;
	
	/**
	 * Path to journal file where changes for the root file system are written if the write policy is FS_JOURNAL.
	 */
	String rootImageJournalPath = null;
	
	/**
	 * The image for an extra file system.
	 */
	String  extraImagePath = null;
	
	/**
	 * The write policy for the extra file system.
	 */
	int extraImagePersistence = FS_UNKNOWN;
	
	/**
	 * Path to journal file where changes for the extra file system are written if the write policy is FS_JOURNAL.
	 */
	String extraImageJournalPath = null;
	
	/**
	 * Path where the extra image will be mounted in the simulator.
	 */
	String extraImageMountPoint = null;
	
	/**
	 * File system type of the extra image.
	 */
	String extraImageType = null;
	public static final String FS_EXT2 = "ext2"; //$NON-NLS-1$
	public static final String FS_ISO9660 = "iso9660"; //$NON-NLS-1$
	
	/**
	 * Flag if network is to be configured.
	 */
	boolean networkInit = false;
	
	/**
	 * Flag if SSH server is to be started inside the simulator.
	 */
	boolean sshInit = false;
	
	/**
	 * The IP address for the host if network is flagged to be configured.
	 */
	String ipHost = null;

	/**
	 * The IP address for the simulator  if network is flagged to be configured.
	 */
	String ipSimulator = null;

	/**
	 * The IP address for the host if network is flagged to be configured.
	 */
	String macSimulator = null;
	
	/**
	 * The subnet mask for the network created between the host and the simulator
	 */
	String netmaskSimulator = null;

	/**
	 * Flag if the an xterm console terminal is to be shown.
	 */
	boolean consoleTerminalInit = false;
	
	/**
	 * Flag if the console terminal will be redirected to a socket.
	 */
	boolean consoleSocketInit = false;
	
	/**
	 * Socket port on the localhost where the simulator will be listening for
	 * console connections.
	 */
	int consolePort = 4000;

	/**
	 * Socket port where the simulator control will connect to receive console.
	 */
	int consoleSocketPort = 4000;
	
	/**
	 * Socket host on the localhost where the simulator will be listening for
	 * console connections. null means localhost.
	 */
	String consoleSocketHost = null;
	
	/**
	 * Number of tries to connect to the console socket until giving up and considering a failure.
	 */
	int consoleSocketPortMaxTries = 10;
	
	/**
	 * How long to wait (milliseconds) between retries.
	 */
	int consoleSocketPortTryWait = 1000;
	
	/**
	 * Flag to enable Java API.
	 */
	boolean javaApiSocketInit = false;
	
	/**
	 * Socket port on the localhost where the simulator will be listening for
	 * onnections from the Java API.
	 */
	int javaApiPort = 4001;
	int javaApiSocketPort = 4001;
	
	/**
	 * Socket host on the localhost where the simulator will be listening for
	 * Java API connections. null means localhost.
	 */
	String javaApiSocketHost = null;

	/**
	 * Number of tries to connect to the java API socket.
	 */
	int javaApiSocketPortMaxTries = 10;
	
	/**
	 * How long to wait (milliseconds) between retries.
	 */
	int javaApiSocketPortTryWait = 1000;

	/**
	 * File of the TCL script used to configure the simulator.
	 */
	URL tclScriptSource = null; // mandatory
	String tclScriptName = null; // mandatory
	
	/**
	 * List of files that are to be deployed to the working directory.
	 */
	URL deployFileSources[] = null;
	String deployFileNames[] = null;

	/**
	 * When set to true, the systemsim GUI will be shown.
	 */
	boolean showSimulatorGUI = true;
	
	/**
	 * Extra command line switches.
	 */
	String extraCommandLineSwitches;
	
	/**
	 * Flag if the main Linux console will echo input.
	 */
	boolean consoleEcho = true;
	
	/**
	 * Extra bash commands to be executed after the simulator has launched and configured.
	 * Commands may be separated by semicolon or by newlines.
	 */
	String consoleCommands = null;

	/** Default constructor. */
	protected AbstractSimulatorConfiguration() {
		super();
	}

	private String createNewString(String s) {
		if (s == null) {
			return null;
		} else {
			return new String(s);
		}
	}
	
	public Object clone() throws CloneNotSupportedException {
		AbstractSimulatorConfiguration other =  (AbstractSimulatorConfiguration) super.clone();

		other.workDirectory = createNewString(workDirectory);			
		other.simulatorBaseDirectory = createNewString(simulatorBaseDirectory);
		other.architectureTclString = createNewString(architectureTclString);
		other.kernelImagePath = createNewString(kernelImagePath);
		other.rootImagePath = createNewString(rootImagePath);
		other.rootImageJournalPath = createNewString(rootImageJournalPath);
		other.extraImagePath = createNewString(extraImagePath);
		other.extraImageJournalPath = createNewString(extraImagePath);
		other.extraImageMountPoint = createNewString(extraImageMountPoint);
		other.extraImageType = createNewString(extraImageType);
		other.ipHost = createNewString(ipHost);
		other.ipSimulator = createNewString(ipSimulator);
		other.macSimulator = createNewString(macSimulator);
		other.netmaskSimulator = createNewString(netmaskSimulator);
		other.extraCommandLineSwitches = createNewString(extraCommandLineSwitches);
		other.consoleCommands = createNewString(consoleCommands);
		try {
			if (tclScriptSource != null) {
				other.tclScriptSource = new URL(tclScriptSource.toString());
			} else {
				other.tclScriptSource = null;
			}
		} catch (MalformedURLException e) {
			other.tclScriptSource = null;
		}
		other.tclScriptName = createNewString(tclScriptName);
		other.deployFileSources = new URL[deployFileSources.length];
		other.deployFileNames = new String[deployFileNames.length];
		for (int i = 0; i < deployFileSources.length; i++) {
			try {
				if (deployFileSources != null) {
					other.deployFileSources[i] = new URL(deployFileSources[i].toString());
				} else {
					other.deployFileSources = null;
				}
				other.deployFileNames[i] = createNewString(deployFileNames[i]);
			} catch (MalformedURLException e) {
				other.deployFileSources[i] = null;
				other.deployFileNames[i] = null;
			}
		}
		
		return other;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#verify()
	 */
	public void verify() throws IllegalConfigurationException {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getArchitectureTclString()
	 */
	public String getArchitectureTclString() {
		return architectureTclString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#setArchitectureTclString(java.lang.String)
	 */
	public void setArchitectureTclString(String architectureTclString) {
		this.architectureTclString = architectureTclString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doConsoleEcho()
	 */
	public boolean doConsoleEcho() {
		return consoleEcho;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#setConsoleEcho(boolean)
	 */
	public void setConsoleEcho(boolean consoleEcho) {
		this.consoleEcho = consoleEcho;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getDeployFileNames()
	 */
	public String[] getDeployFileNames() {
		return deployFileNames;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#setDeployFileNames(java.lang.String[])
	 */
	public void setDeployFileNames(String[] deployFileNames) {
		this.deployFileNames = deployFileNames;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getDeployFileSources()
	 */
	public URL[] getDeployFileSources() {
		return deployFileSources;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#setDeployFileSources(java.net.URL[])
	 */
	public void setDeployFileSources(URL[] deployFileSources) {
		this.deployFileSources = deployFileSources;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getExtraCommandLineSwitches()
	 */
	public String getExtraCommandLineSwitches() {
		return extraCommandLineSwitches;
	}

	public void setExtraCommandLineSwitches(String extraCommandLineSwitches) {
		this.extraCommandLineSwitches = extraCommandLineSwitches;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getExtraImageJournalPath()
	 */
	public String getExtraImageJournalPath() {
		return extraImageJournalPath;
	}

	public void setExtraImageJournalPath(String extraImageJournalPath) {
		this.extraImageJournalPath = extraImageJournalPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getExtraImagePath()
	 */
	public String getExtraImagePath() {
		return extraImagePath;
	}

	public void setExtraImagePath(String extraImagePath) {
		this.extraImagePath = extraImagePath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getExtraImagePersistence()
	 */
	public int getExtraImagePersistence() {
		return extraImagePersistence;
	}

	public void setExtraImagePersistence(int extraImagePersistence) {
		this.extraImagePersistence = extraImagePersistence;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.core.ISimulatorParameters#getExtraImageType()
	 */
	public String getExtraImageType() {
		return extraImageType;
	}
	
	public void setExtraImageType(String extraImageType) {
		this.extraImageType = extraImageType;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.core.ISimulatorParameters#getExtraImageMountPoint()
	 */
	public String getExtraImageMountPoint() {
		return extraImageMountPoint;
	}
	
	public void setExtraImageMountPoint(String extraImageMountPoint) {
		this.extraImageMountPoint = extraImageMountPoint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getIpHost()
	 */
	public String getIpHost() {
		return ipHost;
	}

	public void setIpHost(String ipHost) {
		this.ipHost = ipHost;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getIpSimulator()
	 */
	public String getIpSimulator() {
		return ipSimulator;
	}

	public void setIpSimulator(String ipSimulator) {
		this.ipSimulator = ipSimulator;
	}

	public void setNetmaskSimulator(String netmask) {
		this.netmaskSimulator = netmask;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.core.ISimulatorParameters#getNetmask()
	 */
	public String getNetmaskSimulator() {
		return netmaskSimulator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doJavaApiSocketInit()
	 */
	public boolean doJavaApiSocketInit() {
		return javaApiSocketInit;
	}

	public void setJavaApiSocketInit(boolean javaApiSocketInit) {
		this.javaApiSocketInit = javaApiSocketInit;
	}
	
	public int getJavaApiPort() {
		return javaApiPort;
	}
	
	public void setJavaApiPort(int javaApiPort) {
		this.javaApiPort = javaApiPort;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getJavaApiSocketPort()
	 */
	public int getJavaApiSocketPort() {
		return javaApiSocketPort;
	}

	public void setJavaApiSocketPort(int javaApiSocketPort) {
		this.javaApiSocketPort = javaApiSocketPort;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getJavaApiSocketPortMaxTries()
	 */
	public int getJavaApiSocketPortMaxTries() {
		return javaApiSocketPortMaxTries;
	}

	public void setJavaApiSocketPortMaxTries(int javaApiSocketPortMaxTries) {
		this.javaApiSocketPortMaxTries = javaApiSocketPortMaxTries;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getJavaApiSocketPortTryWait()
	 */
	public int getJavaApiSocketPortTryWait() {
		return javaApiSocketPortTryWait;
	}

	public void setJavaApiSocketPortTryWait(int javaApiSocketPortTryWait) {
		this.javaApiSocketPortTryWait = javaApiSocketPortTryWait;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getKernelImagePath()
	 */
	public String getKernelImagePath() {
		return kernelImagePath;
	}

	public void setKernelImagePath(String kernelImagePath) {
		this.kernelImagePath = kernelImagePath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doConsoleSocketInit()
	 */
	public boolean doConsoleSocketInit() {
		return consoleSocketInit;
	}

	public void setConsoleSocketInit(boolean consoleSocketInit) {
		this.consoleSocketInit = consoleSocketInit;
	}

	public int getConsolePort() {
		return consolePort;
	}
	
	public void setConsolePort(int consolePort) {
		this.consolePort = consolePort;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getConsoleSocketPort()
	 */
	public int getConsoleSocketPort() {
		return consoleSocketPort;
	}

	public void setConsoleSocketPort(int consoleSocketPort) {
		this.consoleSocketPort = consoleSocketPort;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getConsoleSocketPortMaxTries()
	 */
	public int getConsoleSocketPortMaxTries() {
		return consoleSocketPortMaxTries;
	}

	public void setConsoleSocketPortMaxTries(int consoleSocketPortMaxTries) {
		this.consoleSocketPortMaxTries = consoleSocketPortMaxTries;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getConsoleSocketPortTryWait()
	 */
	public int getConsoleSocketPortTryWait() {
		return consoleSocketPortTryWait;
	}

	public void setConsoleSocketPortTryWait(int consoleSocketPortTryWait) {
		this.consoleSocketPortTryWait = consoleSocketPortTryWait;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doConsoleTerminalInit()
	 */
	public boolean doConsoleTerminalInit() {
		return consoleTerminalInit;
	}

	public void setConsoleTerminalInit(boolean consoleTerminalInit) {
		this.consoleTerminalInit = consoleTerminalInit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getMacSimulator()
	 */
	public String getMacSimulator() {
		return macSimulator;
	}

	public void setMacSimulator(String macSimulator) {
		this.macSimulator = macSimulator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doMemorySize()
	 */
	public boolean doMemorySize() {
		return memorySize != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getMemorySize()
	 */
	public int getMemorySize() {
		return memorySize;
	}

	public void setMemorySize(int memorySize) {
		this.memorySize = memorySize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doMountExtraImage()
	 */
	public boolean doMountExtraImage() {
		return extraImagePath != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doNetworkInit()
	 */
	public boolean doNetworkInit() {
		return networkInit;
	}

	public void setNetworkInit(boolean networkInit) {
		this.networkInit = networkInit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getRootImageJournalPath()
	 */
	public String getRootImageJournalPath() {
		return rootImageJournalPath;
	}

	public void setRootImageJournalPath(String rootImageJournalPath) {
		this.rootImageJournalPath = rootImageJournalPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getRootImagePath()
	 */
	public String getRootImagePath() {
		return rootImagePath;
	}

	public void setRootImagePath(String rootImagePath) {
		this.rootImagePath = rootImagePath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getRootImagePersistence()
	 */
	public int getRootImagePersistence() {
		return rootImagePersistence;
	}

	public void setRootImagePersistence(int rootImagePersistence) {
		this.rootImagePersistence = rootImagePersistence;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doShowSimulatorGUI()
	 */
	public boolean doShowSimulatorGUI() {
		return showSimulatorGUI;
	}

	public void setShowSimulatorGUI(boolean showSimulatorGUI) {
		this.showSimulatorGUI = showSimulatorGUI;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getSimulatorBaseDirectory()
	 */
	public String getSimulatorBaseDirectory() {
		return simulatorBaseDirectory;
	}

	public void setSimulatorBaseDirectory(String simulatorBaseDirectory) {
		this.simulatorBaseDirectory = simulatorBaseDirectory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#doSshInit()
	 */
	public boolean doSshInit() {
		return sshInit;
	}

	public void setSshInit(boolean sshInit) {
		this.sshInit = sshInit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getTclScriptName()
	 */
	public String getTclScriptName() {
		return tclScriptName;
	}

	public void setTclScriptName(String tclScriptName) {
		this.tclScriptName = tclScriptName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getTclScriptSource()
	 */
	public URL getTclScriptSource() {
		return tclScriptSource;
	}

	public void setTclScriptSource(URL tclScriptSource) {
		this.tclScriptSource = tclScriptSource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorConfiguration#getWorkDirectory()
	 */
	public String getWorkDirectory() {
		return workDirectory;
	}
	
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.core.ISimulatorParameters#getConsoleSocketHost()
	 */
	public String getConsoleSocketHost() {
		return consoleSocketHost;
	}
	
	public void setConsoleSocketHost(String consoleSocketHost) {
		this.consoleSocketHost = consoleSocketHost;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.core.ISimulatorParameters#getJavaApiSocketHost()
	 */
	public String getJavaApiSocketHost() {
		return javaApiSocketHost;
	}

	public void setJavaApiSocketHost(String javaApiSocketHost) {
		this.javaApiSocketHost = javaApiSocketHost;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.core.ISimulatorParameters#getConsoleCommands()
	 */
	public String getConsoleCommands() {
		return consoleCommands;
	}
	
	public void setConsoleCommands(String consoleCommands) {
		this.consoleCommands = consoleCommands;
	}
	
	public String toString() {
		String r = ""; //$NON-NLS-1$
		r += Messages.AbstractSimulatorConfiguration_SimulatorParameters+this.getClass().getName() + "\n"; //$NON-NLS-1$
		r += AttributeNames.LAUNCH_DELEGATE + ": " + getDelegate().getClass().getName() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.SIMULATOR_BASE_DIRECTORY + ": " + simulatorBaseDirectory + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.SIMULATOR_EXECUTABLE + ": " + getSimulatorExecutable() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.SNIF_EXECUTABLE + ": " + getSnifExecutable() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.WORK_DIRECTORY + ": " + workDirectory + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.LOG_PATH + ": " + getLogDirectory() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.TAP_PATH + ": " + getTapDevicePath() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.ARCHITECTURE_TCL_STRING + ": " + architectureTclString.replace('\n', ';') + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.MEMORY_SIZE + ": " + memorySize + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.DEPLOY_FILE_NAMES + ": " + deployFileNames + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.DEPLOY_FILE_SOURCES + ": " + deployFileSources + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.TCL_SCRIPT_NAME + ": " + tclScriptName + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.TCL_SOURCE_NAME + ": " + tclScriptSource + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.EXTRA_COMMAND_LINE_SWITCHES + ": " + extraCommandLineSwitches + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.SSH_INIT + ": " + sshInit + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		
		r += Messages.AbstractSimulatorConfiguration_Network;
		r += AttributeNames.NETWORK_INIT + ": " + networkInit + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.IP_HOST + ": " + ipHost + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.IP_SIMULATOR + ": " + ipSimulator + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.MAC_SIMULATOR + ": " + macSimulator + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.NETMASK_SIMULATOR + ": " + netmaskSimulator + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

		r += Messages.AbstractSimulatorConfiguration_FileSystem;
		r += AttributeNames.KERNEL_IMAGE_PATH + ": " + kernelImagePath + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.ROOT_IMAGE_PATH + ": " + rootImagePath + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.ROOT_IMAGE_PERSISTENCE + ": " + rootImagePersistence + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.ROOT_IMAGE_JOURNAL_PATH + ": " + rootImageJournalPath + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

		r += Messages.AbstractSimulatorConfiguration_JavaAPI;
		r += Messages.AbstractSimulatorConfiguration_JavaAPIQuestion + Parameters.doUseJavaAPI() + "\n"; //$NON-NLS-1$
		r += Messages.AbstractSimulatorConfiguration_JavaAPIGUIQuestion + Parameters.doHandleJavaApiGuiIssue() + "\n"; //$NON-NLS-1$
		r += AttributeNames.EXTRA_IMAGE_INIT + ": " + doMountExtraImage() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.EXTRA_IMAGE_PATH + ": " + extraImagePath + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.EXTRA_IMAGE_PERSISTENCE + ": " + extraImagePersistence + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.EXTRA_IMAGE_JOURNAL_PATH + ": " + extraImagePath + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.EXTRA_IMAGE_TYPE + ": " + extraImageType + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.EXTRA_IMAGE_MOUNTPOINT + ": " + extraImageMountPoint + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.JAVA_API_INIT + ": " + javaApiSocketInit + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.JAVA_API_PORT + ": " + javaApiPort + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.JAVA_API_SOCKET_PORT + ": " + javaApiSocketPort + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.JAVA_API_SOCKET_HOST + ": " + javaApiSocketHost + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.JAVA_API_SOCKET_MAX_TRIES + ": " + javaApiSocketPortMaxTries + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.JAVA_API_SOCKET_TRY_WAIT + ": " + javaApiSocketPortTryWait + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		
		r += Messages.AbstractSimulatorConfiguration_LinuxConsole;
		r += AttributeNames.CONSOLE_TERMINAL_INIT + ": " + consoleTerminalInit + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_ECHO_INIT + ": " + consoleEcho + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_SOCKET_INIT + ": " + consoleSocketInit + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_PORT + ": " + consolePort + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_SOCKET_PORT + ": " + consoleSocketPort + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_SOCKET_HOST + ": " + consoleSocketHost + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_SOCKET_MAX_TRIES + ": " + consoleSocketPortMaxTries + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_SOCKET_TRY_WAIT + ": " + consoleSocketPortTryWait + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.CONSOLE_COMMANDS + ": " + consoleCommands + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		r += AttributeNames.SHOW_SIMULATOR_GUI + ": " + showSimulatorGUI + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

		return r;
	}
}
