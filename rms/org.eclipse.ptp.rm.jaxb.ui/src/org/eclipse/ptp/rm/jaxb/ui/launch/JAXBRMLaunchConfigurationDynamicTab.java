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
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTab;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.widgets.Composite;

/**
 * Top level constructs the main tab controllers.
 * 
 * @author arossi
 * 
 */
public class JAXBRMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab {

	private final IJAXBResourceManagerConfiguration rmConfig;
	private final LaunchTab launchTabData;

	public JAXBRMLaunchConfigurationDynamicTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
		rmConfig = rm.getJAXBRMConfiguration();
		launchTabData = JAXBRMLaunchConfigurationFactory.getLaunchTab(rmConfig);
	}

	@Override
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		if (!(rm instanceof IJAXBResourceManager)) {
			throw CoreExceptionUtils.newException(Messages.JAXBRMLaunchConfigurationFactory_doCreateError + rm, null);
		}
		IJAXBResourceManager jaxb = (IJAXBResourceManager) rm;
		IJAXBResourceManagerControl rmControl = jaxb.getControl();
		if (launchTabData != null) {
			ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
			String title = null;
			TabController controller = launchTabData.getBasic();
			if (controller != null) {
				addDynamicTab(new JAXBRMConfigurableAttributesTab(rmControl, dialog, controller));
			}
			controller = launchTabData.getAdvanced();
			if (controller != null) {
				addDynamicTab(new JAXBRMConfigurableAttributesTab(rmControl, dialog, controller));
			}
			title = launchTabData.getCustomController();
			if (title != null) {
				addDynamicTab(new JAXBRMCustomBatchScriptTab(rmControl, dialog, title));
			}
		}
		super.createControl(parent, rm, queue);
	}
}
