/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.debug.internal.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.fdt.debug.core.CDebugCorePlugin;
import org.eclipse.fdt.debug.core.IFDTLaunchConfigurationConstants;
import org.eclipse.fdt.debug.core.ICDebugConstants;
import org.eclipse.fdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.fdt.debug.core.model.CVariableFormat;

/**
 * Represents a register in the CDI model.
 */
public class CRegister extends CGlobalVariable implements IRegister {

	/**
	 * Constructor for CRegister.
	 */
	protected CRegister( CRegisterGroup parent, ICDIRegisterDescriptor cdiRegisterDescriptor ) {
		super( parent, null, cdiRegisterDescriptor );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
	}

	/**
	 * Constructor for CRegister.
	 */
	protected CRegister( CRegisterGroup parent, ICDIRegisterDescriptor registerObject, String message ) {
		super( parent, null, registerObject, message );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegister#getRegisterGroup()
	 */
	public IRegisterGroup getRegisterGroup() throws DebugException {
		return (IRegisterGroup)getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.internal.core.model.CVariable#isBookkeepingEnabled()
	 */
	protected boolean isBookkeepingEnabled() {
		boolean result = false;
		try {
			result = getLaunch().getLaunchConfiguration().getAttribute( IFDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false );
		}
		catch( CoreException e ) {
		}
		return result;
	}
}