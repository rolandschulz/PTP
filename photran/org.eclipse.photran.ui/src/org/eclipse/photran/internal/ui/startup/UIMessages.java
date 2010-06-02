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
package org.eclipse.photran.internal.ui.startup;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
public class UIMessages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.ui.startup.uimessages"; //$NON-NLS-1$

    public static String ShowReleaseNotes_WelcomeTitle;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, UIMessages.class);
    }

    private UIMessages()
    {
    }
}
