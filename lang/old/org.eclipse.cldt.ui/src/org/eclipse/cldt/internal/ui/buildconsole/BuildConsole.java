/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.internal.ui.buildconsole;

import org.eclipse.cldt.internal.ui.FortranPluginImages;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.cldt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

public class BuildConsole extends AbstractConsole {
	
	/**
	 * Property constant indicating the color of a stream has changed. 
	 */
	public static final String P_STREAM_COLOR = FortranUIPlugin.PLUGIN_ID  + ".CONSOLE_P_STREAM_COLOR";	 //$NON-NLS-1$

	private IBuildConsoleManager fConsoleManager;

	public BuildConsole(IBuildConsoleManager manager) {
		super(ConsoleMessages.getString("BuildConsole.buildConsole"), FortranPluginImages.DESC_BUILD_CONSOLE); //$NON-NLS-1$
		fConsoleManager = manager;
	}

	public IPageBookViewPage createPage(IConsoleView view) {
		return new BuildConsolePage(view, this);
	}

	public void setTitle(IProject project) {
		String title = ConsoleMessages.getString("BuildConsole.buildConsole"); //$NON-NLS-1$
		if (project != null) {
			title += " [" + project.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		setName(title);
	}

	public IBuildConsoleManager getConsoleManager() {
	    return fConsoleManager;
	}
}
