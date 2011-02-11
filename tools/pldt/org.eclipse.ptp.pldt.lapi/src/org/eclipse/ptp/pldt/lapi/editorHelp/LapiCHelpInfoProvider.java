/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpProviderImpl;

/**
 * 
 * This class implements ICHelpProvider and provides API-specific information <br>
 * (F1, hover, content assist, etc.)
 * 
 */

public class LapiCHelpInfoProvider extends CHelpProviderImpl {

	public LapiCHelpInfoProvider() {
		// for debug use only, to see where it's called..
		// System.out.println("LapiCHelpInfoProvider ctor()...");
	}

	public void initialize() {
		helpBook = new LapiCHelpBook();
		// System.out.println("LapiCHelpInfoProvider initialize()...");
	}
}
