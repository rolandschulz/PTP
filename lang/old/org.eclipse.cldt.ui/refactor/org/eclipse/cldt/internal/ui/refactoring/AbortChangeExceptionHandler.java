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

package org.eclipse.cldt.internal.ui.refactoring;

import org.eclipse.cldt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.cldt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cldt.internal.corext.refactoring.base.IChange;
import org.eclipse.cldt.internal.corext.refactoring.base.IChangeExceptionHandler;
import org.eclipse.cldt.ui.FortranUIPlugin;

/**
 * A default implementation of <code>IChangeExceptionHandler</code> which
 * always aborts an change if an exception is caught.
 */
public class AbortChangeExceptionHandler implements IChangeExceptionHandler {
	
	public void handle(ChangeContext context, IChange change, Exception e) {
		FortranUIPlugin.getDefault().log(e);
		throw new ChangeAbortException(e);
	}
}
