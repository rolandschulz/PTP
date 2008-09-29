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
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.cdt.managedbuilder.makegen.gnu.DefaultGCCDependencyCalculator2;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;


/**
 * @author laggarcia
 * @since 1.2.1
 */
public class DefaultXLCDependencyCalculator extends
		DefaultGCCDependencyCalculator2 {

	public IManagedDependencyInfo getDependencySourceInfo(IPath source,
			IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		return new DefaultXLCDependencyCalculatorCommands(source, buildContext,
				tool, topBuildDirectory);
	}

	public IManagedDependencyInfo getDependencySourceInfo(IPath source,
			IResource resource, IBuildObject buildContext, ITool tool,
			IPath topBuildDirectory) {
		return new DefaultXLCDependencyCalculatorCommands(source, resource,
				buildContext, tool, topBuildDirectory);
	}

}
