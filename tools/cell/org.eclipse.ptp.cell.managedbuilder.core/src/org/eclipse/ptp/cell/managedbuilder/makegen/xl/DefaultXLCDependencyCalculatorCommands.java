/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.makegen.xl;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.gnu.DefaultGCCDependencyCalculator2Commands;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * This dependency calculator uses the XL C -qmakedep=gcc -MF options in order
 * to generate .d files as a side effect of compilation. See bugzilla 108715 for
 * the discussion of dependency management that led to the creation of this
 * superclass dependency calculator. Note also that this technique exhibits the
 * failure modes discussed in comment #5.
 * 
 * This class is used with DefaultXLCDependencyCalculator.
 * 
 * @author laggarcia
 * @since 1.2.1
 */
public class DefaultXLCDependencyCalculatorCommands extends
		DefaultGCCDependencyCalculator2Commands {

	public DefaultXLCDependencyCalculatorCommands(IPath source,
			IResource resource, IBuildObject buildContext, ITool tool,
			IPath topBuildDirectory) {
		super(source, resource, buildContext, tool, topBuildDirectory);
	}

	public DefaultXLCDependencyCalculatorCommands(IPath source,
			IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		this(source, (IResource) null, buildContext, tool, topBuildDirectory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getDependencyCommandOptions()
	 */
	public String[] getDependencyCommandOptions() {

		String[] options = new String[2];
		options[0] = "-qmakedep=gcc"; //$NON-NLS-1$
		options[1] = "-MF\"$(@:%.o=%.d)\""; //$NON-NLS-1$

		return options;
	}

}
