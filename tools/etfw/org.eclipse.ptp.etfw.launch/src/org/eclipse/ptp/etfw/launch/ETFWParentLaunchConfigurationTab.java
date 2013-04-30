/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.launch;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.etfw.launch.ui.util.ETFWToolTabBuilder;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Extends JAXBControllerLaunchConfigurationTab with specific changes for ETFw. The LC Map needs to be initialized from the
 * workflow's variable map, not the resource managers.
 * 
 * @author Chris Navarro
 * 
 */
public class ETFWParentLaunchConfigurationTab extends JAXBControllerLaunchConfigurationTab {
	private final IVariableMap variableMap;
	private List<IJAXBLaunchConfigurationTab> tabControllers;

	public ETFWParentLaunchConfigurationTab(ILaunchController control, IProgressMonitor monitor, IVariableMap variableMap)
			throws Throwable {
		super(control, monitor);
		this.variableMap = variableMap;
	}

	@Override
	public void createControl(Composite parent, String id) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		tabFolder = new TabFolder(control, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ETFWToolTabBuilder.initialize();
		for (IJAXBLaunchConfigurationTab tabControl : tabControllers) {
			ETFWToolTabBuilder builder = new ETFWToolTabBuilder(tabControl, this.variableMap);

			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			Control control = null;
			final ScrolledComposite scroller = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
			try {
				control = builder.build(scroller);
				((Composite) control).layout();

				tabItem.setText(tabControl.getController().getTitle());
				String tooltip = tabControl.getController().getTooltip();
				if (tooltip != null) {
					tabItem.setToolTipText(tooltip);
					scroller.setToolTipText(tooltip);
				}

				scroller.setContent(control);
				scroller.setExpandHorizontal(true);
				scroller.setExpandVertical(true);
				scroller.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));

				tabItem.setControl(scroller);
			} catch (Throwable t) {
				t.printStackTrace();
			}

		}
		this.controlId = id;
		control.layout(true, true);
	}

	@Override
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {
		try {
			// This lets us differentiate keys from the old ETFW so they can work from one Profiling Tab
			getVariableMap().initialize(variableMap, getJobControl().getControlId());
			getUpdateHandler().clear();
			getVariableMap().updateFromConfiguration(configuration);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	public void addDynamicContent(List<IJAXBLaunchConfigurationTab> tabs) {
		this.tabControllers = tabs;
	}

	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {
		RMLaunchValidation validation = super.performApply(configuration);

		// TODO the above performApply should update the attribute map, but it doesn't right now so it is handled below
		Iterator<String> iterator = getVariableMap().getAttributes().keySet().iterator();
		while (iterator.hasNext()) {
			String attribute = iterator.next();
			String name = attribute;
			if (name.startsWith(controlId)) {
				name = name.substring(controlId.length() + 1, attribute.length());

				// Check to see if the variable is part of ETFw
				AttributeType temp = variableMap.getAttributes().get(name);
				if (temp != null) {
					if (isWidgetEnabled(temp.getName()) && temp.isVisible()) {
						String attType = temp.getType();

						// If boolean is translated to a string, insert the string into the launch configuration
						String translateBoolean = variableMap.getAttributes().get(name).getTranslateBooleanAs();
						Object value = getVariableMap().getValue(name);
						if (attType.equals("boolean")) { //$NON-NLS-1$
							if (translateBoolean != null) {
								configuration.setAttribute(attribute, value.toString());
							} else {
								boolean val = new Boolean(value.toString());
								configuration.setAttribute(attribute, val);
							}
						} else if (attType.equals("string")) { //$NON-NLS-1$
							configuration.setAttribute(attribute, value.toString());
						} else if (attType.equals("integer")) { //$NON-NLS-1$
							if (value.toString().length() > 0) {
								int val = new Integer(value.toString());
								configuration.setAttribute(attribute, val);
							} else {
								configuration.setAttribute(attribute, value.toString());
							}
						} else {
							configuration.setAttribute(attribute, value.toString());
						}
					}
				}
			}
		}

		return validation;
	}

	/**
	 * Determines if the UI widget is enabled and should be included in the launch configuration. It prevents attributes that are
	 * not enabled from getting included in the launch configuration
	 * 
	 * @param attributeName
	 *            Name of the attribute associated with the widget
	 * @return enabled state of widget
	 */
	public boolean isWidgetEnabled(String attributeName) {
		for (IJAXBLaunchConfigurationTab tabControl : tabControllers) {
			for (IUpdateModel m : tabControl.getLocalWidgets().values()) {
				if (m.getName() != null) {
					if (m.getName().equals(attributeName)) {
						return ((Control) m.getControl()).isEnabled();
					}
				} else {
					// Do nothing, the model has no attribute associated with it
				}
			}
		}
		// Handles the case where attributes are not associated with UI models
		return true;
	}

	@Override
	public void relink() {
		// Some of the jaxb classes need to be re-worked for this to be implemented because their are target configuration specifics
		// in this call hierarchy
	}

}
