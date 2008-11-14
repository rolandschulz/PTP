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
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.cell.environment.cellsimulator.SimulatorProperties;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.LocalDefaultValues;
import org.eclipse.ptp.cell.environment.cellsimulator.ui.ConfigurationPage;
import org.eclipse.ptp.cell.environment.cellsimulator.ui.Messages;
import org.eclipse.ptp.cell.utils.packagemanager.PackageManagementSystemManager;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * Factory for the environment.
 * 
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public class LocalLaunchEnvironment implements ITargetTypeExtension {
	
	public class DummyEnvironmentDialogPage extends AbstractEnvironmentDialogPage {
		
		private String targetName;
		
		public DummyEnvironmentDialogPage(String targetName) {
			super(targetName);
			this.targetName = targetName;
		}

		public boolean isValid() {
			return false;
		}
	
		public String getName() {
			return this.targetName;
		}
	
		public Map getAttributes() {
			return null;
		}
	
		public void createControl(Composite parent) {
			this.setTitle(Messages.ConfigurationPage_DialogTitle_LocalSimulator);
			this.setDescription(Messages.ConfigurationPage_DialogDescription_LocalSimulator);
			this.setErrorMessage(null);

			// Generate the top control, parent of all the following controls.
			GridLayout topLayout = new GridLayout();
			final Composite topControl = new Composite(parent, SWT.NONE);
			setControl(topControl);
			topControl.setLayout(topLayout);
			
			Label label = new Label(topControl, SWT.NONE);
			label.setText(Messages.ConfigurationPage_NoLocalSimulator);
		}
	
	};
	
	
	public LocalLaunchEnvironment() {
		super();
	}

	public ITargetControl controlFactory(ITargetElement element) throws CoreException {
		return new LocalTargetControl(element);
	}

	public String[] getControlAttributeNames() {
		return LocalConfigurationBean.KEY_ARRAY;
	}

	public AbstractEnvironmentDialogPage dialogPageFactory(ITargetElement element) {
		// Check if simulator is installed
		/*if (!PackageManagementSystemManager.getPackageManager().query(SimulatorProperties.simulatorPackage)) {
			return new DummyEnvironmentDialogPage(LocalDefaultValues.DefaultTargetName);
		}*/
		ConfigurationPage page = new ConfigurationPage(element.getName(), 
				new LocalConfigurationBean(element.getAttributes(), element.getId()));
		page.setAvailableAutomaticNetwork(true);
		page.setAvailableAutomaticPort(true);
		page.setAvailableRemoteConnection(false);
		page.setAvailableAutomaticWorkDirectory(true);
		return page;
	}
	
	public AbstractEnvironmentDialogPage dialogPageFactory() {
		// Check if simulator is installed.
		/*if (!PackageManagementSystemManager.getPackageManager().query(SimulatorProperties.simulatorPackage)) {
			return new DummyEnvironmentDialogPage(LocalDefaultValues.DefaultTargetName);
		}*/
		ConfigurationPage page = new ConfigurationPage(LocalDefaultValues.DefaultTargetName,
				new LocalConfigurationBean());
		page.setAvailableAutomaticNetwork(true);
		page.setAvailableAutomaticPort(true);
		page.setAvailableRemoteConnection(false);
		page.setAvailableAutomaticWorkDirectory(true);
		return page;
	}

	public String[] getControlAttributeNamesForCipheredKeys() {
		return LocalConfigurationBean.KEY_CIPHERED_ARRAY;
	}

}
