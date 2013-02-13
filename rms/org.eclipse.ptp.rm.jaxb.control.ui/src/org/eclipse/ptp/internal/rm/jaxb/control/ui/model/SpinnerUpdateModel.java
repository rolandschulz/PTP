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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Spinner;

/**
 * Update Model for Spinner widgets.
 * 
 * @author arossi
 * 
 */
public class SpinnerUpdateModel extends AbstractUpdateModel implements ModifyListener {

	private final Spinner spinner;

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param spinner
	 *            the widget to which this model corresponds
	 */
	public SpinnerUpdateModel(String name, IUpdateHandler handler, Spinner spinner) {
		super(name, handler);
		this.spinner = spinner;
		spinner.addModifyListener(this);
	}

	@Override
	public Object getControl() {
		return spinner;
	}

	/*
	 * @return Integer value of the selection (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return spinner.getSelection();
	}

	/*
	 * Model serves as widget modify listener; calls {@link #storeValue()} (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events .ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		if (refreshing) {
			return;
		}
		try {
			Object value = storeValue();
			handleUpdate(value);
		} catch (Exception ignored) {
		}
	}

	/*
	 * Sets the value on the spinner after translating from the current environment. If <code>null</code>, resets spinner to minimum
	 * value. Turns on the refreshing flag so as not to trigger further updates from the listener. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.getValue(name);
		if (JAXBUIConstants.ZEROSTR.equals(mapValue)) {
			mapValue = null;
		}
		int i = spinner.getMinimum();
		if (mapValue != null) {
			if (mapValue instanceof String) {
				try {
					i = Integer.parseInt((String) mapValue);
				} catch (NumberFormatException e) {
					i = spinner.getMinimum();
				}
			} else {
				i = (Integer) mapValue;
			}
		}
		spinner.setSelection(i);
		refreshing = false;
	}
}
