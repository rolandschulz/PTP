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
package org.eclipse.cldt.internal.ui.text.contentassist;

public class Problem implements IProblem {
	String message = null;
	Problem(String message){
		this.message = message;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.IProblem#getMessage()
	 */
	public String getMessage() {
		return message;
	}
}
