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
 * @author Ricardo M. Matinata
 * @since 1.2
 *
 */
public class CellDebugRemoteDebugger {
	
	private String name;
	
	private ICellDebugLaunchRemoteDebugConfiguration debugConfig;
	
	private String debuggerId;

	public ICellDebugLaunchRemoteDebugConfiguration getDebugConfig() {
		return debugConfig;
	}

	public void setDebugConfig(ICellDebugLaunchRemoteDebugConfiguration debugConfig) {
		this.debugConfig = debugConfig;
	}

	public String getDebuggerId() {
		return debuggerId;
	}

	public void setDebuggerId(String debuggerId) {
		this.debuggerId = debuggerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

}
