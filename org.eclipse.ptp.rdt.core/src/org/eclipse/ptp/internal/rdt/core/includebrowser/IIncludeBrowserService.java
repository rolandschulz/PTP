/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IIncludeBrowserService {
	IIndexInclude[] findIncludedBy(IIndexFileLocation ifl, IProgressMonitor progress);
	IIndexInclude[] findIncludesTo(IIndexFileLocation ifl, IProgressMonitor progress);
	IIndexInclude findInclude(IInclude input) throws CoreException;
}
