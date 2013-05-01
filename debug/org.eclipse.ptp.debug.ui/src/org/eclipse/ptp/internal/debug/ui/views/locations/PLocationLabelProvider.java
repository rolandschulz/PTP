/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.views.locations;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.debug.core.model.IPLocationSet;
import org.eclipse.swt.graphics.Image;

public class PLocationLabelProvider extends LabelProvider implements ITableLabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IPLocationSet) {
			IPLocationSet locSet = (IPLocationSet) element;
			switch (columnIndex) {
			case 0:
				return new Path(locSet.getFile()).lastSegment();
			case 1:
				return locSet.getFunction();
			case 2:
				return Integer.toString(locSet.getLineNumber());
			case 3:
				return Integer.toString(locSet.getTasks().cardinality());
			}
		}
		return null;
	}
}