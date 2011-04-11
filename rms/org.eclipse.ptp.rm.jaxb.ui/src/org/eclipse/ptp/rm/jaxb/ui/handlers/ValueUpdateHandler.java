/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.ui.IFireContentsChangedEnabled;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;

/**
 * When a widget or cell editor commits a change, this handler is called to
 * refresh all registered widget values from the underlying map. The handler
 * then signals an update on the parent tab.
 * 
 * @author arossi
 * 
 */
public class ValueUpdateHandler {

	private final Map<Object, IUpdateModel> controlToModelMap;
	private final IFireContentsChangedEnabled tab;

	/**
	 * @param tab
	 *            the parent controller tab
	 * @see org.eclipse.ptp.rm.jaxb.ui.launch.JAXBControllerLaunchConfigurationTab
	 */
	public ValueUpdateHandler(IFireContentsChangedEnabled tab) {
		controlToModelMap = new HashMap<Object, IUpdateModel>();
		this.tab = tab;
	}

	/**
	 * Adds a widget-to-model mapping to the registered controls map.
	 * 
	 * @param control
	 *            widget or cell editor
	 * @param model
	 *            associate data model
	 */
	public void addUpdateModelEntry(Object control, IUpdateModel model) {
		controlToModelMap.put(control, model);
	}

	/**
	 * Empties the registered controls map.
	 */
	public void clear() {
		controlToModelMap.clear();
	}

	/**
	 * @return the registered controls map.
	 */
	public Map<Object, IUpdateModel> getControlToModelMap() {
		return controlToModelMap;
	}

	/**
	 * Broadcasts update request to all other controls by invoking refresh on
	 * their model objects.
	 * 
	 * The value can largely be ignored.
	 * 
	 * @param source
	 *            the control which has been modified
	 * @param value
	 *            the new value (if any) produced (unused here)
	 */
	public void handleUpdate(Object source, Object value) {
		for (Object control : controlToModelMap.keySet()) {
			if (control == source) {
				continue;
			}
			controlToModelMap.get(control).refreshValueFromMap();
		}

		for (Object control : controlToModelMap.keySet()) {
			if (control instanceof Viewer) {
				WidgetActionUtils.refreshViewer((Viewer) control);
			}
		}

		/*
		 * notify the parent tab about the update
		 */
		tab.fireContentsChanged();
	}
}
