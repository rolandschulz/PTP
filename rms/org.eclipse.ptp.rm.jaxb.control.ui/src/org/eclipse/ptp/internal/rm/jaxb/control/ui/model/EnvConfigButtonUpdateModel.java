/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - modified for {@link EnvManagerConfigButton} from {@link TextUpdateModel}
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.model;

import org.eclipse.ptp.ems.ui.EnvManagerConfigButton;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

/**
 * Update Model for {@link EnvManagerConfigButton} widgets.
 * 
 * @author arossi
 * @author Jeff Overbey
 */
public class EnvConfigButtonUpdateModel extends AbstractUpdateModel implements ModifyListener {

	private final EnvManagerConfigButton button;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param button
	 *            the widget to which this model corresponds
	 */
	public EnvConfigButtonUpdateModel(String name, IUpdateHandler handler, EnvManagerConfigButton button) {
		super(name, handler);
		this.button = button;
		button.addModifyListener(this);
	}

	@Override
	public Object getControl() {
		return button;
	}

	/*
	 * @return String value of the selection (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return button.getConfiguration();
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
	 * Sets the value on the text, either by resolving the arguments for read-only, or by retrieving the value. Turns on the
	 * refreshing flag so as not to trigger further updates from the listener. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.getValue(name);
		if (JAXBUIConstants.ZEROSTR.equals(mapValue)) {
			mapValue = null;
		}
		String s = JAXBControlUIConstants.ZEROSTR;
		if (mapValue != null) {
			s = (String) mapValue;
		}
		button.setConfiguration(s);
		refreshing = false;
	}
}
