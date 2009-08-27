/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
 * Version: 1.16
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * Adapts RemoteSearchQuery instances so that they may be used with Eclipse's
 * search framework. 
 */
public class RemoteSearchQueryAdapter implements ISearchQuery {

	protected ICIndexSubsystem fSubsystem;
	protected RemoteSearchQuery fQuery;
	protected RemoteSearchResult fResult;
	protected Scope fScope;

	public RemoteSearchQueryAdapter(ICIndexSubsystem subsystem, Scope scope, RemoteSearchQuery query) {
		fSubsystem = subsystem;
		fScope = scope;
		fQuery = query;
		fResult = new RemoteSearchResult(this);
	}
	
	public boolean canRerun() {
		return fQuery.canRerun();
	}

	public boolean canRunInBackground() {
		return fQuery.canRunInBackground();
	}

	public String getLabel() {
		String type;
		if ((fQuery.getFlags() & RemoteSearchQuery.FIND_REFERENCES) != 0)
			type = CSearchMessages.PDOMSearch_query_refs_label; 
		else if ((fQuery.getFlags() & RemoteSearchQuery.FIND_DECLARATIONS) != 0)
			type = CSearchMessages.PDOMSearch_query_decls_label; 
		else
 			type = CSearchMessages.PDOMSearch_query_defs_label; 
		return type;
	}

	public ISearchResult getSearchResult() {
		return fResult;
	}

	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		fSubsystem.checkAllProjects(monitor);
		
		RemoteSearchResult result= (RemoteSearchResult) getSearchResult();
		result.removeAll();
		
		// Send query to remote side for processing
		List<RemoteSearchMatch> results = fSubsystem.runQuery(fScope, fQuery, monitor);
	
		if (results.size() > 0) {
			Match[] matches = new Match[results.size()];
			int i = 0;
			try {
				for (RemoteSearchMatch match : results) {
					matches[i] = new RemoteSearchMatchAdapter(match);
					i++;
				}
				fResult.addMatches(matches);
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
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
}
