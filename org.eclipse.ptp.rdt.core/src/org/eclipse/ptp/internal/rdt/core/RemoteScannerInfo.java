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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;

public class RemoteScannerInfo implements IScannerInfo, Serializable {
	private static final long serialVersionUID = 1L;
	
	Map<String, String> fSymbols;
	ArrayList<String> fIncludePaths;

	public RemoteScannerInfo() {
		fSymbols = new HashMap<String, String>();
		fIncludePaths = new ArrayList<String>();
	}
	
	public Map<String, String> getDefinedSymbols() {
		return Collections.unmodifiableMap(fSymbols);
	}

	public String[] getIncludePaths() {
		return fIncludePaths.toArray(new String[fIncludePaths.size()]);
	}
}
