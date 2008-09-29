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
package org.eclipse.ptp.cell.managedbuilder.xl.ui;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.managedbuilder.core.CellDefaultOptionValueHandler;
import org.eclipse.ptp.cell.managedbuilder.xl.ui.debug.Debug;
import org.eclipse.ptp.cell.managedbuilder.xl.ui.internal.XlManagedMakeMessages;
import org.eclipse.ui.PlatformUI;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class VmxValueHandler extends CellDefaultOptionValueHandler {

	public VmxValueHandler() {
		// Make standard constructor available to extension point.
	}

	/**
	 * Handles transfer between values between UI element and back-end in
	 * different circumstances. extraArgument must be the id of the other option
	 * in which this option depends on.
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
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, configuration.getId(), holder.getId(), option.getId(), extraArgument, event);

		if (event == EVENT_APPLY) {
			try {
				Debug.POLICY.trace(Debug.DEBUG_HANDLER, "Value is: {0}", option.getValue()); //$NON-NLS-1$
				if (option.getBooleanValue() == false) {
					IOption otherOption = holder
							.getOptionBySuperClassId(extraArgument);
					if (otherOption.getBooleanValue() == true) {
						MessageDialog
								.openWarning(
										PlatformUI.getWorkbench()
												.getActiveWorkbenchWindow()
												.getShell(),
										XlManagedMakeMessages.VmxWarningDialogTitle,
										NLS
												.bind(
														XlManagedMakeMessages.VmxWarningDialogMessage,
														option.getCommand(),
														otherOption
																.getCommand()));
					}
				}
			} catch (BuildException be) {
				Debug.POLICY.error(Debug.DEBUG_HANDLER, be); 
				Debug.POLICY.logError(be);
			}
			Debug.POLICY.exit(Debug.DEBUG_HANDLER, true); 
			return true;
		}

		Debug.POLICY.exit(Debug.DEBUG_HANDLER, false); 
		return false;
	}
	
}
