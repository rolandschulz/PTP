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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CheckboxCellEditor extends CellEditor {

	private Button checkbox;

	private static final int defaultStyle = SWT.NONE;

	public CheckboxCellEditor() {
		setStyle(defaultStyle);
	}

	public CheckboxCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	public CheckboxCellEditor(Composite parent, boolean initialValue) {
		this(parent, defaultStyle, initialValue);
	}

	public CheckboxCellEditor(Composite parent, int style) {
		this(parent, style, false);
	}

	public CheckboxCellEditor(Composite parent, int style, boolean initialValue) {
		super(parent, style);
		checkbox.setSelection(initialValue);
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
