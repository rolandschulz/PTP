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

 
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cldt.core.CommonLanguageCore;
import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.IBinary;
import org.eclipse.cldt.core.model.IBinaryContainer;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

public class BinaryContainer extends Openable implements IBinaryContainer {

	public BinaryContainer (CProject cProject) {
		super (cProject, null, CommonLanguageCore.getResourceString("CoreModel.BinaryContainer.Binaries"), ICElement.C_VCONTAINER); //$NON-NLS-1$
	}

	public IBinary[] getBinaries() throws CModelException {
		((BinaryContainerInfo)getElementInfo()).sync();
		ICElement[] e = getChildren();
		ArrayList list = new ArrayList(e.length);
		for (int i = 0; i < e.length; i++) {
			if (e[i] instanceof IBinary) {
				IBinary bin = (IBinary)e[i];
				if (bin.isExecutable() || bin.isSharedLib()) {
					list.add(bin);
				}
			}
		}
		IBinary[] b = new IBinary[list.size()];
		list.toArray(b);
		return b;
	}

	public CElementInfo createElementInfo() {
		return new BinaryContainerInfo(this);
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
