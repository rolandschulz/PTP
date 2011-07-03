/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModelEnabled;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ViewerUpdateModel;

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
	private final Map<Object, ICellEditorUpdateModel> cellEditorToModelMap;
	private final Map<Viewer, ViewerUpdateModel> viewerModelMap;
	private final IUpdateModelEnabled tab;

	/**
	 * @param tab
	 *            the parent controller tab
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBControllerLaunchConfigurationTab
	 */
	public ValueUpdateHandler(IUpdateModelEnabled tab) {
		controlToModelMap = new HashMap<Object, IUpdateModel>();
		cellEditorToModelMap = new HashMap<Object, ICellEditorUpdateModel>();
		viewerModelMap = new HashMap<Viewer, ViewerUpdateModel>();
		this.tab = tab;
	}

	/**
	 * Adds a widget-to-model mapping to the appropriate map.
	 * 
	 * @param control
	 *            widget or cell editor
	 * @param model
	 *            associate data model
	 */
	public void addUpdateModelEntry(Object control, IUpdateModel model) {
		if (model instanceof ViewerUpdateModel) {
			ViewerUpdateModel vum = (ViewerUpdateModel) model;
			viewerModelMap.put((Viewer) control, vum);
		} else if (model instanceof ICellEditorUpdateModel) {
			cellEditorToModelMap.put(control, (ICellEditorUpdateModel) model);
		} else {
			controlToModelMap.put(control, model);
		}
	}

	/**
	 * Empties the maps.
	 */
	public void clear() {
		cellEditorToModelMap.clear();
		controlToModelMap.clear();
		viewerModelMap.clear();
	}

	/**
	 * Broadcasts update request to all other controls by invoking refresh on
	 * their model objects.<br>
	 * <br>
	 * 
	 * The order follows this logic: update all cell editors first, then have
	 * the viewers write their template strings, then refresh the rest of the
	 * widgets.<br>
	 * <br>
	 * 
	 * In order to catch any references in table or tree cells to the template
	 * output of other viewers, the first two update sets are iterated once.
	 * 
	 * The value can largely be ignored.
	 * 
	 * @param source
	 *            the control which has been modified
	 * @param value
	 *            the new value (if any) produced (unused here)
	 */
	public void handleUpdate(Object source, Object value) {
		tab.relink();

		for (int i = 0; i < 2; i++) {
			for (Object control : cellEditorToModelMap.keySet()) {
				if (control == source) {
					continue;
				}
				ICellEditorUpdateModel m = cellEditorToModelMap.get(control);
				m.refreshValueFromMap();
			}

			for (ViewerUpdateModel vum : viewerModelMap.values()) {
				vum.storeValue();
			}
		}

		for (Object control : controlToModelMap.keySet()) {
			if (control == source) {
				continue;
			}
			IUpdateModel m = controlToModelMap.get(control);
			m.refreshValueFromMap();
		}

		for (Viewer viewer : viewerModelMap.keySet()) {
			viewer.refresh();
		}

		tab.fireContentsChanged();
	}
}
