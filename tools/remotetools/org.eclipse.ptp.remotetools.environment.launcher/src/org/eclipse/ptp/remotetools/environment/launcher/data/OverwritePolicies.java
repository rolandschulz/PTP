/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.data;

public interface OverwritePolicies {
	
	public static final int UNKNOWN = 0;
	public static final int SKIP = 1;
	public static final int ALWAYS = 2;
	public static final int NEWER = 3;
	public static final int ASK = 4;
	
}
