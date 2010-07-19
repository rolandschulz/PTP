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
package org.eclipse.rephraserengine.internal.ui.browser;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.internal.ui.browser.messages"; //$NON-NLS-1$

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }

    public static String AnnotationsTab_LineColOffset;
    public static String AnnotationsTab_NoAnnotationsToShow;
    public static String AnnotationsTab_UnableToParse;
    public static String DependenciesTab_FileName;
    public static String DependenciesTab_FilesThatDependOnTheSelectedFile;
    public static String DependenciesTab_FilesTheSelectedFileDependsOn;
    public static String DependenciesTab_TimeStamp;
    public static String EdgesTab_EdgeTypes;
    public static String EdgesTab_LineColOffset;
    public static String EdgesTab_ShowAllEdges;
    public static String EdgesTab_ShowSelectedEdges;
    public static String EdgesTab_UnableToParse;
    public static String VPGBrowser_Annotations;
    public static String VPGBrowser_Dependencies;
    public static String VPGBrowser_Edges;
    public static String VPGBrowser_FileNmae;
    public static String VPGBrowser_Files;
    public static String VPGBrowser_WindowTitle;
}
