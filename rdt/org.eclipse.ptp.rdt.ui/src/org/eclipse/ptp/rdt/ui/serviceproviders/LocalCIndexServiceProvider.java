/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractLocalCIndexServiceProvider;
import org.eclipse.ptp.internal.rdt.ui.contentassist.IContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.contentassist.LocalContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.ui.navigation.LocalNavigationService;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.internal.rdt.ui.search.LocalSearchService;

/**
 * @author mikek
 * @since 4.0
 *
 */
public class LocalCIndexServiceProvider extends AbstractLocalCIndexServiceProvider implements IIndexServiceProvider2 {
	public static final String ID = "org.eclipse.ptp.rdt.ui.LocalCIndexServiceProvider"; //$NON-NLS-1$

	private ISearchService fSearchService;
	private IContentAssistService fContentAssistService;
	private INavigationService fNavigationService;
	
	public boolean isRemote() {
		return false;
	}
	
	public synchronized INavigationService getNavigationService() {
		if(fNavigationService == null)
			fNavigationService = new LocalNavigationService();
		return fNavigationService;
	}
	
	@Override
	public synchronized ISearchService getSearchService() {
		if(fSearchService == null)
			fSearchService = new LocalSearchService();
		return fSearchService;
	}

	@Override
	public synchronized IContentAssistService getContentAssistService() {
		if(fContentAssistService == null)
			fContentAssistService = new LocalContentAssistService();
		return fContentAssistService;
	}

}
