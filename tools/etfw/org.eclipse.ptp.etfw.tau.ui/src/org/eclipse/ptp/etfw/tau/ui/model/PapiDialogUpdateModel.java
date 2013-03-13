/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.tau.ui.model;

import org.eclipse.ptp.etfw.tau.ui.ITauConstants;
import org.eclipse.ptp.etfw.tau.ui.PapiOptionDialog;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;

/**
 * This model handles saving/restoring the papi selection custom widget.
 * 
 * @TODO The model, not the widget, should handle storing selections
 * 
 * @author "Chris Navarro"
 * 
 */
public class PapiDialogUpdateModel extends AbstractUpdateModel implements ModifyListener {

	private final PapiOptionDialog papiOptionDialog;

	public PapiDialogUpdateModel(String name, IUpdateHandler handler, Control control) {
		super(name, handler);
		papiOptionDialog = (PapiOptionDialog) control;
	}

	@Override
	public Object getValueFromControl() {
		return null;
	}

	@Override
	public Object getControl() {
		return papiOptionDialog;
	}

	@Override
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
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	@Override
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.getValue(name);
		if (JAXBCoreConstants.ZEROSTR.equals(mapValue)) {
			mapValue = null;
		}

		if (lcMap.get(ITauConstants.TAU_MAKEFILE_TAB_ID) != null) {
			String selItem = lcMap.get(ITauConstants.TAU_MAKEFILE_TAB_ID).getValue().toString();
			papiOptionDialog.setSelection(selItem);
		}

		papiOptionDialog.setVariableMap(lcMap);

		refreshing = false;
	}
}
