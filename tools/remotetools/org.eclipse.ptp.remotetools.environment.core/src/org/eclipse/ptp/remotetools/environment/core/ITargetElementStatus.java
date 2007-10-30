/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.core;

/**
 * External status code definition for target generated events.
 * 
 * @author Ricardo M. Matinata, Richard Maciel
 * @since 1.1
 */
public interface ITargetElementStatus {
	
	/**
	 * The element has been successfully configured and is available (conected)
	 * to the host machine.
	 */
	final public static int STARTED = 0;
	
	/**
	 * The element is not conected to the host machine
	 */
	final public static int STOPPED = 1;
	
	/**
	 * The element is in a state that allows applications to be run in it.
	 */
	final public static int RESUMED = 2;
	
	/**
	 * The element is in a state that does not allow applications to be run in it,
	 * although is conected (live) to the host machine.
	 */
	final public static int PAUSED  = 3;
}
