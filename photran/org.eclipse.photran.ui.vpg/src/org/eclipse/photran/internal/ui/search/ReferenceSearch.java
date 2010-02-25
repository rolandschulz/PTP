/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.ui.vpg.Activator;
import org.eclipse.rephraserengine.ui.search.SearchResult;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

/**
 * Fortran Search Engine Implementation.
 * 
 * @author kdecker3, slieter2
 */
public class ReferenceSearch implements ISearchQuery
{
    private Definition def;
    private SearchScope searchScope;
    private IFile file;
    private SearchResult searchResult;

    public ReferenceSearch(Definition def, SearchScope searchScope, IFile file)
    {
        this.def = def;
        this.searchScope = searchScope;
        this.file = file;
        this.searchResult = new SearchResult(this);
    }

    public boolean canRerun()
    {
        return false;
    }

    public boolean canRunInBackground()
    {
        return true;
    }

    public String getLabel()
    {
        return SearchMessages.getFormattedString("search.header", new Object[] {
                getDef().getCanonicalizedName(),
                Integer.valueOf(((AbstractTextSearchResult)getSearchResult()).getMatchCount())
            });
    }

    public ISearchResult getSearchResult()
    {
        return searchResult;
    }

    public IFile getFile()
    {
        return file;
    }

    public IStatus run(IProgressMonitor monitor)
    {
        //PhotranVPG.getInstance().ensureVPGIsUpToDate(monitor);
        try
        {
            filterAndAddSearchResult(getDef().getTokenRef());
            
            for (PhotranTokenRef ref : getDef().findAllReferences(true))
                filterAndAddSearchResult(ref);
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message == null) message = e.getClass().getName();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
        }

        return new Status(IStatus.OK, Activator.PLUGIN_ID,
                          SearchMessages.getString("ReferenceSearch_1"));
    }

    private void filterAndAddSearchResult(PhotranTokenRef ref)
    {
        if (!searchScope.filterOut(ref, getFile()))
            VPGSearchQuery.addSearchResultFromTokenRef(ref, (SearchResult)getSearchResult());
    }

    public static void searchForReference(Definition p_def,
                                          SearchScope p_searchscope,
                                          IFile p_file)
    {
        NewSearchUI.runQueryInBackground(new ReferenceSearch(p_def,
                                                             p_searchscope,
                                                             p_file));
    }

    public Definition getDef()
    {
        return def;
    }

    public enum SearchScope
    {
        FILE
        {
            public boolean filterOut(PhotranTokenRef tokenRef, IFile file)
            {
                return !tokenRef.getFile().equals(file);
            }
        },
        
        PROJECT
        {
            public boolean filterOut(PhotranTokenRef tokenRef, IFile file)
            {
                return !tokenRef.getFile().getProject().equals(file.getProject());
            }
        },
        
        WORKSPACE
        {
            public boolean filterOut(PhotranTokenRef tokenRef, IFile file)
            {
                return false;
            }
        };

        public abstract boolean filterOut(PhotranTokenRef tokenRef, IFile file);
    };
}
