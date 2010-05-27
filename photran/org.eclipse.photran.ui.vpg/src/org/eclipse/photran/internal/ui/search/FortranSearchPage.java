/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   QNX - Initial API and implementation
 *   IBM Corporation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.ui.search.OpenSearchPageAction;
import org.eclipse.rephraserengine.ui.search.SearchPage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchQuery;

/**
 * A dialog used to run a {@link FortranSearchQuery}.
 * <p>
 * Based on org.eclipse.cdt.internal.ui.search.PDOMSearchPage
 *
 * @author Doug Schaefer
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 * @author Jeff Overbey - Moved language-independent parts into Rephraser Engine
 * 
 * @see FortranSearchQuery
 */
public class FortranSearchPage extends SearchPage implements ISearchPage
{
    /**
     * An action that opens the Fortran Search dialog.  This appears as the Search &gt; Fortran...
     * menu item.
     * 
     * @author Jeff Overbey
     */
    public static class OpenAction extends OpenSearchPageAction
    {
        @Override protected String searchPageID()
        {
            return FortranSearchPage.EXTENSION_ID;
        }
    }
    
    public static final String EXTENSION_ID = "org.eclipse.photran.ui.vpgSearchPage"; //$NON-NLS-1$
    
    // Dialog store id constants
    @Override protected String PAGE_NAME() { return "VPGSearchPage"; } //$NON-NLS-1$

    @Override protected List<Pair<String, Integer>> searchFor()
    {
        List<Pair<String, Integer>> searchFor = new ArrayList<Pair<String, Integer>>(6);
        searchFor.add(Pair.of(Messages.FortranSearchPage_CommonBlock, FortranSearchQuery.FIND_COMMON_BLOCK));
        searchFor.add(Pair.of(Messages.FortranSearchPage_Function, FortranSearchQuery.FIND_FUNCTION));
        searchFor.add(Pair.of(Messages.FortranSearchPage_Subroutine, FortranSearchQuery.FIND_SUBROUTINE));
        searchFor.add(Pair.of(Messages.FortranSearchPage_Module, FortranSearchQuery.FIND_MODULE));
        searchFor.add(Pair.of(Messages.FortranSearchPage_Variable, FortranSearchQuery.FIND_VARIABLE));
        searchFor.add(Pair.of(Messages.FortranSearchPage_Program, FortranSearchQuery.FIND_PROGRAM));
        return searchFor;
    }

    @Override protected List<Pair<String, Integer>> limitTo()
    {
        List<Pair<String, Integer>> searchFor = new ArrayList<Pair<String, Integer>>(3);
        searchFor.add(Pair.of(Messages.FortranSearchPage_AllOccurrences, FortranSearchQuery.FIND_ALL_OCCURANCES));
        searchFor.add(Pair.of(Messages.FortranSearchPage_Declarations, FortranSearchQuery.FIND_DECLARATIONS));
        searchFor.add(Pair.of(Messages.FortranSearchPage_References, FortranSearchQuery.FIND_REFERENCES));
        return searchFor;
    }

    @Override protected IResource getResource(Object obj)
    {
        if (obj instanceof ICElement)
            return ((ICElement)obj).getResource();
        else
            return super.getResource(obj);
    }
    
    @Override protected ISearchQuery createSearchQuery(
        List<IResource> scope,
        String scopeDesc,
        String patternDesc,
        String patternRegex,
        int searchFlags)
    {
        return new FortranSearchQuery(scope, scopeDesc, patternDesc, patternRegex, searchFlags);
    }
    
    @Override protected int defaultSearchFlags()
    {
        return FortranSearchQuery.FIND_ALL_TYPES | FortranSearchQuery.FIND_ALL_OCCURANCES;
    }
}
