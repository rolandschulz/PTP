/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.core.index.impl;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.fdt.core.index.IIndexDelta;

public class IndexDelta implements IIndexDelta {

	private ArrayList files = null;
	private IProject project = null;
	private IndexDeltaType deltaType = null; 
	
	/**
	 * @param filesTrav
	 * @param project
	 * 
	 */
	public IndexDelta(IProject project, ArrayList filesTrav) {
		this(project,filesTrav,null);
	}

	/**
	 * @param filesTrav
	 * @param project
	 * 
	 */
	public IndexDelta(IProject project, ArrayList filesTrav, IndexDeltaType indexDeltaType) {
		this.project = project;
		this.files = filesTrav;
		this.deltaType = indexDeltaType;
	}
	
	/**
	 * @return Returns the files.
	 */
	public ArrayList getFiles() {
		return files;
	}
	/**
	 * @return Returns the project.
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @see org.eclipse.fdt.core.index.IIndexDelta#getDeltaType()
	 */
	public IndexDeltaType getDeltaType() {
		return deltaType;
	}
}
