/****************************
 * IBM Confidential
 * Licensed Materials - Property of IBM
 *
 * IBM Rational Developer for Power Systems Software
 * IBM Rational Team Concert for Power Systems Software
 *
 * (C) Copyright IBM Corporation 2011.
 *
 * The source code for this program is not published or otherwise divested of its trade secrets, 
 * irrespective of what has been deposited with the U.S. Copyright Office.
 */
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
