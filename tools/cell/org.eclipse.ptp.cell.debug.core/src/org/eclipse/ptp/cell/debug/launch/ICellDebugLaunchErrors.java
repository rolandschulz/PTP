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
package org.eclipse.ptp.cell.debug.launch;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.2
 */
public interface ICellDebugLaunchErrors {
	
	public static int ERR_CANCEL = 1100;
	public static int ERR_REMOTE_CONNECT = 1101;
	public static int ERR_REMOTE_EXEC = 1102;
	public static int ERR_REMOTE_CANCEL = 1103;
	public static int ERR_INVALID_ARGS = 1104;
	public static int ERR_LOCAL_FILENFOUND = 1105;
	public static int ERR_LOCAL_REMOTE_MISMATCH = 1106;
	public static int ERR_NOT_REMOTE_CAPABLE = 1107;
	public static int ERR_PORT_MISMATCH = 1108;
	public static int ERR_HOST_MISMATCH = 1109;
}
