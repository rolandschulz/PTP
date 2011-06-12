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

package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

/**
 * Provides type graph analysis services.
 * 
 * Clients should extend <code>AbstractTypeHierarchyService</code>
 * instead of implementing this interface directly.
 */
public interface ITypeHierarchyService {

	/**
	 * Returns the supertype and subtype graph for the class associated with
	 * with given input.
	 */
	THGraph computeGraph(Scope scope, ICElement input, IProgressMonitor monitor)
			throws CoreException, InterruptedException;

	/**
	 * Returns a pair of <code>ICElement</code>s corresponding to the
	 * definition of the parent type of the given input, as well as the
	 * definition of the input.
	 */
	ICElement[] findInput(Scope scope, ICElement memberInput, IProgressMonitor monitor);

	/**
	 * Returns a pair of <code>ICElement</code>s corresponding to the
	 * definition of the parent type of the given selection in the
	 * <code>IWorkingCopy</code>, as well as the definition of the
	 * selection.
	 */
	ICElement[] findInput(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength, IProgressMonitor monitor) throws CoreException;
}