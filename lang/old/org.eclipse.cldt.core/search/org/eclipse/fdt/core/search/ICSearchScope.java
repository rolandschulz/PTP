/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 12, 2003
 */
package org.eclipse.fdt.core.search;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.model.ICElement;

public interface ICSearchScope {
	/**
	 * Checks whether the resource at the given path is enclosed by this scope.
	 *
	 * @param resourcePath if the resource is contained in
	 * @return whether the resource is enclosed by this scope
	 */
	public boolean encloses(String resourcePath);
	/**
	 * Checks whether this scope encloses the given element.
	 *
	 * @param element the given element
	 * @return <code>true</code> if the element is in this scope
	 */
	public boolean encloses(ICElement element);
	/**
	 * Returns the paths to the enclosing projects for this search scope.
	 * <ul>
	 * <li> If the path is a project path, this is the full path of the project
	 *       (see <code>IResource.getFullPath()</code>).
	 *        For example, /MyProject
	 * </li>
	 * 
	 * @return an array of paths to the enclosing projects.
	 */
	IPath[] enclosingProjects();

}
