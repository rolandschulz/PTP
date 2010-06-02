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
package org.eclipse.rephraserengine.core.vpg;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @since 2.0
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.core.vpg.messages"; //$NON-NLS-1$

    public static String VPG_AnnotationOfType;

    public static String VPG_EdgeOfType;

    public static String VPG_PostTransformAnalysis;

    public static String VPG_ProcessingDependentFile;

    public static String VPG_SortingFilesEnqueuingDependents;

    public static String VPG_SortingFilesSortingDependents;

    public static String VPGDB_AnnotationOfType;

    public static String VPGDB_EdgeOfType;

    public static String VPGDB_FilenameOffsetLength;

    public static String VPGLog_ErrorLabel;

    public static String VPGLog_FilenameOffsetLength;

    public static String VPGLog_WarningLabel;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
