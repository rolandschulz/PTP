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

import org.eclipse.core.runtime.CoreException;

/**
 * Presents a preview of a <code>ChangeElement</code>
 */
public interface IChangePreviewViewer {

	/**
	 * Creates the preview viewer's widget hierarchy. This method 
	 * should only be called once. Method <code>getControl()</code>
	 * should be use retrieve the widget hierarchy.
	 * 
	 * @param parent the parent for the widget hierarchy
	 * 
	 * @see #getControl()
	 */
	public void createControl(Composite parent);
	
	/**
	 * Returns the preview viewer's SWT control.
	 * 
	 * @return the preview viewer's SWT control
	 */
	public Control getControl();	
	
	/**
	 * Sets the preview viewer's input element.
	 * 
	 * @param input the input element
	 */
	public void setInput(Object input) throws CoreException;
	
	/**
	 * Refreshes the preview viewer.
	 */
	public void refresh();	
}

