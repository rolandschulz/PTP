/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Mike Kucera (IBM)
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * Holds on to info that is needed by the remote indexer.
 *
 */
public class RemoteIndexerInfoProvider implements IRemoteIndexerInfoProvider, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Map<String,RemoteScannerInfo> pathMap; 
	private final Map<Integer,RemoteScannerInfo> linkageMap; // used by the "parse up front" feature
	private final Map<String,String> languageMap; // (path -> language ID)
	private final Map<String,Boolean> isHeaderMap; // (path -> isHeaderUnit(boolean))
	private final Map<String,Boolean> indexerPreferences; // (preference key -> boolean)
	private final List<String> filesToParseUpFront;
	

	RemoteIndexerInfoProvider(Map<String,RemoteScannerInfo> pathMap, 
						      Map<Integer,RemoteScannerInfo> linkageMap, 
			                  Map<String,String> languageMap, 
			                  Map<String,Boolean> isHeaderMap, 
			                  Map<String,Boolean> indexerPreferences,
			                  List<String> filesToParseUpFront) {
		
		
		this.pathMap = pathMap == null ? Collections.<String,RemoteScannerInfo>emptyMap() : pathMap;
		this.linkageMap = linkageMap == null ? Collections.<Integer,RemoteScannerInfo>emptyMap() : linkageMap;
		this.languageMap = languageMap == null ? Collections.<String,String>emptyMap() : languageMap;
		this.isHeaderMap = isHeaderMap == null ? Collections.<String,Boolean>emptyMap() : isHeaderMap;
		this.indexerPreferences = indexerPreferences == null ? Collections.<String,Boolean>emptyMap() : indexerPreferences;
		this.filesToParseUpFront = filesToParseUpFront == null ? Collections.<String>emptyList() : filesToParseUpFront;
	}
	
	RemoteIndexerInfoProvider() {
		pathMap = Collections.emptyMap();
		linkageMap = Collections.emptyMap();
		languageMap = Collections.emptyMap();
		isHeaderMap = Collections.emptyMap();
		indexerPreferences = Collections.emptyMap();
		filesToParseUpFront = Collections.emptyList();
	}

	public IScannerInfo getScannerInformation(String path) {
		return getScannerInfo(pathMap, path);
	}
	
	public IScannerInfo getDefaultScannerInformation(int linkageId) {
		return getScannerInfo(linkageMap, linkageId);
	}
	
	private static <T> IScannerInfo getScannerInfo(Map<T,RemoteScannerInfo> map, T key) {
		IScannerInfo si = map.get(key);
		return si == null ? new RemoteScannerInfo() : si;
	}
	
	public String getLanguageID(String path) {
		return languageMap.get(path);
	}
	
	public boolean isHeaderUnit(String path) {
		return isHeaderMap.get(path);
	}
	
	public Map<String,Boolean> getIndexerPreferences() {
		return Collections.unmodifiableMap(indexerPreferences);
	}
	
	public List<String> getFilesToParseUpFront() {
		return Collections.unmodifiableList(filesToParseUpFront);
	}
	
	public String toString() {
		return "pathMap:" + pathMap +  //$NON-NLS-1$
		       " linkageMap:" + linkageMap + //$NON-NLS-1$
		       " languageMap:" + languageMap + //$NON-NLS-1$
		       " isHeaderMap:" + isHeaderMap + //$NON-NLS-1$
		       " preferences: " + indexerPreferences; //$NON-NLS-1$
	}

	
}

