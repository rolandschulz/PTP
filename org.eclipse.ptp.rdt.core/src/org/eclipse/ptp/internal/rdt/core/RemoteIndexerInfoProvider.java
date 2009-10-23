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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * Holds on to info that is needed by the remote indexer.
 * This is basically just a transfer object that holds
 * data to be sent to the remote side when indexing.
 * 
 * It is the responsibility of RemoteIndexerInfoProviderFactory to
 * create instances of this class.
 *
 */
public class RemoteIndexerInfoProvider implements IRemoteIndexerInfoProvider, Serializable {
	private static final long serialVersionUID = 2L;
	
	private Map<String,RemoteScannerInfo> pathMap; // file path -> scanner info
	private Map<Integer,RemoteScannerInfo> linkageMap; // used by the "parse up front" feature
	private Map<String,String> languageMap; // (path -> language ID)
	private Map<String,Map<String,String>> languagePropertyMap; // language ID -> key/value pairs
	private Set<String> headerSet; // (path -> isHeaderUnit(boolean))
	private Set<String> indexerPreferences; // (preference key -> boolean)
	private List<String> filesToParseUpFront;
	
	
	//
	RemoteIndexerInfoProvider() { }

	RemoteIndexerInfoProvider(Map<String,RemoteScannerInfo> pathMap, 
						      Map<Integer,RemoteScannerInfo> linkageMap, 
			                  Map<String,String> languageMap, 
			                  Map<String, Map<String,String>> languagePropertyMap,
			                  Set<String> headerSet, 
			                  Set<String> indexerPreferences,
			                  List<String> filesToParseUpFront) {
		
		this.pathMap = pathMap;
		this.linkageMap = linkageMap;
		this.languageMap = languageMap;
		this.languagePropertyMap = languagePropertyMap;
		this.headerSet = headerSet;
		this.indexerPreferences = indexerPreferences;
		this.filesToParseUpFront = filesToParseUpFront;
	}
	
	
	// send as little over the wire as possible
	private void writeObject(ObjectOutputStream out) throws IOException {
		if(empty(pathMap))
			pathMap = null;
		if(empty(linkageMap))
			linkageMap = null;
		if(empty(languageMap))
			languageMap = null;
		if(empty(languagePropertyMap))
			languagePropertyMap = null;
		if(empty(headerSet))
			headerSet = null;
		if(empty(indexerPreferences))
			indexerPreferences = null;
		if(empty(filesToParseUpFront))
			filesToParseUpFront = null;
		
		out.defaultWriteObject();
	}
	
	private static boolean empty(Map<?,?> field) {
		return field != null && field.isEmpty();
	}

	private static boolean empty(Collection<?> field) {
		return field != null && field.isEmpty();
	}

	
	private static <T> IScannerInfo getScannerInfo(Map<T,RemoteScannerInfo> map, T key) {
		if(map == null)
			return new RemoteScannerInfo();
		IScannerInfo si = map.get(key);
		return si == null ? new RemoteScannerInfo() : si;
	}
	
	public IScannerInfo getScannerInformation(String path) {
		return getScannerInfo(pathMap, path);
	}
	
	public IScannerInfo getDefaultScannerInformation(int linkageId) {
		return getScannerInfo(linkageMap, linkageId);
	}
	
	public String getLanguageID(String path) {
		return languageMap == null ? null : languageMap.get(path);
	}
	
	public boolean isHeaderUnit(String path) {
		return headerSet == null ? false : headerSet.contains(path);
	}
	
	public boolean checkIndexerPreference(String key) {
		return indexerPreferences == null ? false : indexerPreferences.contains(key);
	}

	public List<String> getFilesToParseUpFront() {
		if(filesToParseUpFront == null)
			return Collections.emptyList();
		return filesToParseUpFront;
	}
	
	public Map<String,String> getLanguageProperties(String languageId) {
		if(languagePropertyMap == null)
			return Collections.emptyMap();
		return languagePropertyMap.get(languageId);
	}
	

	public String toString() {
		return "pathMap:" + pathMap +  //$NON-NLS-1$
		       " linkageMap:" + linkageMap + //$NON-NLS-1$
		       " languageMap:" + languageMap + //$NON-NLS-1$
		       " languagePropertyMap" + languagePropertyMap + //$NON-NLS-1$
		       " isHeaderMap:" + headerSet + //$NON-NLS-1$
		       " preferences: " + indexerPreferences + //$NON-NLS-1$
		       " filesToParseUpFront: " + filesToParseUpFront; //$NON-NLS-1$
	}

}
