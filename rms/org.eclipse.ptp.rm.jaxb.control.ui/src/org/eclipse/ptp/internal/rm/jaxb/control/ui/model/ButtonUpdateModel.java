/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.model;

import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

/**
 * Update Model for Button widgets.
 * 
 * @author arossi
 * 
 */
public class ButtonUpdateModel extends AbstractUpdateModel implements SelectionListener {

	private final Button button;

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param button
	 *            the widget to which this model corresponds
	 * @param translateBooleanAs
	 *            a comma-delimite pair of strings corresponding to T,F to use as the value instead of the boolean (can be
	 *            <code>null</code>)
	 */
	public ButtonUpdateModel(String name, IUpdateHandler handler, Button button, String translateBooleanAs) {
		super(name, handler);
		this.button = button;
		button.addSelectionListener(this);
		setBooleanToString(translateBooleanAs);
	}

	@Override
	public Object getControl() {
		return button;
	}

	/*
	 * @return value of the selection; boolean or string (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return getBooleanValue(button.getSelection());
	}

	/*
	 * Sets the selection on the button after translating the value from the current environment. Turns on the refreshing flag so as
	 * not to trigger further updates from the listener. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.getValue(name);
		boolean b = false;
		if (JAXBUIConstants.ZEROSTR.equals(mapValue)) {
			mapValue = null;
		}
		b = maybeGetBooleanFromString(mapValue);
		button.setSelection(b);
		refreshing = false;
	}

	/*
	 * Model serves as widget selection listener: calls {@link #storeValue()} (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Model serves as widget selection listener: calls {@link #storeValue()} (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
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

}
