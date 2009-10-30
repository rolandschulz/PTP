/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class SearchPathProperties
{
    private static final String modulePaths = ".";
    private static final String includePaths = ".";

    public static final String ENABLE_VPG_PROPERTY_NAME = "EnableVPG";
    public static final String MODULE_PATHS_PROPERTY_NAME = "FortranModulePaths";
    public static final String INCLUDE_PATHS_PROPERTY_NAME = "FortranIncludePaths";

    /** @return the value of the given property for the project containing the given file */
    public static String getProperty(IFile file, String propertyName)
    {
        if (file == null || file.getProject() == null)
            return "";
        else
            return getProperty(file.getProject(), propertyName);
    }

    /** @return the value of the given property for the given project */
    public static String getProperty(IProject project, String propertyName)
    {
        if (propertyName.equals(ENABLE_VPG_PROPERTY_NAME))
            return "true";
        else if (propertyName.equals(MODULE_PATHS_PROPERTY_NAME))
            return modulePaths;
        else if (propertyName.equals(INCLUDE_PATHS_PROPERTY_NAME))
            return includePaths;
        else
            return null;
    }

    public static String[] parseString(String stringList)
    {
        StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
        ArrayList<String> v = new ArrayList<String>();
        while (st.hasMoreTokens())
            v.add(st.nextToken());
        return v.toArray(new String[v.size()]);
    }
}
