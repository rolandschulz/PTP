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
package org.eclipse.photran.internal.ui.properties;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.ui.properties.messages"; //$NON-NLS-1$

    public static String SearchPathsPropertyPage_EnableAnalysisRefactoring;

    public static String SearchPathsPropertyPage_EnableButtonsDescription;

    public static String SearchPathsPropertyPage_EnableContentAssist;

    public static String SearchPathsPropertyPage_EnableDeclarationView;

    public static String SearchPathsPropertyPage_EnableHoverTips;

    public static String SearchPathsPropertyPage_ErrorSavingProperties;

    public static String SearchPathsPropertyPage_FoldersToBeSearchedForIncludes;

    public static String SearchPathsPropertyPage_FoldersToBeSearchedForModules;

    public static String SearchPathsPropertyPage_PathsDescription;

    public static String SearchPathsPropertyPage_PropertiesCouldNotBeSaved;

    public static String SearchPathsPropertyPage_SelectAFolderToBeSearchedForIncludes;

    public static String SearchPathsPropertyPage_SelectAFolderToBeSearchedForModules;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
