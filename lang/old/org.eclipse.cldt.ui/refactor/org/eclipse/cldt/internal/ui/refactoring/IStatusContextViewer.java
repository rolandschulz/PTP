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
package org.eclipse.cldt.internal.ui.refactoring;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cldt.internal.corext.refactoring.base.Context;

/**
 * A special viewer to present a context for a <code>RefactoringStatusEntry</code>.
 */
public interface IStatusContextViewer {
	
	/**
	 * Creates the status viewer's widget hierarchy. This method 
	 * should only be called once. Method <code>getControl()</code>
	 * should be used to retrieve the widget hierarchy.
	 * 
	 * @param parent the parent for the widget hierarchy
	 * 
	 * @see #getControl()
	 */
	public void createControl(Composite parent);
	
	/**
	 * Returns the status context viewer's SWT control.
	 * 
	 * @return the status context viewer's SWT control
	 */
	public Control getControl();	
	
	/**
	 * Sets the status context viewer's input element.
	 * 
	 * @param input the input element
	 */
	public void setInput(Context input);
}

