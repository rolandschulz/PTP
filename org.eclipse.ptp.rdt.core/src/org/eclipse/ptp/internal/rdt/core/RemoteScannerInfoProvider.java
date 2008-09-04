/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.indexer.IStandaloneScannerInfoProvider;

/**
 * This provider is just a holder for RemoteScannerInfo objects that will
 * be sent to the remote indexer.
 * 
 * @author Mike Kucera
 *
 */
class RemoteScannerInfoProvider implements IStandaloneScannerInfoProvider, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Map<String,RemoteScannerInfo> pathMap;
	private final Map<Integer,RemoteScannerInfo> linkageMap; // used by the "parse up front" feature
	

	RemoteScannerInfoProvider(Map<String,RemoteScannerInfo> pathMap, Map<Integer,RemoteScannerInfo> linkageMap) {
		this.pathMap = pathMap == null ? Collections.<String,RemoteScannerInfo>emptyMap() : pathMap;
		this.linkageMap = linkageMap == null ? Collections.<Integer,RemoteScannerInfo>emptyMap() : linkageMap;
	}
	
	RemoteScannerInfoProvider() {
		pathMap = Collections.emptyMap();
		linkageMap = Collections.emptyMap();
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
	
	public String toString() {
		return pathMap.toString();
	}
}

