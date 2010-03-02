/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Photran modifications
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * Edited by Matthew Michelotti
 * 
 * Overview of changes:
 * -may have changed import statements
 * -added unimplemented methods to conform with new versions of
 *  CDT interfaces. Deprecated these methods.
 */
package org.eclipse.photran.internal.core.preprocessor.c;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
//import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * This is an empty implementation of the ICodeReaderCache interface.  It is used to implement a 
 * cache for the interface that isn't actually a cache, but rather always creates new CodeReaders
 * everytime a CodeReader is retrieved. 
 * 
 * This cache is not optimized to be run from within Eclipse (i.e. it ignores IResources).
 * 
 * @author dsteffle
 */
public class EmptyCodeReaderCache implements ICodeReaderCache {

	/**
	 * Creates a new CodeReader for the given file location.
	 */
	public CodeReader get(String location) {
		CodeReader ret = null;
		ret = InternalParserUtil.createFileReader(location);
		return ret;
	}
	
	/**
	 * This provides support for PartialWorkingCopyCodeReaderFactory.
	 * @param finalPath
	 * @param workingCopies
	 */
	public CodeReader createReader(String finalPath, Iterator<IWorkingCopy> workingCopies ) {
		return InternalParserUtil.createFileReader(finalPath);
	}

	/**
	 * Returns null.
	 */
	public CodeReader remove(String key) {
		return null;
	}

	/**
	 * Returns 0.
	 */
	public int getCurrentSpace() {
		return 0;
	}

	public void flush() {
		// nothing to do
		
	}

    @Deprecated
    //method added to conform with CDT interface
	public CodeReader get(String key, IIndexFileLocation ifl)
			throws CoreException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
