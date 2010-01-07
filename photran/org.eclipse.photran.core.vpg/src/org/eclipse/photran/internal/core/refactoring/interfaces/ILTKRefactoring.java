/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.interfaces;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * This is the interface implemented by all refactorings that subclass from
 * {@link Refactoring}.
 * 
 * @author Jeff Overbey
 */
public interface ILTKRefactoring
{
    String getName();
    RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;
    RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;
    Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;
}
