/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.ui.util;

import org.eclipse.fdt.ui.FortranUIPlugin;

public class Util implements IDebugLogConstants{
	public static boolean VERBOSE_CONTENTASSIST = false;
	private Util() {
	}
	/*
	 * Add a log entry
	 */
	
	public static void debugLog(String message, DebugLogConstant client) {
		if( FortranUIPlugin.getDefault() == null ) return;
		if ( FortranUIPlugin.getDefault().isDebugging() && isActive(client)) {
			while (message.length() > 100) {	
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.out.println(partial + "\\"); //$NON-NLS-1$
			}
			if (message.endsWith("\n")) { //$NON-NLS-1$
				System.err.print(message);
			} else {
				System.out.println(message);
			}
		}
	}
	
	public static boolean isActive(DebugLogConstant client) {
		if (client.equals(IDebugLogConstants.CONTENTASSIST)){
			return VERBOSE_CONTENTASSIST;
		}
		return false;
	}
	
}
