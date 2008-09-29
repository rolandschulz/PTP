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
package org.eclipse.ptp.cell.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;


/**
 * @author laggarcia
 * @since 1.1.0
 * 
 */
public class OptionNotApplicableInCommandLine implements IOptionApplicability {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionUsedInCommandLine(org.eclipse.cdt.managedbuilder.core.IBuildObject,
	 *      org.eclipse.cdt.managedbuilder.core.IHoldsOptions,
	 *      org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	public boolean isOptionUsedInCommandLine(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		// This option should not be used in the command line.
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_APPLICABILITY);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionVisible(org.eclipse.cdt.managedbuilder.core.IBuildObject,
	 *      org.eclipse.cdt.managedbuilder.core.IHoldsOptions,
	 *      org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	public boolean isOptionVisible(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		// This option is always visible to the user.
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_APPLICABILITY);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionEnabled(org.eclipse.cdt.managedbuilder.core.IBuildObject,
	 *      org.eclipse.cdt.managedbuilder.core.IHoldsOptions,
	 *      org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	public boolean isOptionEnabled(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		// This option is alwasy enabled to the user		
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_APPLICABILITY);
		return true;
	}

}
