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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.control.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.LaunchTabBuilder;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.data.ButtonActionType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rmsystem.IResourceManager;
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
 * Launch Tab which builds its control from the JAXB data elements. The number,
 * type and disposition of widgets connected to Properties or Attributes is
 * almost entirely configurable via the XML.<br>
 * <br>
 * Aside from being registered with the update handler, the widgets specific to
 * this tab are also maintained in a local map so that their values (and only
 * theirs) will appear in the environment when performApply() is called. A list
 * of viewers is also kept so that refresh can be called on them when the tab is
 * (re-)initialized or the defaults are reset. <br>
 * <br>
 * If different widgets on different tabs reference the same Property or
 * Attribute, its value will change everywhere. However, if the
 * <code>shared</code> property is set to true, then the local map is populated
 * by values produced from all tabs and defaults are likewise reset on all
 * widgets in the update handler. This shared option is motivated by a scenario
 * in which the attributes are partitioned (empty intersection) between the
 * various tabs, but all of them are necessary to the definition of the job
 * submission. The default (not shared) implies that the tabs have intersecting
 * subsets of the whole attribute set, any one of which is sufficient to
 * configure a launch.
 * 
 * @author arossi
 * 
 */
public class JAXBDynamicLaunchConfigurationTab extends AbstractJAXBLaunchConfigurationTab implements SelectionListener {

	private final IJAXBResourceManager rm;
	private final TabControllerType controller;
	private final ValueUpdateHandler updateHandler;
	private final List<Viewer> viewers;
	private final Map<Object, IUpdateModel> localWidgets;
	private final String[] shared;
	private final Collection<IUpdateModel> sharedModels;

	private ILaunchConfiguration listenerConfiguration;

	/**
	 * @param rm
	 *            the resource manager
	 * @param dialog
	 *            the ancestor main launch dialog
	 * @param controller
	 *            the JAXB data element from which this tab's control will be
	 *            built
	 * @param parentTab
	 *            the parent controller tab
	 */
	public JAXBDynamicLaunchConfigurationTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog,
			TabControllerType controller, JAXBControllerLaunchConfigurationTab parentTab) {
		super(parentTab, dialog);
		this.rm = rm;
		this.controller = controller;
		String s = controller.getIncludeWidgetValuesFrom();
		if (s == null) {
			shared = new String[0];
		} else {
			shared = s.split(JAXBUIConstants.CM);
		}
		sharedModels = new ArrayList<IUpdateModel>();
		String title = controller.getTitle();
		if (title != null) {
			this.title = title;
		}
		updateHandler = parentTab.getUpdateHandler();
		localWidgets = new HashMap<Object, IUpdateModel>();
		viewers = new ArrayList<Viewer>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #canSave(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		return validateWidgets();
	}

	/*
	 * Maintains the top-level control element on which to rehang the rest of
	 * the controls when rebuilt. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		// control = WidgetBuilderUtils.createComposite(parent, 1);
		try {
			LaunchTabBuilder builder = new LaunchTabBuilder(this);
			control = builder.build(parent);
		} catch (Throwable t) {
			throw CoreExceptionUtils.newException(Messages.CreateControlConfigurableError, t);
		}
		createViewScriptGroup(control);
		control.layout(true, true);
		size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
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
	public JAXBControllerLaunchConfigurationTab getParent() {
		return parentTab;
	}

	/**
	 * @return title of tab.
	 */
	@Override
	public String getText() {
		return title;
	}

	/*
	 * Resets the configuation then re-initializes all widgets: 1. clears
	 * viewers and repopulate that list. 2. repopulates the handler with local
	 * widgets. 3. initializes the (new) widgets from the original global map.
	 * 4. initializes the checked state on any checkbox viewers and then
	 * refreshes them.(non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
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

			LCVariableMap lcMap = parentTab.getLCMap();
			lcMap.updateGlobal(listenerConfiguration);
			RMVariableMap rmMap = (RMVariableMap) this.rm.getJAXBConfiguration().getRMVariableMap();

			for (IUpdateModel m : localWidgets.values()) {
				m.initialize(rmMap, lcMap);
			}

			for (IUpdateModel m : localWidgets.values()) {
				if (m instanceof ViewerUpdateModel) {
					((ViewerUpdateModel) m).initializeChecked();
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
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		return validateWidgets();
	}

	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		if (control.isVisible()) {
			RMLaunchValidation v = validateWidgets();
			if (!v.isSuccess()) {
				return v;
			}
		}
		return super.performApply(configuration, rm, queue);
	}

	/**
	 * Invokes a command on the resource manager. This may be called by action
	 * button listeners.
	 * 
	 * @param action
	 *            a command from the control data type
	 * 
	 */
	public void run(ButtonActionType action) throws CoreException {
		rm.getControl().runActionCommand(action.getAction(), action.getClearValue());
		if (action.isRefresh()) {
			try {
				parentTab.initializeFrom(null, rm, null, listenerConfiguration);
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(t.getLocalizedMessage(), t);
			}
		}
	}

	/*
	 * Defaults are taken care of by the load method on the LCVariableMap
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Pull out the local maps from each and set them into the shared array.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.control.ui.launch.AbstractJAXBLaunchConfigurationTab
	 * #setUpSharedEnvironment(java.util.List)
	 */
	@Override
	public void setUpSharedEnvironment(Map<String, AbstractJAXBLaunchConfigurationTab> controllers) {
		sharedModels.clear();
		for (String title : shared) {
			AbstractJAXBLaunchConfigurationTab tab = controllers.get(title);
			if (tab instanceof JAXBDynamicLaunchConfigurationTab) {
				sharedModels.addAll(((JAXBDynamicLaunchConfigurationTab) tab).localWidgets.values());
			}
		}
	}

	/*
	 * If this is not the last selected tab, sets a property in the
	 * configuration marking this tab as visible. This is mainly in order to
	 * trigger activation of the Apply button. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.control.ui.launch.AbstractJAXBLaunchConfigurationTab
	 * #setVisible()
	 */
	@Override
	public void setVisible() {
		try {
			String lastVisited = parentTab.getLastVisited();
			if (title.equals(lastVisited)) {
				return;
			}
			parentTab.setLastVisited(title);
			listenerConfiguration.getWorkingCopy().setAttribute(JAXBUIConstants.VISIBLE, this.toString());
			fireContentsChanged();
		} catch (CoreException t) {
			JAXBUIPlugin.log(t);
		}
	}

	/*
	 * Tab acts a listener for viewScript button. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Tab acts a listener for viewScript button. If the resource manager does
	 * not use a script, this call returns immediately; else it calls
	 * #realizeLocalScript(ILaunchConfiguration), then displays the result in a
	 * read-only dialog. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
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

	/*
	 * Writes values of local widgets or cell editors (if the viewer row is
	 * selected) to the local map. We enforce the same exclusion of
	 * <code>null</code> as we do on the LCVariableMap#put method (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.launch.AbstractJAXBLaunchConfigurationTab#
	 * doRefreshLocal()
	 */
	@Override
	protected void doRefreshLocal() {
		Object value = null;
		List<ViewerUpdateModel> viewerModels = new ArrayList<ViewerUpdateModel>();
		for (IUpdateModel m : getModels()) {
			if (!m.isWritable()) {
				continue;
			}
			if (m instanceof ViewerUpdateModel) {
				viewerModels.add((ViewerUpdateModel) m);
				continue;
			}
			if (m instanceof ICellEditorUpdateModel) {
				if (((ICellEditorUpdateModel) m).isChecked()) {
					value = m.getValueFromControl();
					if (value != null) {
						localMap.put(m.getName(), value);
					}
				}
			} else {
				value = m.getValueFromControl();
				if (value != null) {
					localMap.put(m.getName(), value);
				}
			}
		}
		for (ViewerUpdateModel m : viewerModels) {
			value = m.getValueFromControl();
			if (value != null) {
				localMap.put(m.getName(), value);
			}
			m.putCheckedSettings(localMap);
		}
	}

	/**
	 * Adds the View Script and Restore Defaults buttons to the bottom of the
	 * control pane.
	 * 
	 * @param control
	 */
	private void createViewScriptGroup(final Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(3, true, 5, 5, 2, 2);
		GridData gd = WidgetBuilderUtils.createGridData(SWT.NONE, 3);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);

		if (parentTab.hasScript()) {
			WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, this);
		}

		WidgetBuilderUtils.createPushButton(grp, Messages.ViewConfig, this);

		WidgetBuilderUtils.createPushButton(grp, Messages.DefaultValues, new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				resetDefaults();
			}
		});
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
		refreshLocal(config);
		Job job = new Job(JAXBControlUIConstants.ZEROSTR) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Map<Object, Object> attr = config.getAttributes();
					for (Map.Entry e : attr.entrySet()) {
						Object v = e.getValue();
						if (v != null && !JAXBControlUIConstants.ZEROSTR.equals(v)) {
							buffer.append(e.getKey()).append(JAXBControlUIConstants.EQ).append(v)
									.append(JAXBControlUIConstants.LINE_SEP);
						}
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
	 * Gathers widgets to include in the local mapping.
	 * 
	 * @return the set of widgets to be accessed for values.
	 */
	private Collection<IUpdateModel> getModels() {
		Collection<IUpdateModel> models = new ArrayList<IUpdateModel>();
		models.addAll(sharedModels);
		models.addAll(localWidgets.values());
		return models;
	}

	/**
	 * After refreshing the local map, it swaps in the map to be the active
	 * environment, adds any environment variables from the Environment Tab,
	 * then schedules a script handler job. Swaps the previous map back into the
	 * active environement when the handler returns.
	 * 
	 * @param config
	 * @return the script
	 * @throws Throwable
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized String realizeLocalScript(ILaunchConfiguration config) throws Throwable {
		String value = JAXBControlUIConstants.ZEROSTR;
		refreshLocal(config);
		LCVariableMap lcMap = parentTab.getLCMap();
		Map<String, Object> current = lcMap.swapVariables(localMap);
		Map env = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		ScriptHandler job = new ScriptHandler(null, parentTab.getScript(), lcMap, env, true);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		value = job.getScriptValue();
		lcMap.swapVariables(current);
		return value;
	}

	/**
	 * Runs only on local widgets. First resets default values from the
	 * LCVariableMap on widgets and selected rows in the viewers, then has the
	 * viewers rewrite their templated strings. The update handler is called to
	 * refresh all the widgets from the map, and then the viewers are refreshed.
	 */
	private synchronized void resetDefaults() {
		Collection<IUpdateModel> models = localWidgets.values();
		for (IUpdateModel m : models) {
			if (m instanceof ICellEditorUpdateModel) {
				if (((ICellEditorUpdateModel) m).isChecked()) {
					m.restoreDefault();
				}
			} else {
				m.restoreDefault();
			}
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
	 * Runs the validator on the widgets, if they have one.
	 * 
	 * @return invalid on first failure; else valid;
	 */
	private RMLaunchValidation validateWidgets() {
		for (IUpdateModel m : localWidgets.values()) {
			String error = m.validate();
			if (error != null) {
				return new RMLaunchValidation(false, error);
			}
		}
		return new RMLaunchValidation(true, null);
	}
}
