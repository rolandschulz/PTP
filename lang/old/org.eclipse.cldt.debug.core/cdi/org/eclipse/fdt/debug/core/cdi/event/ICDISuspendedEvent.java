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
 * Notifies that the originator has been suspended. 
 * The originators:
 * <ul>
 * <li>target (ICDITarget)
 * <li>thread (ICDIThread)
 * </ul>
 * The reason of the suspension can be one of the following session 
 * objects:
 * <ul>
 * <li>breakpoint (ICDIBreakpoint)
 * <li>signal (ICDISignalReceived)
 * <li>end of the stepping range (ICDIEndSteppingRange)
 * </ul>
 * 
 * @since Jul 10, 2002
 */
public interface ICDISuspendedEvent extends ICDIEvent {

	/**
	 * Returns the session object that caused the suspension.
	 * 
	 * @return ICDIObject
	 */
	ICDISessionObject getReason();

}
