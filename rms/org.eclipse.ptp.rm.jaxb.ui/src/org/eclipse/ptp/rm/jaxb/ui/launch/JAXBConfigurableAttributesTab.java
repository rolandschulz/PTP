package org.eclipse.ptp.rm.jaxb.ui.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.util.LaunchTabBuilder;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
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
import org.eclipse.swt.widgets.Group;

public class JAXBConfigurableAttributesTab extends BaseRMLaunchConfigurationDynamicTab implements IJAXBUINonNLSConstants {

	private class JAXBUpdateOnlyDataSource extends RMLaunchConfigurationDynamicTabDataSource {

		protected JAXBUpdateOnlyDataSource(JAXBConfigurableAttributesTab tab) {
			super(tab);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
		}

		@Override
		protected void copyToFields() {
		}

		@Override
		protected void copyToStorage() {
		}

		@Override
		protected void loadDefault() {
		}

		@Override
		protected void loadFromStorage() {
		}

		@Override
		protected void validateLocal() throws ValidationException {
		}
	}

	private final JAXBLaunchConfigurationDynamicTab parentTab;
	private final TabController controller;
	private final Map<Object, IUpdateModel> localWidgets;
	private final List<Viewer> viewers;
	private final Map<String, Object> localMap;
	private final String title;

	private boolean initialized;

	private Composite control;
	private Button viewScript;

	public JAXBConfigurableAttributesTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog, TabController controller,
			JAXBLaunchConfigurationDynamicTab parentTab) {
		super(dialog);
		this.parentTab = parentTab;
		this.controller = controller;
		String t = controller.getTitle();
		if (t == null) {
			t = Messages.DefaultDynamicTab_title;
		}
		this.title = t;
		localWidgets = new HashMap<Object, IUpdateModel>();
		localMap = new TreeMap<String, Object>();
		viewers = new ArrayList<Viewer>();
		initialized = false;
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

	public JAXBLaunchConfigurationDynamicTab getParent() {
		return parentTab;
	}

	@Override
	public String getText() {
		return title;
	}

	@Override
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		if (!initialized) {
			try {
				if (viewScript != null) {
					viewScript.addSelectionListener(createViewScriptListener(configuration));
				}

				LCVariableMap lcMap = parentTab.getLCMap();
				lcMap.restoreGlobal();

				localMap.clear();
				viewers.clear();

				for (Map.Entry<Object, IUpdateModel> e : localWidgets.entrySet()) {
					Object key = e.getKey();
					IUpdateModel value = e.getValue();
					value.initialize(lcMap);
					String name = value.getName();
					localMap.put(name, lcMap.get(name));
					if (key instanceof Viewer) {
						viewers.add((Viewer) key);
					}
				}
				parentTab.getLCMap().swapVariables(localMap);

				for (IUpdateModel m : localWidgets.values()) {
					m.refreshValueFromMap();
				}

				for (IUpdateModel m : localWidgets.values()) {
					if (m instanceof ViewerUpdateModel) {
						((ViewerUpdateModel) m).initializeSelected();
					}
				}

				for (Viewer v : viewers) {
					WidgetActionUtils.refreshViewer(v);
				}

				parentTab.getUpdateHandler().addListener(getListener());
				initialized = true;
			} catch (Throwable t) {
				JAXBUIPlugin.log(t);
			}
		}
		return super.initializeFrom(control, rm, queue, configuration);
	}

	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		LCVariableMap lcMap = parentTab.getLCMap();
		try {
			lcMap.swapVariables(localMap);
			lcMap.saveToConfiguration(configuration);
		} catch (CoreException t) {
			JAXBUIPlugin.log(t);
		}
		return super.performApply(configuration, rm, queue);
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	@Override
	public void updateControls() {
		/*
		 * This controls the visible and enabled settings of the widgets. For
		 * this tab, these are not configurable, so this is a NOP
		 */
	}

	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new JAXBUpdateOnlyDataSource(this);
	};

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new RMLaunchConfigurationDynamicTabWidgetListener(this) {
			@Override
			protected void maybeFireContentsChanged() {
			}
		};
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

	private SelectionListener createViewScriptListener(final ILaunchConfiguration configuration) {
		return new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					if (!parentTab.hasScript()) {
						WidgetActionUtils.warningMessage(control.getShell(), Messages.ScriptNotSupportedWarning,
								Messages.ScriptNotSupportedWarning_title);
						return;
					}
					String text = realizeLocalScript(configuration);
					new ScrollingEditableMessageDialog(control.getShell(), Messages.DisplayScript, text, true).open();
				} catch (Throwable t) {
					WidgetActionUtils.errorMessage(control.getShell(), t, Messages.DisplayScriptError,
							Messages.DisplayScriptErrorTitle, false);
				}
			}
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized String realizeLocalScript(ILaunchConfiguration config) throws Throwable {
		String value = ZEROSTR;
		parentTab.getLCMap();
		localMap.put(DIRECTORY, config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, ZEROSTR));
		localMap.put(EXEC_PATH, config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, ZEROSTR));
		localMap.put(PROG_ARGS, config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, ZEROSTR));
		Boolean append = config.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		if (append == null) {
			append = true;
		}
		Map env = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		ScriptHandler job = new ScriptHandler(null, parentTab.getScript(), LCVariableMap.getActiveInstance(), env, append);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		value = job.getScriptValue();
		return value;
	}

	private synchronized void resetDefaults() {
		for (IUpdateModel m : localWidgets.values()) {
			m.restoreDefault();
		}
		for (IUpdateModel m : localWidgets.values()) {
			if (m instanceof ViewerUpdateModel) {
				((ViewerUpdateModel) m).storeValue();
			}
		}
		parentTab.getUpdateHandler().handleUpdate(null, null);
		for (Viewer v : viewers) {
			WidgetActionUtils.refreshViewer(v);
		}
	}
}
