/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.core/browser
 * Class: org.eclipse.cdt.internal.core.browser.ASTTypeReference
 * Version: 1.1
 */

package org.eclipse.ptp.internal.rdt.core.search;

import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class RemoteASTTypeReference implements ITypeReference {
	IIndexFileLocation fFileLocation;
	int fOffset;
	int fLength;
	IPath fLocation;
	
	public RemoteASTTypeReference(IIndexFileLocation fileLocation, IBinding resolveBinding, IPath location, int offset, int length) {
		fFileLocation = fileLocation;
		fOffset = offset;
		fLength = length;
		fLocation = location;
	}

	public ICElement[] getCElements() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLength() {
		return fLength;
	}

	public IPath getLocation() {
		return fLocation;
	}

	public int getOffset() {
		return fOffset;
	}

	public IPath getPath() {
		return fLocation;
	}

	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath getRelativeIncludePath(IProject project) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath getRelativePath(IPath relativeToPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public IResource getResource() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITranslationUnit getTranslationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	public IWorkingCopy getWorkingCopy() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isLineNumber() {
		// TODO Auto-generated method stub
		return false;
	}

	public IIndexFileLocation getIFL() {
		return fFileLocation;
	}
}
