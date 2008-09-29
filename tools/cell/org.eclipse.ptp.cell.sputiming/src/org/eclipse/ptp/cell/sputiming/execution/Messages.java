/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.sputiming.execution;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.sputiming.execution.messages"; //$NON-NLS-1$

	public static String CompilerParametersDumpString;

	public static String SPUTimingExecution_ExecuteCompiler_CompilerNotFoundInPath;

	public static String SPUTimingParametersDumpString;

	public static String StartCompiler_CompilerExecError;
	public static String StartCompiler_CompilerProcessLabel;
	public static String StartCompiler_OSCompilerError;
	public static String StartCompiler_Timeout;
	public static String StartSPUTiming_SPUTimingExecError;
	public static String StartSPUTiming_ProcessLabel;
	public static String StartSPUTiming_OSSPUTimingError;
	public static String StartSPUTiming_Timeout;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
