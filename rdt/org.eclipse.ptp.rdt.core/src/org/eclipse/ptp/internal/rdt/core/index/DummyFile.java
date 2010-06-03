/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.io.Serializable;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.core.runtime.CoreException;

/**
 * @author crecoskie
 *
 */
public class DummyFile implements IIndexFile, Serializable {
	private static final long serialVersionUID = 1L;
	
	IIndexFileLocation fLocation;
	
	public DummyFile(IIndexFileLocation location) {
		fLocation = location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#findNames(int, int)
	 */
	public IIndexName[] findNames(int offset, int length) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getContentsHash()
	 */
	public long getContentsHash() throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getIncludes()
	 */
	public IIndexInclude[] getIncludes() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getLinkageID()
	 */
	public int getLinkageID() throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getLocation()
	 */
	public IIndexFileLocation getLocation() throws CoreException {
		return fLocation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getMacros()
	 */
	public IIndexMacro[] getMacros() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getParsedInContext()
	 */
	public IIndexInclude getParsedInContext() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getScannerConfigurationHashcode()
	 */
	public int getScannerConfigurationHashcode() throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getTimestamp()
	 */
	public long getTimestamp() throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getUsingDirectives()
	 */
	public ICPPUsingDirective[] getUsingDirectives() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
