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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTabType;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IFireContentsChangedEnabled;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of the parent tab. It displays the children inside the tab
 * folder, and relays updates to them.<br>
 * <br>
 * The JAXB data subtree for building tabs, remote services delegate, Script (if
 * any), update handler and launch configuration variable map (environment built
 * from the resource manager environment) are held by the parent and accessed by
 * the child tabs.
 * 
 * @author arossi
 */
public class JAXBControllerLaunchConfigurationTab extends ExtensibleJAXBControllerTab implements IFireContentsChangedEnabled {

	private final RemoteServicesDelegate delegate;
	private final IJAXBResourceManagerConfiguration rmConfig;
	private final LaunchTabType launchTabData;
	private final ScriptType script;
	private final ValueUpdateHandler updateHandler;

	private ScrolledComposite scrolledParent;
	private final LCVariableMap lcMap;

	/**
	 * @param rm
	 *            the resource manager
	 * @param dialog
	 *            the launch dialog parent
	 * @throws Throwable
	 */
	public JAXBControllerLaunchConfigurationTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog) throws Throwable {
		super(dialog);
		rmConfig = rm.getJAXBConfiguration();
		script = rmConfig.getResourceManagerData().getControlData().getScript();
		launchTabData = rmConfig.getResourceManagerData().getControlData().getLaunchTab();
		delegate = rm.getControl().getRemoteServicesDelegate();
		updateHandler = new ValueUpdateHandler(this);
		if (launchTabData != null) {
			TabControllerType controller = launchTabData.getBasic();
			if (controller != null) {
				addDynamicTab(new JAXBDynamicLaunchConfigurationTab(rm, dialog, controller, this));
			}
			controller = launchTabData.getAdvanced();
			if (controller != null) {
				addDynamicTab(new JAXBDynamicLaunchConfigurationTab(rm, dialog, controller, this));
			}
			String title = launchTabData.getCustomController();
			if (title != null) {
				addDynamicTab(new JAXBImportedScriptLaunchConfigurationTab(rm, dialog, title, this));
			}
		}
		lcMap = new LCVariableMap();
	}

	/*
	 * No composites or controls are specific to the parent. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.launch.ExtensibleJAXBControllerTab#createControl
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	@Override
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		updateHandler.clear();
		if (parent instanceof ScrolledComposite) {
			scrolledParent = (ScrolledComposite) parent;
		}
		super.createControl(parent, rm, queue);
	}

	/*
	 * Delegates to all registered listeners, which then call
	 * handleContentsChanged. The ResourceTab ContentsChangedListener is always
	 * included here, and this triggers its updateButtons and performApply
	 * methods, which then propagates down to the child tabs. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab
	 * #fireContentsChanged()
	 */
	@Override
	public void fireContentsChanged() {
		super.fireContentsChanged();
	}

	/**
	 * @return remote services info
	 */
	public RemoteServicesDelegate getDelegate() {
		return delegate;
	}

	/**
	 * @return JAXB elements for building the launch tab controls
	 */
	public LaunchTabType getLaunchTabData() {
		return launchTabData;
	}

	/**
	 * @return launch tab environment map (built from the resource manager
	 *         environment)
	 */
	public LCVariableMap getLCMap() {
		return lcMap;
	}

	/**
	 * Needed by the LaunchTabBuilder
	 * 
	 * @return the ResourceManager (base) configuration
	 */
	public IJAXBResourceManagerConfiguration getRmConfig() {
		return rmConfig;
	}

	/**
	 * @return JAXB data element for resource manager script
	 */
	public ScriptType getScript() {
		return script;
	}

	/**
	 * @return handler responsible for notifying all widgets to refresh their
	 *         values from the launch tab environment map
	 */
	public ValueUpdateHandler getUpdateHandler() {
		return updateHandler;
	}

	/**
	 * @return whether this resource manager uses a script
	 */
	public boolean hasScript() {
		return script != null;
	}

	/*
	 * Rebuilds the launch tab environment map, and clears the controls
	 * registered with the update handler. This is necessary because on calls to
	 * this method subsequent to the first, the widgets it contained will have
	 * been disposed. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.launch.ExtensibleJAXBControllerTab#initializeFrom
	 * (org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		try {
			lcMap.initialize(rmConfig.getRMVariableMap());
			updateHandler.clear();
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
			return new RMLaunchValidation(false, t.getMessage());
		}
		return super.initializeFrom(control, rm, queue, configuration);
	}

	/*
	 * Attempt to suggest resizing to scrolled ResourcesTab composite.
	 */
	public void resize(Control control) {
		if (scrolledParent != null) {
			scrolledParent.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}
}
