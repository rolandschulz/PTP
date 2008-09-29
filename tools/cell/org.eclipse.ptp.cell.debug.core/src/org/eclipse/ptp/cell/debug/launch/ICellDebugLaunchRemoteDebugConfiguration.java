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
public interface ICellDebugLaunchRemoteDebugConfiguration {
	
	public String getDbgBinaryName();
	
	public String getDbgPort();

}
