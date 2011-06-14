/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
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

public abstract class AbstractTypeHierarchyService implements ITypeHierarchyService {
	public THGraph computeGraph(Scope scope, ICElement input, IProgressMonitor monitor) throws CoreException, InterruptedException {
		return null;
	}
	
	public ICElement[] findInput(Scope scope, ICElement input) {
		return null;
	}
	
	public ICElement[] findInput(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength) throws CoreException {
		return null;
	}
}
