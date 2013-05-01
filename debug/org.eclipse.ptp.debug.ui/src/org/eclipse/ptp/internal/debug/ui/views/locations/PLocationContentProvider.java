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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.model.IPLocationSet;

public class PLocationContentProvider implements IStructuredContentProvider {
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IPSession) {
			IPLocationSet[] sets = ((IPSession) inputElement).getLocationSetManager().getLocationSets();
			return sets;
		}
		return new Object[0];
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
