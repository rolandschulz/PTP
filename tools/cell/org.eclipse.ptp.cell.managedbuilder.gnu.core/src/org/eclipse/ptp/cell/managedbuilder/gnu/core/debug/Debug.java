/******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.gnu.core.debug;

import org.eclipse.ptp.cell.managedbuilder.gnu.core.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_SUPPLIER = false;
	public static boolean DEBUG_SEARCHER = false;

	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_SUPPLIER = DEBUG && POLICY.getBooleanOption("debug/supplier"); //$NON-NLS-1$
			DEBUG_SEARCHER = DEBUG && POLICY.getBooleanOption("debug/searcher"); //$NON-NLS-1$
		}
	}
	
}
