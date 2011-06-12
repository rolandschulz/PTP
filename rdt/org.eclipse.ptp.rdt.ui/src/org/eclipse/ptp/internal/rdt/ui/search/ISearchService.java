/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.search.ui.ISearchQuery;

/**
 * Provides search queries that are compatible with Eclipse's search framework.
 */
public interface ISearchService {
	/**
	 * Returns an ISearchQuery that can perform a code search for the given scope
	 * and search parameters.  The resulting ISearchQuery must provide a
	 * RemoteSearchResult.
	 * 
	 * @param scope
	 * @param scopeDescription
	 * @param patternStr
	 * @param isCaseSensitive
	 * @param searchFlags
	 * @return
	 * @throws CoreException 
	 */
	ISearchQuery createSearchPatternQuery(Scope indexScope, ICElement[] searchScope, String scopeDescription, String patternStr, boolean isCaseSensitive, int searchFlags);

	/**
	 * 
	 * @param scope
	 * @param object
	 * @param limitTo
	 * @return
	 * @throws CoreException 
	 */
	ISearchQuery createSearchElementQuery(Scope indexScope, ICElement[] searchScope, ISourceReference object, int limitTo);

	/**
	 * 
	 * @param scope
	 * @param element
	 * @param selNode
	 * @param limitTo
	 * @return
	 * @throws CoreException
	 */
	ISearchQuery createSearchTextSelectionQuery(Scope indexScope, ICElement[] searchScope, ITranslationUnit element, ITextSelection selNode, int limitTo);
}
