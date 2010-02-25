/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    Quillback: Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 *******************************************************************************/
package org.eclipse.rephraserengine.ui.search;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

/**
 * An implementation of {@link ISearchQuery} that performs searches using
 * Photran's VPG.  Based on org.eclipse.cdt.internal.ui.search.PDOMSearchQuery
 * from CDT 5.0.
 *
 * @author Doug Schaefer
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 * @author Kurt Hendle
 * @author Jeff Overbey
 * 
 * @param <T>
 */
public abstract class SearchQuery<T extends AbstractTextSearchResult> implements ISearchQuery
{
    protected List<IResource> scope;
    protected String scopeDesc;
    protected String patternDescription;
    protected String patternRegex;
    protected int searchFlags;

    protected T result;

    public SearchQuery(
            List<IResource> scope,
            String scopeDesc,
            String patternDescription,
            String patternRegex,
            int flags)
    {
        this.scope = scope;
        this.scopeDesc = scopeDesc;
        this.patternDescription = patternDescription;
        this.patternRegex = patternRegex;
        this.searchFlags = flags;
        
        this.result = createInitialSearchResult();
    }

    protected abstract T createInitialSearchResult();

    public String getLabel()
    {
        return "'" + patternDescription + "' - " + result.getMatchCount() + " occurence(s) in " + scopeDesc;
    }

    public boolean canRerun()
    {
        return true;
    }

    public boolean canRunInBackground()
    {
        return true;
    }

    public T getSearchResult()
    {
        return result;
    }

    /**
     * Runs this search query, adding the results to the search result
     */
    public final IStatus run(IProgressMonitor monitor)
    {
        try
        {
            prepareToSearch(new SubProgressMonitor(monitor, 0));
            result.removeAll();
            runSearch(monitor);
            finishSearch();
        }
        catch (CoreException e)
        {
            return e.getStatus();
        }

        return Status.OK_STATUS;
    }

    protected void prepareToSearch(IProgressMonitor monitor)
    {
    }

    protected void runSearch(IProgressMonitor monitor) throws CoreException
    {
        int numResources = countResources();
        int numPasses = numPasses();

        monitor.beginTask("Searching for " + patternDescription + " in " + scopeDesc, numResources*numPasses);

        SearchResourceVisitor visitor = new SearchResourceVisitor(monitor);
        for (IResource resource : scope)
            resource.accept(visitor);
        
        for (int i = 1; i < numPasses; i++)
            runAdditionalSearchPass(i+1, new SubProgressMonitor(monitor, numResources));
        
        monitor.done();
    }

    private int countResources() throws CoreException
    {
        final int[] counter = new int[] { 0 };
        CountResourceVisitor countVisitor = new CountResourceVisitor(counter);
        for (IResource resource : scope)
        {
            resource.accept(countVisitor);
        }
        int numResources = counter[0];
        return numResources;
    }

    /**
     * @return the number of search passes, i.e., the number of times the search resources will be iterated through
     */
    protected int numPasses()
    {
        return 1;
    }

    /**
     * @param passNum pass number (2, 3, 4, ...)
     * @param monitor
     */
    protected void runAdditionalSearchPass(int passNum, IProgressMonitor monitor) throws CoreException
    {
    }

    /**
     * An IResourceVisitor to just count the number of nodes that we'll visit when searching through
     * the given resources.
     */
    private class CountResourceVisitor implements IResourceVisitor
    {
        private int[] counter;

        public CountResourceVisitor(int[] counter)
        {
            this.counter = counter;
        }

        public boolean visit(IResource resource)
        {
            if (!shouldProcess(resource))
            {
                return false;
            }
            else
            {
                counter[0]++;
                return !(resource instanceof IFile);
            }
        }
    }
    
    protected abstract boolean shouldProcess(IResource resource);
    
    private class SearchResourceVisitor implements IResourceVisitor
    {
        private IProgressMonitor monitor;

        public SearchResourceVisitor(IProgressMonitor monitor)
        {
            this.monitor = monitor;
        }

        public boolean visit(IResource resource)
        {
            if (!shouldProcess(resource)) return false;

            monitor.worked(1);

            if (resource instanceof IFile)
            {
                monitor.subTask("Searching " + resource.getName());
                search((IFile)resource);
                return false;
            }
            else return true;
        }
    }
    
    protected abstract void search(IFile file);

    protected void finishSearch()
    {
    }
}
