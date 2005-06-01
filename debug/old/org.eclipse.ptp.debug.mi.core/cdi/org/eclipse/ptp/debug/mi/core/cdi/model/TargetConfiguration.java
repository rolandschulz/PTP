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
package org.eclipse.ptp.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;

public class TargetConfiguration extends PTPObject implements ICDITargetConfiguration {
	public TargetConfiguration(Target target) {
		super(target);
	}

	public boolean supportsTerminate() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsTerminate()");
		return false;
	}

	public boolean supportsDisconnect() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsDisconnect()");
		return false;
	}

	public boolean supportsSuspend() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsSuspend()");
		return false;
	}

	public boolean supportsResume() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsResume()");
		return false;
	}

	public boolean supportsRestart() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsRestart()");
		return false;
	}

	public boolean supportsStepping() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsStepping()");
		return false;
	}

	public boolean supportsInstructionStepping() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsInstructionStepping()");
		return false;
	}

	public boolean supportsBreakpoints() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsBreakpoints()");
		return false;
	}

	public boolean supportsRegisters() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsRegisters()");
		return false;
	}

	public boolean supportsRegisterModification() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsRegisterModification()");
		return false;
	}

	public boolean supportsSharedLibrary() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsSharedLibrary()");
		return false;
	}

	public boolean supportsMemoryRetrieval() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsMemoryRetrieval()");
		return false;
	}

	public boolean supportsMemoryModification() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsMemoryModification()");
		return false;
	}

	public boolean supportsExpressionEvaluation() {
		// Auto-generated method stub
		System.out.println("TargetConfiguration.supportsExpressionEvaluation()");
		return false;
	}

}
