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
package org.eclipse.fdt.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.fdt.core.ICDescriptor;
import org.eclipse.fdt.core.ICOwner;
import org.eclipse.fdt.core.ICOwnerInfo;

public class COwner implements ICOwnerInfo {

	final COwnerConfiguration fConfig;

	public COwner(COwnerConfiguration config) throws CoreException {
		fConfig = config;
	}

	public String getID() {
		return fConfig.getOwnerID();
	}

	public String getName() {
		return fConfig.getName();
	}

	public String getPlatform() {
		return fConfig.getPlatform();
	}

	void configure(IProject project, ICDescriptor cproject) throws CoreException {
		ICOwner owner = fConfig.createOwner();
		if (owner != null) {
			owner.configure(cproject);
		}
	}

	void update(IProject project, ICDescriptor cproject, String extensionID) throws CoreException {
		ICOwner owner = fConfig.createOwner();
		if (owner != null) {
			owner.update(cproject, extensionID);
		}
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof COwner) {
			return ((COwner) obj).getID().equals(getID());
		}
		return false;
	}

	public int hashCode() {
		return getID().hashCode();
	}
}
