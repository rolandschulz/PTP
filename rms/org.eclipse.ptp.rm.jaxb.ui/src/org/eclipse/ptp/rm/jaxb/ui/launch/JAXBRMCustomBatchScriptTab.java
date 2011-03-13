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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * @author arossi
 * 
 */
public class JAXBRMCustomBatchScriptTab extends BaseRMLaunchConfigurationDynamicTab implements IJAXBUINonNLSConstants {

	private final IJAXBResourceManagerConfiguration rmConfig;

	protected Button okButton;

	protected boolean readOnly;
	protected Text scrollable;
	protected String title;
	protected String value;
	protected FontMetrics fFontMetrics;
	protected Composite control;

	/**
	 * @param dialog
	 */
	public JAXBRMCustomBatchScriptTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog) {
		super(dialog);

		rmConfig = rm.getJAXBRMConfiguration();

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
		control = new Composite(parent, SWT.NONE);
		initializeDialogUnits(control);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 550;
		data.widthHint = Dialog.convertWidthInCharsToPixels(fFontMetrics, 160);
		scrollable = new Text(control, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		scrollable.setLayoutData(data);
		scrollable.setEditable(true);
		scrollable.setVisible(true);
		Display d = Display.getCurrent();
		// three fonts for Mac, Linux, Windows ...
		FontData[][] f = { d.getFontList(COURIER, true), d.getFontList(COURIER, false), d.getFontList(COURIER, true),
				d.getFontList(COURIER, false), d.getFontList(COURIER, true), d.getFontList(COURIER, false) };
		int i = 0;
		for (; i < f.length; i++) {
			if (f[i].length > 0) {
				scrollable.setFont(new Font(d, f[i]));
				break;
			}
		}
		if (i == f.length) {
			Dialog.applyDialogFont(scrollable);
		}
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
		return Messages.CustomBatchScriptTab_title;
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

	private void initializeDialogUnits(Control control) {
		// Compute and store a font metric
		GC gc = new GC(control);
		gc.setFont(JFaceResources.getDialogFont());
		fFontMetrics = gc.getFontMetrics();
		gc.dispose();
	}

}
