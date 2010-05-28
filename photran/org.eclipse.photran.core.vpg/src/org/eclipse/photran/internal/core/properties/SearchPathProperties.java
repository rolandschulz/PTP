/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Provides access to the module paths and include paths for a project.
 * <p>
 * The user may set these via the Fortran &gt; Analysis/Refactoring category in the
 * project properties dialog.
 * 
 * @author Jeff Overbey
 * @author Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 * @author Timofey Yuvashev
 * 
 * @see org.eclipse.photran.internal.ui.properties.SearchPathsPropertyPage
 */
public class SearchPathProperties extends AbstractProperties
{
    public static final String ENABLE_VPG_PROPERTY_NAME = "EnableVPG"; //$NON-NLS-1$
    public static final String ENABLE_DECL_VIEW_PROPERTY_NAME = "EnableDeclView"; //$NON-NLS-1$
    public static final String ENABLE_CONTENT_ASSIST_PROPERTY_NAME = "EnableContentAssist"; //$NON-NLS-1$
    public static final String ENABLE_HOVER_TIP_PROPERTY_NAME = "EnableHoverTip"; //$NON-NLS-1$
    public static final String MODULE_PATHS_PROPERTY_NAME = "FortranModulePaths"; //$NON-NLS-1$
    public static final String INCLUDE_PATHS_PROPERTY_NAME = "FortranIncludePaths"; //$NON-NLS-1$
    
    public SearchPathProperties()
    {
    }
    
    public SearchPathProperties(IProject project)
    {
        setProject(project);
    }
    
    @Override protected void initializeDefaults(IProject proj, IPreferenceStore properties)
    {
        String defaultDir = proj.getFullPath().toOSString();
        
        //Set the default include and module paths to the root of the project
        properties.setDefault(INCLUDE_PATHS_PROPERTY_NAME, 
                              defaultDir);
        properties.setDefault(MODULE_PATHS_PROPERTY_NAME, 
                              defaultDir);
    }

    @Override public String getProperty(IProject project, String propertyName)
    {
        String result = super.getProperty(project, propertyName);

        if (propertyName.equals(ENABLE_DECL_VIEW_PROPERTY_NAME)
                || propertyName.equals(ENABLE_CONTENT_ASSIST_PROPERTY_NAME))
            return result.equals("true") && getProperty(project, ENABLE_VPG_PROPERTY_NAME).equals("true") //$NON-NLS-1$ //$NON-NLS-2$
                   ? "true" //$NON-NLS-1$
                   : ""; //$NON-NLS-1$
        else
            return result == null ? "" : result; //$NON-NLS-1$
    }
}