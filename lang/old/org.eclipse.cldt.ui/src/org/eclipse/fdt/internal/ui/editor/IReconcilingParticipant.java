/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.ui.editor;




/**
 * Interface of an object participating in reconciling.
 */
public interface IReconcilingParticipant {
	
	/**
	 * Called after reconciling has been finished.
	 */
	void reconciled(boolean SomethingHasChanged);
}
