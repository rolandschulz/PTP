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
import org.eclipse.core.runtime.CoreException;



public interface IRenameProcessor extends IRefactoringProcessor {

	public String getCurrentElementName();
	
	public RefactoringStatus checkNewElementName(String newName) throws CoreException;
	
	public void setNewElementName(String newName);
	
	public String getNewElementName();
	
}
