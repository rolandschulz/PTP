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
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteLocationConverter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchElementQuery;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchPatternQuery;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchTextSelectionQuery;
import org.eclipse.ptp.internal.rdt.ui.search.actions.RemoteSearchTextSelectionQueryAdapter;
import org.eclipse.search.ui.ISearchQuery;

/**
 * Provides search results from CDT's local index.
 */
public class LocalSearchService implements ISearchService {
	public ISearchQuery createSearchPatternQuery(Scope indexScope, ICElement[] searchScope, String scopeDescription, String patternStr, boolean isCaseSensitive, int searchFlags) {
		RemoteSearchPatternQuery query = new RemoteSearchPatternQuery(searchScope, scopeDescription, patternStr, isCaseSensitive, searchFlags);
		return new RemoteSearchPatternQueryAdapter(null, indexScope, query);
	}

	public ISearchQuery createSearchElementQuery(Scope indexScope, ICElement[] searchScope, ISourceReference object, int limitTo) {
		String path = EFSExtensionManager.getDefault().getPathFromURI(object.getTranslationUnit().getLocationURI());		
		RemoteSearchElementQuery query = new RemoteSearchElementQuery(searchScope, object, path, limitTo);
		return new RemoteSearchElementQueryAdapter(null, indexScope, query);
	}

	public ISearchQuery createSearchTextSelectionQuery(Scope indexScope, ICElement[] searchScope, ITranslationUnit element, ITextSelection selNode, int limitTo) {
		RemoteSearchTextSelectionQuery query = new RemoteSearchTextSelectionQuery(searchScope, element, selNode.getText(), selNode.getOffset(), selNode.getLength(), limitTo);
		return new RemoteSearchTextSelectionQueryAdapter(null, indexScope, query);
	}
}
