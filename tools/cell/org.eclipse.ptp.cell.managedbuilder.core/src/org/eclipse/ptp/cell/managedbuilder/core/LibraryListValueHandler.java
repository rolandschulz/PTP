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
public class LibraryListValueHandler extends CellDefaultOptionValueHandler {

	protected static final String COMMA = ","; //$NON-NLS-1$

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public LibraryListValueHandler() {
		// Make default constructor available for extension point.
		super();
	}

	/**
	 * Handles transfer between values between UI element and back-end in
	 * different circumstances. extraArgument must be a list with the ids of the
	 * libraries shortcut options on which this option should pay attention to
	 * define their values.
	 * 
	 * If any of these other options in the extraArgument list are selected to
	 * be used, the respective library name will be included in this StringList
	 * option. If any of these other options in the extraArgument list is
	 * deselected, the respective library name will be removed from this
	 * StringList option. Please refer to
	 * org.eclipse.ptp.cell.managedbuilder.core.EspecialLibraryShortcutValueHandler
	 * for more information.
	 * 
	 * If a library name is included in this StringList option and this library
	 * is referenced in the extraArgument list, the respective especial library
	 * shortcut option will be selected. If a library name is removed from this
	 * StringList option and this library is referenced in the extraArgument
	 * list, the respective especial library shortcut option will be deselected.
	 * 
	 * The extraArgument list must have the following format:
	 * 
	 * <pre>
	 *  first especial library shortcut option id,second especial library shortcut option id,...,nth special library shortcut option id
	 *  
	 *  @param configuration  build configuration of option 
	 *                        (may be IConfiguration or IResourceInfo)
	 *  @param holder         contains the holder of the option
	 *  @param option         the option that is handled
	 *  @param extraArgument  extra argument for handler
	 *  @param event          event to be handled 
	 * 
	 *  @return  True when the event was handled, false otherwise.
	 *  This enables default event handling can take place.
	 * 
	 */
	public boolean handleValue(IBuildObject configuration,
			IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, configuration.getId(), holder.getId(), option.getId(), extraArgument, event);

		if (event == EVENT_APPLY) {
			try {
				Debug.POLICY.trace(Debug.DEBUG_HANDLER, "Value is: {0}", option.getValue()); //$NON-NLS-1$
				String[] libraries = option.getLibraries();
				String[] especialLibrariesIds = extraArgument.split(COMMA);
				if (libraries.length > 0) {
					for (int i = 0; i < especialLibrariesIds.length; i++) {
						IOption especialLibraryOption = holder
								.getOptionBySuperClassId(especialLibrariesIds[i]);
						String especialLibrary = especialLibraryOption
								.getCommand().replaceFirst(option.getCommand(),
										EMPTY_STRING);
						// If there are any libraries selected, figure out if
						// there is any especial library shortcut option
						// registered for that library and select or deselect it
						// accordingly.
						boolean setTrue = false;
						for (int j = 0; j < libraries.length; j++) {
							if (especialLibrary.equals(libraries[j])) {
								setTrue = true;
							}
						}
						if (especialLibraryOption.getBooleanValue() != setTrue) {
							Debug.POLICY.trace(Debug.DEBUG_HANDLER, "Set special library {0} to {1}.", especialLibrary, setTrue); //$NON-NLS-1$
							setValue(configuration, holder, especialLibraryOption, setTrue);
						} else {
							Debug.POLICY.trace(Debug.DEBUG_HANDLER, "Keep special library {0} with {1}.", especialLibrary, especialLibraryOption.getBooleanValue()); //$NON-NLS-1$
						}
					}
				} else {
					// No library is set. No especial library should be set
					// also.
					Debug.POLICY.trace(Debug.DEBUG_HANDLER, "No library set. Set all {0} special libraries to {1}.", especialLibrariesIds.length, false); //$NON-NLS-1$
					for (int i = 0; i < especialLibrariesIds.length; i++) {
						IOption especialLibraryOption = holder.getOptionBySuperClassId(especialLibrariesIds[i]);
						if (especialLibraryOption.getBooleanValue()) {
							setValue(configuration, holder, especialLibraryOption, false);
						}
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
