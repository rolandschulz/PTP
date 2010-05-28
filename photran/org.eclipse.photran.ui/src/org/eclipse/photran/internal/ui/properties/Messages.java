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

    public static String FortranPropertyPage_NeedToCloseAndReOpenEditors;

    public static String FortranPropertyPage_PreferencesChangedTitle;

    public static String FortranSourceFormEditor_FileNameOrExtensionColumnLabel;

    public static String FortranSourceFormEditor_SourceFormColumnLabel;

    public static String SourceFormPropertyPage_ErrorTitle;

    public static String SourceFormPropertyPage_ErrorTouchingFile;

    public static String SourceFormPropertyPage_ErrorTouchingProject;

    public static String SourceFormPropertyPage_LinkText;

    public static String SourceFormPropertyPage_PropertiesCouldNotBeSaved;

    public static String SourceFormPropertyPage_SourceFormFilenameAssocsLabel;

    public static String WorkspacePathEditor_ErrorTitle;

    public static String WorkspacePathEditor_ResourceDoesNotExist;

    public static String WorkspacePathEditor_ResourceIsNotAContainer;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
