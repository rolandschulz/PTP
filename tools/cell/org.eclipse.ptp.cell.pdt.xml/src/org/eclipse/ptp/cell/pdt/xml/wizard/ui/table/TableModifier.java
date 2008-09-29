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

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.cell.pdt.xml.Activator;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardGroupsPositionAndColorPage;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardGroupsPositionAndColorPage.Column;
import org.eclipse.swt.widgets.TableItem;



/**
 * Implementation of {@link ICellModifier} to handle Position and Color table
 * 
 * @author Richard Maciel
 *
 */
public class TableModifier implements ICellModifier {
	
	/**
	 * 
	 */
	private final PdtWizardGroupsPositionAndColorPage positionAndColorPage;
	private TableViewer tableViewer;

	/**
	 * @param pdtWizardGroupsPositionAndColorPage
	 */
	public TableModifier(TableViewer viewer,
			PdtWizardGroupsPositionAndColorPage positionAndColorPage) {
		this.positionAndColorPage = positionAndColorPage;
		//this.pdtWizardGroupsPositionAndColorPage = pdtWizardGroupsPositionAndColorPage;
		tableViewer = viewer;
	}

	public boolean canModify(Object element, String property) {
		if(property.equals(Column.NAME.getAssociatedName())) {
			return false;
		}
		
		return true;
	}

	public Object getValue(Object element, String property) {
		if(element instanceof EventGroup) {
			EventGroup evtGrp = (EventGroup)element;
			
			if(property.equals(Column.NAME.getAssociatedName())) {
				return evtGrp.getName().toString();
			} else if(property.equals(Column.YSTART.getAssociatedName())) {
				return evtGrp.getYStart().toString();
			} else if(property.equals(Column.YEND.getAssociatedName())) {
				return evtGrp.getYEnd().toString();
			} else if(property.equals(Column.COLOR.getAssociatedName())) {
				return Integer.toHexString(evtGrp.getColor());
			}
		}
		return null;
	}

	/**
	 * Validate field modification, generating appropriate error message if needed.
	 * 
	 */
	public void modify(Object element, String property, Object value) {
		Debug.read();
		// Reset error message
		//this.pdtWizardGroupsPositionAndColorPage.setErrorMessage(null);
		
		if(element instanceof TableItem) {
			TableItem tblItem = (TableItem)element;
			
			// Get data from table item
			EventGroup evtGrp = (EventGroup)tblItem.getData();
			
			String strVal = (String)value;
			
			// Validate selected field
			Column col = null;
			try {
				if(property.equals(Column.YSTART.getAssociatedName())) {
					//itemIndex = 0;
					col = Column.YSTART;
					evtGrp.setYStart(Float.parseFloat(strVal));
					//tblItem.setText(itemIndex, strVal);
				} else if(property.equals(Column.YEND.getAssociatedName())) {
					col = Column.YEND;
					//itemIndex = 1;
					evtGrp.setYEnd(Float.parseFloat(strVal));
					//tblItem.setText(itemIndex, strVal);
				} else if(property.equals(Column.COLOR.getAssociatedName())) {
					col = Column.COLOR;
					//itemIndex = 2;
					evtGrp.setColor(Integer.parseInt(strVal, 16));
					//tblItem.setText(itemIndex, strVal);
				}
			} catch (NumberFormatException e) {
				// Ignore changes and send a message for the user
				Status status = new Status(Status.ERROR, Activator.getDefault().getBundle().toString(), 0, Messages.TableModifier_Modify_ErrorMessage_InvalidTableInput, null);
				
				positionAndColorPage.displayErrorDialog(status);
			}
			tableViewer.update(evtGrp, null);
		}
	}
	
}