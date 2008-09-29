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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;


public class TableContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		return ((EventGroupForest)inputElement).getGroupsUnion().toArray();
	}

	public void dispose() {
		// Nothing to do here
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
}