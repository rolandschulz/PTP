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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.IJobController;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.control.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ControlStateListener;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.IJAXBLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.IJAXBParentLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.LaunchTabBuilder;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ButtonActionType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * Launch Tab which builds its control from the JAXB data elements. The number, type and disposition of widgets connected to
 * Properties or Attributes is almost entirely configurable via the XML.<br>
 * <br>
 * Aside from being registered with the update handler, the widgets specific to this tab are also maintained in a local map so that
 * their values (and only theirs) will appear in the environment when performApply() is called. <br>
 * <br>
 * If different widgets on different tabs reference the same Property or Attribute, its value will change everywhere. However, if
 * the <code>shared</code> property is given a list of other controllers, their values are included in the local environment.
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 * @since 7.0
 */
public class JAXBDynamicLaunchConfigurationTab extends AbstractJAXBLaunchConfigurationTab implements IJAXBLaunchConfigurationTab,
		SelectionListener {

	protected final IJobController fControl;
	protected final ValueUpdateHandler updateHandler;
	protected final List<Viewer> viewers;
	protected final Map<Object, IUpdateModel> localWidgets;
	protected final IRMLaunchConfigurationDynamicTab fDynamicTab;
	protected Collection<ControlStateListener> listeners;
	protected ILaunchConfiguration listenerConfiguration;

	protected TabControllerType controller;
	protected String[] shared;
	protected final Collection<IUpdateModel> sharedModels;

	/**
	 * @param rm
	 *            the resource manager
	 * @param dialog
	 *            the ancestor main launch dialog
	 * @param controller
	 *            the JAXB data element from which this tab's control will be built
	 * @param parentTab
	 *            the parent controller tab
	 */
	public JAXBDynamicLaunchConfigurationTab(IJobController control, TabControllerType controller,
			JAXBControllerLaunchConfigurationTab parentTab, IProgressMonitor monitor) {
		this(control, parentTab);
		setProgressMonitor(monitor);
		String title = controller.getTitle();
		if (title != null) {
			this.title = title;
		}
		this.controller = controller;
		String s = controller.getIncludeWidgetValuesFrom();
		if (s == null) {
			shared = new String[0];
		} else {
			shared = s.split(JAXBUIConstants.CM);
		}
	}

	/**
	 * 
	 * @param rm
	 *            the resource manager
	 * @param dialog
	 *            the ancestor main launch dialog
	 * @param parentTab
	 *            the parent controller tab
	 */
	protected JAXBDynamicLaunchConfigurationTab(IJobController control, JAXBControllerLaunchConfigurationTab parentTab) {
		super(parentTab);
		fControl = control;
		fDynamicTab = parentTab;
		sharedModels = new ArrayList<IUpdateModel>();
		updateHandler = parentTab.getUpdateHandler();
		localWidgets = new HashMap<Object, IUpdateModel>();
		viewers = new ArrayList<Viewer>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #canSave(org.eclipse.swt.widgets.Control)
	 */
	public RMLaunchValidation canSave(Control control) {
		return checkForValidationError();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite,
	 * java.lang.String)
	 */
	public void createControl(Composite parent, String id) throws CoreException {
		try {
			LaunchTabBuilder builder = new LaunchTabBuilder(this);
			if (listeners != null) {
				listeners.clear();
			}
			control = builder.build(parent);
		} catch (Throwable t) {
			t.printStackTrace();
			throw CoreExceptionUtils.newException(Messages.CreateControlConfigurableError + JAXBUIConstants.SP + title, t);
		}
		createViewScriptGroup(control);
	}

	/**
	 * The top-level control.
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Used by the LaunchTabBuilder.
	 * 
	 * @return the JAXB data element used to build the control
	 */
	public TabControllerType getController() {
		return controller;
	}

	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * @return the remote connection
	 */
	public IJobController getJobControl() {
		return fControl;
	}

	/**
	 * Used by the LaunchTabBuilder.
	 * 
	 * @return the map of widgets specific to this tab.
	 */
	public Map<Object, IUpdateModel> getLocalWidgets() {
		return localWidgets;
	}

	/**
	 * Used by the LaunchTabBuilder
	 * 
	 * @return the main controller tab
	 */
	public IJAXBParentLaunchConfigurationTab getParent() {
		return parentTab;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.IJAXBLaunchConfigurationTab#getRemoteConnection()
	 */
	// Based on org.eclipse.ptp.rm.launch.RMLaunchUtils#getRemoteConnection(ILaunchConfiguration, IProgressMonitor)
	public IRemoteConnection getRemoteConnection() {
		final IJobController jobControl = getParent().getJobControl();
		final String remoteServicesID = jobControl.getRemoteServicesId();
		final String connectionName = jobControl.getConnectionName();

		if (remoteServicesID != null && connectionName != null) {
			final PTPRemoteCorePlugin corePlugin = PTPRemoteCorePlugin.getDefault();
			final IRemoteServices services = corePlugin.getRemoteServices(remoteServicesID, getProgressMonitor());
			if (services != null) {
				return services.getConnectionManager().getConnection(connectionName);
			}
		}
		return null;
	}

	/**
	 * @return title of tab.
	 */
	@Override
	public String getText() {
		return title;
	}

	/*-
	 * Resets the configuation then re-initializes all widgets: 
	 * 1. clears viewers and repopulate that list. 
	 * 2. repopulates the handler with local widgets. 
	 * 3. initializes the (new) widgets from the map. 
	 * 4. initializes the checked state on any checkbox viewers and then refreshes them; 
	 *    sets enabled and visible on non-viewer widgets, and then sets state only the 
	 *    control state listeners
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 *      #initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {
		listenerConfiguration = configuration;
		try {
			ValueUpdateHandler handler = getParent().getUpdateHandler();

			viewers.clear();
			for (Map.Entry<Object, IUpdateModel> e : localWidgets.entrySet()) {
				Object key = e.getKey();
				if (key instanceof Viewer) {
					Viewer viewer = (Viewer) key;
					viewers.add(viewer);
				}
				handler.addUpdateModelEntry(key, e.getValue());
			}

			LCVariableMap lcMap = parentTab.getVariableMap();
			IVariableMap rmMap = fControl.getEnvironment();

			for (IUpdateModel m : localWidgets.values()) {
				m.initialize(rmMap, lcMap);
			}

			for (IUpdateModel m : localWidgets.values()) {
				Control mControl = null;
				if (m instanceof ViewerUpdateModel) {
					ViewerUpdateModel vmodel = (ViewerUpdateModel) m;
					vmodel.initializeChecked();
					mControl = vmodel.getSWTControl();
				} else if (!(m instanceof ICellEditorUpdateModel)) {
					mControl = (Control) m.getControl();
				}
				if (mControl != null) {
					mControl.setVisible(true);
				}
			}

			if (listeners != null) {
				for (ControlStateListener l : listeners) {
					l.setState();
				}
			}

			for (Viewer v : viewers) {
				v.refresh();
			}
		} catch (Throwable t) {
			JAXBControlUIPlugin.log(t);
			return new RMLaunchValidation(false, t.getMessage());
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig) {
		return checkForValidationError();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.AbstractJAXBLaunchConfigurationTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (control == null) {
			return new RMLaunchValidation(false, null);
		}
		if (control.isVisible()) {
			RMLaunchValidation v = checkForValidationError();
			if (!v.isSuccess()) {
				return v;
			}
		}
		return super.performApply(configuration);
	}

	/**
	 * Invokes a command on the resource manager. This may be called by action button listeners.
	 * 
	 * @param action
	 *            a command from the control data type
	 * 
	 */
	public void run(ButtonActionType action) throws CoreException {
		getJobControl().runActionCommand(action.getAction(), action.getClearValue(), listenerConfiguration);
		if (action.isRefresh()) {
			try {
				fDynamicTab.initializeFrom(null);
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(t.getLocalizedMessage(), t);
			}
		}
	}

	/*
	 * Defaults are taken care of by the load method on the LCVariableMap (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		return new RMLaunchValidation(true, null);
	}

	/**
	 * @param listeners
	 *            for wiring widgets together based on state events
	 */
	public void setListeners(Collection<ControlStateListener> listeners) {
		this.listeners = listeners;
	}

	/*
	 * Pull out the local maps from each and set them into the shared array. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.AbstractJAXBLaunchConfigurationTab #setUpSharedEnvironment(java.util.List)
	 */
	@Override
	public void setUpSharedEnvironment(Map<String, AbstractJAXBLaunchConfigurationTab> controllers) throws CoreException {
		sharedModels.clear();
		for (String title : shared) {
			AbstractJAXBLaunchConfigurationTab tab = controllers.get(title);
			if (tab instanceof JAXBDynamicLaunchConfigurationTab) {
				sharedModels.addAll(((JAXBDynamicLaunchConfigurationTab) tab).localWidgets.values());
			}
		}
	}

	/*
	 * Reactivates the tab; if this was not the last tab, the Apply button should become active.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.AbstractJAXBLaunchConfigurationTab #setVisible()
	 */
	@Override
	public void setVisible() {
		try {
			refreshLocal(listenerConfiguration.getWorkingCopy());
			fireContentsChanged();
		} catch (CoreException t) {
			JAXBUIPlugin.log(t);
		}
	}

	/*
	 * Tab acts a listener for viewScript button. (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Tab acts a listener for viewScript button. If the resource manager does not use a script, this call returns immediately; else
	 * it calls #realizeLocalScript(ILaunchConfiguration), then displays the result in a read-only dialog. (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		Button b = (Button) e.getSource();
		Shell shell = Display.getDefault().getActiveShell();
		String title = JAXBUIConstants.ZEROSTR;
		try {
			String text = JAXBUIConstants.ZEROSTR;
			if (Messages.ViewConfig.equals(b.getText())) {
				text = displayConfigurationContents(listenerConfiguration);
				title = Messages.DisplayConfig;
			} else if (Messages.ViewExcluded.equals(b.getText())) {
				text = displayExcluded();
				title = Messages.ViewExcluded;
			} else if (!parentTab.hasScript()) {
				MessageDialog.openWarning(shell, Messages.ScriptNotSupportedWarning_title, Messages.ScriptNotSupportedWarning
						+ JAXBControlUIConstants.LINE_SEP);
				return;
			} else {
				text = realizeLocalScript(listenerConfiguration);
				title = Messages.DisplayScript;
			}
			new ScrollingEditableMessageDialog(shell, title, text, true).open();
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(shell, t, Messages.DisplayError, Messages.DisplayErrorTitle, false);
		}
	}

	/**
	 * @return the first error in the map
	 */
	private RMLaunchValidation checkForValidationError() {
		String error = parentTab.getUpdateHandler().getFirstError();
		if (error != null) {
			return new RMLaunchValidation(false, error);
		}
		return new RMLaunchValidation(true, null);
	}

	/**
	 * Adds the View Script and Restore Defaults buttons to the bottom of the control pane.
	 * 
	 * @param control
	 */
	private void createViewScriptGroup(final Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(4, true, 5, 5, 2, 2);
		GridData gd = WidgetBuilderUtils.createGridData(SWT.NONE, 4);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);

		if (parentTab.hasScript()) {
			Button b = WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, this);
			SWTUtil.setButtonDimensionHint(b);
		}

		if (controller.isShowViewConfig()) {
			Button b = WidgetBuilderUtils.createPushButton(grp, Messages.ViewConfig, this);
			SWTUtil.setButtonDimensionHint(b);
			b.setToolTipText(Messages.ViewConfigTooltip);
		}

		if (controller.isShowViewExcluded()) {
			Button b = WidgetBuilderUtils.createPushButton(grp, Messages.ViewExcluded, this);
			SWTUtil.setButtonDimensionHint(b);
			b.setToolTipText(Messages.ViewExcludedTooltip);
		}

		Button b = WidgetBuilderUtils.createPushButton(grp, Messages.DefaultValues, new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				resetDefaults();
			}
		});
		SWTUtil.setButtonDimensionHint(b);
	}

	/**
	 * Creates a contents string from the current configuration.
	 * 
	 * @param config
	 *            current
	 * @return string representing contents
	 * @throws Throwable
	 */
	private synchronized String displayConfigurationContents(final ILaunchConfiguration config) throws Throwable {
		final StringBuffer buffer = new StringBuffer();
		Job job = new Job(JAXBControlUIConstants.ZEROSTR) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Map<String, Object> validCurrent = RMVariableMap.getValidAttributes(config);
					for (Map.Entry<String, Object> e : validCurrent.entrySet()) {
						Object v = e.getValue();
						buffer.append(e.getKey()).append(JAXBControlUIConstants.EQ).append(v)
								.append(JAXBControlUIConstants.LINE_SEP);
					}
				} catch (CoreException t) {
					return CoreExceptionUtils.getErrorStatus(t.getMessage(), t);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		return buffer.toString();
	}

	/**
	 * Creates a contents string from the excluded properties.
	 * 
	 * @return string representing contents
	 * @throws Throwable
	 */
	private String displayExcluded() throws Throwable {
		final StringBuffer buffer = new StringBuffer();
		Job job = new Job(JAXBControlUIConstants.ZEROSTR) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Map<String, Object> excluded = parentTab.getVariableMap().getExcluded();
				for (Map.Entry<String, Object> e : excluded.entrySet()) {
					Object v = e.getValue();
					buffer.append(e.getKey()).append(JAXBControlUIConstants.EQ).append(v).append(JAXBControlUIConstants.LINE_SEP);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		return buffer.toString();
	}

	/**
	 * Creates the set of all widget variables on shared tabs that are valid.
	 * 
	 * @param localInvalid
	 *            locally excluded override external if there is an intersection
	 * @return set of shared variables that are valid
	 */
	private Set<String> getSharedValid(Set<String> localInvalid) {
		Set<String> sharedInvalid = new HashSet<String>();
		LCVariableMap lcMap = parentTab.getVariableMap();
		for (String title : shared) {
			String invalid = (String) lcMap.getValue(JAXBUIConstants.INVALID + title);
			if (invalid != null) {
				String[] names = invalid.split(JAXBUIConstants.SP);
				for (String name : names) {
					sharedInvalid.add(name.trim());
				}
			}
		}

		Set<String> sharedValid = new TreeSet<String>();
		for (IUpdateModel m : sharedModels) {
			String name = m.getName();
			if (name != null && !sharedInvalid.contains(name) && !localInvalid.contains(name)) {
				sharedValid.add(name);
			}
		}
		return sharedValid;
	}

	/**
	 * After refreshing the local map, it swaps in the map to be the active environment, adds any environment variables from the
	 * Environment Tab, then schedules a script handler job. Swaps the previous map back into the active environement when the
	 * handler returns.
	 * 
	 * @param config
	 * @return the script
	 * @throws Throwable
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized String realizeLocalScript(ILaunchConfiguration config) throws Throwable {
		String value = JAXBControlUIConstants.ZEROSTR;
		LCVariableMap lcMap = parentTab.getVariableMap();
		lcMap.shiftToCurrent(getControllerTag());
		Map env = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		ScriptHandler job = new ScriptHandler(null, parentTab.getScript(), lcMap, env, true);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		value = job.getScriptValue();
		lcMap.restoreGlobal();
		return value;
	}

	/**
	 * Auxiliary for updating configuration/map values. Writes value of local widget or cell editor (if the viewer row is selected)
	 * to the map. Also writes to the lists pertaining to current state of the environment.
	 * 
	 * @param model
	 *            of widget
	 * @param lcMap
	 *            configuration wrapper
	 */
	private void refresh(IUpdateModel model, LCVariableMap lcMap) {
		String name = model.getName();
		if (name == null || JAXBUIConstants.ZEROSTR.equals(name)) {
			return;
		}
		Object value = null;
		boolean selected = true;

		Control c = null;
		if (model instanceof ViewerUpdateModel) {
			ViewerUpdateModel vmodel = (ViewerUpdateModel) model;
			vmodel.putCheckedSettings(lcMap);
			c = vmodel.getSWTControl();
		} else if (model instanceof ICellEditorUpdateModel) {
			ICellEditorUpdateModel cellModel = (ICellEditorUpdateModel) model;
			selected = cellModel.isChecked();
			c = cellModel.getParent().getControl();
		} else {
			c = (Control) model.getControl();
		}

		if (model.isWritable() && selected) {
			value = model.getValueFromControl();
			lcMap.putValue(name, value);
		}

		boolean visible = c == null ? false : (!getParent().isInitialized() || c.isVisible());
		boolean enabled = c == null ? false : c.isEnabled();
		if (visible) {
			visibleList.add(name);
			if (enabled) {
				enabledList.add(name);
				if (model.isWritable() && selected) {
					validSet.add(name);
				}
			}
		} else if (enabled) {
			enabledList.add(name);
		}
	}

	/**
	 * Calls {@link #refresh(IUpdateModel, LCVariableMap)} for each entry in the local widgets map.
	 * 
	 * Calls {@link ControlStateListener#setState()} on all control state listeners to update any widget state that is based on an
	 * attribute value rather than a button ID.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.launch.AbstractJAXBLaunchConfigurationTab# doRefreshLocal()
	 */
	@Override
	protected void doRefreshLocal() {
		LCVariableMap lcMap = parentTab.getVariableMap();
		for (IUpdateModel m : localWidgets.values()) {
			refresh(m, lcMap);
		}
		if (listeners != null) {
			for (ControlStateListener l : listeners) {
				l.setState();
			}
		}
	}

	/**
	 * The complement of validSet.
	 * 
	 * @return all locally invalid variables
	 */
	protected Set<String> getLocalInvalid() {
		Set<String> locaInvalid = new TreeSet<String>();
		for (IUpdateModel m : localWidgets.values()) {
			String name = m.getName();
			if (name == null) {
				continue;
			}
			if (!validSet.contains(name)) {
				locaInvalid.add(name);
			}
		}
		locaInvalid.add(JAXBControlConstants.SCRIPT_PATH);
		return locaInvalid;
	}

	/**
	 * Runs only on local widgets. First resets default values from the LCVariableMap on widgets and rows in the viewers, then has
	 * the viewers rewrite their templated strings. The update handler is called to refresh all the widgets from the map, and then
	 * the viewers are refreshed.
	 */
	protected synchronized void resetDefaults() {
		Collection<IUpdateModel> models = localWidgets.values();
		for (IUpdateModel m : models) {
			m.restoreDefault();
		}
		for (IUpdateModel m : models) {
			if (m instanceof ViewerUpdateModel) {
				((ViewerUpdateModel) m).storeValue();
			}
		}
		updateHandler.handleUpdate(null, null);
		for (Viewer v : viewers) {
			v.refresh();
		}
	}

	/**
	 * VISIBLE, ENABLED, (LOCALLY) INVALID and VALID.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.AbstractJAXBLaunchConfigurationTab #writeLocalProperties()
	 */
	@Override
	protected void writeLocalProperties() {
		LCVariableMap lcMap = parentTab.getVariableMap();
		String id = getControllerTag();
		lcMap.putValue(JAXBUIConstants.CURRENT_CONTROLLER, id);

		StringBuffer list = new StringBuffer();
		for (String var : visibleList) {
			list.append(var).append(JAXBUIConstants.SP);
		}
		lcMap.putValue(JAXBUIConstants.VISIBLE + id, list.toString().trim());

		list.setLength(0);
		for (String var : enabledList) {
			list.append(var).append(JAXBUIConstants.SP);
		}
		lcMap.putValue(JAXBUIConstants.ENABLED + id, list.toString().trim());

		Set<String> set = getLocalInvalid();
		list.setLength(0);
		for (String var : set) {
			list.append(var).append(JAXBUIConstants.SP);
		}
		lcMap.putValue(JAXBUIConstants.INVALID + id, list.toString().trim());

		set = getSharedValid(set);
		set.addAll(validSet);
		set.addAll(lcMap.getHidden());
		set.add(JAXBUIConstants.CURRENT_CONTROLLER);
		set.add(JAXBUIConstants.VISIBLE + id);
		set.add(JAXBUIConstants.ENABLED + id);
		set.add(JAXBUIConstants.INVALID + id);
		set.add(JAXBUIConstants.VALID + id);

		list.setLength(0);
		for (String var : set) {
			list.append(var).append(JAXBUIConstants.SP);
		}
		lcMap.putValue(JAXBUIConstants.VALID + id, list.toString().trim());
	}
}
