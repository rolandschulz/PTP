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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
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
	private final String itemsFrom;
	private final String choices[];
	private final String translateChoiceAs[];

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved.
	 * @param itemsFrom
	 *            property or attribute having combo items
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param combo
	 *            the widget to which this model corresponds
	 */
	public ComboUpdateModel(String name, String itemsFrom, IUpdateHandler handler, Combo combo, String choices,
			String translateChoiceAs) {
		super(name, handler);
		this.combo = combo;
		this.itemsFrom = itemsFrom;
		this.combo.addModifyListener(this);
		this.combo.addSelectionListener(this);
		if (choices != null) {
			this.choices = choices.split(",");
		}
		else {
			this.choices = null;
		}
		if (translateChoiceAs != null) {
			this.translateChoiceAs = translateChoiceAs.split(",");
		}
		else {
			this.translateChoiceAs = null;
		}
	}

	@Override
	public Object getControl() {
		return combo;
	}

	/*
	 * @return value of the selection (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		String selection;

		/*
		 * If the selection matches a value in the choices array, return the
		 * corresponding translateChoiceAs value. Otherwise just return the selection
		 */
		selection = WidgetActionUtils.getSelected(combo);
		if ((translateChoiceAs != null) && (choices != null)) {
			for (int i = 0; i < choices.length; i++) {
				if (selection.equals(choices[i])) {
					return translateChoiceAs[i];
				}
			}
		}
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rm.jaxb.control.ui.model.AbstractUpdateModel#initialize(org.eclipse.ptp.rm.jaxb.core.IVariableMap,
	 * org.eclipse.ptp.rm.jaxb.core.IVariableMap)
	 */
	@Override
	public void initialize(ILaunchConfiguration configuration, IVariableMap rmMap, IVariableMap lcMap) {
		if (itemsFrom != null) {
			String[] items = WidgetActionUtils.getItemsFrom(rmMap, itemsFrom);
			if (items.length == 0) {
				items = WidgetActionUtils.getItemsFrom(lcMap, itemsFrom);
			}
			/*
			 * If an items value matches an entry in the translateChoiceAs array
			 * replace it with the corresponding choices array entry.
			 */
			if ((translateChoiceAs != null) && (choices != null)) {
				for (int i = 0; i < items.length; i++) {
					for (int j = 0; j < translateChoiceAs.length; j++) {
						if (items[i].equals(translateChoiceAs[j])) {
							items[i] = choices[j];
							break;
						}
					}
				}
			}
			items = WidgetBuilderUtils.normalizeComboItems(items);
			combo.setItems(items);
		}
		super.initialize(configuration, rmMap, lcMap);
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
	 * Sets the map value one the combo. Sets refreshing flag to block further updates being triggered during the refresh.
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.getValue(name);
		String s = JAXBControlUIConstants.ZEROSTR;
		if (mapValue != null) {
			s = (String) mapValue;
			if ((translateChoiceAs != null) && (choices != null)) {
				for (int i = 0; i < translateChoiceAs.length; i++) {
					if (s.equals(translateChoiceAs[i])) {
						s = choices[i];
						break;
					}
				}
			}
		}
		s = WidgetActionUtils.select(combo, s);
		refreshing = false;
	}

	/*
	 * Model serves as widget selection listener; calls {@link #storeValue()} Sets refreshing flag to block further updates being
	 * triggered during the refresh. (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Model serves as widget selection listener; calls {@link #storeValue()} Sets refreshing flag to block further updates being
	 * triggered during the refresh. (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
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
