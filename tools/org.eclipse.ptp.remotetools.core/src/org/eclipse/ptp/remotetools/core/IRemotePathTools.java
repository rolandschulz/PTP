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
package org.eclipse.ptp.remotetools.core;

public interface IRemotePathTools {

	public String rootPath();

	public boolean isRoot(String path);

	public boolean isAbsolute(String path);

	public boolean isRelative(String path);

	public boolean isLeave(String path);

	public String leave(String path);

	public String canonicalize(String path);

	public String join(String base, String path);

	public String parent(String path);
	
	public String quote(String path, boolean full);

}
