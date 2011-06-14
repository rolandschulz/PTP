/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.RemoteScannerInfo;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchElementQuery;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchPatternQuery;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchTextSelectionQuery;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.internal.rdt.ui.search.actions.RemoteSearchTextSelectionQueryAdapter;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.search.ui.ISearchQuery;

public class RemoteSearchService extends AbstractRemoteService implements ISearchService {

	public RemoteSearchService(IConnectorService connectorService) {
		super(connectorService);
	}
	
	public RemoteSearchService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}

	public ISearchQuery createSearchPatternQuery(Scope indexScope, ICElement[] searchScope, String scopeDescription, String patternStr, boolean isCaseSensitive, int searchFlags) {
		RemoteSearchPatternQuery query = new RemoteSearchPatternQuery(convertScope(searchScope), scopeDescription, patternStr, isCaseSensitive, searchFlags);
		return new RemoteSearchPatternQueryAdapter(getSubSystem(), indexScope, query);
	}

	private ICElement[] convertScope(ICElement[] searchScope) {
		if (searchScope == null) {
			return null;
		}
		ICElement[] result = new ICElement[searchScope.length];
		for (int i = 0; i < searchScope.length; i++) {
			try {
				result[i] = ModelAdapter.adaptElement(null, searchScope[i], 0, false);
			} catch (CModelException e) {
				RDTLog.logError(e);	
			}
		}
		return result;
	}

	private ISourceReference adaptReference(ISourceReference object) {
		// Working around the lack of multiple inheritance in Java :(
		if (object instanceof ICElement) {
			try {
				ICElement element = ModelAdapter.adaptElement(null, (ICElement) object, 0, true);
				if (element instanceof ISourceReference) {
					return (ISourceReference) element;
				}
			} catch (CModelException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return null;
	}
	
	public ISearchQuery createSearchElementQuery(Scope indexScope, ICElement[] searchScope, ISourceReference object, int limitTo) {
		String path = EFSExtensionManager.getDefault().getPathFromURI(object.getTranslationUnit().getLocationURI());
		RemoteSearchElementQuery query = new RemoteSearchElementQuery(convertScope(searchScope), adaptReference(object), path, limitTo);
		return new RemoteSearchElementQueryAdapter(getSubSystem(), indexScope, query);
	}

	public ISearchQuery createSearchTextSelectionQuery(Scope indexScope, ICElement[] searchScope, ITranslationUnit element, ITextSelection selNode, int limitTo) {
		try {
			ITranslationUnit unit = ModelAdapter.adaptElement(null, element, 0, false);
			if (unit instanceof TranslationUnit) {
				TranslationUnit tu = (TranslationUnit) unit;
				// TODO is it ok to use an empty scanner info?
				tu.setASTContext(new RemoteScannerInfo(), null);
			}
			RemoteSearchTextSelectionQuery query = new RemoteSearchTextSelectionQuery(convertScope(searchScope), unit, selNode.getText(), selNode.getOffset(), selNode.getLength(), limitTo);
			return new RemoteSearchTextSelectionQueryAdapter(getSubSystem(), indexScope, query);
		} catch (CModelException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
