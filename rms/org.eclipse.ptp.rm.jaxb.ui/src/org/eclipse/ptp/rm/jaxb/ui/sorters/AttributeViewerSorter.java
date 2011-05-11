/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.sorters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.JAXBRMUIConstants;

/**
 * Sorts the viewer on the name column. Clicking a second time on the column
 * reverses the direction of the sort.
 * 
 * @author arossi
 * 
 */
public class AttributeViewerSorter extends ViewerSorter {
	protected int toggle = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.
	 * viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		int result = 0;

		if (o1 instanceof ICellEditorUpdateModel && o2 instanceof ICellEditorUpdateModel) {
			ICellEditorUpdateModel c1 = (ICellEditorUpdateModel) o1;
			ICellEditorUpdateModel c2 = (ICellEditorUpdateModel) o2;
			String name1 = c1.getDisplayValue(JAXBRMUIConstants.COLUMN_NAME);
			String name2 = c2.getDisplayValue(JAXBRMUIConstants.COLUMN_NAME);
			result = name1.compareTo(name2);
		}

		return result * toggle;
	}

	public void toggle() {
		toggle *= -1;
	}
}
