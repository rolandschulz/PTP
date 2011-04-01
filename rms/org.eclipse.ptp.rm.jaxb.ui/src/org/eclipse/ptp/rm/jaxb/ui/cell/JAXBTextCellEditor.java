/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - subclassed to export text.
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.cell;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class JAXBTextCellEditor extends TextCellEditor {
	public JAXBTextCellEditor() {
		super();
	}

	public JAXBTextCellEditor(Composite parent) {
		super(parent);
	}

	public JAXBTextCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	public Text getText() {
		return text;
	}
}
