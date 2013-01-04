/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchQuery
 * Version: 1.34
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteLineSearchElement;
import org.eclipse.ptp.internal.rdt.core.search.RemoteLineSearchElement.RemoteLineSearchElementMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchTextSelectionQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Adapts RemoteSearchQuery instances so that they may be used with Eclipse's
 * search framework. 
 */
public abstract class RemoteSearchQueryAdapter implements ISearchQuery {

	protected ICIndexSubsystem fSubsystem;
	protected RemoteSearchQuery fQuery;
	protected RemoteSearchResult fResult;
	protected Scope fScope;
	
	private final static MatchesComparator MATCHES_COMPARATOR = new MatchesComparator();
	
	protected static final long LABEL_FLAGS= 
		CElementLabels.M_PARAMETER_TYPES | 
		CElementLabels.ALL_FULLY_QUALIFIED |
		CElementLabels.TEMPLATE_ARGUMENTS;

	protected RemoteSearchQueryAdapter() {
		throw new IllegalStateException();
	}

	public RemoteSearchQueryAdapter(ICIndexSubsystem subsystem, Scope scope, RemoteSearchQuery query) {
		fSubsystem = subsystem;
		fScope = scope;
		
		if(query == null) {
			throw new IllegalArgumentException("Query must not be null."); //$NON-NLS-1$
		}
			
		
		fQuery = query;
		fResult = new RemoteSearchResult(this);
	}
	
	protected String labelForBinding(String defaultLabel) {
		if(fQuery == null) {
			return null;
		}
		
		ICElement elem = fQuery.getcElement();
	
		if (elem != null) {
			return CElementLabels.getElementLabel(elem, LABEL_FLAGS);
		}

		return defaultLabel;
	}
	
	private static final class MatchesComparator implements Comparator<Match> {
		public int compare(Match m1, Match m2) {
			int diff=0;
			if(m1 instanceof RemoteSearchMatchAdapter && m2 instanceof RemoteSearchMatchAdapter){
				RemoteSearchMatchAdapter rm1 = (RemoteSearchMatchAdapter)m1;
				RemoteSearchMatchAdapter rm2 = (RemoteSearchMatchAdapter)m2;
				IIndexFileLocation rm1_loc= rm1.getLocation();
				IIndexFileLocation rm2_loc= rm2.getLocation();
				if(rm1_loc!=null && rm2_loc!=null){
					URI rm1_uri = rm1_loc.getURI();
					URI rm2_uri = rm2_loc.getURI();
					if(rm1_uri!=null && rm2_uri!=null){
						diff = rm1_uri.compareTo(rm2_uri);
					}
				}
			}
			if(diff == 0){
			   diff= m1.getOffset() - m2.getOffset();
			}
			if (diff == 0){
				diff= m2.getLength() -m1.getLength();
			}
		
			
			return diff;
		}
	}
	
	public boolean canRerun() {
		return fQuery != null ? fQuery.canRerun() : true;
	}

	public boolean canRunInBackground() {
		return fQuery != null ? fQuery.canRunInBackground() : true;
	}
	
	public abstract String getResultLabel(int matchCount);
	
	public String getResultLabel(String pattern, int matchCount) {
		return getResultLabel(pattern, null, matchCount);
	}
	
	public String getResultLabel(String pattern, String scope, int matchCount) {
		
		if(fQuery == null) {
			return org.eclipse.ptp.rdt.ui.messages.Messages.getString("RemoteSearchPatternQueryAdapter_0"); //$NON-NLS-1$
		}
		
		// Report pattern and number of matches
		String label;
		final int kindFlags= fQuery.getFlags() & RemoteSearchQuery.FIND_ALL_OCCURRENCES;
		switch (kindFlags) {
		case RemoteSearchQuery.FIND_REFERENCES:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_refs_result_label, pattern);
			break;
		case RemoteSearchQuery.FIND_DECLARATIONS:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_decls_result_label, pattern);
			break;
		case RemoteSearchQuery.FIND_DEFINITIONS:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_defs_result_label, pattern);
			break;
		case RemoteSearchQuery.FIND_DECLARATIONS_DEFINITIONS:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_decldefs_result_label, pattern);
			break;
		default:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_occurrences_result_label, pattern);
			break;
		}

		if (scope != null) 
			label= NLS.bind(CSearchMessages.PDOMSearchPatternQuery_PatternQuery_labelPatternInScope, label, scope);

		String countLabel = Messages.format(CSearchMessages.CSearchResultCollector_matches, new Integer(
				matchCount));
		return label + " " + countLabel; //$NON-NLS-1$
	}

	public String getLabel() {
		if(fQuery == null) {
			return null;
		}
		
		String type;
		if ((fQuery.getFlags() & RemoteSearchQuery.FIND_REFERENCES) != 0)
			type = CSearchMessages.PDOMSearchQuery_refs_label; 
		else if ((fQuery.getFlags() & RemoteSearchQuery.FIND_DECLARATIONS) != 0)
			type = CSearchMessages.PDOMSearchQuery_decls_label; 
		else
 			type = CSearchMessages.PDOMSearchQuery_defs_label; 
		return type;
	}

	public ISearchResult getSearchResult() {
		return fResult;
	}

	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		fSubsystem.checkAllProjects(monitor);
		
		RemoteSearchResult result= (RemoteSearchResult) getSearchResult();
		result.removeAll();
		
		if(fQuery!=null){
			fQuery.cleanupResult();
		}
		
		// Send query to remote side for processing and get it back with matches
		fQuery = fSubsystem.runQuery2(fScope, fQuery, monitor);
	
		if (fQuery!=null) {
			postProcessQuery();
			return new Status(IStatus.OK, "org.eclipse.ptp.rdt.ui", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			// no matches found
			return new Status(IStatus.OK, "org.eclipse.ptp.rdt.ui", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public RemoteSearchQuery getQuery() {
		return fQuery;
	}

	public ICProject[] getProjects() {
		return new ICProject[0];
	}
	
	private void postProcessQuery(){
		if(fQuery == null) {
			return;
		}
		
		List<RemoteSearchMatch> remoteSearchMatches = fQuery.getMatches();
		Map<IIndexFile, Set<RemoteLineSearchElementMatch>> remoteLineSearchElementMatches = fQuery.getRemoteLineSearchElementMatchs();
		Map<IIndexFileLocation, Set<RemoteLineSearchElementMatch>> localLineSearchElementMatches = fQuery.getLocalLineSearchElementMatches();
		Map<IIndexFileLocation, RemoteLineSearchElement[]> localLineSearchElements = fQuery.getLocalLineSearchElements();
		Map<IIndexFile, RemoteLineSearchElement[]> remoteLineSearchElements = fQuery.getRemoteLineSearchElements();
			
			
		try {
			if(remoteSearchMatches.size()>0){
				int i = 0;
				Match[] matches = new Match[remoteSearchMatches.size()];
				for (RemoteSearchMatch match : remoteSearchMatches) {
					matches[i] = new RemoteSearchMatchAdapter(match);
					i++;
				}
				Arrays.sort(matches, MATCHES_COMPARATOR);
				fResult.addMatches(matches);
			}
			if(remoteLineSearchElementMatches.size()>0){
				collectNames(remoteLineSearchElementMatches, remoteLineSearchElements);
			}
			if(localLineSearchElementMatches.size()>0){
				createLocalMatches(localLineSearchElementMatches, localLineSearchElements);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	
		
	}
	
	private void collectNames(Map<IIndexFile, Set<RemoteLineSearchElementMatch>> remoteLineSearchElementMatches, Map<IIndexFile, RemoteLineSearchElement[]> remoteLineSearchElements) throws CoreException {
		
		// compute mapping from paths to dirty text editors
		IEditorPart[] dirtyEditors = CUIPlugin.getDirtyEditors();
		Map<IPath, ITextEditor> pathsDirtyEditors = new HashMap<IPath, ITextEditor>();
		for (IEditorPart editorPart : dirtyEditors) {
			if (editorPart instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor)editorPart;
				IEditorInput editorInput = editorPart.getEditorInput();
				if (editorInput instanceof IPathEditorInput) {
					IPathEditorInput pathEditorInput = (IPathEditorInput)editorInput;
					pathsDirtyEditors.put(pathEditorInput.getPath(), textEditor);
				}
			}
		}
		// for each file with matches create line elements with matches
		for (Entry<IIndexFile, Set<RemoteLineSearchElementMatch>> entry : remoteLineSearchElementMatches.entrySet()) {
			IIndexFile file = entry.getKey();
			Set<RemoteLineSearchElementMatch> matches = entry.getValue();
			RemoteLineSearchElement[] lineElements = {};
			// check if there is dirty text editor corresponding to file and convert matches
			IPath absolutePath = IndexLocationFactory.getAbsolutePath(file.getLocation());
			if (pathsDirtyEditors.containsKey(absolutePath)) {
				matches = convertMatchesPositions(file, matches);
				// scan dirty editor and group matches by line elements
				ITextEditor textEditor = pathsDirtyEditors.get(absolutePath);
				IEditorInput input = textEditor.getEditorInput(); 
				IDocument document = textEditor.getDocumentProvider().getDocument(input);
				RemoteLineSearchElementMatch[] matchesArray = matches.toArray(new RemoteLineSearchElementMatch[matches.size()]);
				lineElements = RemoteLineSearchElementAdapter.createElements(file.getLocation(), matchesArray, document);
			} else {
				// scan file and group matches by line elements
				/*
				RemoteLineSearchElementMatch[] matchesArray = matches.toArray(new RemoteLineSearchElementMatch[matches.size()]);
				lineElements = RemoteLineSearchElement.createElements(file.getLocation(), matchesArray);
				*/
				lineElements = remoteLineSearchElements.get(file);
			}
			if(lineElements!=null){
				// create real PDOMSearchMatch with corresponding line elements 
				for (RemoteLineSearchElement searchElement : lineElements) {
					for (RemoteLineSearchElementMatch lineMatch : searchElement.getMatches()) {
						int offset = lineMatch.getOffset();
						int length = lineMatch.getLength();
						RemoteSearchMatchAdapter match = new RemoteSearchMatchAdapter(searchElement, offset, length);
						if (lineMatch.isPolymorphicCall())
							match.setIsPolymorphicCall();
						fResult.addMatch(match);
					}
				}
			}
		}
		
	}
	
	private Set<RemoteLineSearchElementMatch> convertMatchesPositions(IIndexFile file, Set<RemoteLineSearchElementMatch> matches) throws CoreException {
		IPath path = IndexLocationFactory.getPath(file.getLocation());
		long timestamp = file.getTimestamp();
		IPositionConverter converter = CCorePlugin.getPositionTrackerManager().findPositionConverter(path, timestamp);
		if (converter != null) {
			Set<RemoteLineSearchElementMatch> convertedMatches = new HashSet<RemoteLineSearchElementMatch>();
			for (RemoteLineSearchElementMatch match : matches) {
				IRegion region = new Region(match.getOffset(), match.getLength());
				region = converter.historicToActual(region);
				int offset = region.getOffset();
				int length = region.getLength();
				boolean isPolymorphicCall = match.isPolymorphicCall();
				ICElement enclosingElement = match.getEnclosingElement();
				boolean isWriteAccess = match.isWriteAccess();
				convertedMatches.add(new RemoteLineSearchElementMatch(offset, length, isPolymorphicCall, enclosingElement, isWriteAccess));
			}
			matches = convertedMatches;
		}
		return matches;
	}
	
	protected void createLocalMatches(Map<IIndexFileLocation, Set<RemoteLineSearchElementMatch>> localLineSearchElementMatches, Map<IIndexFileLocation, RemoteLineSearchElement[]> localLineSearchElements) throws CoreException{
		
		if (localLineSearchElementMatches.isEmpty())
			return;
		
		if(fQuery instanceof RemoteSearchTextSelectionQuery){
			String fullPath = ((RemoteSearchTextSelectionQuery)fQuery).getTuFullPath();
		
			
			// Search for dirty editor
			ITextEditor dirtyTextEditor = null;
			if(fullPath!=null){
				
				for (IEditorPart editorPart : CUIPlugin.getDirtyEditors()) {
					if (editorPart instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor) editorPart;
						IEditorInput editorInput = editorPart.getEditorInput();
						if (editorInput instanceof IPathEditorInput) {
							IPathEditorInput pathEditorInput = (IPathEditorInput) editorInput;
							IPath path = pathEditorInput.getPath();
							if (fullPath.equals(path.toOSString())) {
								dirtyTextEditor = textEditor;
								break;
							}
						}
					}
				}
			}
			for (Entry<IIndexFileLocation, Set<RemoteLineSearchElementMatch>> entry : localLineSearchElementMatches.entrySet()) {
				IIndexFileLocation fileloc = entry.getKey();
				Set<RemoteLineSearchElementMatch> matches = entry.getValue();
				// Create line search elements
				RemoteLineSearchElementMatch[] matchesArray = matches.toArray(new RemoteLineSearchElementMatch[matches.size()]);
				RemoteLineSearchElement[] lineElements;
				if (dirtyTextEditor != null) {
					IEditorInput input = dirtyTextEditor.getEditorInput();
					IDocument document = dirtyTextEditor.getDocumentProvider().getDocument(input);
					lineElements = RemoteLineSearchElementAdapter.createElements(fileloc, matchesArray, document);
				} else {
					lineElements = localLineSearchElements.get(fileloc);
				}
				if(lineElements!=null){
					// Create real PDOMSearchMatch with corresponding line elements 
					for (RemoteLineSearchElement searchElement : lineElements) {
						for (RemoteLineSearchElementMatch lineMatch : searchElement.getMatches()) {
							int offset = lineMatch.getOffset();
							int length = lineMatch.getLength();
							RemoteSearchMatchAdapter match = new RemoteSearchMatchAdapter(searchElement, offset, length);
							fResult.addMatch(match);
						}
					}
				}
			}
		}
		
	}
}
