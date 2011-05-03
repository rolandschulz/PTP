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
 * <p>
 * <b>Note:</b> This requires a fix to CDT post-5.0.0 release to org.eclipse.cdt.internal.core.model.TranslationUnit - to recognize
 * content-type of UPC to be a deriviative ("kindOf") the C content type.
 * <p>
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=237331
 * <p>
 * Further note: a workaround has been applied in UCCCHelpBook to not require the CDT patch
 * 
 * 
 * @author Beth Tibbitts
 * 
 */
public class UPCCHelpInfoProvider extends CHelpProviderImpl {

	public UPCCHelpInfoProvider() {
		// System.out.println("UPCCHelpInfoProvider ctor()...");//debug only , to see when called
	}

	public void initialize() {
		helpBook = new UPCCHelpBook();
		// System.out.println("UPCCHelpInfoProvider initialize()...");
	}
}
