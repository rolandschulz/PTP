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

import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.indexer.IStandaloneScannerInfoProvider;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.ptp.rdt.core.ILanguagePropertyProvider;

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
	
	
	/**
	 * Returns true if the given indexer preference is
	 * enabled.
	 */
	boolean checkIndexerPreference(String key);
	
	/**
	 * Returns the language ID for the given file path.
	 * This tells the remote indexer which parser to use for the file.
	 */
	String getLanguageID(String path);
	
	/**
	 * Returns true if the file represented by the given path
	 * is a header file, returns false if its a source file.
	 * 
	 * TODO is this information actually being used?
	 */
	boolean isHeaderUnit(String path);
	
	/**
	 * Returns a list of files to be parsed up front
	 * before the project is indexed.
	 */
	List<String> getFilesToParseUpFront();
	
	/**
	 * Returns an extensible set of language properties.
	 * TODO
	 * @see ILanguagePropertyProvider
	 * @see ILanguage
	 */
	Map<String,String> getLanguageProperties(String languageId);
}

