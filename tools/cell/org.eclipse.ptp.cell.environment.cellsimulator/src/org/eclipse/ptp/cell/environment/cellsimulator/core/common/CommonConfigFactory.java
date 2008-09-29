/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.cellsimulator.core.common;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.AttributeNames;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.Parameters;
import org.eclipse.ptp.cell.simulator.SimulatorPlugin;
import org.eclipse.ptp.cell.simulator.core.AbstractSimulatorConfiguration;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.extensions.Architecture;
import org.eclipse.ptp.cell.simulator.extensions.LaunchProfile;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.remotetools.utils.verification.IllegalAttributeException;

/**
 * Defines rules to build the target configuration from an attribute hash map.
 * The attributes are divided into three groups. One that are specific how to launch the simulator, another
 * that are related to the connection from the environment to the simulator.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public abstract class CommonConfigFactory {
	
	CommonConfigurationBean bean;
	
	public CommonConfigFactory(CommonConfigurationBean bean) {
		this.bean = bean;
	}
	
	protected void fillSimulatorParameters(AbstractSimulatorConfiguration parameters) throws CoreException {
		ControlAttributes attributes = bean.getAttributes();
		
		try {
			parameters.setSimulatorBaseDirectory(attributes.getString(CommonConfigurationBean.ATTR_SIMULATOR_BASE_DIRECTORY, null));
			
			String architectureId = attributes.getString(CommonConfigurationBean.ATTR_ARCHITECTURE_ID, null);
			if (architectureId != null) {
					Architecture architecture = SimulatorPlugin.getArchitectureManager().getArchitecture(architectureId);
					if (architecture != null) {
						try {
							parameters.setArchitectureTclString(architecture.getTclScriptContent());
						} catch (IOException e) {
							throw new IllegalAttributeException(Messages.CommonConfigFactory_ReadTCLScriptFailed, e);
						}
					}
			}
			parameters.setMemorySize(attributes.verifyInt(Messages.CommonConfigFactory_MemorySize, CommonConfigurationBean.ATTR_MEMORY_SIZE));
			String profileId = attributes.getString(CommonConfigurationBean.ATTR_PROFILE_ID, null);
			if (profileId != null) {
				LaunchProfile profile = SimulatorPlugin.getLaunchProfileManager().getLaunchProfile(profileId);
				if (profile != null) {
					parameters.setDeployFileNames(profile.getDeployPaths());
					parameters.setDeployFileSources(profile.getDeployURLs());
					parameters.setTclScriptName(profile.getTclScriptPath());
					parameters.setTclScriptSource(profile.getTclScriptURL());
				}
			}
			parameters.setExtraCommandLineSwitches(attributes.getString(CommonConfigurationBean.ATTR_EXTRA_COMMAND_LINE_SWITCHES, null));
			
			parameters.setNetworkInit(true);
			parameters.setSshInit(true);
			
			if (attributes.getBoolean(CommonConfigurationBean.ATTR_EXTRA_IMAGE_INIT, false)) {
				parameters.setExtraImagePath(attributes.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_PATH, null));
				String persistence = attributes.verifyString(AttributeNames.ROOT_IMAGE_PERSISTENCE, CommonConfigurationBean.ATTR_EXTRA_IMAGE_PERSISTENCE);
				if (persistence.equals(CommonConfigurationBean.ID_PERSISTENCE_DISCARD)) {
					parameters.setExtraImagePersistence(ISimulatorParameters.FS_DISCARD);
				} else if (persistence.equals(CommonConfigurationBean.ID_PERSISTENCE_JOURNAL)) {
					parameters.setExtraImagePersistence(ISimulatorParameters.FS_JORNAL);
				} else if (persistence.equals(CommonConfigurationBean.ID_PERSISTENCE_WRITE)) {
					parameters.setExtraImagePersistence(ISimulatorParameters.FS_WRITE);
				} else {
					throw new IllegalAttributeException(AttributeNames.ROOT_IMAGE_PERSISTENCE, Messages.CommonConfigFactory_InvalidPersistenceID);
				}
				parameters.setExtraImageJournalPath(attributes.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_JOURNAL_PATH, null));
				parameters.setExtraImageType(attributes.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_TYPE, null));
				parameters.setExtraImageMountPoint(attributes.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_MOUNTPOINT, null));
			}
			
			parameters.setKernelImagePath(attributes.getString(CommonConfigurationBean.ATTR_KERNEL_IMAGE_PATH, null));
			parameters.setRootImagePath(attributes.getString(CommonConfigurationBean.ATTR_ROOT_IMAGE_PATH, null));
			String persistence = attributes.verifyString(AttributeNames.EXTRA_IMAGE_PERSISTENCE, CommonConfigurationBean.ATTR_ROOT_IMAGE_PERSISTENCE);
			if (persistence.equals(CommonConfigurationBean.ID_PERSISTENCE_DISCARD)) {
				parameters.setRootImagePersistence(ISimulatorParameters.FS_DISCARD);
			} else if (persistence.equals(CommonConfigurationBean.ID_PERSISTENCE_JOURNAL)) {
				parameters.setRootImagePersistence(ISimulatorParameters.FS_JORNAL);
			} else if (persistence.equals(CommonConfigurationBean.ID_PERSISTENCE_WRITE)) {
				parameters.setRootImagePersistence(ISimulatorParameters.FS_WRITE);
			} else {
				throw new IllegalAttributeException(CommonConfigurationBean.ATTR_ROOT_IMAGE_PERSISTENCE, Messages.CommonConfigFactory_InvalidPersistenceID);
			}
			parameters.setRootImageJournalPath(attributes.getString(CommonConfigurationBean.ATTR_ROOT_IMAGE_JOURNAL_PATH, null));
			
			parameters.setConsoleCommands(attributes.getString(CommonConfigurationBean.ATTR_CUSTOMIZATION_SCRIPT, null));

		} catch (IllegalAttributeException e) {
			throw new CoreException(new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
	}
	
	public TargetConfig createTargetConfig() throws CoreException { 
		ControlAttributes attributes = bean.getAttributes();

		try {
			TargetConfig config = new TargetConfig();
			if (attributes.getBoolean(CommonConfigurationBean.ATTR_AUTOMATIC_AUTHENTICATION, true)) {
				config.setLoginUserName(Parameters.AUTOMATIC_USERNAME);
				config.setLoginPassword(Parameters.AUTOMATIC_PASSWORD);
			} else {
				config.setLoginUserName(attributes.verifyString(AttributeNames.USERNAME, CommonConfigurationBean.ATTR_USERNAME));
				config.setLoginPassword(attributes.getString(CommonConfigurationBean.ATTR_PASSWORD, "")); //$NON-NLS-1$
			}
			config.setLoginTimeout(attributes.verifyInt(AttributeNames.TIMEOUT, CommonConfigurationBean.ATTR_TIMEOUT));
			// Generates an exception if the cipher type attribute is ""
			if(attributes.getString(CommonConfigurationBean.ATTR_SIMULATOR_CIPHER_TYPE).equals("")) //$NON-NLS-1$
				throw new IllegalAttributeException(Messages.CommonConfigFactory_CannotBeEmpty, AttributeNames.CIPHER_TYPE);
			config.setSimulatorCipherType(attributes.getString(CommonConfigurationBean.ATTR_SIMULATOR_CIPHER_TYPE));
			config.setLoginPort(Integer.parseInt(Parameters.LOGIN_PORT));
			
			config.setConsoleShowLinux(attributes.getBoolean(CommonConfigurationBean.ATTR_CONSOLE_SHOW_LINUX, true));
			config.setConsoleShowSimulator(attributes.getBoolean(CommonConfigurationBean.ATTR_CONSOLE_SHOW_SIMULATOR, false));
			
			config.setDoAutomaticNetworkConfiguration(attributes.getBoolean(CommonConfigurationBean.ATTR_AUTOMATIC_NETWORK, false));
			config.setDoAutomaticPortConfiguration(attributes.getBoolean(CommonConfigurationBean.ATTR_AUTOMATIC_PORTCONFIG, false));
			
			config.setSystemWorkspace(attributes.getString(CommonConfigurationBean.ATTR_SYSTEM_WORKSPACE));
			return config;
		} catch (IllegalAttributeException e) {
			throw new CoreException(new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
	}
	
	public abstract ISimulatorParameters createSimulatorParameters() throws CoreException;
}
