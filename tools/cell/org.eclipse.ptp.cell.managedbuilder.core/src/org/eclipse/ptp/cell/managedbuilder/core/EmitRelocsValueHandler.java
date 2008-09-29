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
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;


/**
 * @author laggarcia
 * 
 */
public class EmitRelocsValueHandler extends PDTAffectedOptionValueHandler {

	/**
	 * 
	 */
	public EmitRelocsValueHandler() {
		// Make default constructor available for extension point.
		super();
	}

	/**
	 * Handles the selection of emit relocations on the linker.
	 * 
	 * Depending on the emit relocations option being checked or not the PDT
	 * linker shortcut option can be affected.
	 * 
	 * extraArguments must be a list of option ids separated by comma. The first
	 * id must be the PDT linker shortcut option id and the second id must be
	 * the static objects (SPU) or library search path (PPU) option id, as this
	 * last option in conjunction with the emit relocations option defines if
	 * the linker build is enabled for PDT use or not.
	 * 
	 * @param configuration
	 *            build configuration of option (may be IConfiguration or
	 *            IResourceInfo)
	 * @param holder
	 *            contains the holder of the option
	 * @param option
	 *            the option that is handled
	 * @param extraArgument
	 *            extra argument for handler
	 * @param event
	 *            event to be handled
	 * 
	 * @return True when the event was handled, false otherwise. This enables
	 *         default event handling can take place.
	 */
	public boolean handleValue(IBuildObject configuration,
			IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, configuration.getId(), holder
				.getId(), option.getId(), extraArgument, event);

		try {
			if (event == EVENT_APPLY) {
				String[] arguments = extraArgument.split(COMMA);
				String holderId = holder.getId();
				if (holderId.contains(PPU)) {
					setValue(configuration, holder, holder
							.getOptionBySuperClassId(arguments[0]),
							arePDTPPULinkerBuildOptionsEnabled(holder, option));
				} else if (holderId.contains(SPU)) {
					setValue(
							configuration,
							holder,
							holder.getOptionBySuperClassId(arguments[0]),
							arePDTSPULinkerBuildOptionsEnabled(
									holder,
									option,
									holder
											.getOptionBySuperClassId(arguments[1])));
				} else {
					Assert.isTrue(false, "Invalid option"); //$NON-NLS-1$
				}
			}
			return true;
		} catch (BuildException be) {
			Debug.POLICY.error(Debug.DEBUG_HANDLER, be);
			Debug.POLICY.logError(be);
		}
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, false);
		return false;
	}

}
