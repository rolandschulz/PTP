/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core;

import java.io.File;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.core.indexer.StandaloneIndexerInputAdapter;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;

public class RemoteIndexerInputAdapter extends ASTFilePathResolver {
	private static final boolean CASE_INSENSITIVE_FILE_SYSTEM = new File("a").equals(new File("A")); //$NON-NLS-1$//$NON-NLS-2$
	private final StandaloneIndexerInputAdapter adapter = new StandaloneIndexerInputAdapter(null);

	@Override
	public boolean doesIncludeFileExist(String includePath) {
		return adapter.doesIncludeFileExist(includePath);
	}

	@Override
	public String getASTPath(IIndexFileLocation ifl) {
		return adapter.getASTPath(ifl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver#getFileSize(java
	 * .lang.String)
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public long getFileSize(String astFilePath) {
		return adapter.getFileSize(astFilePath);
	}

	@Override
	public boolean isSource(String astFilePath) {
		// TODO need better logic here
		return true;
	}

	@Override
	public IIndexFileLocation resolveASTPath(String astFilePath) {
		return adapter.resolveASTPath(astFilePath);
	}

	@Override
	public IIndexFileLocation resolveIncludeFile(String includePath) {
		return adapter.resolveIncludeFile(includePath);
	}

	@Override
	public boolean isCaseInsensitiveFileSystem() {
		return CASE_INSENSITIVE_FILE_SYSTEM;
	}

}
