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
package org.eclipse.rephraserengine.core.vpg.eclipse;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @since 2.0
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.core.vpg.eclipse.messages"; //$NON-NLS-1$

    public static String EclipseVPG_CalculatingDependencies;

    public static String EclipseVPG_Indexing;

    public static String EclipseVPG_IndexIsUpToDate;

    public static String EclipseVPG_SearchingForWorkspaceModifications;

    /** @since 3.0 */
    public static String EclipseVPG_WritingDatabaseToDisk;

    public static String EclipseVPG_XofY;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
