/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.launch.ui.extensions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.ui.LazyEnvManagerDetector;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBParentLaunchConfigurationTab;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModelEnabled;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTabType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of the parent tab. It displays the children inside the tab folder, and relays updates to them.<br>
 * <br>
 * The JAXB data subtree for building tabs, remote services delegate, Script (if any), update handler and launch configuration
 * variable map (environment built from the resource manager environment) are held by the parent and accessed by the child tabs.
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 * @since 7.0
 */
public class JAXBControllerLaunchConfigurationTab extends ExtensibleJAXBControllerTab implements IJAXBParentLaunchConfigurationTab,
		IUpdateModelEnabled, SelectionListener {

	protected final ILaunchController fControl;
	protected final LaunchTabType launchTabData;
	protected final IUpdateHandler updateHandler;
	protected final LCVariableMap lcMap;
	protected IRemoteConnection fRemoteConnection;
	protected ScriptType script;

	/**
	 * @param control
	 *            the launch controller that will launch jobs
	 * @param dialog
	 *            the parent launch configuration dialog
	 * @param monitor
	 *            progress monitor
	 * @throws Throwable
	 */
	public JAXBControllerLaunchConfigurationTab(ILaunchController control, ILaunchConfigurationDialog dialog,
			IProgressMonitor monitor) throws Throwable {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		fControl = control;
		LCVariableMap varMap = null;
		setLaunchConfigurationDialog(dialog);

		try {
			ResourceManagerData data = control.getConfiguration();
			if (data == null) {
				throw new Throwable(Messages.JAXBControllerLaunchConfigurationTab_unableToObtainConfigurationInfo);
			}
			script = data.getControlData().getScript();
			fRemoteConnection = RemoteServicesUtils.getConnectionWithProgress(fControl.getRemoteServicesId(),
					fControl.getConnectionName(), progress.newChild(30));
			if (fRemoteConnection == null) {
				throw new Throwable(Messages.JAXBControllerLaunchConfigurationTab_unableToObtainConnectionInfo);
			}
			/*
			 * Connection should be open by now, but just in case...
			 */
			if (!fRemoteConnection.isOpen()) {
				fRemoteConnection.open(progress.newChild(50));
			}
			varMap = new LCVariableMap();
			varMap.setEnvManager(getEnvManager(progress.newChild(10)));
			voidRMConfig = false;
		} catch (Throwable t) {
			script = null;
			voidRMConfig = true;
			WidgetActionUtils.errorMessage(Display.getDefault().getActiveShell(), t, Messages.VoidLaunchTabMessage,
					Messages.VoidLaunchTabTitle, false);
		}

		if (!voidRMConfig) {
			launchTabData = control.getConfiguration().getControlData().getLaunchTab();
			updateHandler = new ValueUpdateHandler(this);
			if (launchTabData != null) {
				List<TabControllerType> dynamic = launchTabData.getDynamic();
				for (TabControllerType controller : dynamic) {
					addDynamicTab(new JAXBDynamicLaunchConfigurationTab(control, controller, this));
				}
				LaunchTabType.Import importTab = launchTabData.getImport();
				if (importTab != null) {
					addDynamicTab(new JAXBImportedScriptLaunchConfigurationTab(control, importTab, this));
				}
			}
			varMap = new LCVariableMap();
			varMap.setEnvManager(getEnvManager(progress.newChild(10)));
		} else {
			getControllers().clear();
			launchTabData = null;
			updateHandler = null;
		}
		lcMap = varMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.ExtensibleJAXBControllerTab#createControl(org.eclipse.swt.widgets.Composite,
	 * java.lang.String)
	 */
	@Override
	public void createControl(Composite parent, String id) throws CoreException {
		if (!voidRMConfig) {
			updateHandler.clear();
		}
		super.createControl(parent, id);
		if (tabFolder != null) {
			tabFolder.addSelectionListener(this);
		}
	}

	/*
	 * Delegates to all registered listeners, which then call handleContentsChanged. The ResourceTab ContentsChangedListener is
	 * always included here, and this triggers its updateButtons and performApply methods, which then propagates down to the child
	 * tabs. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab#fireContentsChanged()
	 */
	@Override
	public void fireContentsChanged() {
		super.fireContentsChanged();
	}

	public IRemoteConnection getConnection() {
		return fRemoteConnection;
	}

	private IEnvManager getEnvManager(IProgressMonitor monitor) {
		LazyEnvManagerDetector envMgr = new LazyEnvManagerDetector(fRemoteConnection);
		envMgr.setProgressMonitor(monitor);
		return envMgr;
	}

	/**
	 * Needed by the LaunchTabBuilder
	 * 
	 * @return the ResourceManager (base) configuration
	 */
	public ILaunchController getJobControl() {
		return fControl;
	}

	/**
	 * @return JAXB elements for building the launch tab controls
	 */
	public LaunchTabType getLaunchTabData() {
		return launchTabData;
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
	public IUpdateHandler getUpdateHandler() {
		return updateHandler;
	}

	/**
	 * @return launch tab environment map (built from the resource manager environment)
	 */
	public LCVariableMap getVariableMap() {
		return lcMap;
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
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.ExtensibleJAXBControllerTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public RMLaunchValidation initializeFrom(final ILaunchConfiguration configuration) {
		if (!voidRMConfig) {
			try {
				lcMap.initialize(fControl.getEnvironment(), fControl.getControlId());
				updateHandler.clear();
				lcMap.updateFromConfiguration(configuration);
			} catch (CoreException e) {
				return new RMLaunchValidation(false, e.getLocalizedMessage());
			} catch (Throwable e) {
				return new RMLaunchValidation(false, e.getLocalizedMessage());
			}
		}
		RMLaunchValidation validation = super.initializeFrom(configuration);
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
	 * resizes the tab and calls setVisible
	 */
	private void setVisibleOnSelected() {
		lastIndex = tabFolder.getSelectionIndex();
		AbstractJAXBLaunchConfigurationTab t = getControllers().get(lastIndex);
		t.getControl().setVisible(true);
		t.setVisible();
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
	
	public IVariableMap getRMVariableMap() {
		return null;
	}
	
}
