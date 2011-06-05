/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.launch;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Base class for the JAXB LaunchConfiguration tabs which provide views of
 * editable widgets. Up to three such tabs can be configured as children of the
 * controller tab.<br>
 * <br>
 * 
 * Each tab maintains its own local map of values which are set from its
 * widgets, and this swapped into the environment active map when configuration
 * changes are needed.
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBDynamicLaunchConfigurationTab
 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBImportedScriptLaunchConfigurationTab
 * 
 * @author arossi
 * 
 */
public abstract class AbstractJAXBLaunchConfigurationTab extends AbstractRMLaunchConfigurationDynamicTab {

	protected final JAXBControllerLaunchConfigurationTab parentTab;
	protected final Map<String, Object> localMap;
	protected String title;
	protected Composite control;
	protected Point size;

	/**
	 * @param parentTab
	 *            the controller
	 * @param dialog
	 *            the dialog to which this tab belongs
	 * @param tabIndex
	 *            child index for the parent
	 */
	protected AbstractJAXBLaunchConfigurationTab(JAXBControllerLaunchConfigurationTab parentTab, ILaunchConfigurationDialog dialog) {
		super(dialog);
		this.parentTab = parentTab;
		this.title = Messages.DefaultDynamicTab_title;
		localMap = new TreeMap<String, Object>();
	}

	/**
	 * Tab-specific handling of local variable map.
	 */
	protected abstract void doRefreshLocal();

	/**
	 * @return image to display in the folder tab for this LaunchTab
	 */
	public abstract Image getImage();

	/**
	 * @return size of control as originally computed.
	 * 
	 */
	public Point getSize() {
		return size;
	}

	/**
	 * @return text to display in the folder tab for this LaunchTab
	 */
	public abstract String getText();

	/**
	 * This performApply is triggered whenever there is an update on the
	 * controller. We do not want the values of the tab to be flushed to the
	 * configuration unless this tab is the origin of the change; hence we check
	 * to see if the tab is visible.<br>
	 * <br>
	 * If write to configuration is indicated, then the local map is refreshed,
	 * swapped in to the active map, and then flushed to the configuration.
	 * 
	 * @param configuration
	 *            working copy of current launch configuration
	 * @param current
	 *            resource manager
	 * @param queue
	 *            (unused)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		if (control.isVisible()) {
			Map<String, Object> current = null;
			LCVariableMap lcMap = parentTab.getLCMap();
			try {
				refreshLocal(configuration);
				current = lcMap.swapVariables(localMap);
				lcMap.writeToConfiguration(configuration);
			} catch (CoreException t) {
				JAXBControlUIPlugin.log(t);
				return new RMLaunchValidation(false, t.getMessage());
			} finally {
				try {
					lcMap.swapVariables(current);
				} catch (CoreException t) {
					JAXBControlUIPlugin.log(t);
				}
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/**
	 * Subclasses should call this method, but implement doRefreshLocal().
	 * 
	 * @param current
	 *            configuration
	 */
	protected void refreshLocal(ILaunchConfiguration config) throws CoreException {
		Map<String, Object> saved = parentTab.getLCMap().getStandardConfigurationProperties(config, localMap);
		localMap.clear();
		localMap.putAll(saved);
		doRefreshLocal();
	}

	/**
	 * Set up shared environment, if any; adapter method which does nothing
	 * 
	 * @param controllers
	 *            from the parent tab, mapped to their titles
	 */
	public void setUpSharedEnvironment(Map<String, AbstractJAXBLaunchConfigurationTab> controllers) {
		// does nothing
	}

	/**
	 * In case button activations need to take place when the tab changes
	 */
	public void setVisible() {
		// does nothing
	}
}
