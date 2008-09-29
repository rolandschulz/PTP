/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.xl.ui.internal;

import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.ptp.cell.managedbuilder.xl.ui.debug.Debug;
import org.eclipse.ptp.cell.utils.packagemanager.PackageManagementSystemManager;


/**
 * @author laggarcia
 * @since 1.2.2
 */
public class ManagedXLToolChainSupport implements IManagedIsToolChainSupported {

	protected static final String DELIMITER = XLPackages
			.getString("ManagedXLToolChainSupport.XLPackagesDelimiter"); //$NON-NLS-1$

	protected static final String PACKAGE_LIST = XLPackages
			.getString("ManagedXLToolChainSupport.XLPackages"); //$NON-NLS-1$

	public boolean isSupported(IToolChain toolChain,
			PluginVersionIdentifier version, String instance) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_SUPPORT, toolChain.getId(), instance);
		boolean result = PackageManagementSystemManager.getPackageManager().queryAll(
				PACKAGE_LIST, DELIMITER);
		Debug.POLICY.exit(Debug.DEBUG_SUPPORT, result);		
		return result;
	}

}
