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
package org.eclipse.rephraserengine.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.ui.messages"; //$NON-NLS-1$

    public static String UIUtil_ErrorTitle;

    public static String UIUtil_FilesBeingEditedHaveUnsavedChanges;

    public static String UIUtil_NoteTitle;

    public static String UIUtil_SaveFileTitle;

    public static String UIUtil_UnableToCreateTempFile;

    public static String UIUtil_UnableToOpenWebBrowser;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
