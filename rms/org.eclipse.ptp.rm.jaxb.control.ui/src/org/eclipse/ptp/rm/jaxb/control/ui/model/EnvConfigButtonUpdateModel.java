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
package org.eclipse.ptp.rm.jaxb.control.ui.model;

import java.util.List;

import org.eclipse.ptp.ems.ui.EnvManagerConfigButton;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;

/**
 * Update Model for {@link EnvManagerConfigButton} widgets.
 * 
 * @author arossi
 * @author Jeff Overbey
 */
public class EnvConfigButtonUpdateModel extends DynamicControlUpdateModel {

	private final EnvManagerConfigButton button;

	/**
	 * Read-only dynamic text.
	 * 
	 * @param args
	 *            to be resolved in refreshed environment and used as the text
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param text
	 *            the widget to which this model corresponds
	 */
	public EnvConfigButtonUpdateModel(List<ArgType> args, ValueUpdateHandler handler, EnvManagerConfigButton button) {
		super(args, handler);
		this.button = button;
	}

	/**
	 * Default (editable) text.
	 * 
	 * @param name
	 *            name of the model, which will correspond to the name of a
	 *            Property or Attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param button
	 *            the widget to which this model corresponds
	 */
	public EnvConfigButtonUpdateModel(String name, ValueUpdateHandler handler, EnvManagerConfigButton button) {
		super(name, handler);
		this.button = button;
	}

	@Override
	public Object getControl() {
		return button;
	}

	/*
	 * @return String value of the selection (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return button.getConfiguration();
	}

	/*
	 * Sets the value on the text, either by resolving the arguments for
	 * read-only, or by retrieving the value. Turns on the refreshing flag so as
	 * not to trigger further updates from the listener. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		if (!canSave) {
			mapValue = getResolvedDynamic();
		} else {
			mapValue = lcMap.get(name);
		}
		String s = JAXBControlUIConstants.ZEROSTR;
		if (mapValue != null) {
			s = (String) mapValue;
		}
		button.setConfiguration(s);
		refreshing = false;
	}
}
