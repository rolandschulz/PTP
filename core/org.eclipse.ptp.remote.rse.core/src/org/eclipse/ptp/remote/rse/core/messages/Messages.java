/******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remote.rse.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages {
	public static String RSEConnection_close;
	public static String RSEConnection_noPortFwd;
	public static String RSEConnection_noShellService;
	
	static {
		NLS.initializeMessages("org.eclipse.ptp.remote.rse.core.messages.messages", Messages.class);
	}
}
