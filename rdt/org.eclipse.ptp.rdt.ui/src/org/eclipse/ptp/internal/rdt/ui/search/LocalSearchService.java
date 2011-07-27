/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.internal.ui.search.PDOMSearchElementQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchPatternQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchTextSelectionQuery;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.search.ui.ISearchQuery;

/**
 * Provides search results from CDT's local index.
 */
public class LocalSearchService implements ISearchService {
	public ISearchQuery createSearchPatternQuery(Scope indexScope, ICElement[] searchScope, String scopeDescription, String patternStr, boolean isCaseSensitive, int searchFlags) {
		PDOMSearchPatternQuery localQuery = new PDOMSearchPatternQuery(searchScope, scopeDescription, patternStr, isCaseSensitive, searchFlags);
		return localQuery;
	}

	public ISearchQuery createSearchElementQuery(Scope indexScope, ICElement[] searchScope, ISourceReference object, int limitTo) {
		PDOMSearchElementQuery localQuery = new PDOMSearchElementQuery(searchScope, object, limitTo);
		return localQuery;
	}

	public ISearchQuery createSearchTextSelectionQuery(Scope indexScope, ICElement[] searchScope, ITranslationUnit element, ITextSelection selNode, int limitTo) {
		PDOMSearchTextSelectionQuery localQuery = new PDOMSearchTextSelectionQuery(searchScope, element, selNode, limitTo);
		return localQuery;
	}
}
