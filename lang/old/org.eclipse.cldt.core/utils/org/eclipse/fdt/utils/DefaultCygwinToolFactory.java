/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.utils;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.fdt.core.ICExtension;
import org.eclipse.fdt.core.ICExtensionReference;


public class DefaultCygwinToolFactory extends DefaultGnuToolFactory implements ICygwinToolsFactroy {

	
	/**
	 * 
	 */
	public DefaultCygwinToolFactory(ICExtension ext) {
		super(ext);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.utils.CygwinToolsProvider#getCygPath()
	 */
	public CygPath getCygPath() {
		IPath cygPathPath = getCygPathPath();
		CygPath cygpath = null;
		if (cygPathPath != null && !cygPathPath.isEmpty()) {
			try {
				cygpath = new CygPath(cygPathPath.toOSString());
			} catch (IOException e1) {
			}
		}
		return cygpath;
	}

	protected IPath getCygPathPath() {
		ICExtensionReference ref =  fExtension.getExtensionReference();
		String value = ref.getExtensionData("cygpath"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "cygpath"; //$NON-NLS-1$
		}
		return new Path(value);
	}

}
