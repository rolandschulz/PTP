/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 *  M Venkataramana - original code: http://eclipse.dzone.com/users/venkat_r_m
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.tests.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * TreeRowData acts as a holder of a model object represented as a row in a
 * TreeViewer.
 * 
 * TreeRowData can be interpreted by TreeDataLabelProvider as a row in a
 * TreeViewer.
 */
public abstract class TreeRowData {
	/**
	 * Subclasses can override this to indicate that a row is not modifiable.
	 */
	public boolean canModify() {
		return true;
	}

	/**
	 * Subclasses should return the display value of a specified column.
	 * 
	 * If null is returned, then null is again passed back in setColumnValue. A
	 * null for a combo implies, nothing would get selected.
	 */
	public abstract String getColumnDisplayValue(String columnName);

	/**
	 * Subclasses can override this to display an image besides each cell text.
	 * TODO: I hope this works. Not tested.
	 */
	public Image getColumnImage(String columnName) {
		return null;
	}

	/**
	 * Subclasses should set the displayValue in their underlying model object
	 * representing the row.
	 * 
	 * The displayValue can be transformed into a business specific value and
	 * then stored inside the model object representing the row.
	 * 
	 * If nothing is selected in a combo, then null is passed in displayValue If
	 * nothing or empty-text is typed in a text-field, null is passed in
	 * displayValue
	 */
	public abstract void setColumnValue(String columnName, String displayValue);
}
