/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

/**
 * Update Model for Combo widgets.
 * 
 * @author arossi
 * 
 */
public class ComboUpdateModel extends AbstractUpdateModel implements ModifyListener, SelectionListener {

	private final Combo combo;

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of a
	 *            Property or Attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param combo
	 *            the widget to which this model corresponds
	 */
	public ComboUpdateModel(String name, ValueUpdateHandler handler, Combo combo) {
		super(name, handler);
		this.combo = combo;
		this.combo.addModifyListener(this);
		this.combo.addSelectionListener(this);
	}

	@Override
	public Object getControl() {
		return combo;
	}

	/*
	 * @return value of the selection (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return WidgetActionUtils.getSelected(combo);
	}

	/*
	 * Model serves as widget modify listener; uses the ValidateJob to delay
	 * processing of text. Sets refreshing flag to block further updates being
	 * triggered during the refresh. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events
	 * .ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		if (refreshing) {
			return;
		}
		validateJob.cancel();
		validateJob.schedule(VALIDATE_TIMER);
	}

	/*
	 * Sets the map value one the combo. Sets refreshing flag to block further
	 * updates being triggered during the refresh. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.get(name);
		String s = ZEROSTR;
		if (mapValue != null) {
			s = (String) mapValue;
		}
		s = WidgetActionUtils.select(combo, s);
		refreshing = false;
	}

	/*
	 * Model serves as widget selection listener; calls {@link #storeValue()}
	 * Sets refreshing flag to block further updates being triggered during the
	 * refresh. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}

	/*
	 * Model serves as widget selection listener; calls {@link #storeValue()}
	 * Sets refreshing flag to block further updates being triggered during the
	 * refresh. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}
}
