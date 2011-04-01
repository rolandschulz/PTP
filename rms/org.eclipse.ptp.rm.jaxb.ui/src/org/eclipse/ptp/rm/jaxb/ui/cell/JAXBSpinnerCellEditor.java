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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

public class JAXBSpinnerCellEditor extends CellEditor {

	private Spinner spinner;

	private static final int defaultStyle = SWT.NONE;

	public JAXBSpinnerCellEditor() {
		setStyle(defaultStyle);
	}

	public JAXBSpinnerCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	public JAXBSpinnerCellEditor(Composite parent, int style) {
		this(parent, style, null, null);
	}

	public JAXBSpinnerCellEditor(Composite parent, int style, Integer min, Integer max) {
		super(parent, style);
		spinner.setMinimum(min);
		spinner.setMaximum(max);
	}

	public JAXBSpinnerCellEditor(Composite parent, Integer min, Integer max) {
		this(parent, defaultStyle, min, max);
	}

	public Spinner getSpinner() {
		return spinner;
	}

	@Override
	protected Control createControl(Composite parent) {
		spinner = new Spinner(parent, getStyle());
		return spinner;
	}

	@Override
	protected Object doGetValue() {
		return spinner.getSelection();
	}

	@Override
	protected void doSetFocus() {
		spinner.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(value instanceof Integer);
		spinner.setSelection((Integer) value);
	}
}
