/*******************************************************************************
 * Copyright (c) 2010 University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Albert L. Rossi (NCSA) - design and implementation (bug 310188)
 *                            - added column 05/11/2010
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.providers;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ptp.rm.pbs.core.attributes.AttributePlaceholder;
import org.eclipse.swt.graphics.Image;

/**
 * Used to extract labels for viewers whose model is the AttributePlaceholder.
 * 
 * @see org.eclipse.ptp.rm.pbs.core.attributes.AttributePlaceholder
 * @author arossi
 */
public class AttributeLabelProvider implements ITableLabelProvider {
	public AttributeLabelProvider() {
		super();
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object obj, int index) {
		AttributePlaceholder ap = (AttributePlaceholder) obj;
		switch (index) {
		case 0:
			return ap.getName();
		case 1:
			return ap.getDefaultString();
		case 2:
			return ap.getToolTip();
		default:
		}
		return ""; //$NON-NLS-1$
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
