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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArglistImpl;
import org.eclipse.ptp.rm.jaxb.core.variables.LTVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.AttributeChoiceDialog;
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
import org.eclipse.swt.widgets.Button;
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
			Map<String, String> vars = LTVariableMap.getActiveInstance().getVariables();
			Map<String, String> disc = LTVariableMap.getActiveInstance().getDiscovered();
			for (Control c : valueWidgets.keySet()) {
				Widget w = valueWidgets.get(c);
				String name = w.getSaveAs();
				if (name == null) {
					continue;
				}
				String value = WidgetActionUtils.getValueString(c);
				if (vars.containsKey(name)) {
					vars.put(name, value);
				} else {
					disc.put(name, value);
				}
			}
		}

		@Override
		protected void copyToFields() {
			IVariableMap map = LTVariableMap.getActiveInstance();
			Map<String, ?> vars = map.getVariables();
			Map<String, ?> disc = map.getDiscovered();
			StringBuffer b = new StringBuffer();
			for (Control c : valueWidgets.keySet()) {
				Object value = null;
				Widget w = valueWidgets.get(c);
				String ref = w.getValueFrom();
				if (ref != null) {
					Object o = vars.get(ref);
					if (o == null) {
						o = disc.get(ref);
					}
					if (o instanceof Property) {
						value = ((Property) o).getValue();
					} else if (o instanceof JobAttribute) {
						value = ((JobAttribute) o).getValue();
					}
				} else {
					Arglist arglist = w.getDisplayValue();
					if (arglist != null) {
						b.setLength(0);
						new ArglistImpl(null, arglist, map).toString(b);
						value = b.toString();
					}
				}

				if (value == null) {
					value = w.getValue();
				}

				if (value == null) {
					WidgetActionUtils.setValue(c, null);
				} else {
					WidgetActionUtils.setValue(c, value.toString());
				}
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

		/*
		 * Defaults are recorded in the Property or JobAttribute definitions and
		 * are accessed via the RMVariableMap.
		 * 
		 * Only widgets whose value is a reference may have a valid default; the
		 * default value does not overwrite a non-null value.
		 */
		@Override
		protected void loadDefault() {
			Map<String, Object> vars = RMVariableMap.getActiveInstance().getVariables();
			Map<String, Object> disc = RMVariableMap.getActiveInstance().getDiscovered();
			for (Control c : valueWidgets.keySet()) {
				Widget w = valueWidgets.get(c);
				String value = w.getValue();
				if (value != null) {
					continue;
				}

				String name = w.getValueFrom();
				if (name == null) {
					continue;
				}

				Object o = vars.get(name);
				String defaultValue = null;
				if (o == null) {
					o = disc.get(name);
				}
				if (o instanceof Property) {
					defaultValue = ((Property) o).getDefault();
				} else if (o instanceof JobAttribute) {
					defaultValue = ((JobAttribute) o).getDefault();
				}
				if (defaultValue != null) {
					w.setValue(defaultValue);
				}
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
			Map<String, Object> vars = RMVariableMap.getActiveInstance().getVariables();
			/*
			 * If there are validators, run them against the value
			 */
			for (Control c : valueWidgets.keySet()) {
				Widget w = valueWidgets.get(c);
				String name = w.getValueFrom();
				if (name == null) {
					continue;
				}
				Object o = vars.get(name);
				if (o instanceof JobAttribute) {
					JobAttribute ja = (JobAttribute) o;
					Validator v = ja.getValidator();
					if (v != null) {
						try {
							WidgetActionUtils.validate(c, v, ja.getDefault(), fileManager);
						} catch (Throwable t) {
							throw new ValidationException(t.getMessage());
						}
					}
				} else if (o instanceof Property) {
					Property p = (Property) o;
					WidgetActionUtils.validate(c, p.getDefault());
				}
			}
		}
	}

	private class JAXBUniversalWidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {
		public JAXBUniversalWidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}
		/*
		 * The list of listeners will always include the ContentsChangedListener
		 * of the Resources Tab, which bottoms out in an updateButtons call
		 * enabling the "Apply" button.
		 * 
		 * The performApply() method of the ResourcesTab calls performApply() on
		 * the BaseRMLaunchConfigurationDynamicTab which in turn calls the
		 * storeAndValidate() method of the DataSource.
		 * 
		 * The methods loadAndUpdate() and justUpdate() on the DataSource can be
		 * used to refresh. The former is called on RM initialization, which
		 * takes place when the RM becomes visible.
		 */
	}

	private class SelectAttributesListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public synchronized void widgetSelected(SelectionEvent e) {
			try {
				Object source = e.getSource();
				if (source == selectAttributes) {
					buildMain(updateVisibleAttributes(true));
				} else if (source == viewScript) {

				}
			} catch (Throwable t) {
				WidgetActionUtils.errorMessage(control.getShell(), t, Messages.WidgetSelectedError,
						Messages.WidgetSelectedErrorTitle, false);
			}
		}
	}

	private final IRemoteFileManager fileManager;
	private final JAXBRMLaunchConfigurationDynamicTab pTab;
	private final TabController controller;
	private final Map<Control, Widget> valueWidgets;

	private AttributeChoiceDialog selectionDialog;

	private Composite dynamicControl;
	private Composite control;
	private final String title;
	private Button selectAttributes;
	private Button viewScript;

	private JAXBUniversalWidgetListener universalListener;
	private JAXBUniversalDataSource dataSource;

	public JAXBRMConfigurableAttributesTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog,
			TabController controller, JAXBRMLaunchConfigurationDynamicTab pTab) {
		super(dialog);
		fileManager = rm.getRemoteFileManager();
		this.pTab = pTab;
		this.controller = controller;
		String t = controller.getTitle();
		if (t == null) {
			t = Messages.DefaultDynamicTab_title;
		}
		this.title = t;
		valueWidgets = new HashMap<Control, Widget>();
		createListener();
		createDataSource();
		try {
			pTab.getRmConfig().setActive();
			LTVariableMap.setActiveInstance(RMVariableMap.getActiveInstance());
		} catch (Throwable t1) {
			JAXBUIPlugin.log(t1);
		}
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);
		selectionDialog = new AttributeChoiceDialog(parent.getShell());

		if (controller.isDynamic()) {
			createDynamicSelectionGroup(control);
		} else if (pTab.hasScript()) {
			createViewScriptGroup(control);
		}
		try {
			buildMain(updateVisibleAttributes(false));
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
		}
	}

	public Control getControl() {
		return control;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return title;
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		/*
		 * See dataSource loadDefaults
		 */
		return null;
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
		if (universalListener == null) {
			universalListener = new JAXBUniversalWidgetListener(this);
		}
		return universalListener;
	}

	private void buildMain(Map<String, Boolean> checked) {
		universalListener.disable();
		dataSource.storeAndValidate();

		if (dynamicControl != null) {
			dynamicControl.dispose();
			valueWidgets.clear();
		}

		if (control.isDisposed()) {
			return;
		}

		dynamicControl = WidgetBuilderUtils.createComposite(control, 1);
		LaunchTabBuilder builder = new LaunchTabBuilder(controller, RMVariableMap.getActiveInstance(), valueWidgets, checked);
		try {
			builder.build(dynamicControl);
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
		}

		/*
		 * We need to repeat this here (the ResourcesTab does it when it
		 * initially builds the control).
		 */
		pTab.resize(control);

		dataSource.loadAndUpdate();
		updateControls();
		universalListener.enable();
	}

	private void createDynamicSelectionGroup(Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(4, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(4);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(grp, Messages.ConfigureLaunchSettings, SWT.RIGHT, 1);
		selectAttributes = WidgetBuilderUtils.createPushButton(grp, Messages.SelectAttributesForDisplay,
				new SelectAttributesListener());
		if (pTab.hasScript()) {
			viewScript = WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, new SelectAttributesListener());
			WidgetBuilderUtils.createLabel(grp, Messages.ViewValuesReplaced, SWT.LEFT, 1);
		}
	}

	private void createViewScriptGroup(Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(2, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(2);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(grp, Messages.ViewValuesReplaced, SWT.RIGHT, 1);
		viewScript = WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, new SelectAttributesListener());
	}

	private Map<String, Boolean> updateVisibleAttributes(boolean showDialog) throws Throwable {
		Map<String, Boolean> checked = null;
		selectionDialog.clearChecked();
		Map<String, String> selected = pTab.getRmConfig().getSelectedAttributeSet();
		selectionDialog.setCurrentlyVisible(selected);
		if (!showDialog || Window.OK == selectionDialog.open()) {
			checked = selectionDialog.getChecked();
			if (selected == null) {
				selected = new TreeMap<String, String>();
			} else {
				selected.clear();
			}

			Iterator<String> k = checked.keySet().iterator();
			if (k.hasNext()) {
				String key = k.next();
				if (checked.get(key)) {
					selected.put(key, key);
				} else {
					k.remove();
				}
			}
			while (k.hasNext()) {
				String key = k.next();
				if (checked.get(key)) {
					selected.put(key, key);
				} else {
					k.remove();
				}
			}
			pTab.getRmConfig().setSelectedAttributeSet(selected);
		}
		return checked;
	}
}
