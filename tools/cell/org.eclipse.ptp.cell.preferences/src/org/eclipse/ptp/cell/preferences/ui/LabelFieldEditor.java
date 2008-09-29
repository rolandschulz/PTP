/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.preferences.ui;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A field editor for displaying labels not associated with other widgets.
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class LabelFieldEditor extends FieldEditor {

	private Label label;

	// All labels can use the same preference name since they don't
	// store any preference.
	public LabelFieldEditor(String value, Composite parent) {
		super(Messages.LabelDefaultName, value, parent);
	}

	// Adjusts the field editor to be displayed correctly
	// for the given number of columns.
	protected void adjustForNumColumns(int numColumns) {
		((GridData) label.getLayoutData()).horizontalSpan = numColumns;
	}

	// Fills the field editor's controls into the given parent.
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		label = getLabelControl(parent);
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = numColumns;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		
		label.setLayoutData(gridData);
	}

	// Returns the number of controls in the field editor.
	public int getNumberOfControls() {
		return 1;
	}

	// Labels do not persist any preferences, so these methods are empty.
	protected void doLoad() {
	}
	protected void doLoadDefault() {
	}
	protected void doStore() {
	}
}
