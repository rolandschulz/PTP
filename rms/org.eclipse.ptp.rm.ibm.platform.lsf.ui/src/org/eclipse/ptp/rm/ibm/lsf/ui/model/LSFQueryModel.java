// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui.model;

import org.eclipse.ptp.rm.ibm.lsf.ui.widgets.LSFQueryControl;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;

public class LSFQueryModel extends AbstractUpdateModel implements ModifyListener {

	private final LSFQueryControl control;
	private Object initialValue;

	/**
	 * Create the model for the application list widget. This model holds the
	 * name of the application selected by the user.
	 * 
	 * @param name
	 *            name of the model, which will correspond to the name of an
	 *            attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param control
	 *            the widget to which this model corresponds
	 */
	public LSFQueryModel(String name, IUpdateHandler handler, Control uiControl) {
		super(name, handler);
		control = (LSFQueryControl) uiControl;
		control.addModifyListener(this);
	}

	@Override
	/**
	 * Get the control associated with this model instance
	 * 
	 * @return the control associated with this model
	 */
	public Object getControl() {
		return control;
	}

	@Override
	/**
	 * Get the value selected by the user
	 * 
	 * @return Selected object's application name
	 */
	public Object getValueFromControl() {
		Object value;

		if (control.widgetSelected()) {
			value = control.getSelectedValue();
		} else {
			value = initialValue;
		}
		if ((value != null) && (!value.equals(""))) { //$NON-NLS-1$
			lcMap.putValue(name, value);
		}
		return value;
	}

	@Override
	/**
	 * Update model with name of application selected by the user
	 * 
	 * @param e: The notification event
	 */
	public void modifyText(ModifyEvent e) {
		if (refreshing) {
			return;
		}
		try {
			Object value = storeValue();
			handleUpdate(value);
		} catch (Exception ignored) {
			// Ignore
		}
	}

	@Override
	/**
	 * Update the widget based on a change in the attribute value represented by the widget
	 * The widget is read-only so this method does nothing.
	 */
	public void refreshValueFromMap() {
		mapValue = lcMap.getValue(name);
		if (mapValue == null) {
			mapValue = ""; //$NON-NLS-1$
			if (initialValue == null) {
				initialValue = ""; //$NON-NLS-1$
			}
		} else {
			initialValue = mapValue;
		}
		control.setSelectedValues((String) mapValue);
	}

}
