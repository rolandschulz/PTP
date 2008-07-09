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

package org.eclipse.ptp.internal.rdt.core.index;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

public interface INavigationService {
	/**
	 * Returns an index-based representation of the given input, or
	 * <code>null</code> it cannot be found in the index.
	 * 
	 * @param scope
	 * @param input
	 * @return
	 * @throws CoreException 
	 * @throws InterruptedException 
	 */
	ICElement findElement(Scope scope, ICElement input) throws CoreException,
			InterruptedException;
}
