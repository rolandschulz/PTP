/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.fdt.managedbuilder.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.fdt.core.CCorePlugin;
import org.eclipse.fdt.core.ICDescriptor;
import org.eclipse.fdt.core.ICOwner;

/**
 * @since 2.0
 */
public class ManagedMakeProject implements ICOwner {

	/**
	 * Zero-argument constructor to fulfill the contract for 
	 * implementation calsses supplied via an extension point 
	 */
	public ManagedMakeProject() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.ICOwner#configure(org.eclipse.fdt.core.ICDescriptor)
	 */
	public void configure(ICDescriptor cproject) throws CoreException {
		cproject.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		cproject.remove(CCorePlugin.BUILDER_MODEL_ID);
		cproject.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.ICOwner#update(org.eclipse.fdt.core.ICDescriptor, java.lang.String)
	 */
	public void update(ICDescriptor cproject, String extensionID)
			throws CoreException {
		// TODO Auto-generated method stub

	}
	
	private void updateBinaryParsers(ICDescriptor cproject) throws CoreException {
	}
}
