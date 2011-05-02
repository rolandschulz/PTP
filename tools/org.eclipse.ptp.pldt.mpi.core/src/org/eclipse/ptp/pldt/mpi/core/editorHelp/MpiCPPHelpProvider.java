/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpProviderImpl;

/**
 * 
 * Provides help information for MPI APIs for C++(F1, hover, content assist, etc.)
 */
public class MpiCPPHelpProvider extends CHelpProviderImpl
{
	/**
	 * Default ctor required, or else for some reason this
	 * class never gets instantiated.
	 * 
	 */
	public MpiCPPHelpProvider() {
		// default ctor for testing that it gets called
		// System.out.println("MpiCHelpProvider ctor()...");

	}

	public void initialize()
	{
		helpBook = new MpiCPPHelpBook();
		// System.out.println("MpiCHelpProvider initialize()...");
	}
}
