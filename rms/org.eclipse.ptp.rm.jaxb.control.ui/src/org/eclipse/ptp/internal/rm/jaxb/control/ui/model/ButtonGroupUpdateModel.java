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

import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Update model for the entire button group. <br>
 * <br>
 * The group control is used to map the entire group; the group itself updates a single attribute value.
 * 
 * @author arossi
 * 
 */
public class ButtonGroupUpdateModel extends AbstractUpdateModel implements SelectionListener {

	private String selectedLabel;
	private final List<Button> buttons;
	private final Composite control;
	private Button lastSelected;

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param control
	 *            the control object to which this model is mapped in the handler
	 * @param button
	 *            the button widgets to which this model corresponds
	 */
	public ButtonGroupUpdateModel(String name, IUpdateHandler handler, Composite control, List<Button> buttons) {
		super(name, handler);
		this.control = control;
		this.buttons = buttons;
		for (Button b : buttons) {
			b.addSelectionListener(this);
		}
		lastSelected = null;
		selectedLabel = JAXBUIConstants.ZEROSTR;
	}

	@Override
	public Object getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return selectedLabel;
	}

	/*
	 * Sets the selection on the button corresponding to the selected label, after translating the value from the current
	 * environment. Turns on the refreshing flag so as not to trigger further updates from the listener. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.getValue(name);
		if (mapValue != null && !JAXBUIConstants.ZEROSTR.equals(mapValue)) {
			selectedLabel = String.valueOf(mapValue);
		} else {
			selectedLabel = JAXBUIConstants.ZEROSTR;
		}
		for (Button b : buttons) {
			if (b.getText().equals(selectedLabel)) {
				b.setSelection(true);
			} else {
				b.setSelection(false);
			}
		}
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
		selectedLabel = JAXBUIConstants.ZEROSTR;
		Button selected = (Button) e.getSource();
		if (selected.getSelection()) {
			selectedLabel = selected.getText();
			if (lastSelected != null && lastSelected != selected) {
				lastSelected.setSelection(false);
			} else {
				for (Button b : buttons) {
					if (b != selected) {
						b.setSelection(false);
					}
				}
			}
		}
		lastSelected = selected;
		try {
			Object value = storeValue();
			handleUpdate(value);
		} catch (Exception ignored) {
		}
	}
}
