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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModelEnabled;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTabType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of the parent tab. It displays the children inside the tab folder, and relays updates to them.<br>
 * <br>
 * The JAXB data subtree for building tabs, remote services delegate, Script (if any), update handler and launch configuration
 * variable map (environment built from the resource manager environment) are held by the parent and accessed by the child tabs.
 * 
 * @author arossi
 */
public class JAXBControllerLaunchConfigurationTab extends ExtensibleJAXBControllerTab implements IUpdateModelEnabled,
		SelectionListener {

	private RemoteServicesDelegate delegate;
	private final IJAXBResourceManagerConfiguration rmConfig;
	private final LaunchTabType launchTabData;
	private final ValueUpdateHandler updateHandler;
	private final LCVariableMap lcMap;

	private ScriptType script;

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

		try {
			ResourceManagerData data = rmConfig.getResourceManagerData();
			if (data != null) {
				script = data.getControlData().getScript();
			}
			voidRMConfig = false;
		} catch (Throwable t) {
			script = null;
			voidRMConfig = true;
			WidgetActionUtils.errorMessage(dialog.getActiveTab().getControl().getShell(), t, Messages.VoidLaunchTabMessage,
					Messages.VoidLaunchTabTitle, false);
		}

		if (!voidRMConfig) {
			launchTabData = rmConfig.getResourceManagerData().getControlData().getLaunchTab();
			updateHandler = new ValueUpdateHandler(this);
			if (launchTabData != null) {
				List<TabControllerType> dynamic = launchTabData.getDynamic();
				for (TabControllerType controller : dynamic) {
					addDynamicTab(new JAXBDynamicLaunchConfigurationTab(rm, dialog, controller, this));
				}
				LaunchTabType.Import importTab = launchTabData.getImport();
				if (importTab != null) {
					addDynamicTab(new JAXBImportedScriptLaunchConfigurationTab(rm, dialog, importTab, this));
				}
			}
			lcMap = new LCVariableMap();
		} else {
			getControllers().clear();
			launchTabData = null;
			updateHandler = null;
			lcMap = null;
		}
	}

	/*
	 * No composites or controls are specific to the parent. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.launch.ExtensibleJAXBControllerTab#createControl (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.elements.IPQueue)
	 */
	@Override
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		if (!voidRMConfig) {
			updateHandler.clear();
		}
		super.createControl(parent, rm, queue);
		if (tabFolder != null) {
			tabFolder.addSelectionListener(this);
		}
	}

	/*
	 * Delegates to all registered listeners, which then call handleContentsChanged. The ResourceTab ContentsChangedListener is
	 * always included here, and this triggers its updateButtons and performApply methods, which then propagates down to the child
	 * tabs. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab #fireContentsChanged()
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
	 * @return launch tab environment map (built from the resource manager environment)
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
	 * @return handler responsible for notifying all widgets to refresh their values from the launch tab environment map
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
	 * Rebuilds the launch tab environment map, and clears the controls registered with the update handler. This is necessary
	 * because on calls to this method subsequent to the first, the widgets it contained will have been disposed. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.launch.ExtensibleJAXBControllerTab#initializeFrom (org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public RMLaunchValidation initializeFrom(Control control, final IResourceManager rm, IPQueue queue,
			final ILaunchConfiguration configuration) {
		if (!voidRMConfig) {
			try {
				getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							delegate = ((IJAXBResourceManager) rm).getControl().getRemoteServicesDelegate(monitor);
							if (delegate.getRemoteConnection() == null) {
								throw new InvocationTargetException(null, Messages.UninitializedRemoteServices);
							}
							String rmId = rm.getConfiguration().getUniqueName();
							lcMap.initialize(rmConfig.getRMVariableMap(), rmId);
							updateHandler.clear();
							lcMap.updateFromConfiguration(configuration);
						} catch (Throwable t) {
							JAXBControlUIPlugin.log(t);
							throw new InvocationTargetException(t, t.getLocalizedMessage());
						}
					}

				});
			} catch (InvocationTargetException e) {
				return new RMLaunchValidation(false, e.getLocalizedMessage());
			} catch (InterruptedException e) {
				return new RMLaunchValidation(false, e.getLocalizedMessage());
			}
		}
		RMLaunchValidation validation = super.initializeFrom(control, rm, queue, configuration);
		if (!getControllers().isEmpty()) {
			tabFolder.setSelection(lastIndex);
			setVisibleOnSelected();
		}
		return validation;
	}

	/**
	 * Calls relink on the LCVariableMap for current controller.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModelEnabled#relink()
	 */
	public void relink() {
		AbstractJAXBLaunchConfigurationTab t = getControllers().get(tabFolder.getSelectionIndex());
		lcMap.relinkHidden(t.getControllerTag());
	}

	/**
	 * see {@link #widgetSelected(SelectionEvent)}
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		setVisibleOnSelected();
	}

	/**
	 * calls {@link #setVisibleOnSelected()}
	 */
	public void widgetSelected(SelectionEvent e) {
		setVisibleOnSelected();
	}

	/**
	 * resizes the tab and calls setVisible
	 */
	private void setVisibleOnSelected() {
		lastIndex = tabFolder.getSelectionIndex();
		AbstractJAXBLaunchConfigurationTab t = getControllers().get(lastIndex);
		t.getControl().setVisible(true);
		t.setVisible();
	}
}
