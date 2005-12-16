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
package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.ptp.debug.core.cdi.model.IPCDITargetConfiguration;

/**
 * @author Clement chu
 *
 */
public class TargetConfiguration extends PObject implements IPCDITargetConfiguration {
	public TargetConfiguration(Target target) {
		super(target);
	}
	public boolean supportsTerminate() {
		return true;
	}
	public boolean supportsDisconnect() {
		return false;
	}
	public boolean supportsSuspend() {
		return true;
	}
	public boolean supportsResume() {
		return true;
	}
	public boolean supportsRestart() {
		return false;
	}
	public boolean supportsStepping() {
		return true;
	}
	public boolean supportsInstructionStepping() {
		return false;
	}
	public boolean supportsBreakpoints() {
		return true;
	}
	public boolean supportsRegisters() {
		return false;
	}
	public boolean supportsRegisterModification() {
		return false;
	}
	public boolean supportsSharedLibrary() {
		return false;
	}
	public boolean supportsMemoryRetrieval() {
		return false;
	}
	public boolean supportsMemoryModification() {
		return false;
	}
	public boolean supportsExpressionEvaluation() {
		return true;
	}
}
