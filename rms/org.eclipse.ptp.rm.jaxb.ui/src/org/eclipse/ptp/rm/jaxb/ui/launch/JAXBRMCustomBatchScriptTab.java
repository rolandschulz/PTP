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
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
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
import org.eclipse.swt.widgets.Text;

/**
 * @author arossi
 * 
 */
public class JAXBRMCustomBatchScriptTab extends BaseRMLaunchConfigurationDynamicTab implements IJAXBUINonNLSConstants {

	private class WidgetListener implements SelectionListener {
		private boolean disabled = false;

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public synchronized void widgetSelected(SelectionEvent e) {
			if (disabled) {
				return;
			}
		}

		private synchronized void disable() {
			disabled = true;
		}

		private synchronized void enable() {
			disabled = false;
		}
	}

	private final IJAXBResourceManagerConfiguration rmConfig;

	private Button okButton;
	private boolean readOnly;
	private Text choice;
	private Text editor;
	private Button browseHomeButton;
	private Button browseProjectButton;
	private Button loadScript;
	private Button saveToFile;
	private Button clear;
	private final String title;
	private String value;
	private Composite control;
	private String selected;
	private final WidgetListener listener;

	/**
	 * @param dialog
	 */
	public JAXBRMCustomBatchScriptTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog, String title) {
		super(dialog);
		rmConfig = rm.getJAXBRMConfiguration();
		listener = new WidgetListener();
		if (title == null) {
			title = Messages.CustomBatchScriptTab_title;
		}
		this.title = title;
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
		GridLayout layout = WidgetBuilderUtils.createGridLayout(6, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(6);
		Composite comp = WidgetBuilderUtils.createComposite(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(comp, Messages.BatchScriptPath, SWT.LEFT, 1);
		GridData gdsub = WidgetBuilderUtils.createGridDataFillH(3);
		choice = WidgetBuilderUtils.createText(comp, SWT.BORDER, gdsub, true, selected);
		browseHomeButton = WidgetBuilderUtils.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_1, listener);
		browseProjectButton = WidgetBuilderUtils
				.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_2, listener);

		layout = WidgetBuilderUtils.createGridLayout(1, true);
		gd = WidgetBuilderUtils.createGridDataFill(DEFAULT, DEFAULT, 1);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		int style = SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL;
		gdsub = WidgetBuilderUtils.createGridDataFill(DEFAULT, DEFAULT, 1);
		editor = WidgetBuilderUtils.createText(grp, style, gdsub, false, ZEROSTR);
		WidgetBuilderUtils.applyMonospace(editor);

		layout = WidgetBuilderUtils.createGridLayout(6, true);
		gd = WidgetBuilderUtils.createGridDataFillH(6);
		comp = WidgetBuilderUtils.createComposite(control, SWT.NONE, layout, gd);
		loadScript = WidgetBuilderUtils.createPushButton(comp, Messages.LoadScript, listener);
		saveToFile = WidgetBuilderUtils.createPushButton(comp, Messages.SaveToFileButton, listener);
		clear = WidgetBuilderUtils.createPushButton(comp, Messages.ClearScript, listener);
		selected = ZEROSTR;
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
		return new RMLaunchValidation(true, null);
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
}
