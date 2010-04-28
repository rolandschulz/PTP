/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.providers;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.pbs.ui.AttributePlaceholder;

/**
 * Used to populate viewers whose model is the AttributePlaceholder.
 * 
 * @see org.eclipse.ptp.rm.pbs.ui.AttributePlaceholder
 * @author arossi
 */
public class AttributeContentProvider implements IStructuredContentProvider {
	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Map<?, ?>) {
			return ((Map<?, ?>) inputElement).values().toArray(new AttributePlaceholder[0]);
		} else if (inputElement instanceof AttributePlaceholder) {
			return new Object[] { inputElement };
		}
		return new Object[0];
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
