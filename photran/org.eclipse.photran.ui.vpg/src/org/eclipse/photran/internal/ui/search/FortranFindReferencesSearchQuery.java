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
public class FortranFindReferencesSearchQuery implements ISearchQuery
{
    private Definition def;
    private SearchScope searchScope;
    private IFile file;
    private SearchResult searchResult;

    public FortranFindReferencesSearchQuery(Definition def, SearchScope searchScope, IFile file)
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
        int count = ((AbstractTextSearchResult)getSearchResult()).getMatchCount();
        String description = "'" + getDef().getCanonicalizedName() + "' - "; //$NON-NLS-1$ //$NON-NLS-2$
        if (count == 1)
            return description + Messages.bind(Messages.FortranFindReferencesSearchQuery_OneMatch, searchScope);
        else
            return description + Messages.bind(Messages.FortranFindReferencesSearchQuery_nMatches, count, searchScope);
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

        return new Status(IStatus.OK, Activator.PLUGIN_ID, Messages.FortranFindReferencesSearchQuery_SearchSuccessful);
    }

    private void filterAndAddSearchResult(PhotranTokenRef ref)
    {
        if (!searchScope.filterOut(ref, getFile()))
            FortranSearchQuery.addSearchResultFromTokenRef(ref, (SearchResult)getSearchResult());
    }

    public static void searchForReference(Definition p_def,
                                          SearchScope p_searchscope,
                                          IFile p_file)
    {
        NewSearchUI.runQueryInBackground(new FortranFindReferencesSearchQuery(p_def,
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
            @Override public boolean filterOut(PhotranTokenRef tokenRef, IFile file)
            {
                return !tokenRef.getFile().equals(file);
            }
            
            @Override public String toString()
            {
                return Messages.FortranFindReferencesSearchQuery_FileScope;
            }
        },
        
        PROJECT
        {
            @Override public boolean filterOut(PhotranTokenRef tokenRef, IFile file)
            {
                return !tokenRef.getFile().getProject().equals(file.getProject());
            }
            
            @Override public String toString()
            {
                return Messages.FortranFindReferencesSearchQuery_ProjectScope;
            }
        },
        
        WORKSPACE
        {
            @Override public boolean filterOut(PhotranTokenRef tokenRef, IFile file)
            {
                return false;
            }
            
            @Override public String toString()
            {
                return Messages.FortranFindReferencesSearchQuery_WorkspaceScope;
            }
        };

        public abstract boolean filterOut(PhotranTokenRef tokenRef, IFile file);
    };
}
