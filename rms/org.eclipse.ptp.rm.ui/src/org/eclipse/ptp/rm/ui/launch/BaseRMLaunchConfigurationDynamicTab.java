/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Albert L. Rossi - made this class extend AbstractRMLaunchConfigurationDynamicTab
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

/**
 * @since 2.0
 */
public abstract class BaseRMLaunchConfigurationDynamicTab extends AbstractRMLaunchConfigurationDynamicTab {
	private final RMLaunchConfigurationDynamicTabDataSource dataSource = createDataSource();

	private final RMLaunchConfigurationDynamicTabWidgetListener widgetListener = createListener();

	/**
	 * @since 2.0
	 */
	public BaseRMLaunchConfigurationDynamicTab(ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #canSave(org.eclipse.swt.widgets.Control)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation canSave(Control control) {
		if (dataSource.canSave()) {
			return new RMLaunchValidation(true, null);
		}
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	/**
	 * Get the image for the tab control
	 * 
	 * @return image
	 */
	public abstract Image getImage();

	/**
	 * Get the text for the tab control
	 * 
	 * @return text
	 */
	public abstract String getText();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation initializeFrom(Control control, ILaunchConfiguration configuration) {
		dataSource.setConfiguration(configuration);
		widgetListener.disable();
		dataSource.loadAndUpdate();
		widgetListener.enable();
		if (dataSource.getErrorMessage() == null) {
			return new RMLaunchValidation(true, null);
		}
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig) {
		if (dataSource.canAccept()) {
			return new RMLaunchValidation(true, null);
		}
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {
		dataSource.setConfigurationWorkingCopy(configuration);
		dataSource.storeAndValidate();
		if (dataSource.getErrorMessage() == null) {
			return new RMLaunchValidation(true, null);
		}
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	/**
	 * Update the tab's controls
	 */
	public abstract void updateControls();

	/**
	 * Create data source to handle tab content. The listener must extend {@link RMLaunchConfigurationDynamicTabDataSource} and add
	 * specific behavior for widgets of the the tab.
	 * 
	 * @return the listener
	 */
	protected abstract RMLaunchConfigurationDynamicTabDataSource createDataSource();

	/**
	 * Create listener for the tab. The listener must extend {@link RMLaunchConfigurationDynamicTabWidgetListener} and add specific
	 * behavior for widgets of the the tab.
	 * 
	 * @return the listener
	 */
	protected abstract RMLaunchConfigurationDynamicTabWidgetListener createListener();

	/*
	 * (non-Javadoc) Avoid API change.
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab #fireContentsChanged()
	 */
	@Override
	protected void fireContentsChanged() {
		super.fireContentsChanged();
	}

	/**
	 * @return
	 */
	protected RMLaunchConfigurationDynamicTabDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @return
	 */
	protected RMLaunchConfigurationDynamicTabWidgetListener getListener() {
		return widgetListener;
	}
}
