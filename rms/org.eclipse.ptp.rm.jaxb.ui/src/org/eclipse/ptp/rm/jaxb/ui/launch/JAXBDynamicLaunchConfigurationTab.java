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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.JAXBRMUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.util.LaunchTabBuilder;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

	private final TabControllerType controller;
	private final ValueUpdateHandler updateHandler;
	private final List<Viewer> viewers;
	private final Map<Object, IUpdateModel> localWidgets;
	private final boolean shared;

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
		this.controller = controller;
		shared = controller.isSharedEnvironment();
		String title = controller.getTitle();
		if (title != null) {
			this.title = title;
		}
		updateHandler = parentTab.getUpdateHandler();
		localWidgets = new HashMap<Object, IUpdateModel>();
		viewers = new ArrayList<Viewer>();
	}

	/*
	 * Value validation is handled up front, so this just return true.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #canSave(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
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
		control = WidgetBuilderUtils.createComposite(parent, 1);
		try {
			LaunchTabBuilder builder = new LaunchTabBuilder(this);
			builder.build(control);
		} catch (Throwable t) {
			CoreExceptionUtils.newException(Messages.CreateControlConfigurableError, t);
		}
		createViewScriptGroup(control);
		parentTab.resize(this.control);
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
	 * 1. resets the configuation. 2. clears viewers and repopulate that list.
	 * 3. repopulates the handler with local widgets. 4. initializes the (new)
	 * widgets from the original global map. 5. initializes the checked state on
	 * any checkbox viewers and then refreshes them. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		try {
			ValueUpdateHandler handler = getParent().getUpdateHandler();
			listenerConfiguration = configuration;

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
			lcMap.updateGlobal(configuration);

			for (IUpdateModel m : localWidgets.values()) {
				m.initialize(lcMap);
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
			JAXBUIPlugin.log(t);
			return new RMLaunchValidation(false, t.getMessage());
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Value validation is now handled up front, so this just returns true.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
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
		Shell shell = Display.getDefault().getActiveShell();
		try {
			if (!parentTab.hasScript()) {
				MessageDialog.openWarning(shell, Messages.ScriptNotSupportedWarning_title, Messages.ScriptNotSupportedWarning
						+ JAXBRMUIConstants.LINE_SEP);
				return;
			}
			String text = realizeLocalScript(listenerConfiguration);
			new ScrollingEditableMessageDialog(shell, Messages.DisplayScript, text, true).open();
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(shell, t, Messages.DisplayScriptError, Messages.DisplayScriptErrorTitle, false);
		}
	}

	/*
	 * Writes values of local widgets or cell editors (if the viewer row is
	 * selected) to the local map. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.launch.AbstractJAXBLaunchConfigurationTab#
	 * doRefreshLocal()
	 */
	@Override
	protected void doRefreshLocal() {
		for (IUpdateModel m : getModels()) {
			if (m instanceof ICellEditorUpdateModel) {
				if (((ICellEditorUpdateModel) m).isChecked()) {
					localMap.put(m.getName(), m.getValueFromControl());
				}
			} else {
				localMap.put(m.getName(), m.getValueFromControl());
				if (m instanceof ViewerUpdateModel) {
					((ViewerUpdateModel) m).putCheckedSettings(localMap);
				}
			}
		}
	}

	/*
	 * Adds the Job Output, View Script and Restore Defaults buttons to the
	 * bottom of the control pane.
	 */
	private void createViewScriptGroup(final Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(2, true, 5, 5, 2, 2);
		GridData gd = WidgetBuilderUtils.createGridData(SWT.NONE, 2);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);

		if (parentTab.hasScript()) {
			WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, this);
		}

		WidgetBuilderUtils.createPushButton(grp, Messages.DefaultValues, new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				resetDefaults();
			}
		});
	}

	/*
	 * Determines whether the set of widgets is local or global.
	 * 
	 * @return the set of widgets to be accessed for values.
	 */
	private Collection<IUpdateModel> getModels() {
		Collection<IUpdateModel> models = null;
		if (shared) {
			models = updateHandler.getControlToModelMap().values();
		} else {
			models = localWidgets.values();
		}
		return models;
	}

	/*
	 * After refreshing the local map, it swaps in the map to be the active
	 * environment, adds any environment variables from the Environment Tab,
	 * then schedules a script handler job. Swaps the previous map back into the
	 * active environement when the handler returns.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized String realizeLocalScript(ILaunchConfiguration config) throws Throwable {
		String value = JAXBRMUIConstants.ZEROSTR;
		refreshLocal(config);
		LCVariableMap lcMap = parentTab.getLCMap();
		Map<String, Object> current = lcMap.swapVariables(localMap);
		Map env = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		ScriptHandler job = new ScriptHandler(null, parentTab.getScript(), lcMap, env);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		value = job.getScriptValue();
		lcMap.swapVariables(current);
		return value;
	}

	/*
	 * First resets default values from the LCVariableMap on widgets and
	 * selected rows in the viewers, then has the viewers rewrite their
	 * templated strings. The update handler is called to refresh all the
	 * widgets from the map, and then the viewers are refreshed.
	 */
	private synchronized void resetDefaults() {
		Collection<IUpdateModel> models = getModels();
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
}
