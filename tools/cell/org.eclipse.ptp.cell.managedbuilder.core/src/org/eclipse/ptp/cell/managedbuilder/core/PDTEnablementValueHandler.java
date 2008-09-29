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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;
import org.eclipse.ui.PlatformUI;


/**
 * @author laggarcia
 * 
 */
public class PDTEnablementValueHandler extends CellDefaultOptionValueHandler {

	protected static final String LINKER = "linker"; //$NON-NLS-1$

	protected static final String COMPILER = "compiler"; //$NON-NLS-1$

	protected static final String PPU = "ppu"; //$NON-NLS-1$

	protected static final String SPU = "spu"; //$NON-NLS-1$

	protected static final String PPC32 = "32"; //$NON-NLS-1$

	protected static final String PPC64 = "64"; //$NON-NLS-1$

	protected final String COMMA = ","; //$NON-NLS-1$

	protected final String EMPTY_STRING = ""; //$NON-NLS-1$

	public PDTEnablementValueHandler() {
		// Make default constructor available for extension point.
		super();
	}

	/**
	 * Handles the selection of correct build options when PDT build option is
	 * enabled.
	 * 
	 * PDT build option works as a shortcut to properly set numerous other build
	 * options that need to be set when building a project that will run with
	 * PDT tool.
	 * 
	 * extraArguments must be the list of the ids of the options affected by
	 * this shortcut. For PDT linker options extraArguments must contain the
	 * Emit relocs option id and the library search path option id, in this
	 * order. For PDT compiler options extraArguments must contain the symbols
	 * definition option id and the include path option id, in this order.
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
		try {
			Assert.isLegal(option.getBasicValueType() == IOption.BOOLEAN,
					"invalid option type"); //$NON-NLS-1$
			if (event == EVENT_APPLY) {
				String[] affectedOptionIds = extraArgument.split(COMMA);
				String optionId = option.getId();
				boolean optionTicked = option.getBooleanValue();

				if (optionId.contains(LINKER)) {
					if (optionTicked) {
						Debug.POLICY.trace(Debug.DEBUG_HANDLER,
								"PDT linker build option was selected"); //$NON-NLS-1$
						if (optionId.contains(SPU)) {
							// Only in SPU case we have to issue a warning for
							// the user as the PPU Compiler doesn't need a
							// specific PDT build setting.
							MessageDialog
									.openWarning(
											PlatformUI.getWorkbench()
													.getActiveWorkbenchWindow()
													.getShell(),
											Messages.PDTBuildFlagsWarningDialogTitle,
											Messages.PDTLinkerBuildFlagsWarningDialogMessage);
						}
					} else {
						Debug.POLICY.trace(Debug.DEBUG_HANDLER,
								"PDT linker build option was deselected"); //$NON-NLS-1$
					}
					updateLinkerOptions(configuration, holder, optionId,
							affectedOptionIds, optionTicked);
				} else if (optionId.contains(COMPILER)) {
					if (optionTicked) {
						Debug.POLICY.trace(Debug.DEBUG_HANDLER,
								"PDT compiler build option was selected"); //$NON-NLS-1$
						MessageDialog
								.openWarning(
										PlatformUI.getWorkbench()
												.getActiveWorkbenchWindow()
												.getShell(),
										Messages.PDTBuildFlagsWarningDialogTitle,
										Messages.PDTCompilerBuildFlagsWarningDialogMessage);
					} else {
						Debug.POLICY.trace(Debug.DEBUG_HANDLER,
								"PDT compiler build option was deselected"); //$NON-NLS-1$
					}
					updateCompilerOptions(configuration, holder, optionId,
							affectedOptionIds, optionTicked);
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

	private void updateLinkerOptions(IBuildObject configuration,
			IHoldsOptions holder, String optionId, String[] affectedOptionIds,
			boolean optionTicked) {
		// For the linker options, we expect one or two affected Options.
		Assert
				.isLegal(affectedOptionIds.length == 1
						|| affectedOptionIds.length == 2,
						"PDT linker build options should always affect 1 or 2 other linker options."); //$NON-NLS-1$
		try {
			// Setting the first affected option
			// For the linker options, we expect the first affected Option in
			// the list to be the "-Wl,-q" (emit-relocs)
			setValue(configuration, holder, holder
					.getOptionBySuperClassId(affectedOptionIds[0]),
					optionTicked);
			// Setting the second affected option (only for SPUs)
			// For the linker options, we expect the second affected Option in
			// the list to be the library search path
			if (optionId.contains(SPU)) {
				IOption affectedOption = holder
						.getOptionBySuperClassId(affectedOptionIds[1]);
				String libraryPath = PDTBuildProperties.SPU_PDT_LIBRARY;
				setValue(configuration, holder, affectedOption, affectedOption
						.getBasicStringListValue(), libraryPath, optionTicked);

			}
		} catch (BuildException be) {
			Debug.POLICY.error(Debug.DEBUG_HANDLER, be);
			Debug.POLICY.logError(be);
		}
	}

	private void updateCompilerOptions(IBuildObject configuration,
			IHoldsOptions holder, String optionId, String[] affectedOptionIds,
			boolean optionTicked) {
		// For the compiler options, we expect one (for PPU) or two (for SPU)
		// affected Options.
		Assert
				.isLegal(
						affectedOptionIds.length == 1
								|| affectedOptionIds.length == 2,
						"PDT compiler build options should always affect 1 or 2 other compiler options."); //$NON-NLS-1$
		try {
			if (optionId.contains(SPU)) {
				// Only the SPU compiler is affected by PDT flags
				// Setting the first affected Option for SPU
				// For the SPU compiler we expect the first affected Option in
				// the list
				// to be the Symbol Definition in case of SPU.
				IOption affectedOption = holder
						.getOptionBySuperClassId(affectedOptionIds[0]);
				affectedOption = setValue(configuration, holder,
						affectedOption, affectedOption
								.getBasicStringListValue(),
						PDTBuildProperties.PDT_MAIN_SYMBOL, optionTicked);
				affectedOption = setValue(configuration, holder,
						affectedOption, affectedOption
								.getBasicStringListValue(),
						PDTBuildProperties.PDT_EXIT_SYMBOL, optionTicked);
				setValue(configuration, holder, affectedOption, affectedOption
						.getBasicStringListValue(),
						PDTBuildProperties.MFCIO_TRACE_SYMBOL, optionTicked);
				// Setting the second affected Option for SPU
				// For the compiler we expect this affected Option in the list
				// to be the Include Paths
				// As PDT needs the include path to be the first option in the
				// command line, we will remove it and include it again as the
				// include routine always put the new value at first place
				affectedOption = holder
						.getOptionBySuperClassId(affectedOptionIds[1]);
				String includePath = PDTBuildProperties.SPU_PDT_INCLUDE_PATH;
				if (optionTicked) {
					affectedOption = setValue(configuration, holder,
							affectedOption, affectedOption
									.getBasicStringListValue(), includePath,
							false);
				}
				setValue(configuration, holder, affectedOption, affectedOption
						.getBasicStringListValue(), includePath, optionTicked);
			} else {
				Assert.isTrue(false, "Invalid option"); //$NON-NLS-1$
			}

		} catch (BuildException be) {
			Debug.POLICY.error(Debug.DEBUG_HANDLER, be);
			Debug.POLICY.logError(be);
		}
	}

}
