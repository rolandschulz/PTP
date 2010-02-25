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
import org.eclipse.rephraserengine.ui.search.SearchPage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchQuery;

/**
 * A dialog used to run a {@link VPGSearchQuery}.
 * <p>
 * Based on org.eclipse.cdt.internal.ui.search.PDOMSearchPage
 *
 * @author Doug Schaefer
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 * @author Jeff Overbey - Moved language-independent parts into Rephraser Engine
 * 
 * @see VPGSearchQuery
 */
//@SuppressWarnings("restriction")
public class VPGSearchPage extends SearchPage implements ISearchPage {
    
    public static final String EXTENSION_ID = "org.eclipse.photran.ui.vpgSearchPage";
    
    // Dialog store id constants
    @Override protected String PAGE_NAME() { return "VPGSearchPage"; }

    @Override protected List<Pair<String, Integer>> searchFor()
    {
        List<Pair<String, Integer>> searchFor = new ArrayList<Pair<String, Integer>>(6);
        searchFor.add(Pair.of("Common block", VPGSearchQuery.FIND_COMMON_BLOCK));
        searchFor.add(Pair.of("Function", VPGSearchQuery.FIND_FUNCTION));
        searchFor.add(Pair.of("Subroutine", VPGSearchQuery.FIND_SUBROUTINE));
        searchFor.add(Pair.of("Module", VPGSearchQuery.FIND_MODULE));
        searchFor.add(Pair.of("Variable", VPGSearchQuery.FIND_VARIABLE));
        searchFor.add(Pair.of("Program", VPGSearchQuery.FIND_PROGRAM));
        return searchFor;
    }

    @Override protected List<Pair<String, Integer>> limitTo()
    {
        List<Pair<String, Integer>> searchFor = new ArrayList<Pair<String, Integer>>(3);
        searchFor.add(Pair.of("All occurrences", VPGSearchQuery.FIND_ALL_OCCURANCES));
        searchFor.add(Pair.of("Declarations", VPGSearchQuery.FIND_DECLARATIONS));
        searchFor.add(Pair.of("References", VPGSearchQuery.FIND_REFERENCES));
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
        return new VPGSearchQuery(scope, scopeDesc, patternDesc, patternRegex, searchFlags);
    }
    
    @Override protected int defaultSearchFlags()
    {
        return VPGSearchQuery.FIND_ALL_TYPES | VPGSearchQuery.FIND_ALL_OCCURANCES;
    }
}
