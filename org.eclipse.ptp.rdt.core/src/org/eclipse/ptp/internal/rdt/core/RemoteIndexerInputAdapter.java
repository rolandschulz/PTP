/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.core.indexer.StandaloneIndexerInputAdapter;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;

public class RemoteIndexerInputAdapter extends ASTFilePathResolver {

	private final StandaloneIndexerInputAdapter adapter = new StandaloneIndexerInputAdapter(null);
	
	
	@Override
	public boolean doesIncludeFileExist(String includePath) {
		return adapter.doesIncludeFileExist(includePath);
	}

	@Override
	public String getASTPath(IIndexFileLocation ifl) {
		return adapter.getASTPath(ifl);
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


}
