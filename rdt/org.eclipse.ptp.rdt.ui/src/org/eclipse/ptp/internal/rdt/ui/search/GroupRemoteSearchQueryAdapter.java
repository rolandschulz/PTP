/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * Manages queries for multiple servers and adapts RemoteSearchQuery instances so that they may be used with Eclipse's
 * search framework. 
 * 
 */
public class GroupRemoteSearchQueryAdapter implements ISearchQuery {
	
	private List<ISearchQuery> jobs;
	private ISearchResult result;
	
	public GroupRemoteSearchQueryAdapter(List<ISearchQuery> _jobs) {
		jobs = _jobs;
		// NPE check is in RemoteSearchPage and there is always one job in the list
		// when this object is called.
		result = jobs.get(0).getSearchResult();
	}
	
	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}
	
	public ISearchResult getSearchResult() {
		return result;
	}
	
	private Match[] computeContainedMatches(AbstractTextSearchResult result, URI locationURI) throws CoreException {
		List<Match> list = new ArrayList<Match>(); 
		Object[] elements = result.getElements();
		for (int i = 0; i < elements.length; ++i) {
			//if (locationURI.normalize().equals(((RemoteSearchElement)elements[i]).getLocation().getURI().normalize())) {
				Match[] matches = result.getMatches(elements[i]);
				for (int j = 0; j < matches.length; ++j) {
					if (matches[j] instanceof RemoteSearchMatchAdapter) {
						list.add(matches[j]);
					}
				}
			//}
		}
		return list.toArray(new Match[list.size()]);
	}

	public IStatus run(IProgressMonitor monitor) {
		final int numOfJobs = jobs.size();
		monitor.beginTask(Messages.getString("GroupRemoteSearchQueryAdapter_0"), 2); //$NON-NLS-1$
		final String JOBFAMILYNAME = Messages.getString("GroupRemoteSearchQueryAdapter_1"); //$NON-NLS-1$
		
		// one job group associated with one progress monitor group.
		IJobManager jobMan = Job.getJobManager();
		IProgressMonitor myGroup = jobMan.createProgressGroup();  
		myGroup.beginTask(JOBFAMILYNAME, numOfJobs * 300);
		
		try {
			for (final ISearchQuery query: jobs) {
				if (monitor.isCanceled()) {
					jobMan.cancel(JOBFAMILYNAME);
					return new Status(IStatus.CANCEL, "org.eclipse.ptp.rdt.ui", ""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				Job job = new Job(JOBFAMILYNAME) {
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask(JOBFAMILYNAME, 300);  
						
						if (monitor.isCanceled()) {
							return new Status(IStatus.CANCEL, "org.eclipse.ptp.rdt.ui", ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						query.run(monitor);
						// progress to 300/300
						monitor.worked(300);
						monitor.done(); 
						return Status.OK_STATUS;
					}
					
					public boolean belongsTo(Object family) {
						return getName().equals(family);
					}
				};
				job.setProgressGroup(myGroup, numOfJobs * 300);
				job.setPriority(Job.LONG);
				job.schedule();
			}
			
			try {
				jobMan.join(JOBFAMILYNAME, null);
				// progress to 1/2
				monitor.worked(1);
			} catch (InterruptedException e) {
				UIPlugin.log(e);
			}
			
			combineResults();
			// progress to 2/2
			monitor.worked(1);
		} finally {
			monitor.done();
		}
		
		return new Status(IStatus.OK, "org.eclipse.ptp.rdt.ui", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void combineResults() {
		RemoteSearchResult result = (RemoteSearchResult) jobs.get(0).getSearchResult();
		for (int i = 1; i < jobs.size(); i ++) {
			RemoteSearchResult result2 = (RemoteSearchResult) jobs.get(i).getSearchResult();
			
			Match[] matches = null;
			try {
				matches = computeContainedMatches(result2, null);
			} catch (CoreException e) {
				UIPlugin.log(e);
			}
			
			result.addMatches(matches);
		}
		this.result = result;
	}

	public String getLabel() {
		return result.getLabel();
	}
	
}
