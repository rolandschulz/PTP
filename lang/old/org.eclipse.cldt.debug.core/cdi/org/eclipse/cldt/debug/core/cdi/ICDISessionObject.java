/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.debug.core.cdi;

/**
 * 
 * Represents an object associated with a debug session.
 * 
 * @since Jul 9, 2002
 */
public interface ICDISessionObject {

	/**
	 * Returns the debug session this object is associated with.
	 * 
	 * @return the debug session this object is associated with
	 */
	ICDISession getSession();
}
