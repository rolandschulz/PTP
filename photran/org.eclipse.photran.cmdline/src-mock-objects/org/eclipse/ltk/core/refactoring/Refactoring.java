/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public abstract class Refactoring
{
    public abstract String getName();
    public abstract RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;
    public abstract RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;
    public abstract Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

}
