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
package org.eclipse.cldt.internal.core.model;

 
import java.util.Map;

import org.eclipse.cldt.core.CommonLanguageCore;
import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.IArchive;
import org.eclipse.cldt.core.model.IArchiveContainer;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArchiveContainer extends Openable implements IArchiveContainer {

	public ArchiveContainer (CProject cProject) {
		super (cProject, null, CommonLanguageCore.getResourceString("CoreModel.ArchiveContainer.Archives"), ICElement.C_VCONTAINER); //$NON-NLS-1$
	}

	public IArchive[] getArchives() throws CModelException {
		((ArchiveContainerInfo)getElementInfo()).sync();
		ICElement[] e = getChildren();
		IArchive[] a = new IArchive[e.length];
		System.arraycopy(e, 0, a, 0, e.length);
		return a;
	}

	public CElementInfo createElementInfo() {
		return new ArchiveContainerInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		// this will bootstrap/start the runner for the project.
		CModelManager.getDefault().getBinaryRunner(getCProject(), true);
		return true;
	}

}
