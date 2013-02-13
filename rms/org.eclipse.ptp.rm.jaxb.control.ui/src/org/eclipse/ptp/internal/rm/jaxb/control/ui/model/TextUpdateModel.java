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

import java.util.List;

import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

/**
 * Update Model for Text widgets.
 * 
 * @author arossi
 * 
 */
public class TextUpdateModel extends DynamicControlUpdateModel implements ModifyListener {

	private final Text text;

	/**
	 * Read-only dynamic text.
	 * 
	 * @param args
	 *            to be resolved in refreshed environment and used as the text
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param text
	 *            the widget to which this model corresponds
	 */
	public TextUpdateModel(List<ArgType> args, IUpdateHandler handler, Text text) {
		super(args, handler);
		this.text = text;
	}

	/**
	 * Default (editable) text.
	 * 
	 * @param name
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param text
	 *            the widget to which this model corresponds
	 */
	public TextUpdateModel(String name, IUpdateHandler handler, Text text) {
		super(name, handler);
		this.text = text;
		text.addModifyListener(this);
	}

	@Override
	public Object getControl() {
		return text;
	}

	/*
	 * @return String value of the selection (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return text.getText();
	}

	/*
	 * Model serves as widget modify listener; uses the ValidateJob to delay processing of text. Sets refreshing flag to block
	 * further updates being triggered during the refresh. (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events .ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		if (refreshing) {
			return;
		}
		validateJob.cancel();
		validateJob.schedule(JAXBControlUIConstants.VALIDATE_TIMER);
	}

	/*
	 * Sets the value on the text, either by resolving the arguments for read-only, or by retrieving the value. Turns on the
	 * refreshing flag so as not to trigger further updates from the listener. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		if (!canSave) {
			mapValue = getResolvedDynamic();
		} else {
			mapValue = lcMap.getValue(name);
		}
		String s = JAXBControlUIConstants.ZEROSTR;
		if (mapValue != null) {
			s = mapValue.toString();
		}
		text.setText(s);
		refreshing = false;
	}
}
