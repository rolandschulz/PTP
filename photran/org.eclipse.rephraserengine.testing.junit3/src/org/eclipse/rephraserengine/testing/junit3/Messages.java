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
package org.eclipse.rephraserengine.testing.junit3;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.testing.junit3.messages"; //$NON-NLS-1$

    public static String GeneralTestSuiteFromFiles_DirectoryNotFound;

    public static String GeneralTestSuiteFromFiles_NoTestFilesFoundInDirectory;

    public static String GeneralTestSuiteFromFiles_SomeOptionalTestFilesAreNotPresent;

    public static String GeneralTestSuiteFromFiles_UnableToFindDirectory;

    public static String GeneralTestSuiteFromMarkers_FileCannotBeRead;

    public static String GeneralTestSuiteFromMarkers_FileNotFound;

    public static String GeneralTestSuiteFromMarkers_NoMarkersFound;

    public static String GeneralTestSuiteFromMarkers_SomeOptionalTestFilesAreNotPresent;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
