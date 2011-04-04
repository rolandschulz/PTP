/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTab;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.variables.LTVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Top level constructs the main tab controllers.
 * 
 * @author arossi
 * 
 */
public class JAXBRMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab {

	private LTVariableMap ltMap;

	private final IJAXBResourceManagerConfiguration rmConfig;
	private final LaunchTab launchTabData;
	private final boolean hasScript;

	private ScrolledComposite scrolledParent;

	public JAXBRMLaunchConfigurationDynamicTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog) throws Throwable {
		super(dialog);
		rmConfig = rm.getJAXBConfiguration();
		rmConfig.setActive();
		launchTabData = JAXBRMLaunchConfigurationFactory.getLaunchTab(rmConfig);

		hasScript = JAXBRMLaunchConfigurationFactory.hasScript(rmConfig);
		if (launchTabData != null) {
			TabController controller = launchTabData.getBasic();
			if (controller != null) {
				addDynamicTab(new JAXBRMConfigurableAttributesTab(rm, dialog, controller, this));
			}
			controller = launchTabData.getAdvanced();
			if (controller != null) {
				addDynamicTab(new JAXBRMConfigurableAttributesTab(rm, dialog, controller, this));
			}
			String title = launchTabData.getCustomController();
			if (title != null) {
				addDynamicTab(new JAXBRMCustomBatchScriptTab(rm, dialog, title, this));
			}
		}
	}

	@Override
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		if (parent instanceof ScrolledComposite) {
			scrolledParent = (ScrolledComposite) parent;
		}
		super.createControl(parent, rm, queue);
	}

	public LaunchTab getLaunchTabData() {
		return launchTabData;
	}

	public LTVariableMap getLocalMap() {
		if (ltMap == null) {
			try {
				getRmConfig().setActive();
			} catch (Throwable t) {
				JAXBUIPlugin.log(t);
			}
			ltMap = LTVariableMap.createInstance(RMVariableMap.getActiveInstance());
			LTVariableMap.setActiveInstance(ltMap);
		}
		return ltMap;
	}

	public IJAXBResourceManagerConfiguration getRmConfig() {
		return rmConfig;
	}

	public boolean hasScript() {
		return hasScript;
	}

	public void resize(Control control) {
		if (scrolledParent != null) {
			scrolledParent.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}
}
