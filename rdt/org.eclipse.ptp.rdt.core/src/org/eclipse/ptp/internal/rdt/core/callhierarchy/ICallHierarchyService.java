/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

/**
 * Provides call graph analysis services.
 * 
 * Clients should extend <code>AbstractCallHierarchyService</code>
 * instead of implementing this interface directly.
 */
public interface ICallHierarchyService {

	/**
	 * Returns all the functions and methods that call the given element.
	 * @param callee 
	 * @throws InterruptedException 
	 */
	CalledByResult findCalledBy(Scope scope, ICElement callee, IProgressMonitor pm)
			throws CoreException, InterruptedException;

	/**
	 * Returns all the functions and methods that are called by the given
	 * element.
	 * @throws InterruptedException 
	 */
	CallsToResult findCalls(Scope scope, ICElement caller, IProgressMonitor pm)
			throws CoreException, InterruptedException;

	/**
	 * Returns the <code>ICElement</code>s for all the function/method
	 * definitions related to the given element.
	 * 
	 * @param input
	 * @return
	 */
	ICElement[] findDefinitions(Scope scope, ICElement input, IProgressMonitor pm);

	/**
	 * Returns the <code>ICElement</code>s for all the function/method
	 * definitions related to the given selection in the
	 * <code>IWorkingCopy</code>.
	 * 
	 * @param project the project <code>workingCopy</code> belongs to
	 * @param workingCopy contains the selected text 
	 * @param selectionStart index of the start of the selection within
	 *        <code>workingCopy</code>
	 * @param selectionLength number of characters in the selection
	 * @return the <code>ICElement</code>s for all the function/method
	 * definitions related to the given selection.
	 */
	ICElement[] findDefinitions(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength, IProgressMonitor pm) throws CoreException;
}