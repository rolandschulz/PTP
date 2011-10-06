/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.ptp.rdt.managedbuilder.xlc.ui.scannerdiscovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.xlc.core.scannerconfig.PerFileXLCScannerInfoCollector;




/**
 * @since 3.2
 */
@SuppressWarnings("restriction")
public class RemotePerFileXLCScannerInfoCollector extends
		PerFileXLCScannerInfoCollector {
	
	public static final String MACRO_DEFAULT_VALUE="1"; //$NON-NLS-1$
	/*
	 * (non-Javadoc)
	 * this is a function to correct the results generated from its parent.
	 * the symbols gotten from super.getAllSymbols() may contain the entries like this, <__null=0,1>, this function fixes it to <__null, 0>
	 */
	protected Map<String, String> getAllSymbols() {
		
		Map<String, String> super_symbols = super.getAllSymbols();
		Map<String, String> updatedSymbols = new HashMap<String, String>(super_symbols.size());
		if(super_symbols.size()>0){
			
			
			Set<String> symbolKeys = super_symbols.keySet();
			
			for(String thisSymbol : symbolKeys ){
				int assignIndex = thisSymbol.indexOf("="); //$NON-NLS-1$
				if(assignIndex<0){
					updatedSymbols.put(thisSymbol, super_symbols.get(thisSymbol));
				}else{
					String key = ScannerConfigUtil.getSymbolKey(thisSymbol);
					String value = ScannerConfigUtil.getSymbolValue(thisSymbol);
					if(value==null || value.length()==0){
						value = MACRO_DEFAULT_VALUE;
					}
					updatedSymbols.put(key, value);
				}
			}
			
		}
		return updatedSymbols;
	}

}
