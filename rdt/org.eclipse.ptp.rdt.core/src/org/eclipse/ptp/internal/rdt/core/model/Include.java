/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceReference;

public class Include extends SourceManipulation implements IInclude {
	private static final long serialVersionUID = 1L;

	protected boolean fIsSystemInclude;
	private boolean fIsResolved;
	private boolean fIsActive;
	private String fFullPathName;
	
	public Include(CElement parent, String simpleName, boolean systemInclude) {
		super(parent, ICElement.C_INCLUDE, simpleName);
		fIsSystemInclude = systemInclude;
	}

	public Include(Parent parent, IInclude element) throws CModelException {
		super(parent, element, (ISourceReference) element);
		fIsSystemInclude = element.isStandard();
		fIsResolved = element.isResolved();
		fIsActive = element.isActive();
		fFullPathName = element.getFullFileName();
	}

	public String getFullFileName() {
		return fFullPathName;
	}

	public String getIncludeName() {
		return getElementName();
	}

	public boolean isActive() {
		return fIsActive;
	}

	public boolean isLocal() {
		return false;
	}

	public boolean isResolved() {
		return fIsResolved;
	}

	public boolean isStandard() {
		return fIsSystemInclude;
	}

	public void setFullPathName(String path) {
		fFullPathName = path;
	}

	public void setActive(boolean active) {
		fIsActive = active;
	}

	public void setResolved(boolean resolved) {
		fIsResolved = resolved;
	}

	public void setIndex(int size) {
	}
}
