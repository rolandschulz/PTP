/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.internal.rdt.ui.scannerinfo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.SymbolTab;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;

public class RemoteSymbolTab extends SymbolTab {

	public String getHelpContextId() {
		return RDTHelpContextIds.REMOTE_SYMBOL_TAB;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractLangsListTab#getLangSetting(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
		ICLanguageSetting[] langSettings_orig = super.getLangSetting(rcDes);
		List<ICLanguageSetting> langSettings = new ArrayList<ICLanguageSetting>();
		
		// bug 378579 - remove the old Remote XL C++ Compiler's C source file input type in case 
		// this is a project created before the fix for bug 378579
		for (int i = 0; i < langSettings_orig.length; i++) {
			if (langSettings_orig[i].getId().indexOf("org.eclipse.ptp.rdt.managedbuilder.xlc.ui.cpp.c.compiler.input") == -1) { //$NON-NLS-1$
				langSettings.add(langSettings_orig[i]);
			}
		}
		return langSettings.toArray(new ICLanguageSetting[langSettings.size()]);
	}
}
