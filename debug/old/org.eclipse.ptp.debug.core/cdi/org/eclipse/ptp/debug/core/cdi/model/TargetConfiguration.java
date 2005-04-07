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
package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
/**
 */
public class TargetConfiguration extends PTPObject implements ICDITargetConfiguration {
	
	public TargetConfiguration(Target target) {
		super(target);
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsBreakpoints()
	 */
	public boolean supportsBreakpoints() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsDisconnect()
	 */
	public boolean supportsDisconnect() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsExpressionEvaluation()
	 */
	public boolean supportsExpressionEvaluation() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsInstructionStepping()
	 */
	public boolean supportsInstructionStepping() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryModification()
	 */
	public boolean supportsMemoryModification() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryRetrieval()
	 */
	public boolean supportsMemoryRetrieval() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisterModification()
	 */
	public boolean supportsRegisterModification() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisters()
	 */
	public boolean supportsRegisters() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSharedLibrary()
	 */
	public boolean supportsSharedLibrary() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRestart()
	 */
	public boolean supportsRestart() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsStepping()
	 */
	public boolean supportsStepping() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsTerminate()
	 */
	public boolean supportsTerminate() {
		return false;
		
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsResume()
	 */
	public boolean supportsResume() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSuspend()
	 */
	public boolean supportsSuspend() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#terminateSessionOnExit()
	 */
	public boolean terminateSessionOnExit() {
		return true;
	}
}
