package org.eclipse.ptp.cell.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;


public abstract class CellDefaultOptionValueHandler extends
		ManagedOptionValueHandler {

	public CellDefaultOptionValueHandler() {
		// Make default constructor available for extension point.
		super();
	}

	/**
	 * Sets an option value.
	 * 
	 * @param configuration
	 *            build configuration of option (may be IConfiguration or
	 *            IResourceInfo)
	 * @param holder
	 *            contains the holder of the option
	 * @param option
	 *            the option that is handled
	 * @param value
	 *            value to be set in to the option
	 * @return IOption The modified option. This can be the same option or a
	 *         newly created option.
	 */
	protected IOption setValue(IBuildObject configuration,
			IHoldsOptions holder, IOption option, boolean value)
			throws BuildException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, configuration.getId(), holder
				.getId(), option.getId(), value);
		Assert.isLegal(option.getBasicValueType() == IOption.BOOLEAN, option
				.getId()
				+ " is not a valid IOption.BOOLEAN option"); //$NON-NLS-1$
		IOption returnOption;
		if (configuration instanceof IConfiguration) {
			Debug.POLICY.enter(Debug.DEBUG_HANDLER, "IConfiguration"); //$NON-NLS-1$
			returnOption = ((IConfiguration) configuration).setOption(holder,
					option, value);
		} else if (configuration instanceof IResourceInfo) {
			Debug.POLICY.enter(Debug.DEBUG_HANDLER, "IResourceInfo"); //$NON-NLS-1$
			returnOption = ((IResourceInfo) configuration).setOption(holder,
					option, value);
		} else {
			// TODO: Handle this
			Debug.POLICY.error(Debug.DEBUG_HANDLER,
					"neither IConfiguration nor IResourceInfo"); //$NON-NLS-1$
			returnOption = null;
		}
		Debug.POLICY.exit(Debug.DEBUG_HANDLER);
		return returnOption;
	}

	/**
	 * Sets an option value.
	 * 
	 * @param configuration
	 *            build configuration of option (may be IConfiguration or
	 *            IResourceInfo)
	 * @param holder
	 *            contains the holder of the option
	 * @param option
	 *            the option that is handled
	 * @param value
	 *            value to be set in to the option
	 * @return IOption The modified option. This can be the same option or a
	 *         newly created option.
	 */
	protected IOption setValue(IBuildObject configuration,
			IHoldsOptions holder, IOption option, String[] value)
			throws BuildException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, configuration.getId(), holder
				.getId(), option.getId(), value);
		Assert.isLegal(option.getBasicValueType() == IOption.STRING_LIST,
				option.getId() + " is not a valid IOption.STRING_LIST option"); //$NON-NLS-1$
		IOption returnOption;
		if (configuration instanceof IConfiguration) {
			Debug.POLICY.enter(Debug.DEBUG_HANDLER, "IConfiguration"); //$NON-NLS-1$
			returnOption = ((IConfiguration) configuration).setOption(holder,
					option, value);
		} else if (configuration instanceof IResourceInfo) {
			Debug.POLICY.enter(Debug.DEBUG_HANDLER, "IResourceInfo"); //$NON-NLS-1$
			returnOption = ((IResourceInfo) configuration).setOption(holder,
					option, value);
		} else {
			// TODO: Handle this
			returnOption = null;
			Debug.POLICY.error(Debug.DEBUG_HANDLER,
					"neither IConfiguration nor IResourceInfo"); //$NON-NLS-1$			
		}
		Debug.POLICY.exit(Debug.DEBUG_HANDLER);
		return returnOption;
	}

	/**
	 * Include or exclude a String from a String list Option value.
	 * 
	 * @param configuration
	 *            build configuration of option (may be IConfiguration or
	 *            IResourceInfo)
	 * @param holder
	 *            contains the holder of the option
	 * @param option
	 *            the option that is handled
	 * @param previousValue
	 *            value to be set in to the option
	 * @param specificValue
	 *            value to be included or excluded from the string list option
	 * @param include
	 *            specifies if the specificValue will be included in the first
	 *            place of the string list option or excluded from the string
	 *            list option
	 * @return IOption The modified option. This can be the same option or a
	 *         newly created option.
	 */
	protected IOption setValue(IBuildObject configuration,
			IHoldsOptions holder, IOption option, String[] previousValues,
			String specificValue, boolean include) throws BuildException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_HANDLER, configuration.getId(), holder
				.getId(), option.getId(), previousValues, specificValue,
				include);
		Assert.isLegal(option.getBasicValueType() == IOption.STRING_LIST,
				option.getId() + " is not a valid IOption.STRING_LIST option"); //$NON-NLS-1$
		int specificValueCounter = 0;
		// Check if the specificValue is in the previousValues String list.
		for (int i = 0; i < previousValues.length; i++) {
			if (previousValues[i].equals(specificValue)) {
				specificValueCounter++;
			}
		}
		String[] newValues;
		if (include) {
			if (specificValueCounter == 0) {
				newValues = new String[previousValues.length + 1];
				// In some occasions the included value must be at first place
				newValues[0] = specificValue;
				for (int i = 0, j = 1; i < previousValues.length; i++, j++) {
					newValues[j] = previousValues[i];
				}
				Debug.POLICY.trace(Debug.DEBUG_HANDLER,
						"Included specific value {0} in list.", specificValue); //$NON-NLS-1$
			} else {
				newValues = previousValues;
				Debug.POLICY
						.trace(
								Debug.DEBUG_HANDLER,
								"Specific value {0} is already in list.", specificValue); //$NON-NLS-1$
			}
		} else {
			newValues = new String[previousValues.length - specificValueCounter];
			for (int i = 0, j = 0; i < previousValues.length; i++) {
				if (!previousValues[i].equals(specificValue)) {
					newValues[j] = previousValues[i];
					j++;
				} else {
					Debug.POLICY.trace(Debug.DEBUG_HANDLER,
							"Removed specific value {0} from list.", //$NON-NLS-1$
							specificValue);
				}
			}
		}
		IOption returnOption = setValue(configuration, holder, option,
				newValues);
		Debug.POLICY.exit(Debug.DEBUG_HANDLER);
		return returnOption;
	}
	
	/**
	 * 
	 * @param stringList the string list to be checked
	 * @param value the value that will be searched on the string list
	 * @return true if the value is in the string list
	 */
	protected boolean isValueInStringList(String[] stringList, String value) {
		boolean found = false;
		for (int i = 0; (i < stringList.length) && (!found); i++) {
			if (stringList[i].equals(value)) {
				found = true;
			}
		}
		return found;
	}
	
	/**
	 * 
	 * @param stringList the string list to be checked
	 * @param values the values that will be searched on the string list
	 * @return true if all the values are in the string list
	 */
	protected boolean areValuesInStringList(String[] stringList, String... values) {
		boolean found = true;
		for (int i = 0; (i < values.length) && (found); i++) {
			if (!isValueInStringList(stringList, values[i])) {
				found = false;
			}
		}
		return found;
	}
	
}