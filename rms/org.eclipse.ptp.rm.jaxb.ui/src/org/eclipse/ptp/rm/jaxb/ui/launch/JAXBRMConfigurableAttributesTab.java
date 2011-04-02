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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.LTVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.ILaunchTabValueHandler;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * @author arossi
 * 
 */
public class JAXBRMConfigurableAttributesTab extends BaseRMLaunchConfigurationDynamicTab implements IJAXBUINonNLSConstants {

	private class JAXBUniversalDataSource extends RMLaunchConfigurationDynamicTabDataSource {

		protected JAXBUniversalDataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			for (ILaunchTabValueHandler h : handlers) {
				h.setValuesOnMap(LTVariableMap.getActiveInstance());
			}
		}

		@Override
		protected void copyToFields() {
			for (ILaunchTabValueHandler h : handlers) {
				h.getValuesFromMap(LTVariableMap.getActiveInstance());
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected void copyToStorage() {
			try {
				ILaunchConfigurationWorkingCopy config = getConfigurationWorkingCopy();
				if (config == null) {
					return;
				}

				Map attrMap = config.getAttributes();
				LTVariableMap ltmap = LTVariableMap.getActiveInstance();
				Map<?, ?>[] m = new Map<?, ?>[] { ltmap.getVariables(), ltmap.getDiscovered() };
				for (int i = 0; i < m.length; i++) {
					for (Object k : m[i].keySet()) {
						Object v = m[i].get(k);
						attrMap.put(k, v);
					}
				}
				config.setAttributes(attrMap);
			} catch (Throwable t) {
				WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ErrorOnCopyToStorage,
						Messages.ErrorOnCopyToStorageTitle, false);
			}
		}

		@Override
		protected void loadDefault() {
			for (ILaunchTabValueHandler h : handlers) {
				h.setDefaultValuesOnControl(RMVariableMap.getActiveInstance());
			}
		}

		/*
		 * The LTVariableMap is initialized from the active instance of the
		 * RMVariableMap once. Its values are updated from the most recent
		 * LaunchConfiguration here.
		 */
		@Override
		protected void loadFromStorage() {
			try {
				ILaunchConfiguration config = getConfiguration();
				if (config == null) {
					return;
				}

				Map<?, ?> attrMap = config.getAttributes();
				LTVariableMap ltmap = LTVariableMap.getActiveInstance();
				Map<String, String> vars = ltmap.getVariables();
				Map<String, String> disc = ltmap.getDiscovered();
				for (Object k : attrMap.keySet()) {
					if (vars.containsKey(k)) {
						vars.put((String) k, (String) attrMap.get(k));
					} else if (disc.containsKey(k)) {
						disc.put((String) k, (String) attrMap.get(k));
					}
				}
			} catch (Throwable t) {
				WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ErrorOnLoadFromStore, Messages.ErrorOnLoadTitle,
						false);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
			try {
				for (ILaunchTabValueHandler h : handlers) {
					h.validateControlValues(RMVariableMap.getActiveInstance(), delegate.getRemoteFileManager());
				}
			} catch (Throwable t) {
				throw new ValidationException(t.getMessage());
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private String realizeScript() throws Throwable {
			String value = ZEROSTR;
			if (script != null) {
				ILaunchConfiguration configuration = getConfiguration();
				LTVariableMap map = LTVariableMap.getActiveInstance();
				map.maybeOverwrite(DIRECTORY, IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, configuration);
				map.maybeOverwrite(EXEC_PATH, IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, configuration);
				map.maybeOverwrite(PROG_ARGS, IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, configuration);
				boolean append = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
				Map env = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
				ScriptHandler job = new ScriptHandler(null, script, map, env, append);
				job.schedule();
				try {
					job.join();
				} catch (InterruptedException ignored) {
				}
				value = job.getScriptValue();
			}
			return value;
		}
	}

	private final JAXBRMLaunchConfigurationDynamicTab pTab;
	private final RemoteServicesDelegate delegate;
	private final TabController controller;
	private final List<ILaunchTabValueHandler> handlers;
	private final Script script;
	private final String title;
	private Composite control;
	private JAXBUniversalDataSource dataSource;

	public JAXBRMConfigurableAttributesTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog, TabController controller,
			JAXBRMLaunchConfigurationDynamicTab pTab) {
		super(dialog);
		delegate = rm.getControl().getRemoteServicesDelegate();
		this.pTab = pTab;
		this.controller = controller;
		String t = controller.getTitle();
		if (t == null) {
			t = Messages.DefaultDynamicTab_title;
		}
		this.title = t;
		handlers = new ArrayList<ILaunchTabValueHandler>();
		this.script = pTab.getRmConfig().getResourceManagerData().getControlData().getScript();
		resetEnv();
		createDataSource();
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);
		try {
			LaunchTabBuilder builder = new LaunchTabBuilder(this);
			builder.build(control);
			createViewScriptGroup(control);
			pTab.resize(control);
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
		}
	}

	public Control getControl() {
		return control;
	}

	public TabController getController() {
		return controller;
	}

	@Override
	public JAXBUniversalDataSource getDataSource() {
		return (JAXBUniversalDataSource) createDataSource();
	}

	public RemoteServicesDelegate getDelegate() {
		return delegate;
	}

	public List<ILaunchTabValueHandler> getHandlers() {
		return handlers;
	}

	@Override
	public Image getImage() {
		return null;
	}

	public JAXBRMLaunchConfigurationDynamicTab getParentTab() {
		return pTab;
	}

	@Override
	public String getText() {
		return title;
	}

	@Override
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		resetEnv();
		return super.initializeFrom(control, rm, queue, configuration);
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
		if (dataSource == null) {
			dataSource = new JAXBUniversalDataSource(this);
		}
		return dataSource;
	}

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new RMLaunchConfigurationDynamicTabWidgetListener(this) {
		};
	}

	private void createViewScriptGroup(final Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(2, true);
		GridData gd = WidgetBuilderUtils.createGridData(SWT.NONE, 2);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		if (pTab.hasScript()) {
			WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					try {
						dataSource.copyFromFields();
						String text = dataSource.realizeScript();
						new ScrollingEditableMessageDialog(control.getShell(), Messages.DisplayScript, text, true).open();
					} catch (Throwable t) {
						WidgetActionUtils.errorMessage(control.getShell(), t, Messages.DisplayScriptError,
								Messages.DisplayScriptErrorTitle, false);
					}
				}
			});
		}
		WidgetBuilderUtils.createPushButton(grp, Messages.DefaultValues, new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				dataSource.loadDefault();
			}
		});
	}

	private void resetEnv() {
		try {
			pTab.getRmConfig().setActive();
			LTVariableMap.setActiveInstance(RMVariableMap.getActiveInstance());
		} catch (Throwable t1) {
			JAXBUIPlugin.log(t1);
		}
	}
}
