/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cldt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cldt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cldt.core.parser.ast.IASTInclusion;
import org.eclipse.cldt.core.search.ICSearchScope;
import org.eclipse.cldt.internal.core.CharOperation;
import org.eclipse.cldt.internal.core.index.IEntryResult;
import org.eclipse.cldt.internal.core.index.impl.IndexInput;
import org.eclipse.cldt.internal.core.index.impl.IndexedFile;
import org.eclipse.cldt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cldt.internal.core.search.indexing.AbstractIndexer;

/**
 * @author bgheorgh
 */
public class IncludePattern extends CSearchPattern {
	protected char [] simpleName;
	protected char [] decodedSimpleName;
	/**
	 * 
	 */
	public IncludePattern(char[] name, int matchMode, LimitTo limitTo, boolean caseSensitive) {
		super( matchMode, caseSensitive, limitTo );	
		simpleName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#decodeIndexEntry(org.eclipse.cdt.internal.core.index.IEntryResult)
	 */
	protected void decodeIndexEntry(IEntryResult entryResult) {
		char[] word = entryResult.getWord();
	
		int firstSlash = CharOperation.indexOf( SEPARATOR, word, 0 );

		this.decodedSimpleName = CharOperation.subarray(word, firstSlash + 1, -1);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#feedIndexRequestor(org.eclipse.cdt.internal.core.search.IIndexSearchRequestor, int, int[], org.eclipse.cdt.internal.core.index.impl.IndexInput, org.eclipse.cdt.core.search.ICSearchScope)
	 */
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, int[] indexFlags, IndexInput input, ICSearchScope scope) throws IOException {
		for (int i = 0, max = references.length; i < max; i++) {
			IndexedFile file = input.getIndexedFile(references[i]);
			String path;
			if (file != null && scope.encloses(path =file.getPath())) {
				requestor.acceptIncludeDeclaration(path, decodedSimpleName);
			}
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#resetIndexInfo()
	 */
	protected void resetIndexInfo() {
		decodedSimpleName = null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#indexEntryPrefix()
	 */
	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestIncludePrefix(
							_limitTo,
							simpleName,
							_matchMode, _caseSensitive
			);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#matchIndexEntry()
	 */
	protected boolean matchIndexEntry() {
		/* check simple name matches */
		if (simpleName != null){
			if( ! matchesName( simpleName, decodedSimpleName ) ){
				return false; 
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchPattern#matchLevel(org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate, org.eclipse.cdt.core.search.ICSearchConstants.LimitTo)
	 */
	public int matchLevel(ISourceElementCallbackDelegate node, LimitTo limit) {
		// TODO Auto-generated method stub
		if (!( node instanceof IASTInclusion )) {
			return IMPOSSIBLE_MATCH;
		}
		
		if( ! canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
		
		IASTInclusion inc = (IASTInclusion) node;
		String fileName = inc.getFullFileName();
		
		if(CharOperation.equals(simpleName,fileName.toCharArray(),_caseSensitive)){
			return ACCURATE_MATCH;
		}
		
		return IMPOSSIBLE_MATCH;
	}

}
