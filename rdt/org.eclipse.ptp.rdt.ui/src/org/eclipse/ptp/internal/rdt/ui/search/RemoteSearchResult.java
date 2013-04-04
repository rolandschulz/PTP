/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchResult
 * Version: 1.17
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.resources.EFSFileStorage;
import org.eclipse.cdt.internal.ui.search.HidePolymorphicCalls;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchElement;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.editors.text.ILocationProviderExtension;
import org.eclipse.ui.part.FileEditorInput;

public class RemoteSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {

	private static final String KEY_SHOW_POLYMORPHIC_CALLS = "ShowPolymorphicCalls"; //$NON-NLS-1$
	final static MatchFilter[] ALL_FILTERS = new MatchFilter[] {HidePolymorphicCalls.FILTER};
	final static MatchFilter[] NO_FILTERS = {};

	private RemoteSearchQueryAdapter fQuery;
	private boolean indexerBusy;
	
	public RemoteSearchResult(RemoteSearchQueryAdapter query) {
		super();
		this.fQuery = query;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	private String getFileName(IEditorPart editor) {
		final IEditorInput input = editor.getEditorInput();
		String pathStr= null;

		if (input instanceof FileEditorInput) {
			final FileEditorInput fileInput = (FileEditorInput)input;
			
			IPath location = fileInput.getFile().getLocation();
			
			if(location != null)
				pathStr= fileInput.getFile().getLocation().toOSString();
			else
				pathStr= fileInput.getFile().getLocationURI().toString();
		} else if (input instanceof ExternalEditorInput) {
			final ExternalEditorInput extInput = (ExternalEditorInput)input;
			if(extInput.getPath() != null) 	{
				pathStr= extInput.getPath().toOSString();
			}
			else {
				pathStr= extInput.getURI().toString();
			}
				
		} else if (input instanceof IStorageEditorInput) {
				try {
					final IStorage storage = ((IStorageEditorInput)input).getStorage();
					if (storage.getFullPath() != null) {
						pathStr= storage.getFullPath().toOSString();
					}
				} catch (CoreException exc) {
					// ignore
				}
		} else if (input instanceof IPathEditorInput) {
			IPath path= ((IPathEditorInput)input).getPath();
			if(path!=null)
				pathStr = path.toOSString();
		} else {
			ILocationProvider provider= (ILocationProvider) input.getAdapter(ILocationProvider.class);
			if (provider != null) {
				IPath path= provider.getPath(input);
				if(path!=null){
					pathStr=path.toOSString();
				}
			}
		}		

		return pathStr;
	}
	
	private URI getLocationURI(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (input instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput)input;
			
			return fileInput.getFile().getLocationURI();
		} else if (input instanceof ExternalEditorInput) {
			ExternalEditorInput extInput = (ExternalEditorInput)input;
			if (extInput.getTranslationUnit() == null) {
				return null;
			}
			return extInput.getTranslationUnit().getLocationURI();
		} else if (input instanceof IStorageEditorInput) {
				try {
					IStorage storage = ((IStorageEditorInput)input).getStorage();
					if (storage instanceof EFSFileStorage) {
						return ((EFSFileStorage) storage).getLocationURI();
					}
				} catch (CoreException exc) {
					// ignore
				}
		} else if (input instanceof IPathEditorInput) {
			IPath path= ((IPathEditorInput)input).getPath();
			return URIUtil.toURI(path);
		}
		ILocationProviderExtension provider= (ILocationProviderExtension) input.getAdapter(ILocationProviderExtension.class);
		if (provider != null) {
			return provider.getURI(input);
		}
		return null;
	}
	
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		URI uri = getLocationURI(editor);
		if (uri != null && match instanceof RemoteSearchMatchAdapter) {
			return uri.normalize().equals((((RemoteSearchMatchAdapter)match).getLocation().getURI().normalize()));
		}
		return false;
	}
	
	private Match[] computeContainedMatches(AbstractTextSearchResult result, String filename) throws CoreException {
		IPath pfilename= new Path(filename);
		List<Match> list = new ArrayList<Match>(); 
		Object[] elements = result.getElements();
		for (int i = 0; i < elements.length; ++i) {
			if (pfilename.equals(IndexLocationFactory.getAbsolutePath(((RemoteSearchElement)elements[i]).getLocation()))) {
				Match[] matches = result.getMatches(elements[i]);
				for (int j = 0; j < matches.length; ++j) {
					if (matches[j] instanceof RemoteSearchMatchAdapter) {
						list.add(matches[j]);
					}
				}
			}
		}
		return list.toArray(new Match[list.size()]);
	}
	
	private Match[] computeContainedMatches(AbstractTextSearchResult result, URI locationURI) throws CoreException {
		List<Match> list = new ArrayList<Match>(); 
		Object[] elements = result.getElements();
		for (int i = 0; i < elements.length; ++i) {
			if (locationURI.normalize().equals(((RemoteSearchElement)elements[i]).getLocation().getURI().normalize())) {
				Match[] matches = result.getMatches(elements[i]);
				for (int j = 0; j < matches.length; ++j) {
					if (matches[j] instanceof RemoteSearchMatchAdapter) {
						list.add(matches[j]);
					}
				}
			}
		}
		return list.toArray(new Match[list.size()]);
	}
	
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		try {
			URI uri = getLocationURI(editor);
			if (uri != null)
				return computeContainedMatches(result, uri);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return new Match[0];
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		try {
			URI uri = file.getLocationURI();
			return computeContainedMatches(result, uri);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return new Match[0];
	}

	public IFile getFile(Object element) {
		if (element instanceof IIndexName) {
			IIndexName name = (IIndexName)element;
			try {
				IIndexFileLocation location = name.getFile().getLocation();
				if(location.getFullPath()!=null) {
					return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(location.getFullPath()));
				}
				else {
					return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(location.getURI())[0];
				}
			} catch(CoreException ce) { /* fall-through to return null */ }
		}
		else if (element instanceof RemoteSearchElement) {
			RemoteSearchElement searchElement = (RemoteSearchElement) element;

			IIndexFileLocation location = searchElement.getLocation();
			if (location.getFullPath() != null) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(location.getFullPath()));
			} else {
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(location.getURI());
				if(files != null && files.length > 0) 
					return files[0];
			}
		}
		return null;
	}

	public String getLabel() {
		// report pattern and number of matches
		return fQuery.getResultLabel(getMatchCount());
		
	}

	public String getTooltip() {
		return null;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public RemoteSearchQueryAdapter getQuery() {
		return fQuery;
	}

	/**
	 * Remember whether the indexer was busy when the search was performed.
	 * @param b
	 */
	public void setIndexerBusy(boolean b) {
		this.indexerBusy = b;
	}
	
	/**
	 * Tell if the indexer was busy when search results were gathered.
	 */
	public boolean wasIndexerBusy() {
		return indexerBusy;
	}
	
	@Override
	public MatchFilter[] getAllMatchFilters() {
		return ALL_FILTERS;
	}

	@Override
	public MatchFilter[] getActiveMatchFilters() {	
		MatchFilter[] result = super.getActiveMatchFilters();
		if (result == null) {
			if (CUIPlugin.getDefault().getDialogSettings().getBoolean(KEY_SHOW_POLYMORPHIC_CALLS)) {
				return ALL_FILTERS;
			}
			return NO_FILTERS;
		}
		return result;
	}

	@Override
	public void setActiveMatchFilters(MatchFilter[] filters) {
		boolean showPoly= false;
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] == HidePolymorphicCalls.FILTER) {
				showPoly= true;
			}
		}
		CUIPlugin.getDefault().getDialogSettings().put(KEY_SHOW_POLYMORPHIC_CALLS, showPoly);
		super.setActiveMatchFilters(filters);
	}


}
