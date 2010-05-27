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
package org.eclipse.rephraserengine.ui.search;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.ui.search.messages"; //$NON-NLS-1$

    public static String SearchPage_11;

    public static String SearchPage_AnyStringAnyCharLabel;

    public static String SearchPage_IllegalCharacterInPatternString;

    public static String SearchPage_InvalidSearchPatternTitle;

    public static String SearchPage_LimitToLabel;

    public static String SearchPage_RegularExpressionLabel;

    public static String SearchPage_ScopeDescription_EnclosingProjects;

    public static String SearchPage_ScopeDescription_SelectedResources;

    public static String SearchPage_ScopeDescription_WorkingSet;

    public static String SearchPage_ScopeDescription_Workspace;

    public static String SearchPage_SearchForLabel;

    public static String SearchPage_SearchPatternIsInvalid;

    public static String SearchPage_SearchPatternLabel;

    public static String SearchQuery_nMatches;

    public static String SearchQuery_OneMatch;

    public static String SearchQuery_Searching;

    public static String SearchQuery_SearchingFor;

    public static String SearchResult_SearchResultTooltip;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
