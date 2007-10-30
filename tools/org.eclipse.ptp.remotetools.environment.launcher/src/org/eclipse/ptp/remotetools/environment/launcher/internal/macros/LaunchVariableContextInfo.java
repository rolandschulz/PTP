/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.internal.macros;

import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.remotetools.environment.launcher.macros.ILaunchVariableContextInfo;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class LaunchVariableContextInfo implements ILaunchVariableContextInfo {

	private int type;

	private Object data;

	public LaunchVariableContextInfo(int type, Object data) {
		this.type = type;
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getContextData()
	 */
	public Object getContextData() {
		return this.data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getContextType()
	 */
	public int getContextType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getNext()
	 */
	public IVariableContextInfo getNext() {
		switch (this.type) {
		case CONTEXT_LAUNCH:
			if (this.data instanceof ILaunchConfiguration) {
				IWorkspace wsp = ResourcesPlugin.getWorkspace();
				if (wsp != null) {
					return new LaunchVariableContextInfo(CONTEXT_WORKSPACE, wsp);
				}
			}
			break;
		case CONTEXT_WORKSPACE:
			return null;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getSuppliers()
	 */
	public ICdtVariableSupplier[] getSuppliers() {
		switch (this.type) {
		case CONTEXT_LAUNCH:
			if (this.data instanceof ILaunchConfiguration) {
				return new ICdtVariableSupplier[] { LaunchVariableManager.launchMacroSupplier };
			}
			break;
		case CONTEXT_WORKSPACE:
			if (this.data instanceof IWorkspace) {
				return new ICdtVariableSupplier[] { LaunchVariableManager.eclipseVariablesMacroSupplier };
			}
			break;
		}
		return null;
	}

}
