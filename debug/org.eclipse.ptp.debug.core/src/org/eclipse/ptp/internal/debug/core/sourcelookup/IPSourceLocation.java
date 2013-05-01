/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author Clement chu
 * 
 */
public interface IPSourceLocation extends IAdaptable {
	/**
	 * 
	 */
	public void dispose();

	/**
	 * @param name
	 * @return
	 * @throws CoreException
	 */
	public Object findSourceElement(String name) throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getMemento() throws CoreException;

	/**
	 * @param memento
	 * @throws CoreException
	 */
	public void initializeFrom(String memento) throws CoreException;

	/**
	 * @return
	 */
	public boolean searchForDuplicateFiles();

	/**
	 * @param search
	 */
	public void setSearchForDuplicateFiles(boolean search);
}
