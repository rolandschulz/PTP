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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ptp.cell.simulator.conf.Parameters;
import org.eclipse.ptp.utils.core.linux.ArgumentParser;


/**
 * A typical implementation of {@link ISimulatorDelegate} for running the simulator
 * on a Linux host. It assumes that the simulator will receive parameters as
 * implemented in the org.eclipse.ptp.cell.simulator.profile.default plug-in.
 * @author Daniel Felix Ferber
 */
public abstract class AbstractSimulatorDelegate implements ISimulatorDelegate{
	ISimulatorParameters configuration;

	public AbstractSimulatorDelegate(ISimulatorParameters configuration) {
		super();
		this.configuration = configuration;
	}
	
	public ISimulatorParameters getConfiguration() {
		return configuration;
	}

	protected String[] toVariablesArray(Map env) {
		Iterator iterator = env.entrySet().iterator();
		String strings[] = new String[env.size()];
		int i = 0;
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			strings[i] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
			i++;
		}
		return strings;
	}
	
	protected void addDefaultVariables(Map env) {
		env.put("SYSTEMSIM_TOP", configuration.getSimulatorBaseDirectory()); //$NON-NLS-1$
		/*
		 * Add Cell Simulator specific variables.
		 */
		if (configuration.getArchitectureTclString() != null) {
			env.put("CELL_CPU_CONFIG", configuration.getArchitectureTclString()); //$NON-NLS-1$
		}
		if (configuration.doMemorySize()) {
			env.put("CELL_MEMORY_SIZE", Integer.toString(configuration.getMemorySize())); //$NON-NLS-1$
		}
		if (configuration.getKernelImagePath() != null) {
			env.put("CELL_KERNEL_IMAGE", configuration.getKernelImagePath()); //$NON-NLS-1$
		}
		if (configuration.getRootImagePath() != null) {
			env.put("CELL_ROOT_IMAGE", configuration.getRootImagePath()); //$NON-NLS-1$
		}
		if (configuration.getRootImagePersistence() == ISimulatorParameters.FS_DISCARD) {
			env.put("CELL_ROOT_PERSISTENCE", "discard"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (configuration.getRootImagePersistence() == ISimulatorParameters.FS_WRITE) {
			env.put("CELL_ROOT_PERSISTENCE", "write"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (configuration.getRootImagePersistence() == ISimulatorParameters.FS_JORNAL) {
			env.put("CELL_ROOT_PERSISTENCE", "journal"); //$NON-NLS-1$ //$NON-NLS-2$
			if (configuration.getRootImageJournalPath() != null) {
				env.put("CELL_ROOT_JOURNAL", configuration.getRootImageJournalPath()); //$NON-NLS-1$
			}
		} 
		
		if (configuration.doMountExtraImage() && (configuration.getExtraImagePath() != null)) {
			env.put("CELL_EXTRA_IMAGE", configuration.getExtraImagePath()); //$NON-NLS-1$
			if (configuration.getExtraImagePersistence() == ISimulatorParameters.FS_DISCARD) {
				env.put("CELL_EXTRA_PERSISTENCE", "discard"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (configuration.getExtraImagePersistence() == ISimulatorParameters.FS_WRITE) {
				env.put("CELL_EXTRA_PERSISTENCE", "write"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (configuration.getExtraImagePersistence() == ISimulatorParameters.FS_JORNAL) {
				env.put("CELL_EXTRA_PERSISTENCE", "journal"); //$NON-NLS-1$ //$NON-NLS-2$
				if (configuration.getExtraImageJournalPath() != null) {
					env.put("CELL_EXTRA_JOURNAL", configuration.getExtraImageJournalPath()); //$NON-NLS-1$
				}	
			}
			if (configuration.getExtraImageMountPoint() != null) {
				env.put("CELL_EXTRA_MOUNTPOINT", configuration.getExtraImageMountPoint()); //$NON-NLS-1$
			}
			if (configuration.getExtraImageType() != null) {
				env.put("CELL_EXTRA_TYPE", configuration.getExtraImageType()); //$NON-NLS-1$
			}
		}
		if (configuration.doNetworkInit()) {
			env.put("CELL_NET_INIT", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			if (configuration.getIpHost() != null) {
				env.put("CELL_NET_IP_HOST", configuration.getIpHost()); //$NON-NLS-1$
			}
			if (configuration.getIpSimulator() != null) {
				env.put("CELL_NET_IP_SIMULATOR", configuration.getIpSimulator()); //$NON-NLS-1$
			}
			if (configuration.getMacSimulator() != null) {
				env.put("CELL_NET_MAC_SIMULATOR", configuration.getMacSimulator()); //$NON-NLS-1$
			}
			if (configuration.getNetmaskSimulator() != null) {
				env.put("CELL_NET_MASK", configuration.getNetmaskSimulator()); //$NON-NLS-1$
			}			
			if (configuration.doSshInit()) {
				env.put("CELL_SSH_INIT", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				env.put("CELL_SSH_INIT", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			env.put("CELL_NET_INIT", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (configuration.doConsoleSocketInit()) {
			env.put("CELL_CONSOLE_PORT", Integer.toString(configuration.getConsolePort())); //$NON-NLS-1$
		}
		if (configuration.doConsoleEcho()) {
			env.put("CELL_CONSOLE_ECHO", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			env.put("CELL_CONSOLE_ECHO", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (configuration.getConsoleCommands() != null) {
			env.put("CELL_CONSOLE_COMMANDS", configuration.getConsoleCommands()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Create the command line to launch the simulator.
	 */
	public String[] createCommandLine() throws SimulatorException {
		/**
		 * Create essential command line.
		 */
		Vector commandVector = new Vector();
		commandVector.add(configuration.getSimulatorExecutable()); // Programm executable call
		if (configuration.doShowSimulatorGUI()) {
			commandVector.add("-g"); // Start the simulator in graphical user interface mode //$NON-NLS-1$
		}
		commandVector.add("-f"); //$NON-NLS-1$
		commandVector.add(configuration.getTclScriptName());
		if (configuration.doJavaApiSocketInit() && Parameters.doUseJavaAPI()) {
			commandVector.add("-s"); //$NON-NLS-1$
			commandVector.add(Integer.toString(configuration.getJavaApiPort()));
		}
		if (! configuration.doConsoleTerminalInit()) {
			commandVector.add("-n"); // Suppress the simulated console xterm window at start-up. //$NON-NLS-1$
		}
		
		/**
		 * Add extra arguments provided by the user.
		 */
		if (configuration.getExtraCommandLineSwitches() != null) {
			ArgumentParser argumentParser = new ArgumentParser(configuration.getExtraCommandLineSwitches());
			commandVector.addAll(argumentParser.getTokenList());
		}
		
		String[] commandArray = new String[commandVector.size()];
		commandArray = (String[]) commandVector.toArray(commandArray);
		       
		return commandArray;
	}
}
