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
package org.eclipse.fdt.internal.corext.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.fdt.internal.corext.refactoring.base.IChange;
import org.eclipse.fdt.internal.corext.refactoring.base.RefactoringStatus;


public interface IRefactoringProcessor extends IAdaptable {
	
	public void initialize(Object[] elements) throws CoreException;
	
	public boolean isAvailable() throws CoreException;
	
	public String getProcessorName();
	
	public int getStyle();
	
//	public IProject[] getAffectedProjects() throws CoreException;
	
	public Object[] getElements();
	
	public Object[] getDerivedElements() throws CoreException;
	
//	public IResourceModifications getResourceModifications() throws CoreException;
	
	public RefactoringStatus checkActivation() throws CoreException;
	
	public RefactoringStatus checkInput(IProgressMonitor pm) throws CoreException;
	
	public IChange createChange(IProgressMonitor pm) throws CoreException;
}
