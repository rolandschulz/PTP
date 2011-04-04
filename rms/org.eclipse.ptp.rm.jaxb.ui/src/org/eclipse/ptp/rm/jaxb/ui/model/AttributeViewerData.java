/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.ArrayList;
import java.util.List;

public class AttributeViewerData {

	private final List<AttributeViewerCellData> rows;

	public AttributeViewerData() {
		rows = new ArrayList<AttributeViewerCellData>();
	}

	public void addRow(AttributeViewerCellData data) {
		rows.add(data);
	}

	public List<AttributeViewerCellData> getRows() {
		return rows;
	}
}
