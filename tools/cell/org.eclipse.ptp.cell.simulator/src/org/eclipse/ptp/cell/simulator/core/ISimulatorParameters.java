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

import java.net.URL;

import org.eclipse.ptp.cell.simulator.internal.SimulatorControl;


/**
 * Provides all information that {@link SimulatorControl} requires to launch the simulator.
 * Implementation of this interface will provide methods to set the parameters and provide
 * specific implementation of architecture dependent methods.
 * <p>
 * No defaults default are assumed for most of the parameters. 
 * If a parameter is left empty (null), then the parameter will not be passes to the simulator.
 * The TCL script will assume its own default in these cases.
 * @author Daniel Felix Ferber
 */
public interface ISimulatorParameters extends Cloneable {

	// Types of file system persistence
	public static final int FS_UNKNOWN = 0;
	public static final int FS_DISCARD = 1;
	public static final int FS_WRITE = 2;
	public static final int FS_JORNAL = 3;

	/**
	 * Test if all mandatory parameters were set.
	 * @throws IllegalConfigurationException
	 */
	public abstract void verify() throws IllegalConfigurationException;

	/**
	 * The installation directory. This attribute must be set.
	 */
	public abstract String getSimulatorBaseDirectory();
	public abstract String getSimulatorExecutable();
	public abstract String getSnifExecutable();
	public abstract String getWorkDirectory();
	public abstract String getLogDirectory();
	public abstract String getTapDevicePath();
	public abstract String getPIDPath();
	
	public abstract String getArchitectureTclString();
	public abstract boolean doMemorySize();
	public abstract int getMemorySize();

	public abstract String[] getDeployFileNames();
	public abstract URL[] getDeployFileSources();

	public abstract String getTclScriptName();
	public abstract URL getTclScriptSource();
	
	public abstract String getExtraCommandLineSwitches();

	public abstract boolean doMountExtraImage();
	public abstract String getExtraImageJournalPath();
	public abstract String getExtraImagePath();
	public abstract int getExtraImagePersistence();
	public abstract String getExtraImageMountPoint();
	public abstract String getExtraImageType();

	public abstract boolean doNetworkInit();
	public abstract boolean doSshInit();
	public abstract String getIpHost();
	public abstract String getIpSimulator();
	public abstract String getMacSimulator();
	public abstract String getNetmaskSimulator();

	public abstract boolean doJavaApiSocketInit();
	public abstract int getJavaApiPort();
	public abstract int getJavaApiSocketPort();
	public abstract String getJavaApiSocketHost();
	public abstract int getJavaApiSocketPortMaxTries();
	public abstract int getJavaApiSocketPortTryWait();

	public abstract String getKernelImagePath();
	public abstract String getRootImageJournalPath();
	public abstract String getRootImagePath();
	public abstract int getRootImagePersistence();

	public abstract boolean doConsoleTerminalInit();
	public abstract boolean doConsoleSocketInit();
	public abstract boolean doConsoleEcho();
	public abstract int getConsolePort();
	public abstract int getConsoleSocketPort();
	public abstract String getConsoleSocketHost();
	public abstract int getConsoleSocketPortMaxTries();
	public abstract int getConsoleSocketPortTryWait();
	
	public abstract String getConsoleCommands();

	public abstract boolean doShowSimulatorGUI();

	public abstract ISimulatorDelegate getDelegate();

	public abstract Object clone() throws CloneNotSupportedException;

	public abstract String getWorkDirectoryRelativePath(String tclScriptName);
}