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
package org.eclipse.fdt.debug.core.cdi.event;

import org.eclipse.fdt.debug.core.cdi.ICDISessionObject;

/**
 * 
 * Notifies that the program has exited.
 * The originators:
 * <ul>
 * <li>target (ICDITarget)
 * </ul>
 * 
 * @since Jul 10, 2002
 */
public interface ICDIExitedEvent extends ICDIDestroyedEvent {
	/**
	 * Returns the information provided by the session when program 
	 * is exited.
	 * 
	 * @return the exit information
	 */
	ICDISessionObject getReason();
}
