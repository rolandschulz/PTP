/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.cellsimulator.core.remote;

import org.eclipse.ptp.cell.environment.cellsimulator.conf.Parameters;
import org.eclipse.ptp.cell.simulator.conf.AttributeNames;
import org.eclipse.ptp.cell.simulator.core.AbstractSimulatorConfiguration;
import org.eclipse.ptp.cell.simulator.core.ISimulatorDelegate;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.core.IllegalConfigurationException;
import org.eclipse.ptp.cell.simulator.core.IllegalParameterException;
import org.eclipse.ptp.remotetools.core.IRemoteDirectory;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;



public class RemoteSimulatorConfiguration extends AbstractSimulatorConfiguration {

	ISimulatorDelegate simulatorDelegate;
	IRemoteExecutionManager executionManager;

	public RemoteSimulatorConfiguration() {
		super();
		this.executionManager = null;
		this.simulatorDelegate = null;
	}

	public RemoteSimulatorConfiguration(IRemoteExecutionManager executionManager) {
		super();
		this.executionManager = executionManager;
		simulatorDelegate = new RemoteSimulatorDelegate(this, executionManager);
	}

	private void checkDelegate() {
		if (simulatorDelegate == null) {
			throw new IllegalStateException(Messages.RemoteSimulatorConfiguration_IncorrectAssociation);
		}
	}
	
	public ISimulatorDelegate getDelegate() {
		checkDelegate();
		return simulatorDelegate;
	}

	public String getSimulatorExecutable() {
		checkDelegate();
		return executionManager.getRemotePathTools().join(getSimulatorBaseDirectory(), Parameters.PATH_SIMULATOR);
	}

	public String getSnifExecutable() {
		checkDelegate();
		return executionManager.getRemotePathTools().join(getSimulatorBaseDirectory(), Parameters.PATH_SNIF);
	}

	public String getWorkDirectoryRelativePath(String path) {
		return executionManager.getRemotePathTools().join(getWorkDirectory(), path);
	}

	public String getLogDirectory() {
		checkDelegate();
		return getWorkDirectoryRelativePath(Parameters.PATH_RUNINFO);
	}
	
	public String getPIDPath() {
		checkDelegate();
		return executionManager.getRemotePathTools().join(getLogDirectory(), Parameters.PATH_PID);
	}

	public String getTapDevicePath() {
		checkDelegate();
		return executionManager.getRemotePathTools().join(getLogDirectory(), Parameters.PATH_TAPDEVICE);
	}
	
	public void verify() throws IllegalConfigurationException {
		super.verify();
		if (executionManager != null) {
			try {
				verifyPath(READABLE_FILE, AttributeNames.KERNEL_IMAGE_PATH, getKernelImagePath());
				verifyPath(EXISTING_FILE, AttributeNames.ROOT_IMAGE_PATH, getRootImagePath());
				if (doMountExtraImage()) {
					if (getExtraImagePersistence() == ISimulatorParameters.FS_WRITE) {
						verifyPath(READABLE_FILE | WRITE, AttributeNames.EXTRA_IMAGE_PATH, getExtraImagePath());
					} else {
						verifyPath(READABLE_FILE, AttributeNames.EXTRA_IMAGE_PATH, getExtraImagePath());
					}
				}
				// verifyPath(ACCESSIBLE_DIR | WRITE, AttributeNames.WORK_DIRECTORY, getWorkDirectory());
			} catch (RemoteConnectionException e) {
				throw new IllegalStateException(Messages.RemoteSimulatorConfiguration_RemoteHostVerificationFailed);
			} catch (CancelException e) {
				throw new IllegalStateException(Messages.RemoteSimulatorConfiguration_RemoteHostVerificationFailed);
			}
		}
	}
	
	protected static int FILE = 1<<1;
	protected static int DIRECTORY = 1<<2;
	protected static int READ = 1<<3;
	protected static int WRITE = 1<<4;

	protected static int EXISTING_FILE = FILE;
	protected static int EXISTING_DIR = DIRECTORY;
	protected static int READABLE_FILE = EXISTING_FILE  | READ;
	protected static int WRITABLE_FILE = EXISTING_FILE  | READ | WRITE;
	protected static int ACCESSIBLE_DIR = DIRECTORY  | READ;
	
	protected void verifyPath(int option, String attributeName, String path) throws IllegalParameterException, RemoteConnectionException, CancelException{
		IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
		IRemotePathTools pathTools = executionManager.getRemotePathTools();

		if (! pathTools.isAbsolute(path)) {
			throw new IllegalParameterException(Messages.RemoteSimulatorConfiguration_PathMustBeAbsolute, attributeName, path);
		}
		
		IRemoteItem item;
		try {
			item = fileTools.getItem(path);
		} catch (RemoteOperationException e) {
			throw new IllegalParameterException(Messages.RemoteSimulatorConfiguration_PathDoesNotExist, attributeName, path);			
		}
		
		if ((option & FILE) != 0) {
			if (! (item instanceof IRemoteFile)) {
				throw new IllegalParameterException(Messages.RemoteSimulatorConfiguration_MustBeFile, attributeName, path);
			}
		}
		if ((option & DIRECTORY) != 0) {
			if (! (item instanceof IRemoteDirectory)) {
				throw new IllegalParameterException(Messages.RemoteSimulatorConfiguration_MustBeDir, attributeName, path);
			}
		}
		if ((option & READ) != 0) {
			if (! item.isReadable()) {
				throw new IllegalParameterException(Messages.RemoteSimulatorConfiguration_MustBeReadable, attributeName, path);
			}
		}
		if ((option & WRITE) != 0) {
			if (! item.isWritable()) {
				throw new IllegalParameterException(Messages.RemoteSimulatorConfiguration_MustBeWritable, attributeName, path);
			}
		}
	}		

}
