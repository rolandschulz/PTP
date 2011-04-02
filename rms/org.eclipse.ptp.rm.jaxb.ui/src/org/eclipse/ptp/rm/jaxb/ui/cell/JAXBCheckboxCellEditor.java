/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.cell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class JAXBCheckboxCellEditor extends CellEditor implements IJAXBUINonNLSConstants {

	private Button checkbox;

	private static final int defaultStyle = SWT.NONE;

	public JAXBCheckboxCellEditor() {
		setStyle(defaultStyle);
	}

	public JAXBCheckboxCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	public JAXBCheckboxCellEditor(Composite parent, boolean initialValue) {
		this(parent, defaultStyle, initialValue);
	}

	public JAXBCheckboxCellEditor(Composite parent, int style) {
		this(parent, style, false);
	}

	public JAXBCheckboxCellEditor(Composite parent, int style, boolean initialValue) {
		super(parent, style);
		checkbox.setSelection(initialValue);
	}

	public Button getCheckbox() {
		return checkbox;
	}

	@Override
	protected Control createControl(Composite parent) {
		checkbox = new Button(parent, SWT.CHECK | SWT.LEFT);
		return checkbox;
	}

	@Override
	protected Object doGetValue() {
		return checkbox.getSelection();
	}

	@Override
	protected void doSetFocus() {
		checkbox.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(value instanceof Boolean);
		checkbox.setSelection((Boolean) value);
	}
}
