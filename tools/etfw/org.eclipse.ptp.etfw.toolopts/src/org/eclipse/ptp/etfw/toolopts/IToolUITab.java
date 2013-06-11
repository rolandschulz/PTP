package org.eclipse.ptp.etfw.toolopts;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 5.0
 */
public interface IToolUITab extends IAppInput {

	/**
	 * @return the key to this tab's input string defined in the ui and saved in the launch configuration
	 */
	public String getConfigID();

	/**
	 * @return the key to this tab's environment variables defined in the ui and saved in the launch configuration
	 */
	public String getConfigVarID();

	/**
	 * @return the name of this ui tab
	 */
	public String getName();

	/**
	 * @return the string defined by this ui tab, configured in the launch configuration
	 */
	public String getOptionString();

	/**
	 * @return the name of the tool getting input from this tab
	 */
	public String getToolName();

	/**
	 * @return the map of environment variable names and values defined in this tab and saved in the launch configuration
	 */
	public Map<String, String> getVarMap();

	/**
	 * Initializes the pane's widgets with values from the launch configuration
	 * @param configuration
	 * @throws CoreException
	 */
	public void initializePane(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * Returns true if this tab is embedded (managed internally but not displayed), otherwise false.
	 * @return
	 */
	public boolean isEmbedded();

	/**
	 * @return true if this is a virtual tab, not defined using an XML workflow. Otherwise false.
	 */
	public boolean isVirtual();

	/**
	 * Create the tab's widgets in the supplied composite
	 * @param comp
	 */
	public void makeToolPane(Composite comp);

	/**
	 * Create the tab's widgets in the supplied composite using the given listener for the widget's activity.
	 * @param comp
	 * @param paneListener
	 */
	public void makeToolPane(Composite comp, ToolPaneListener paneListener);

	/**
	 * For every option in this pane, if it is active/selected add its
	 * name/value string to the collection of active values for the whole pane
	 * 
	 */
	public void OptUpdate();

	/**
	 * Saves widget values in the tab to the launch configuration
	 * @param configuration
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration);

	/**
	 * Sets the default values in the launch configuration
	 * @param configuration
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration);

	/**
	 * Places the current string of name/value pairs in the option-display text
	 * box
	 * 
	 * @since 5.0
	 * 
	 */
	abstract void updateOptDisplay();

	/**
	 * If the object is a tool in this pane update the associated strings and
	 * displays for the object
	 * 
	 * @param source
	 *            The object being searched for and updated if found
	 * @return True if the object is found, otherwise false
	 */
	public boolean updateOptField(Object source);
}
