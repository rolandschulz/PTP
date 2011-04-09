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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class JAXBDynamicLaunchConfigurationTab extends AbstractJAXBLaunchConfigurationTab implements SelectionListener {

	private final TabController controller;
	private final ValueUpdateHandler updateHandler;
	private final List<Viewer> viewers;
	private final Map<Object, IUpdateModel> localWidgets;
	private final boolean shared;

	private Button viewScript;
	private ILaunchConfiguration listenerConfiguration;

	public JAXBDynamicLaunchConfigurationTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog, TabController controller,
			JAXBControllerLaunchConfigurationTab parentTab, int tabIndex) {
		super(parentTab, dialog, tabIndex);
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

	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		/*
		 * Value validation is handled up front
		 */
		return new RMLaunchValidation(true, null);
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);
		LaunchTabBuilder builder = new LaunchTabBuilder(this);
		try {
			builder.build(control);
		} catch (Throwable t) {
			CoreExceptionUtils.newException(Messages.CreateControlConfigurableError, t);
		}
		createViewScriptGroup(control);
		parentTab.resize(this.control);
	}

	public Control getControl() {
		return control;
	}

	/**
	 * Used by the LaunchTabBuilder
	 * 
	 * @return the JAXB data element used to build the control
	 */
	public TabController getController() {
		return controller;
	}

	@Override
	public Image getImage() {
		return null;
	}

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

	@Override
	public String getText() {
		return title;
	}

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		try {
			ValueUpdateHandler handler = getParent().getUpdateHandler();
			listenerConfiguration = configuration;
			if (viewScript != null) {
				viewScript.addSelectionListener(this);
			}

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
			lcMap.restoreGlobal();

			for (IUpdateModel m : localWidgets.values()) {
				m.initialize(lcMap);
			}

			for (IUpdateModel m : localWidgets.values()) {
				if (m instanceof ViewerUpdateModel) {
					((ViewerUpdateModel) m).initializeSelected();
				}
			}

			for (Viewer v : viewers) {
				WidgetActionUtils.refreshViewer(v);
			}
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
			return new RMLaunchValidation(false, t.getMessage());
		}
		return new RMLaunchValidation(true, null);
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		/*
		 * Value validation is now handled up front
		 */
		return new RMLaunchValidation(true, null);
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		/*
		 * taken care of by the load method on the LCVariableMap
		 */
		return new RMLaunchValidation(true, null);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		Shell shell = Display.getDefault().getActiveShell();
		try {
			if (!parentTab.hasScript()) {
				WidgetActionUtils.warningMessage(shell, Messages.ScriptNotSupportedWarning,
						Messages.ScriptNotSupportedWarning_title);
				return;
			}
			String text = realizeLocalScript(listenerConfiguration);
			new ScrollingEditableMessageDialog(shell, Messages.DisplayScript, text, true).open();
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(shell, t, Messages.DisplayScriptError, Messages.DisplayScriptErrorTitle, false);
		}
	}

	@Override
	protected void doRefreshLocal() {
		for (IUpdateModel m : getModels()) {
			if (m instanceof ICellEditorUpdateModel) {
				if (((ICellEditorUpdateModel) m).isSelected()) {
					localMap.put(m.getName(), m.getValueFromControl());
				}
			} else {
				localMap.put(m.getName(), m.getValueFromControl());
			}
		}
	}

	private void createViewScriptGroup(final Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(2, true);
		GridData gd = WidgetBuilderUtils.createGridData(SWT.NONE, 2);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		if (parentTab.hasScript()) {
			viewScript = WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, null);
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

	private Collection<IUpdateModel> getModels() {
		Collection<IUpdateModel> models = null;
		if (shared) {
			models = updateHandler.getControlToModelMap().values();
		} else {
			models = localWidgets.values();
		}
		return models;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized String realizeLocalScript(ILaunchConfiguration config) throws Throwable {
		String value = ZEROSTR;
		refreshLocal(config);
		LCVariableMap lcMap = parentTab.getLCMap();
		Map<String, Object> current = lcMap.swapVariables(localMap);

		Boolean append = config.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		if (append == null) {
			append = true;
		}
		Map env = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		ScriptHandler job = new ScriptHandler(null, parentTab.getScript(), lcMap, env, append);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		value = job.getScriptValue();
		lcMap.swapVariables(current);
		return value;
	}

	private synchronized void resetDefaults() {
		Collection<IUpdateModel> models = getModels();
		for (IUpdateModel m : models) {
			if (m instanceof ICellEditorUpdateModel) {
				if (((ICellEditorUpdateModel) m).isSelected()) {
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
			WidgetActionUtils.refreshViewer(v);
		}
	}
}
