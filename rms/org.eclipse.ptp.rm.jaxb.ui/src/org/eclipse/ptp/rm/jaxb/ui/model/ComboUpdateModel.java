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

public class ComboUpdateModel extends AbstractUpdateModel implements ModifyListener, SelectionListener {

	private final Combo combo;

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

	public Object getValueFromControl() {
		return WidgetActionUtils.getSelected(combo);
	}

	public void modifyText(ModifyEvent e) {
		if (refreshing) {
			return;
		}
		validateJob.cancel();
		validateJob.schedule(VALIDATE_TIMER);
	}

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

	public void widgetDefaultSelected(SelectionEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}

	public void widgetSelected(SelectionEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}
}
