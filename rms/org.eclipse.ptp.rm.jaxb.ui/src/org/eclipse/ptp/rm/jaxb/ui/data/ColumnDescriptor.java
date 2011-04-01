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
package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;

public class ColumnDescriptor implements IJAXBUINonNLSConstants {

	private final String columnName;
	private final int width;

	public ColumnDescriptor(ColumnData data) {
		columnName = data.getName();
		width = data.getWidth();
	}

	public String getColumnName() {
		return columnName;
	}

	public int getWidth() {
		return width;
	}

	public boolean isWidthSpecified() {
		return width != -1;
	}
}
