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
package org.eclipse.cldt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cldt.utils.DefaultCygwinToolFactory;
import org.eclipse.cldt.utils.ICygwinToolsFactroy;
import org.eclipse.core.runtime.IPath;

/**
 */
public class CygwinPEParser extends PEParser {

	
	private DefaultCygwinToolFactory toolFactory;

	/**
	 * @see org.eclipse.cldt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "Cygwin PE"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.coff.parser.PEParser#createBinaryArchive(org.eclipse.core.runtime.IPath)
	 */
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new CygwinPEBinaryArchive(this, path);
	}
	/**
	 * @param path
	 * @return
	 */
	protected IBinaryExecutable createBinaryExecutable(IPath path) {
		return new CygwinPEBinaryExecutable(this, path, IBinaryFile.EXECUTABLE);
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryObject createBinaryCore(IPath path) {
		return new CygwinPEBinaryObject(this, path, IBinaryFile.CORE);
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryObject createBinaryObject(IPath path) {
		return new CygwinPEBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryShared createBinaryShared(IPath path) {
		return new CygwinPEBinaryShared(this, path);
	}
	
	/**
	 * @return
	 */
	protected DefaultCygwinToolFactory createToolFactory() {
		return new DefaultCygwinToolFactory(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(ICygwinToolsFactroy.class)) {
			if (toolFactory == null) {
				toolFactory = createToolFactory(); 
			}
			return toolFactory;
		}
		// TODO Auto-generated method stub
		return super.getAdapter(adapter);
	}
}