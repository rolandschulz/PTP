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

public class ValueUpdateHandler {

	private final Map<Object, IUpdateModel> controlToModelMap;
	private final IFireContentsChangedEnabled tab;

	public ValueUpdateHandler(IFireContentsChangedEnabled tab) {
		controlToModelMap = new HashMap<Object, IUpdateModel>();
		this.tab = tab;
	}

	public void addUpdateModelEntry(Object control, IUpdateModel model) {
		controlToModelMap.put(control, model);
	}

	public void clear() {
		controlToModelMap.clear();
	}

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
