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
package org.eclipse.ptp.rdt.xlc.remote;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.lrparser.xlc.XlcCTokenMap;
import org.eclipse.cdt.core.lrparser.xlc.XlcKeywords;
import org.eclipse.cdt.core.lrparser.xlc.XlcCScannerExtensionConfiguration;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPref;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.lrparser.xlc.c.XlcCParser;
import org.eclipse.ptp.rdt.core.IConfigurableLanguage;

public class RemoteXlcCLanguage extends GCCLanguage implements IConfigurableLanguage {

	public static final String ID = "org.eclipse.cdt.core.lrparser.xlc.c"; //$NON-NLS-1$ 
	
	private Map<String,String> remoteProperties;
	
	public void setProperties(Map<String, String> properties) {
		this.remoteProperties = properties;
	}
	
	
	@Override
	protected IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String,String> properties) {
		boolean supportVectors = Boolean.valueOf(remoteProperties.get(XlcPref.SUPPORT_VECTOR_TYPES.toString()));
		boolean supportDecimals = Boolean.valueOf(remoteProperties.get(XlcPref.SUPPORT_DECIMAL_FLOATING_POINT_TYPES.toString()));
		return new XlcCParser(scanner, new XlcCTokenMap(supportVectors, supportDecimals), getBuiltinBindingsProvider(), index, properties);
	}
	
	
	
	public String getId() {
		return ID;
	}
	
	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return XlcCScannerExtensionConfiguration.getInstance();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if(ICLanguageKeywords.class.equals(adapter))
			return XlcKeywords.ALL_C_KEYWORDS;
		
		return super.getAdapter(adapter);
	}
}
