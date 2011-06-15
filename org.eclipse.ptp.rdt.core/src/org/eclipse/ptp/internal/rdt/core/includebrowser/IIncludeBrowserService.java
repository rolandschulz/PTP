/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.includebrowser;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IIncludeBrowserService {

	IIndexIncludeValue[] findIncludedBy(IIndexFileLocation location, ICProject project, IProgressMonitor monitor);
	
	IIndexIncludeValue[] findIncludesTo(IIndexFileLocation location, ICProject project, IProgressMonitor monitor);
	
	IIndexIncludeValue findInclude(IInclude input, IProgressMonitor monitor) throws CoreException;
	
	boolean isIndexed(IIndexFileLocation location, ICProject project, IProgressMonitor monitor);
}
