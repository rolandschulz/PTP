/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModelEnabled;

/**
 * When a widget or cell editor commits a change, this handler is called to
 * refresh all registered widget values from the underlying map. The handler
 * then signals an update on the parent tab.
 * 
 * @author arossi
 * 
 */
public class ValueUpdateHandler implements IUpdateHandler {

	private final Map<Object, IUpdateModel> controlToModelMap;
	private final Map<Object, ICellEditorUpdateModel> cellEditorToModelMap;
	private final Map<Viewer, IUpdateModel> viewerModelMap;
	private final TreeMap<String, String> errors;
	private final IUpdateModelEnabled tab;

	/**
	 * @param tab
	 *            the parent controller tab
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.launch.JAXBControllerLaunchConfigurationTab
	 */
	public ValueUpdateHandler(IUpdateModelEnabled tab) {
		controlToModelMap = new HashMap<Object, IUpdateModel>();
		cellEditorToModelMap = new HashMap<Object, ICellEditorUpdateModel>();
		viewerModelMap = new HashMap<Viewer, IUpdateModel>();
		errors = new TreeMap<String, String>();
		this.tab = tab;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateHandler#addError(java.lang.String, java.lang.String)
	 */
	public void addError(String source, String error) {
		errors.put(source, error);
		tab.fireContentsChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateHandler#addUpdateModelEntry(java.lang.Object,
	 * org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateModel)
	 */
	public void addUpdateModelEntry(Object control, IUpdateModel model) {
		if (model instanceof ViewerUpdateModel) {
			viewerModelMap.put((Viewer) control, model);
		} else if (model instanceof ICellEditorUpdateModel) {
			cellEditorToModelMap.put(control, (ICellEditorUpdateModel) model);
		} else {
			controlToModelMap.put(control, model);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateHandler#clear()
	 */
	public void clear() {
		cellEditorToModelMap.clear();
		controlToModelMap.clear();
		viewerModelMap.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateHandler#getFirstError()
	 */
	public String getFirstError() {
		if (!errors.isEmpty()) {
			String name = errors.firstKey();
			if (name != null) {
				return name + JAXBUIConstants.CO + JAXBUIConstants.SP + errors.get(name);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateHandler#handleUpdate(java.lang.Object, java.lang.Object)
	 */
	public void handleUpdate(Object source, Object value) {
		if (!errors.isEmpty()) {
			return;
		}

		tab.relink();

		for (int i = 0; i < 2; i++) {
			for (Object control : cellEditorToModelMap.keySet()) {
				if (control == source) {
					continue;
				}
				ICellEditorUpdateModel m = cellEditorToModelMap.get(control);
				m.refreshValueFromMap();
			}

			for (IUpdateModel vum : viewerModelMap.values()) {
				try {
					vum.storeValue();
				} catch (Exception e) {
					// Ignore exception
				}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateHandler#removeError(java.lang.String)
	 */
	public void removeError(String source) {
		errors.remove(source);
		if (!errors.isEmpty()) {
			tab.fireContentsChanged();
		}
	}
}
