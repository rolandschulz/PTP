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

import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTab;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;

/**
 * Top level constructs the main tab controllers.
 * 
 * @author arossi
 * 
 */
public class JAXBRMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab {

	public JAXBRMLaunchConfigurationDynamicTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
		IJAXBResourceManagerConfiguration rmConfig = rm.getJAXBRMConfiguration();
		LaunchTab launchTabData = JAXBRMLaunchConfigurationFactory.getLaunchTab(rmConfig);
		boolean hasScript = JAXBRMLaunchConfigurationFactory.hasScript(rmConfig);
		if (launchTabData != null) {
			String title = null;
			TabController controller = launchTabData.getBasic();
			if (controller != null) {
				addDynamicTab(new JAXBRMConfigurableAttributesTab(rm, dialog, controller, hasScript));
			}
			controller = launchTabData.getAdvanced();
			if (controller != null) {
				addDynamicTab(new JAXBRMConfigurableAttributesTab(rm, dialog, controller, hasScript));
			}
			title = launchTabData.getCustomController();
			if (title != null) {
				addDynamicTab(new JAXBRMCustomBatchScriptTab(rm, dialog, title));
			}
		}
	}
}
