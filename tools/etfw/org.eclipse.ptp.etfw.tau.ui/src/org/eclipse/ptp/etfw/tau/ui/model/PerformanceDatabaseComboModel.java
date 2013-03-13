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

import org.eclipse.ptp.etfw.tau.ui.PerformanceDatabaseCombo;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * This stores the performance database custom widget selection.
 * 
 * @TODO This needs to be finished and tested.
 * 
 * @author "Chris Navarro"
 * 
 */
public class PerformanceDatabaseComboModel extends AbstractUpdateModel {

	private final PerformanceDatabaseCombo comboComposite;

	public PerformanceDatabaseComboModel(String name, IUpdateHandler handler, Control control) {
		super(name, handler);

		comboComposite = (PerformanceDatabaseCombo) control;
	}

	@Override
	public Object getValueFromControl() {
		return getSelected(comboComposite.getCombo());
	}

	private String getSelected(Combo combo) {
		if (combo.isDisposed()) {
			return JAXBCoreConstants.ZEROSTR;
		}
		if (combo.getItemCount() == 0) {
			return combo.getText();
		}
		int i = combo.getSelectionIndex();
		if (i < 0) {
			return combo.getText();
		}
		return combo.getItem(i);
	}

	@Override
	public void refreshValueFromMap() {

	}

	@Override
	public Object getControl() {
		return comboComposite;
	}

}
