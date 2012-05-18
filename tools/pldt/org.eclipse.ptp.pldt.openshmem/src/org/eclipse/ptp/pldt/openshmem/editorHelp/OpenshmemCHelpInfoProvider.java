/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpProviderImpl;

/**
 * 
 * This class implements ICHelpProvider and provides OpenSHMEM information <br>
 * (F1, hover, content assist, etc.)
 * 
 */

public class OpenshmemCHelpInfoProvider extends CHelpProviderImpl {

	public OpenshmemCHelpInfoProvider() {
	}

	public void initialize() {
		helpBook = new OpenshmemCHelpBook();
	}
}
