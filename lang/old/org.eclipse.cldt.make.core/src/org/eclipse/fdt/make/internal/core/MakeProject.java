/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.make.internal.core;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.fdt.core.FortranCorePlugin;
import org.eclipse.fdt.core.ICDescriptor;
import org.eclipse.fdt.core.ICOwner;
import org.eclipse.fdt.make.core.MakeCorePlugin;

public class MakeProject implements ICOwner {

	public void configure(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.remove(FortranCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		cDescriptor.remove(FortranCorePlugin.BUILDER_MODEL_ID);
		updateBinaryParsers(cDescriptor);
	}

	public void update(ICDescriptor cDescriptor, String extensionID) throws CoreException {
		if (extensionID.equals(FortranCorePlugin.BINARY_PARSER_UNIQ_ID)) {
			updateBinaryParsers(cDescriptor);
		}
	}

	private void updateBinaryParsers(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.remove(FortranCorePlugin.BINARY_PARSER_UNIQ_ID);
		Preferences makePrefs = MakeCorePlugin.getDefault().getPluginPreferences();
		String id = makePrefs.getString(FortranCorePlugin.PREF_BINARY_PARSER);
		if (id != null && id.length() != 0) {
			String[] ids = parseStringToArray(id);
			for (int i = 0; i < ids.length; i++) {
				cDescriptor.create(FortranCorePlugin.BINARY_PARSER_UNIQ_ID, ids[i]);
			}
		}
	}

	
	private String[] parseStringToArray(String syms) {
		if (syms != null && syms.length() > 0) {
			StringTokenizer tok = new StringTokenizer(syms, ";"); //$NON-NLS-1$
			ArrayList list = new ArrayList(tok.countTokens());
			while (tok.hasMoreElements()) {
				list.add(tok.nextToken());
			}
			return (String[]) list.toArray(new String[list.size()]);
		}
		return new String[0];
	}
}
