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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.LaunchTabBuilder;
import org.eclipse.ptp.rm.jaxb.ui.listener.JAXBRMLaunchTabWidgetListener;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author arossi
 * 
 */
public class JAXBRMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab implements
		IJAXBUINonNLSConstants {

	private final IJAXBResourceManagerConfiguration rmConfig;
	private final ILaunchConfigurationDialog dialog;
	private final LaunchTabBuilder launchTabData;
	private final JAXBRMLaunchTabWidgetListener listener;
	private final Map<Object, Object> widgetToValueIndex;

	private Control control;

	/**
	 * @param dialog
	 */
	public JAXBRMLaunchConfigurationDynamicTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
		rmConfig = rm.getJAXBRMConfiguration();
		this.dialog = dialog;
		this.launchTabData = new LaunchTabBuilder(this);
		this.listener = new JAXBRMLaunchTabWidgetListener(this);
		this.widgetToValueIndex = new HashMap<Object, Object>();
		addDynamicTab(new JAXBRMCustomBatchScriptTab(rm, dialog));
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
	@Override
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		super.createControl(parent, rm, queue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getControl()
	 */
	@Override
	public Control getControl() {
		return control;
	}

	public ILaunchConfigurationDialog getDialog() {
		return dialog;
	}

	public LaunchTabBuilder getLaunchTabData() {
		return launchTabData;
	}

	public JAXBRMLaunchTabWidgetListener getListener() {
		return listener;
	}

	public IJAXBResourceManagerConfiguration getRmConfig() {
		return rmConfig;
	}

	public Map<Object, Object> getWidgetToValueIndex() {
		return widgetToValueIndex;
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
	@Override
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
}
