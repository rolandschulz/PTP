/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * @author crecoskie
 *
 */
@SuppressWarnings("deprecation")
public class StandaloneSavedCodeReaderFactory extends AbstractCodeReaderFactory implements ICodeReaderFactory {

	private ICodeReaderCache cache = null;
	
    public static StandaloneSavedCodeReaderFactory getInstance()
    {
        return instance;
    }
    
    private static StandaloneSavedCodeReaderFactory instance = new StandaloneSavedCodeReaderFactory();
    
    private StandaloneSavedCodeReaderFactory()
    {
    	super(null);
		//int size= CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB;
		
		// TODO:  put in a real cache!
		cache = new EmptyCodeReaderCache();
    }

	public int getUniqueIdentifier() {
        return 0; // this is a dumb method
    }


    public CodeReader createCodeReaderForTranslationUnit(String path) {
		return cache.get(path);
    }

    public CodeReader createCodeReaderForTranslationUnit(ITranslationUnit tu) {
		return new CodeReader(tu.getResource().getLocation().toOSString(), tu.getContents());
    }


    public CodeReader createCodeReaderForInclusion(String path) {
		return cache.get(path);
    }
	

	public ICodeReaderCache getCodeReaderCache() {
		return cache;
	}
	
	@Override
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl,
			String astPath) throws CoreException, IOException {
		return cache.get(astPath, ifl);
	}
}
