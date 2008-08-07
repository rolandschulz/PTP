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
	
	private final Map<String,RemoteScannerInfo> scannerInfoMap;
	

	RemoteScannerInfoProvider(Map<String,RemoteScannerInfo> map) {
		if(map == null)
			throw new NullPointerException();
		this.scannerInfoMap = map;
	}
	
	RemoteScannerInfoProvider() {
		scannerInfoMap = Collections.emptyMap();
	}

	public IScannerInfo getScannerInformation(String path) {
		IScannerInfo scannerInfo = scannerInfoMap.get(path);
		if(scannerInfo == null)
			return new RemoteScannerInfo();
		return scannerInfo;
	}
	
	public String toString() {
		return scannerInfoMap.toString();
	}
}

