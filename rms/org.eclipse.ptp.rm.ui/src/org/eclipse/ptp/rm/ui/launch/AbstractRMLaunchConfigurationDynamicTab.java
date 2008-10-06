/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.launch;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractRMLaunchConfigurationDynamicTab implements
IRMLaunchConfigurationDynamicTab {

	private final ListenerList listenerList = new ListenerList();

	private final RMLaunchConfigurationDynamicTabDataSource dataSource = createDataSource();
	private final RMLaunchConfigurationDynamicTabWidgetListener widgetListener = createListener();

	/**
	 * Create listener for the tab. The listener must extend {@link RMLaunchConfigurationDynamicTabWidgetListener} and add
	 * specific behavior for widgets of the the tab.
	 * @return the listener
	 */
	protected abstract RMLaunchConfigurationDynamicTabWidgetListener createListener();

	/**
	 * Create data source to handle tab content. The listener must extend {@link RMLaunchConfigurationDynamicTabDataSource} and add
	 * specific behavior for widgets of the the tab.
	 * @return the listener
	 */
	protected abstract RMLaunchConfigurationDynamicTabDataSource createDataSource();

	protected RMLaunchConfigurationDynamicTabWidgetListener getListener() {
		return widgetListener;
	}

	protected RMLaunchConfigurationDynamicTabDataSource getDataSource() {
		return dataSource;
	}

	public void addContentsChangedListener(IRMLaunchConfigurationContentsChangedListener listener) {
		listenerList.add(listener);
	}

	public void removeContentsChangedListener(IRMLaunchConfigurationContentsChangedListener listener) {
		listenerList.remove(listener);
	}

	/**
	 * This should be called when GUI elements are modified by the user,
	 * e.g. a Text widget should have its ModifyListener's
	 * modifyText method set up to notify all of the contents
	 * changed listeners.
	 */
	protected void fireContentsChanged() {
		Object[] listeners = listenerList.getListeners();
		for (Object listener : listeners) {
			((IRMLaunchConfigurationContentsChangedListener) listener).handleContentsChanged(this);
		}
	}

	public RMLaunchValidation canSave(Control control,
			IResourceManager rm, IPQueue queue) {
		if (dataSource.canSave())
			return new RMLaunchValidation(true, null);
		else
			return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig,
			IResourceManager rm, IPQueue queue) {
		if (dataSource.canAccept())
			return new RMLaunchValidation(true, null);
		else
			return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	public RMLaunchValidation initializeFrom(Control control,
			IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) {
		dataSource.setResourceManager(rm);
		dataSource.setQueue(queue);
		dataSource.setConfiguration(configuration);
		widgetListener.disable();
		dataSource.loadAndUpdate();
		widgetListener.enable();
		if (dataSource.getErrorMessage() == null)
			return new RMLaunchValidation(true, null);
		else
			return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	public RMLaunchValidation performApply(
			ILaunchConfigurationWorkingCopy configuration, IResourceManager rm,
			IPQueue queue) {
		dataSource.setResourceManager(rm);
		dataSource.setQueue(queue);
		dataSource.setConfigurationWorkingCopy(configuration);
		dataSource.storeAndValidate();
		if (dataSource.getErrorMessage() == null)
			return new RMLaunchValidation(true, null);
		else
			return new RMLaunchValidation(false, dataSource.getErrorMessage());
	}

	//	public RMLaunchValidation setDefaults(
	//			ILaunchConfigurationWorkingCopy configuration, IResourceManager rm,
	//			IPQueue queue) {
	//		dataSource.setResourceManager(rm);
	//		dataSource.setQueue(queue);
	//		dataSource.setConfigurationWorkingCopy(configuration);
	//		dataSource.storeDefaults();
	//		if (dataSource.getErrorMessage() == null) {
	//			return new RMLaunchValidation(true, null);
	//		} else {
	//			return new RMLaunchValidation(false, dataSource.getErrorMessage());
	//		}
	//	}

	public abstract Image getImage();

	public abstract String getText();

	abstract public void updateControls();
}
