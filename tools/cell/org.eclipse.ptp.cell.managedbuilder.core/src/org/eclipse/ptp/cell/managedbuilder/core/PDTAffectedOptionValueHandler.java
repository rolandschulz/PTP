/******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;


/**
 * @author laggarcia
 * 
 */
public class PDTAffectedOptionValueHandler extends
		CellDefaultOptionValueHandler {

	protected final String PPU = "ppu"; //$NON-NLS-1$

	protected final String SPU = "spu"; //$NON-NLS-1$

	protected static final String PPC32 = "32"; //$NON-NLS-1$

	protected static final String PPC64 = "64"; //$NON-NLS-1$

	protected final String COMMA = ","; //$NON-NLS-1$

	/**
	 * 
	 */
	public PDTAffectedOptionValueHandler() {
		// Make default constructor available for extension point.
		super();
	}

	protected boolean arePDTPPULinkerBuildOptionsEnabled(IHoldsOptions holder,
			IOption emitRelocs) {
		try {
			return emitRelocs.getBooleanValue();
		} catch (BuildException be) {
			Debug.POLICY.error(Debug.DEBUG_HANDLER, be);
			Debug.POLICY.logError(be);
		}
		return false;
	}

	protected boolean arePDTSPULinkerBuildOptionsEnabled(IHoldsOptions holder,
			IOption emitRelocs, IOption lib) {
		try {
			if (emitRelocs.getBooleanValue()
					&& isValueInStringList(lib.getBasicStringListValue(),
							PDTBuildProperties.SPU_PDT_LIBRARY)) {
				return true;
			}
		} catch (BuildException be) {
			Debug.POLICY.error(Debug.DEBUG_HANDLER, be);
			Debug.POLICY.logError(be);
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	protected boolean arePDTCompilerBuildOptionsEnabled(IHoldsOptions holder,
			IOption symbolDefinition, IOption includePath) {
		String holderId = holder.getId();
		try {
			// Only SPU compilers are affected by PDT build flags
			if (holderId.contains(SPU)) {
				if (areValuesInStringList(symbolDefinition
						.getBasicStringListValue(),
						PDTBuildProperties.MFCIO_TRACE_SYMBOL,
						PDTBuildProperties.PDT_EXIT_SYMBOL,
						PDTBuildProperties.PDT_MAIN_SYMBOL)
						&& isValueInStringList(includePath
								.getBasicStringListValue(),
								PDTBuildProperties.SPU_PDT_INCLUDE_PATH)) {
					return true;
				}
			} else {
				Assert.isTrue(false, "Invalid option"); //$NON-NLS-1$
			}
		} catch (BuildException be) {
			Debug.POLICY.error(Debug.DEBUG_HANDLER, be);
			Debug.POLICY.logError(be);
		}
		return false;
	}

}
