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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.data.LaunchTabBuilder;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.AttributeChoiceDialog;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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

	private class JAXBDataSource extends RMLaunchConfigurationDynamicTabDataSource {

		protected JAXBDataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			// TODO Auto-generated method stub

		}

		@Override
		protected void copyToFields() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void copyToStorage() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void loadDefault() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void loadFromStorage() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void validateLocal() throws ValidationException {
			// TODO Auto-generated method stub

		}
	}

	private class SelectAttributesListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public synchronized void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == selectAttributes) {
				buildMain(updateVisibleAttributes(true));
			} else if (source == viewScript) {

			}
		}
	}

	private class UniversalWidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {
		public UniversalWidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}
	}

	private final IJAXBResourceManagerConfiguration rmConfig;
	private final TabController controller;
	private final Map<Control, String> valueWidgets;
	private final boolean hasScript;

	private AttributeChoiceDialog selectionDialog;

	private ScrolledComposite parent;
	private Composite dynamicControl;
	private Composite control;
	private final String title;
	private Button selectAttributes;
	private Button viewScript;

	private UniversalWidgetListener universalListener;
	private JAXBDataSource dataSource;

	public JAXBRMConfigurableAttributesTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog,
			TabController controller, boolean hasScript) {
		super(dialog);
		rmConfig = rm.getJAXBRMConfiguration();
		this.controller = controller;
		this.hasScript = hasScript;
		String t = controller.getTitle();
		if (t == null) {
			t = Messages.DefaultDynamicTab_title;
		}
		this.title = t;
		valueWidgets = new HashMap<Control, String>();
		createListener();
		createDataSource();
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);
		if (parent instanceof ScrolledComposite) {
			this.parent = (ScrolledComposite) parent;
		}
		selectionDialog = new AttributeChoiceDialog(parent.getShell());

		if (controller.isDynamic()) {
			createDynamicSelectionGroup(control);
		} else if (hasScript) {
			createViewScriptGroup(control);
		}
		buildMain(updateVisibleAttributes(false));
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateControls() {
		dataSource.loadFromStorage();
		dataSource.copyToFields();
	}

	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		if (dataSource == null) {
			dataSource = new JAXBDataSource(this);
		}
		return dataSource;
	}

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		if (universalListener == null) {
			universalListener = new UniversalWidgetListener(this);
		}
		return universalListener;
	}

	private void buildMain(Map<String, Boolean> checked) {
		universalListener.disable();

		if (dynamicControl != null) {
			dynamicControl.dispose();
			valueWidgets.clear();
		}
		if (control.isDisposed()) {
			return;
		}
		dynamicControl = WidgetBuilderUtils.createComposite(control, 1);
		LaunchTabBuilder builder = new LaunchTabBuilder(controller, valueWidgets, checked);
		try {
			builder.build(dynamicControl);
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
		}
		/*
		 * We need to repeat this here (the ResourcesTab does it when it
		 * initially builds the control).
		 */
		if (parent != null) {
			parent.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}

		updateControls();

		universalListener.enable();
	}

	private void createDynamicSelectionGroup(Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(4, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(4);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(grp, Messages.ConfigureLaunchSettings, SWT.LEFT, 1);
		selectAttributes = WidgetBuilderUtils.createPushButton(grp, Messages.SelectAttributesForDisplay,
				new SelectAttributesListener());
		if (hasScript) {
			WidgetBuilderUtils.createLabel(grp, Messages.ViewValuesReplaced, SWT.LEFT, 1);
			viewScript = WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, new SelectAttributesListener());
		}
	}

	private void createViewScriptGroup(Composite control2) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(2, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(2);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(grp, Messages.ViewValuesReplaced, SWT.LEFT, 1);
		viewScript = WidgetBuilderUtils.createPushButton(grp, Messages.ViewScript, new SelectAttributesListener());
	}

	private Map<String, Boolean> updateVisibleAttributes(boolean showDialog) {
		Map<String, Boolean> checked = null;
		rmConfig.setActive();
		selectionDialog.clearChecked();
		selectionDialog.setCurrentlyVisible(rmConfig.getSelectedAttributeSet());
		if (!showDialog || Window.OK == selectionDialog.open()) {
			checked = selectionDialog.getChecked();
			StringBuffer sb = new StringBuffer();
			Iterator<String> k = checked.keySet().iterator();
			if (k.hasNext()) {
				String key = k.next();
				if (checked.get(key)) {
					sb.append(key);
				} else {
					k.remove();
				}
			}
			while (k.hasNext()) {
				String key = k.next();
				if (checked.get(key)) {
					sb.append(CM).append(key);
				} else {
					k.remove();
				}
			}
			rmConfig.setSelectedAttributeSet(sb.toString());
		}
		return checked;
	}
}
