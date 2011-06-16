/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.THMemberContentProvider
 * Version: 1.1
 */
package org.eclipse.ptp.internal.rdt.ui.typehierarchy;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class THMemberContentProvider implements IStructuredContentProvider {
	private static final Object[] NO_CHILDREN= new Object[0];
	private THHierarchyModel fModel; 

	public THMemberContentProvider() {
	}
    
    final public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	fModel= (THHierarchyModel) newInput;
    }

    public void dispose() {
    	fModel= null;
    }

    final public Object[] getElements(Object inputElement) {
    	if (fModel == null) {
    		return NO_CHILDREN;
    	}
        return fModel.getMembers();
    }
}
