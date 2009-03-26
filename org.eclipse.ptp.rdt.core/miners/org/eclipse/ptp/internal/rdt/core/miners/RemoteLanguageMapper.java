/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.indexer.ILanguageMapper;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;

/**
 * @author crecoskie
 *
 */
public class RemoteLanguageMapper implements ILanguageMapper {
	
	
	private static final Map<String,ILanguage> RDT_SUPPORTED_LANGUAGES;
	static {
		RDT_SUPPORTED_LANGUAGES = new HashMap<String,ILanguage>();
		RDT_SUPPORTED_LANGUAGES.put(GPPLanguage.ID, GPPLanguage.getDefault());
		RDT_SUPPORTED_LANGUAGES.put(GCCLanguage.ID, GCCLanguage.getDefault());
	}
	
	
	private final IRemoteIndexerInfoProvider provider;
	
	public RemoteLanguageMapper(IRemoteIndexerInfoProvider provider) {
		this.provider = provider;
	}
	
	
	public ILanguage getLanguage(String file) {
		String id = provider.getLanguageID(file);
		return RDT_SUPPORTED_LANGUAGES.get(id);
	}
	
}
