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

import org.eclipse.ptp.etfw.tau.ui.TAUMakefileCombo;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class MakefileComboUpdateModel extends AbstractUpdateModel implements SelectionListener {

	private final TAUMakefileCombo makefileCombo;

	public MakefileComboUpdateModel(String name, IUpdateHandler handler, Control control) {
		super(name, handler);
		this.makefileCombo = (TAUMakefileCombo) control;
		this.makefileCombo.getCombo().addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				try {
					Object value = storeValue();
					handleUpdate(value);
				} catch (Exception ignored) {
				}
			}
		});
	}

	@Override
	public Object getValueFromControl() {
		return makefileCombo.getSelection();
	}

	@Override
	public void refreshValueFromMap() {
		this.makefileCombo.setVariableMap(this.lcMap);
	}

	@Override
	public Object getControl() {
		return makefileCombo;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		try {
			Object value = storeValue();
			handleUpdate(value);
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

}
