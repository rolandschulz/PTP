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
package org.eclipse.ptp.launch.ui.tabs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 */
public abstract class LaunchConfigurationTab extends AbstractLaunchConfigurationTab {
	public static final String DEFAULT_VALUE = "0"; //$NON-NLS-1$
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private ILaunchConfiguration launchConfiguration = null;

	/**
	 * @return the launchConfiguration
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		// cache the launch configuration for updates
		setLaunchConfiguration(configuration);
	}
	
	/**
	 * Cache the launch configuration
	 * 
	 * @param configuration
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		launchConfiguration = configuration;
	}

	/**
	 * Utility routine to create a grid layout
	 * 
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/**
	 * Returns the selected workspace container,or <code>null</code>
	 * 
	 * @param workspaceDir
	 * @return workspace container
	 */
	protected IContainer getContainer(String workspaceDir) {
		IResource res = getResource(workspaceDir);
		if (res instanceof IContainer) {
			return (IContainer) res;
		}
		return null;
	}

	/**
	 * Utility routine to get the contents of a text field
	 * 
	 * @param text
	 * @return string
	 */
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING)) {
			return null;
		}

		return text;
	}

	/**
	 * Get the platform from the launch configuration
	 * 
	 * @param config
	 * @return platform
	 */
	protected String getPlatform(ILaunchConfiguration config) {
		String platform = Platform.getOS();
		try {
			return config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PLATFORM, platform);
		} catch (CoreException e) {
			return platform;
		}
	}

	/**
	 * Gets the project from the configuration
	 * 
	 * @param configuration
	 * @return project
	 */
	protected IProject getProject(ILaunchConfiguration configuration) {
		String proName = null;
		try {
			proName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		} catch (CoreException e) {
			return null;
		}
		if (proName == null) {
			return null;
		}

		return getWorkspaceRoot().getProject(proName);
	}

	/**
	 * Returns the selected workspace resource, or <code>null</code>
	 * 
	 * @param workspaceDir
	 * @return workspace resource
	 */
	protected IResource getResource(String workspaceDir) {
		return getWorkspaceRoot().findMember(new Path(workspaceDir));
	}

	/**
	 * @return
	 */
	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Utility routing to create a GridData
	 * 
	 * @param style
	 * @param space
	 * @return
	 */
	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}
}
