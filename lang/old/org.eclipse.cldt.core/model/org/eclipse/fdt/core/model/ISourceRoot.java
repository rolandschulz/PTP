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

package org.eclipse.fdt.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * ISourceRoot
 */
public interface ISourceRoot extends ICContainer {

	/**
	 * @param resource
	 * @return
	 */
	boolean isOnSourceEntry(IResource resource);

	/**
	 * @param path
	 * @return
	 */
	boolean isOnSourceEntry(IPath path);

	/**
	 * @param element
	 * @return
	 */
	boolean isOnSourceEntry(ICElement element);

}
