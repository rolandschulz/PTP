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
package org.eclipse.cldt.internal.corext.refactoring;

import org.eclipse.cldt.internal.corext.refactoring.base.*;
import org.eclipse.core.runtime.IProgressMonitor;


public class NullChange extends Change {

	private String fName;
	
	public NullChange(String name){
		fName= name;
	}
	
	public NullChange(){
		this(null);
	}
		
	public void perform(ChangeContext context, IProgressMonitor pm) {
	}

	public IChange getUndoChange() {
		return new NullChange(fName);
	}
	
	public String getName(){
		return "NullChange (" + fName + ")";  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public Object getModifiedLanguageElement(){
		return null;
	}
}
