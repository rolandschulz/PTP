/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.internal.rdt.ui.contentassist.IContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteCCodeFoldingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteCodeFormattingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteInactiveHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteSemanticHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public interface IIndexServiceProvider2 extends IIndexServiceProvider {
	public ISearchService getSearchService();

	public IContentAssistService getContentAssistService();

	/**
	 * @since 4.1
	 */
	public INavigationService getNavigationService();

	/**
	 * @since 4.1
	 */
	public IRemoteSemanticHighlightingService getRemoteSemanticHighlightingService();

	/**
	 * @since 4.1
	 */
	public IRemoteCCodeFoldingService getRemoteCodeFoldingService();

	/**
	 * @since 4.3
	 */
	public IRemoteCodeFormattingService getRemoteCodeFormattingService();
}
