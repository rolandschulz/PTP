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
package org.eclipse.ptp.cell.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class EspecialLibraryShortcutValueHandler extends
		CellDefaultOptionValueHandler {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public EspecialLibraryShortcutValueHandler() {
		// Make default constructor available for extension point.
		super();
	}

	/**
	 * Handles transfer between values between UI element and back-end in
	 * different circumstances. extraArgument must be the id of the option that
	 * holds all the libraries that are used by the project.
	 * 
	 * If the especial library option that references this
	 * ManagedOptionValueHandler is selected,the corresponding library should be
	 * included in the StringList option that holds all the libraries used by
	 * this project. If the especial library option that references this
	 * ManagedOptionValueHandler is deselected, the corresponding library should
	 * be removed in the StringList option that holds all the libraries used by
	 * this project.
	 * 
	 * For more information about the relationship between these especial
	 * library options and the StringList option that holds all the libraries
	 * used by this project, see
	 * org.eclipse.ptp.cell.managedbuilder.core.LibraryListValueHandler.
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
	 * 
	 */
	public boolean handleValue(IBuildObject configuration,
			IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, configuration.getId(), holder
				.getId(), option.getId(), extraArgument, event);

		if (event == EVENT_APPLY) {
			IOption librariesOption = holder
					.getOptionBySuperClassId(extraArgument);
			String especialLibrary = option.getCommand().replaceFirst(
					librariesOption.getCommand(), EMPTY_STRING);
			try {
				Debug.POLICY.trace(Debug.DEBUG_HANDLER, "Value is: {0}", option //$NON-NLS-1$
						.getValue());
				if (option.getBooleanValue() == true) {
					setValue(configuration, holder, librariesOption,
							librariesOption.getLibraries(), especialLibrary,
							true);
				} else {
					setValue(configuration, holder, librariesOption,
							librariesOption.getLibraries(), especialLibrary,
							false);
				}
			} catch (BuildException be) {
				// TODO handle this exception properly
				Debug.POLICY.error(Debug.DEBUG_HANDLER, be);
				be.printStackTrace();
			}
			Debug.POLICY.exit(Debug.DEBUG_HANDLER, true);
			return true;
		}

		Debug.POLICY.exit(Debug.DEBUG_HANDLER, false);
		return false;
	}

}
