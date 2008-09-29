/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.wizard.ui.table;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardGroupsPositionAndColorPage.Column;
import org.eclipse.swt.graphics.Image;


public class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		// Do nothing
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String text = ""; //$NON-NLS-1$ // Default
		
		EventGroup evtGrp = (EventGroup)element;
		
		Column col = Column.values()[columnIndex];
		
		switch(col) {
		case NAME:
			text = evtGrp.getName();
			break;
		case YSTART: // yStart
			text = evtGrp.getYStart().toString();
			break;
		case YEND:
			text = evtGrp.getYEnd().toString();
			break;
		case COLOR:
			text = Integer.toHexString(evtGrp.getColor());
		}
		return text;
	}
	
}