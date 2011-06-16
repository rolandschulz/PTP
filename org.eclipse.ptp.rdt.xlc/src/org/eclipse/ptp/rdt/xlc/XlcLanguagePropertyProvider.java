/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.xlc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcLanguagePreferences;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPref;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.core.ILanguagePropertyProvider;


public class XlcLanguagePropertyProvider implements ILanguagePropertyProvider {
	
	public Map<String, String> getProperties(String languageId, IProject project) {
		Map<String,String> props = new HashMap<String,String>();
		for(XlcPref pref : XlcPref.values()) {
			props.put(pref.toString(), XlcLanguagePreferences.get(pref, project));
		}
		return props;
	}

}
