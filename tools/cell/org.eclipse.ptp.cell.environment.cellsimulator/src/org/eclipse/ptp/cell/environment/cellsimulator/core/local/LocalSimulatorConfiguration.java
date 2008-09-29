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
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.Parameters;
import org.eclipse.ptp.cell.simulator.conf.AttributeNames;
import org.eclipse.ptp.cell.simulator.core.AbstractSimulatorConfiguration;
import org.eclipse.ptp.cell.simulator.core.ISimulatorDelegate;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.core.IllegalConfigurationException;
import org.eclipse.ptp.cell.simulator.core.IllegalParameterException;



public class LocalSimulatorConfiguration extends AbstractSimulatorConfiguration {

	ISimulatorDelegate simulatorDelegate;
	
	public LocalSimulatorConfiguration() {
		super();
		simulatorDelegate = new LocalSimulatorDelegate(this);
	}

	public ISimulatorDelegate getDelegate() {
		return simulatorDelegate;
	}
	
	public String getSimulatorExecutable() {
		return getSimulatorBaseDirectory()+"/"+Parameters.PATH_SIMULATOR;//$NON-NLS-1$
	}

	public String getSnifExecutable() {
		return getSimulatorBaseDirectory()+"/"+Parameters.PATH_SNIF;//$NON-NLS-1$
	}

	public String getLogDirectory() {
		return getWorkDirectory() + "/" + Parameters.PATH_RUNINFO;//$NON-NLS-1$
	}

	public String getTapDevicePath() {
		return getLogDirectory() + "/" + Parameters.PATH_TAPDEVICE;//$NON-NLS-1$
	}

	public String getPIDPath() {
		return getLogDirectory() + "/" + Parameters.PATH_PID;//$NON-NLS-1$
	}
	
	public String getWorkDirectoryRelativePath(String path) {
		return getWorkDirectory() + "/" + path; //$NON-NLS-1$
	}


	public void verify() throws IllegalConfigurationException {
		super.verify();
		verifyPath(READABLE_FILE, AttributeNames.KERNEL_IMAGE_PATH, getKernelImagePath());
		verifyPath(EXISTING_FILE, AttributeNames.ROOT_IMAGE_PATH, getRootImagePath());
		if (doMountExtraImage()) {
			if (getExtraImagePersistence() == ISimulatorParameters.FS_WRITE) {
				verifyPath(READABLE_FILE | WRITE, AttributeNames.EXTRA_IMAGE_PATH, getExtraImagePath());
			} else {
				verifyPath(READABLE_FILE, AttributeNames.EXTRA_IMAGE_PATH, getExtraImagePath());
			}
		}
		IPath workDir = new Path(getWorkDirectory());
		IPath stateDir = CellSimulatorTargetPlugin.getDefault().getStateLocation();
		if (stateDir.isPrefixOf(workDir)) {
			// directory will be created inside plugin temp dir, so no checking is required.
		} else {
			// verifyPath(ACCESSIBLE_DIR | WRITE, AttributeNames.WORK_DIRECTORY, getWorkDirectory());
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
	
	protected void verifyPath(int option, String attributeName, String path) throws IllegalParameterException{
		File file = new File(path);

		if (! file.isAbsolute()) {
			throw new IllegalParameterException(Messages.LocalSimulatorConfiguration_PathMustBeAbsolute, attributeName, path);
		}
		if (! file.exists()) {
			throw new IllegalParameterException(Messages.LocalSimulatorConfiguration_PathDoesNotExist, attributeName, path);
		}
		if ((option & FILE) != 0) {
			if (! file.isFile()) {
				throw new IllegalParameterException(Messages.LocalSimulatorConfiguration_MustBeFile, attributeName, path);
			}
		}
		if ((option & DIRECTORY) != 0) {
			if (! file.isDirectory()) {
				throw new IllegalParameterException(Messages.LocalSimulatorConfiguration_MustBeDir, attributeName, path);
			}
		}
		if ((option & READ) != 0) {
			if (! file.canRead()) {
				throw new IllegalParameterException(Messages.LocalSimulatorConfiguration_MustBeReadable, attributeName, path);
			}
		}
		if ((option & WRITE) != 0) {
			if (! file.canWrite()) {
				throw new IllegalParameterException(Messages.LocalSimulatorConfiguration_MustBeWritable, attributeName, path);
			}
		}
	}		

}
