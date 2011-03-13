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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
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

	private class SelectAttributesListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public synchronized void widgetSelected(SelectionEvent e) {
		}
	}

	private final IJAXBResourceManagerConfiguration rmConfig;
	private final TabController controller;
	private Composite control;
	private ScrolledComposite parent;
	private final String title;
	private Button selectAttributes;
	private SelectAttributesListener selectAttributesListener;

	/**
	 * @param dialog
	 */
	public JAXBRMConfigurableAttributesTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog,
			TabController controller) {
		super(dialog);
		rmConfig = rm.getJAXBRMConfiguration();
		this.controller = controller;
		String t = controller.getTitle();
		if (t == null) {
			t = Messages.DefaultDynamicTab_title;
		}
		this.title = t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);
		if (parent instanceof ScrolledComposite) {
			this.parent = (ScrolledComposite) parent;
		}
		/*
		 * fork off the rebuildable part from the first group which controls the
		 * rebuild
		 */
		if (controller.isDynamic()) {
			createSelectionGroup(control);
		}
		buildMain(control);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getImage
	 * ()
	 */
	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getText
	 * ()
	 */
	@Override
	public String getText() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * updateControls()
	 */
	@Override
	public void updateControls() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createDataSource()
	 */
	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new RMLaunchConfigurationDynamicTabDataSource(this) {

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
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createListener()
	 */
	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new RMLaunchConfigurationDynamicTabWidgetListener(this) {
		};
	}

	private void buildMain(Composite control2) {
		// TODO Auto-generated method stub

	}

	private void createSelectionGroup(Composite control2) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(1, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(1);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		selectAttributes = WidgetBuilderUtils.createPushButton(grp, Messages.SelectAttributesForDisplay,
				new SelectAttributesListener());
	}
}
