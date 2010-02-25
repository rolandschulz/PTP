/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.examples.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.util.StringUtil;
import org.eclipse.rephraserengine.ui.search.OpenSearchPageAction;
import org.eclipse.rephraserengine.ui.search.SearchMatch;
import org.eclipse.rephraserengine.ui.search.SearchPage;
import org.eclipse.rephraserengine.ui.search.SearchQuery;
import org.eclipse.rephraserengine.ui.search.SearchResult;
import org.eclipse.search.ui.ISearchQuery;

/**
 * A new page ("Text Search") that is contributed to the workbench Search dialog and added to the
 * Search menu in the menu bar.
 * 
 * @author Jeff Overbey
 */
public class TextSearchPage extends SearchPage
{
    public static final String SEARCH_PAGE_ID = "org.eclipse.rephraserengine.examples.text.searchPage";
    
    /**
     * An action that opens the Text Search dialog.  This appears as the Search &gt; Text...
     * menu item.
     * 
     * @author Jeff Overbey
     */
    public static class OpenTextSearchPageAction extends OpenSearchPageAction
    {
        @Override
        protected String searchPageID()
        {
            return SEARCH_PAGE_ID;
        }
    }
    
    public static class TextSearchQuery extends SearchQuery<SearchResult>
    {
        public TextSearchQuery(List<IResource> scope, String scopeDescription,
            String patternDescription, String patternRegex, int flags)
        {
            super(scope, scopeDescription, patternDescription, patternRegex, flags);
        }

        public static final int FIND_TEXT = 0x10;
        
        public static final int FIND_ALL_OCCURRENCES = 0x01;
        public static final int FIND_FIRST_OCCURRENCES = 0x02;
        
        @Override
        protected SearchResult createInitialSearchResult()
        {
            return new SearchResult(this);
        }

        @Override
        protected boolean shouldProcess(IResource resource)
        {
            return new TextFileResourceFilter().shouldProcess(resource);
        }

        @Override
        protected void search(IFile file)
        {
            String contents = StringUtil.readOrReturnNull(file);
            Matcher matcher = Pattern.compile(patternRegex).matcher(contents == null ? "" : contents);
            if ((searchFlags & FIND_ALL_OCCURRENCES) != 0)
                while (matcher.find())
                    result.addMatch(new SearchMatch(file, matcher.start(), matcher.end()-matcher.start()));
            else
                if (matcher.find())
                    result.addMatch(new SearchMatch(file, matcher.start(), matcher.end()-matcher.start()));
        }
    }

    @Override protected String PAGE_NAME() { return "RephraserExampleTextSearchPage"; }

    @Override protected List<Pair<String, Integer>> searchFor()
    {
        List<Pair<String, Integer>> searchFor = new ArrayList<Pair<String, Integer>>(6);
        searchFor.add(Pair.of("Text", TextSearchQuery.FIND_TEXT));
        return searchFor;
    }

    @Override protected List<Pair<String, Integer>> limitTo()
    {
        List<Pair<String, Integer>> searchFor = new ArrayList<Pair<String, Integer>>(3);
        searchFor.add(Pair.of("All occurrences", TextSearchQuery.FIND_ALL_OCCURRENCES));
        searchFor.add(Pair.of("First occurrences", TextSearchQuery.FIND_FIRST_OCCURRENCES));
        return searchFor;
    }
    
    @Override protected ISearchQuery createSearchQuery(
        List<IResource> scope,
        String scopeDesc,
        String patternDesc,
        String patternRegex,
        int searchFlags)
    {
        return new TextSearchQuery(scope, scopeDesc, patternDesc, patternRegex, searchFlags);
    }
    
    @Override protected int defaultSearchFlags()
    {
        return TextSearchQuery.FIND_TEXT | TextSearchQuery.FIND_ALL_OCCURRENCES;
    }
}