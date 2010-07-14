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
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

public abstract class BaseRMLaunchConfigurationDynamicTab extends AbstractRMLaunchConfigurationDynamicTab {
	private final RMLaunchConfigurationDynamicTabDataSource dataSource = createDataSource();
	private final RMLaunchConfigurationDynamicTabWidgetListener widgetListener = createListener();

	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		if (dataSource.canSave())
			return new RMLaunchValidation(true, null);
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	public abstract Image getImage();

	public abstract String getText();

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		dataSource.setResourceManager(rm);
		dataSource.setQueue(queue);
		dataSource.setConfiguration(configuration);
		widgetListener.disable();
		dataSource.loadAndUpdate();
		widgetListener.enable();
		if (dataSource.getErrorMessage() == null)
			return new RMLaunchValidation(true, null);
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		if (dataSource.canAccept())
			return new RMLaunchValidation(true, null);
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		dataSource.setResourceManager(rm);
		dataSource.setQueue(queue);
		dataSource.setConfigurationWorkingCopy(configuration);
		dataSource.storeAndValidate();
		if (dataSource.getErrorMessage() == null)
			return new RMLaunchValidation(true, null);
		return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	abstract public void updateControls();

	/**
	 * Create data source to handle tab content. The listener must extend
	 * {@link RMLaunchConfigurationDynamicTabDataSource} and add specific
	 * behavior for widgets of the the tab.
	 * 
	 * @return the listener
	 */
	protected abstract RMLaunchConfigurationDynamicTabDataSource createDataSource();

	/**
	 * Create listener for the tab. The listener must extend
	 * {@link RMLaunchConfigurationDynamicTabWidgetListener} and add specific
	 * behavior for widgets of the the tab.
	 * 
	 * @return the listener
	 */
	protected abstract RMLaunchConfigurationDynamicTabWidgetListener createListener();

	/*
	 * (non-Javadoc) Avoid API change.
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab
	 * #fireContentsChanged()
	 */
	@Override
	protected void fireContentsChanged() {
		super.fireContentsChanged();
	}

	protected RMLaunchConfigurationDynamicTabDataSource getDataSource() {
		return dataSource;
	}

	protected RMLaunchConfigurationDynamicTabWidgetListener getListener() {
		return widgetListener;
	}
}
