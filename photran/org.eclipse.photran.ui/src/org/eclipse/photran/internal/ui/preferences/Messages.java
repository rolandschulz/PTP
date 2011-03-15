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
package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.ui.preferences.messages"; //$NON-NLS-1$

    public static String CDTFortranPreferencePage_PreferredDOMParserFieldLabel;

    public static String CDTFortranPreferencePage_PreferredModelBuilderFieldLabel;

    public static String EditorPreferencePage_CommentsFieldLabel;

    public static String EditorPreferencePage_ConvertTabsToSpaces;

    public static String EditorPreferencePage_CPPDirectivesFieldLabel;

    public static String EditorPreferencePage_EnableFolding;

    public static String EditorPreferencePage_FixedFormLineLength;

    public static String EditorPreferencePage_IdentifiersFieldLabel;

    public static String EditorPreferencePage_IntrinsicsFieldLabel;

    public static String EditorPreferencePage_KeywordsFieldLabel;

    public static String EditorPreferencePage_NumbersPunctuationLabel;

    public static String EditorPreferencePage_StringsFieldLabel;

    public static String EditorPreferencePage_TabWidth;

    public static String MainFortranPreferencePage_0;

    public static String MainFortranPreferencePage_1;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
