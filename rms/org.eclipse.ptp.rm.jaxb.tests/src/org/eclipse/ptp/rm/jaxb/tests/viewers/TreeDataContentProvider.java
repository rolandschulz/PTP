/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 *  M Venkataramana - original code: http://eclipse.dzone.com/users/venkat_r_m
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.tests.viewers;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeDataContentProvider implements ITreeContentProvider {
	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getElements(Object inputElement) {
		return ((TreeData) inputElement).getRows().toArray();
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
