/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.launch.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPDTLaunchConfigurationConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

/**
 *
 */
public abstract class PLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
    public static final String DEFAULT_VALUE = "0";
    public static final String EMPTY_STRING = "";

    protected IWorkspaceRoot getWorkspaceRoot() {
    	return ResourcesPlugin.getWorkspace().getRoot();
    }
    
	/**
	 * Returns the selected workspace container,or <code>null</code>
	 */
	protected IContainer getContainer(String workspaceDir) {
		IResource res = getResource(workspaceDir);
		if (res instanceof IContainer) {
			return (IContainer)res;
		}
		return null;
	}
	
	/**
	 * Returns the selected workspace resource, or <code>null</code>
	 */
	protected IResource getResource(String workspaceDir) {
		return getWorkspaceRoot().findMember(new Path(workspaceDir));
	}    
    
    protected IProject getProject(ILaunchConfiguration configuration) {
        String proName = null;
        try {
            proName = configuration.getAttribute(IPDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
        } catch (CoreException e) {
            return null;
        }        
        if (proName == null)
            return null;
        
        return getWorkspaceRoot().getProject(proName);        
    }
    
    protected String getFieldContent(String text) {
        if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
            return null;
        
        return text;
    }
    
    protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = columns;
        gridLayout.makeColumnsEqualWidth = isEqual;
        gridLayout.marginHeight = mh;
        gridLayout.marginWidth = mw;
        return gridLayout;
    }

    protected GridData spanGridData(int style, int space) {
        GridData gd = null;
        if (style == -1)
            gd = new GridData();
        else
            gd = new GridData(style);
        gd.horizontalSpan = space;
        return gd;
    }    
}
