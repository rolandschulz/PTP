/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.ArrayList;
import java.util.List;

public class AttributeViewerData {

	private final List<AttributeViewerRowData> rows;
	private final List<AttributeViewerRowData> selected;

	public AttributeViewerData() {
		rows = new ArrayList<AttributeViewerRowData>();
		selected = new ArrayList<AttributeViewerRowData>();
	}

	public void addRow(AttributeViewerRowData data) {
		rows.add(data);
	}

	public List<AttributeViewerRowData> getAllRows() {
		return rows;
	}

	public List<AttributeViewerRowData> getSelectedRows() {
		selected.clear();
		for (AttributeViewerRowData row : rows) {
			if (row.isVisible()) {
				selected.add(row);
			}
		}
		return selected;
	}
}
