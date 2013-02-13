/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.launch.ui.extensions;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBParentLaunchConfigurationTab;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Base class for the JAXB LaunchConfiguration tabs which provide views of editable widgets. Up to three such tabs can be configured
 * as children of the controller tab.<br>
 * <br>
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.launch.JAXBDynamicLaunchConfigurationTab
 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.launch.JAXBImportedScriptLaunchConfigurationTab
 * 
 * @author arossi
 * @since 7.0
 * 
 */
public abstract class AbstractJAXBLaunchConfigurationTab extends AbstractRMLaunchConfigurationDynamicTab {

	protected final IJAXBParentLaunchConfigurationTab parentTab;
	protected final Set<String> visibleList;
	protected final Set<String> enabledList;
	protected final Set<String> validSet;
	protected String title;
	protected Composite control;

	/**
	 * @param parentTab
	 *            the controller
	 * @param dialog
	 *            the dialog to which this tab belongs
	 * @param tabIndex
	 *            child index for the parent
	 */
	protected AbstractJAXBLaunchConfigurationTab(IJAXBParentLaunchConfigurationTab parentTab) {
		this.parentTab = parentTab;
		this.title = Messages.DefaultDynamicTab_title;
		visibleList = new TreeSet<String>();
		enabledList = new TreeSet<String>();
		validSet = new TreeSet<String>();
	}

	/**
	 * @return image to display in the folder tab for this LaunchTab
	 */
	public abstract Image getImage();

	/**
	 * @return text to display in the folder tab for this LaunchTab
	 */
	public abstract String getText();

	/**
	 * This performApply is triggered whenever there is an update on the controller. We do not want the values of the tab to be
	 * flushed to the configuration unless this tab is the origin of the change; hence we check to see if the tab is visible.<br>
	 * <br>
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#performApply(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (control.isVisible()) {
			try {
				refreshLocal(configuration);
			} catch (CoreException t) {
				PTPLaunchPlugin.log(t);
				return new RMLaunchValidation(false, t.getMessage());
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/**
	 * Set up shared environment, if any; adapter method which does nothing
	 * 
	 * @param controllers
	 *            from the parent tab, mapped to their titles
	 * @throws CoreException
	 */
	public void setUpSharedEnvironment(Map<String, AbstractJAXBLaunchConfigurationTab> controllers) throws CoreException {
		// does nothing
	}

	/**
	 * In case button activations need to take place when the tab changes
	 */
	public abstract void setVisible();

	/**
	 * Tab-specific handling of local variable map.
	 */
	protected abstract void doRefreshLocal();

	/**
	 * Eliminate whitespace.
	 * 
	 * @return title
	 */
	protected String getControllerTag() {
		return title.replaceAll(JAXBUIConstants.SP, JAXBUIConstants.DOT);
	}

	/**
	 * Subclasses should call this method, but implement doRefreshLocal().
	 * 
	 * @param current
	 *            configuration
	 */
	protected void refreshLocal(ILaunchConfigurationWorkingCopy config) throws CoreException {
		visibleList.clear();
		enabledList.clear();
		validSet.clear();
		doRefreshLocal();
		LCVariableMap lcMap = parentTab.getVariableMap();
		lcMap.relinkConfigurationProperties(config);
		parentTab.getVariableMap().relinkHidden(getControllerTag());
		writeLocalProperties();
		parentTab.getVariableMap().flush(config);
	}

	/**
	 * Sets the current environment of the configuration implicitly by defining which variables are valid.
	 */
	protected abstract void writeLocalProperties();
}
