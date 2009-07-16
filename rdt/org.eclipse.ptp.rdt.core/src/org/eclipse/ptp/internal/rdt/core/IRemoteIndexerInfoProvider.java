/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.indexer.IStandaloneScannerInfoProvider;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

/**
 * Provides necessary information to the remote indexer.
 */
public interface IRemoteIndexerInfoProvider extends IStandaloneScannerInfoProvider {

	/**
	 * Indexer preference keys used by the remote indexer.
	 * 
	 * These are mostly copies of the keys in the IndexerPreferences class, but since
	 * that class is not available on the remote side the keys are duplicated here.
	 * 
	 * @see IndexerPreferences
	 */
	public static String 
		//KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG = "indexUnusedHeadersWithDefaultLang", //$NON-NLS-1$
		//KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG = "indexUnusedHeadersWithAlternateLang", //$NON-NLS-1$
		KEY_INDEX_ALL_FILES = "indexAllFiles", //$NON-NLS-1$
		//KEY_INCLUDE_HEURISTICS = "useHeuristicIncludeResolution", //$NON-NLS-1$
		KEY_SKIP_ALL_REFERENCES = "skipReferences", //$NON-NLS-1$
		//KEY_SKIP_IMPLICIT_REFERENCES = "skipImplicitReferences", //$NON-NLS-1$
		KEY_SKIP_TYPE_REFERENCES = "skipTypeReferences", //$NON-NLS-1$
		KEY_SKIP_MACRO_REFERENCES = "updatePolicy"; //$NON-NLS-1$
	
	
	String getLanguageID(String path);
	
	boolean isHeaderUnit(String path);
	
	Map<String,Boolean> getIndexerPreferences();
	
	List<String> getFilesToParseUpFront();
}

