/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CalledByResult;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallsToResult;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask;
import org.eclipse.ptp.internal.rdt.core.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraph;
import org.eclipse.ptp.internal.rdt.ui.contentassist.IContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.ui.IMemento;

/**
 * A C/C++ indexing service provider that does nothing.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author vkong
 * @author crecoskie
 *
 */
public class NullCIndexServiceProvider extends
		AbstractRemoteCIndexServiceProvider implements IIndexServiceProvider2 {

	private final class NullSearchQuery implements ISearchQuery {		
		
		private final class NullSearchResult implements ISearchResult {
			private ISearchQuery fQuery = null;
			
			public NullSearchResult(ISearchQuery query) {
				fQuery = query;
			}

			public void addListener(ISearchResultListener l) {
				// do nothing
			}

			public ImageDescriptor getImageDescriptor() {
				// no image
				return null;
			}

			public String getLabel() {
				return Messages.getString("NullCIndexServiceProvider.1"); //$NON-NLS-1$
			}

			public ISearchQuery getQuery() {
				return fQuery;
			}

			public String getTooltip() {
				return null;
			}

			public void removeListener(ISearchResultListener l) {
				// do nothing
			}
		}

		public boolean canRerun() {
			return false;
		}

		public boolean canRunInBackground() {
			return true;
		}

		public String getLabel() {
			return Messages.getString("NullCIndexServiceProvider.0"); //$NON-NLS-1$
		}

		public ISearchResult getSearchResult() {
			return new NullSearchResult(this);
		}

		public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
			return Status.OK_STATUS;
		}
	}

	public static final String ID = "org.eclipse.ptp.rdt.ui.NullCIndexServiceProvider"; //$NON-NLS-1$
	public static final String NAME = Messages.getString("NullCIndexServiceProvider.name"); //$NON-NLS-1$
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$
	
	/**
	 * @param id
	 * @param name
	 * @param serviceId
	 */
	public NullCIndexServiceProvider(String id, String name, String serviceId) {
		super(id, name, serviceId);
	}

	/**
	 * 
	 */
	public NullCIndexServiceProvider() {
		this(ID, NAME, SERVICE_ID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider#getCallHierarchyService()
	 */
	@Override
	public ICallHierarchyService getCallHierarchyService() {
		return new ICallHierarchyService () {

			public CalledByResult findCalledBy(Scope scope, ICElement callee, IProgressMonitor pm) throws CoreException,
					InterruptedException {
				return new CalledByResult();
			}

			public CallsToResult findCalls(Scope scope, ICElement caller, IProgressMonitor pm) throws CoreException,
					InterruptedException {
				return new CallsToResult();
			}

			public ICElement[] findDefinitions(Scope scope, ICElement input, IProgressMonitor pm) {
				return new ICElement[0];
			}

			public ICElement[] findDefinitions(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart,
					int selectionLength, IProgressMonitor pm) throws CoreException {
				return new ICElement[0];
			}
			
		}; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider#getHost()
	 */
	@Override
	public IHost getHost() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider#getIndexLifeCycleService()
	 */
	@Override
	public synchronized IIndexLifecycleService getIndexLifeCycleService() {
		return new IIndexLifecycleService () {

			public Scope createScope(String name, List<ICElement> elements, IProgressMonitor monitor) {
				return null;
			}

			public Scope getScope(String name) {
				return null;
			}

			public Set<Scope> getScopes() {
				return Collections.emptySet();
			}

			public void reindex(Scope scope, List<ICElement> elements, IProgressMonitor monitor, RemoteIndexerTask task) {
				// do nothing
				
			}

			public void reindex(Scope scope, IProgressMonitor monitor, RemoteIndexerTask task) {
				// do nothing
				
			}

			public void update(Scope scope, List<ICElement> asList,
					List<ICElement> asList2, List<ICElement> asList3,
					IProgressMonitor monitor, RemoteIndexerTask task) {
				// do nothing
				
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider#getNavigationService()
	 */
	@Override
	public INavigationService getNavigationService() {
		return new INavigationService() {

			public OpenDeclarationResult openDeclaration(Scope scope,
					ITranslationUnit workingCopy, String selectedText,
					int selectionStart, int selectionLength,
					IProgressMonitor monitor) {
				
				return null;
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider#getTypeHierarchyService()
	 */
	@Override
	public ITypeHierarchyService getTypeHierarchyService() {
		return new ITypeHierarchyService () {

			public THGraph computeGraph(Scope scope, ICElement input, IProgressMonitor monitor) throws CoreException,
					InterruptedException {
				return new THGraph();
			}

			public ICElement[] findInput(Scope scope, ICElement memberInput, IProgressMonitor monitor) {
				return new ICElement[0];
			}

			public ICElement[] findInput(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart,
					int selectionLength, IProgressMonitor monitor) throws CoreException {
				return new ICElement[0];
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2#getContentAssistService()
	 */
	public IContentAssistService getContentAssistService() {
		return new IContentAssistService() {

			public List<Proposal> computeCompletionProposals(Scope scope, ContentAssistInvocationContext context,
					ITranslationUnit unit) {
				return Collections.emptyList();
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2#getSearchService()
	 */
	public ISearchService getSearchService() {
		return new ISearchService () {

			public ISearchQuery createSearchElementQuery(Scope indexScope, ICElement[] searchScope, ISourceReference object,
					int limitTo) {
				return new NullSearchQuery();
			}

			public ISearchQuery createSearchPatternQuery(Scope indexScope, ICElement[] searchScope, String scopeDescription,
					String patternStr, boolean isCaseSensitive, int searchFlags) {
				return new NullSearchQuery();
			}

			public ISearchQuery createSearchTextSelectionQuery(Scope indexScope, ICElement[] searchScope,
					ITranslationUnit element, ITextSelection selNode, int limitTo) {
				return new NullSearchQuery();
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider#isConfigured()
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#getConfigurationString()
	 */
	public String getConfigurationString() {
		return Messages.getString("NullServiceProvider.config"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		// does not restore anything
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		// does not save anything
	}
}
