/**********************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpProviderImpl;

/**
 * 
 * This class implements ICHelpProvider and provides UPC information <br>
 * (F1, hover, content assist, etc.)
 * 
 */

public class UPCCHelpInfoProvider extends CHelpProviderImpl {

	public UPCCHelpInfoProvider() {
		// for debug use only, to see where it's called..
		//System.out.println("UPCCHelpInfoProvider ctor()...");
	}

	public void initialize() {
		helpBook = new UPCCHelpBook();
		//System.out.println("UPCCHelpInfoProvider initialize()...");
	}
}
