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


/**
 * Represents a container of all the IArchive's found in the project
 * while inspecting the project.
 */
public interface IArchiveContainer extends ICElement, IParent, IOpenable {
	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public IArchive[] getArchives() throws CModelException;
}
