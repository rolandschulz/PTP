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

import java.util.List;

/**
 * TreeData acts as a holder of a model object represented by a TreeViewer.
 * 
 * TreeData can be interpreted by TreeDataContentProvider as input to a
 * TreeViewer.
 */
public abstract class TreeData {
	/**
	 * Subclasses should override this to return the rows in the form of a list
	 * of objects of TreeRowData (essentially subclasses of TreeRowData)
	 */
	public abstract List<? extends TreeRowData> getRows();
}
