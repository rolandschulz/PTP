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
package org.eclipse.fdt.internal.ui.buildconsole;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.fdt.core.ConsoleOutputStream;
import org.eclipse.fdt.core.resources.IConsole;
import org.eclipse.fdt.ui.CUIPlugin;
import org.eclipse.fdt.ui.IBuildConsoleManager;

public class CBuildConsole implements IConsole {
	IProject project;
	IBuildConsoleManager fConsoleManager;
	
	/**
	 * Constructor for BuildConsole.
	 */
	public CBuildConsole() {
		fConsoleManager = CUIPlugin.getDefault().getConsoleManager();
	}

	public void start(IProject project ) {
		this.project = project;
		fConsoleManager.getConsole(project).start(project);
	}
	
	/**
	 * @throws CoreException
	 * @see org.eclipse.fdt.core.resources.IConsole#getOutputStream()
	 */
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return fConsoleManager.getConsole(project).getOutputStream();
	}

	public ConsoleOutputStream getInfoStream() throws CoreException {
		return fConsoleManager.getConsole(project).getInfoStream();
	}

	public ConsoleOutputStream getErrorStream() throws CoreException {
		return fConsoleManager.getConsole(project).getErrorStream();
	}
}
