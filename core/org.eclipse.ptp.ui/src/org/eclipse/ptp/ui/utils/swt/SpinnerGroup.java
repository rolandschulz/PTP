/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.ui.utils.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

/**
 * @author Richard Maciel
 *
 */
public class SpinnerGroup extends GenericControlGroup {

	public static final int MAX_VALUE = Integer.MAX_VALUE;
	
	Spinner spinner;
	
	/**
	 * @param parent
	 * @param mold
	 */
	public SpinnerGroup(Composite parent, GenericControlMold mold) {
		super(parent, mold);
		SpinnerMold sgm = (SpinnerMold)mold;
		spinner.setMinimum(sgm.min);
		spinner.setMaximum(sgm.max);
		spinner.setIncrement(sgm.increment);
	}

	protected Control createCustomControl(int bitmask, GridData gridData) {
		spinner = new Spinner(this, SWT.BORDER);
		return spinner;
	}

	/**
	 * @return the spinner
	 */
	public Spinner getSpinner() {
		return spinner;
	}

	/**
	 * @return the control's value
	 */
	public int getValue() {
		return spinner.getSelection();
	}
	
	/**
	 * Set the control value
	 */
	public void setValue(int newValue) {
		spinner.setSelection(newValue);
	}
}
