/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
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
package org.eclipse.ptp.debug.internal.core;

import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;

/**
 * Definitions of the internal constants for C/C++ Debug plug-in.
 */
public class IPDebugInternalConstants {

	/**
	 * Status handler codes.
	 */
	public static final int STATUS_CODE_QUESTION = 10000;
	public static final int STATUS_CODE_INFO = 10001;
	public static final int STATUS_CODE_ERROR = 10002;

	/**
	 * String preference for the common source containers.
	 */
	public static final String PREF_COMMON_SOURCE_CONTAINERS = PTPDebugCorePlugin.getUniqueIdentifier() + ".cDebug.common_source_containers"; //$NON-NLS-1$
}