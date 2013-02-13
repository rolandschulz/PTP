/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.cell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

/**
 * CellEditor backed by Spinner.
 * 
 * @author arossi
 * 
 */
public class SpinnerCellEditor extends CellEditor {

	private Spinner spinner;

	private static final int defaultStyle = SWT.NONE;

	public SpinnerCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * @param parent
	 */
	public SpinnerCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * @param parent
	 * @param style
	 *            SWT style appropriate for Spinner
	 */
	public SpinnerCellEditor(Composite parent, int style) {
		this(parent, style, null, null);
	}

	/**
	 * @param parent
	 * @param style
	 *            SWT style appropriate for Spinner
	 * @param min
	 *            value on Spinner
	 * @param max
	 *            value on Spinner
	 */
	public SpinnerCellEditor(Composite parent, int style, Integer min, Integer max) {
		super(parent, style);

		if (min != null) {
			if (max == null) {
				max = Integer.MAX_VALUE;
			}
		}

		if (max != null) {
			if (min == null) {
				min = Integer.MIN_VALUE;
			}
		}

		if (min != null && max != null) {
			spinner.setMinimum(min);
			spinner.setMaximum(max);
		}
	}

	/**
	 * @param parent
	 * @param min
	 *            value on Spinner
	 * @param max
	 *            value on Spinner
	 */
	public SpinnerCellEditor(Composite parent, Integer min, Integer max) {
		this(parent, defaultStyle, min, max);
	}

	/**
	 * @return min value on Spinner
	 */
	public int getMin() {
		return spinner.getMinimum();
	}

	/**
	 * @return max value on Spinner
	 */
	public Spinner getSpinner() {
		return spinner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellEditor#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		spinner = new Spinner(parent, getStyle());
		return spinner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
	 */
	@Override
	protected Object doGetValue() {
		return spinner.getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
	 */
	@Override
	protected void doSetFocus() {
		spinner.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(value instanceof Integer);
		spinner.setSelection((Integer) value);
	}
}
