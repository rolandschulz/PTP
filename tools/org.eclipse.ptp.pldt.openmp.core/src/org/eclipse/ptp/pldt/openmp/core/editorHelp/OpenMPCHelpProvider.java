/**********************************************************************
 * Copyright (c) 2005,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpProviderImpl;

/**
 * 
 * This class implements ICHelpProvider and provides OpenMP information <br>
 * (F1, hover, content assist, etc.)
 * 
 * @author tibbitts
 */

public class OpenMPCHelpProvider extends CHelpProviderImpl {

	public void initialize() {
		helpBook = new OpenMPCHelpBook();
	}
}
