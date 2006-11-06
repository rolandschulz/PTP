/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rmsystem.events;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class ResourceManagerContentsChangedEvent implements
		IResourceManagerContentsChangedEvent {

	private final IResourceManager source;
	private final int changedElementClass;
	private final int[] changedElementIds;

	public ResourceManagerContentsChangedEvent(final IResourceManager source, 
			final int changedElementClass, final int[] changedElementIds) throws CoreException {
		this.source = source;
		this.changedElementClass = changedElementClass;
		this.changedElementIds = (int[]) changedElementIds.clone();
		checkChangedElementClass();
	}

	private void checkChangedElementClass() throws CoreException {
		switch (changedElementClass) {
		case MACHINE:
		case QUEUE:
			return;
		}
		final String message = "Unknown element class in ResourceManagerContentsChangedEvent ctor.";
		IStatus status = new Status(Status.ERROR, PTPCorePlugin.PLUGIN_ID, Status.OK, message,
				null);
		throw new CoreException(status);
	}

	public int getChangedElementClass() {
		return changedElementClass;
	}

	public int[] getChangedElementIds() {
		return changedElementIds;
	}

	public IResourceManager getSource() {
		return source;
	}

}
