package org.eclipse.fdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.fdt.core.model.*;
/**
 * A factory that creates <code>IBuffer</code>s for CFiles.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * This    interface is similar to the JDT IBufferFactory interface.
 */
public interface IBufferFactory {

	/**
	 * Creates a buffer for the given owner.
	 * The new buffer will be initialized with the contents of the owner 
	 * if and only if it was not already initialized by the factory (a buffer is uninitialized if 
	 * its content is <code>null</code>).
	 * 
	 * @param owner the owner of the buffer
	 * @see IBuffer
	 */
	IBuffer createBuffer(IOpenable owner);
}
