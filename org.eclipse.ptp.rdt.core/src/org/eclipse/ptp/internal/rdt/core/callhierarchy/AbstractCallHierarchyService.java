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

package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractCallHierarchyService implements ICallHierarchyService {
	public CalledByResult findCalledBy(ICElement callee, IProgressMonitor pm) throws CoreException, InterruptedException {
		return new CalledByResult();
	}

	public CallsToResult findCalls(ICElement caller, IProgressMonitor pm) throws CoreException, InterruptedException {
		return new CallsToResult();
	}

	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI
	 * Version: 1.25
	 */
	/**
	 * Returns <code>true</code> if the given <code>IBinding</code>
	 * is relevant to a call graph.
	 * 
	 * @param binding
	 * @return <code>true</code> if the given <code>IBinding</code>
	 * is relevant to a call graph.
	 */
	public boolean isRelevantForCallHierarchy(IBinding binding) {
		if (binding instanceof ICExternalBinding ||
				binding instanceof IEnumerator ||
				binding instanceof IFunction ||
				binding instanceof IVariable) {
			return true;
		}
		return false;
	}
	
	public ICElement findElement(ICElement input) throws CoreException, InterruptedException {
		return null;
	}
	
	public ICElement[] findDefinitions(ICElement input) {
		return null;
	}
	
	public ICElement[] findDefinitions(ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength) throws CoreException {
		return null;
	}
}