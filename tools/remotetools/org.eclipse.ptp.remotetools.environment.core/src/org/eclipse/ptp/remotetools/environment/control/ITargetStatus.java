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
package org.eclipse.ptp.remotetools.environment.control;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public interface ITargetStatus {
	
	final public static int STARTED = 0;
	final public static int STOPPED = 1;
	final public static int RESUMED = 2;
	final public static int PAUSED  = 3;
}
