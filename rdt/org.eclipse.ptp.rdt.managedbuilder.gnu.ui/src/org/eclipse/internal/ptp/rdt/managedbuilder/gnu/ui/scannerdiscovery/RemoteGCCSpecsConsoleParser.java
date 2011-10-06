
/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.internal.ptp.rdt.managedbuilder.gnu.ui.scannerdiscovery;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCSpecsConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.runtime.CoreException;



public class RemoteGCCSpecsConsoleParser extends GCCSpecsConsoleParser {

	public static String LOG_TAG="RemoteGCCPerFileBOPConsoleParser:"; //$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
	 */
	public void shutdown() {
		super.shutdown();
		
		if(fCollector != null && fCollector instanceof IScannerInfoCollector2) {
			IScannerInfoCollector2 collector = (IScannerInfoCollector2) fCollector;
			try {
				collector.updateScannerConfiguration(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				TraceUtil.outputError(LOG_TAG + " catch a CoreException when shutting the parser ", e.toString()); //$NON-NLS-1$
			}
		}
		
	}
}
