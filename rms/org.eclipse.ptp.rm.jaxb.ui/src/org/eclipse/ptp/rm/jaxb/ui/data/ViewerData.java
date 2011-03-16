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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Control;

public class ViewerData {

	private final List<RowData> rows;

	public ViewerData(Map<Object, Control> data) {

		rows = new ArrayList<RowData>();

		for (Object o : data.keySet()) {
			rows.add(new RowData(o, data.get(o)));
		}
	}

	public List<RowData> getRows() {
		return rows;
	}
}
