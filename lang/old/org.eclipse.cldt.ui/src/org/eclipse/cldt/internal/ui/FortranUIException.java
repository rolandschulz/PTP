package org.eclipse.cldt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * An exception to wrap a status. This is necessary to use the core's IRunnableWithProgress
 * support
 */

public class FortranUIException extends CoreException {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	public FortranUIException(IStatus status) {
		super(status);
	}	
}