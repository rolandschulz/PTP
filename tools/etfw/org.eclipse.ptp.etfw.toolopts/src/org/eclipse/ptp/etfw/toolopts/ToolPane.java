/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.toolopts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * This class represents a panel of tool options which may be selected or
 * excluded via checkboxes. Each option may have means of including other data
 * such as comboboxes or text fields
 * 
 * @author wspear
 * 
 */
public class ToolPane implements IAppInput, IToolUITab {

	/**
	 * A listener class to launch file or directory browsers from browse buttons
	 * for a ToolPane's tools
	 * 
	 * @author wspear
	 * 
	 */
	protected class MakeBrowseListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {

			Object source = e.getSource();
			for (int i = 0; i < options.length; i++) {
				if (source == options[i].browser) {

					ToolMaker.optBrowse(options[i]);
					break;
				}
			}
		}
	}

	public static final int ALL_COMPILERS = 0;
	public static final int CC_COMPILER = 1;
	public static final int CXX_COMPILER = 2;
	public static final int F90_COMPILER = 3;
	public static final int EXEC_UTIL = 4;
	public static final int ANALYSIS = 5;

	public static final int ENV_VAR = 6;

	/**
	 * If true then this pane is merely a placeholder for a pane defined
	 * elsewhere
	 */
	public final boolean virtual;

	/**
	 * Contains the name/value pairs of all options selected and defined in this
	 * pane
	 */
	private StringBuffer optString = null;

	private Map<String, String> varMap = null;

	/**
	 * The listener for browse buttons in this pane
	 */
	protected SelectionListener browseListener;

	/**
	 * The listener for check boxes and value entry fields in this pane
	 */
	protected ToolPaneListener checkListener;

	/**
	 * If true, the showOpts options display is used.
	 */
	public boolean displayOptions = true;

	/**
	 * The text box that shows the selected/defined options in this pane
	 */
	public Text showOpts = null;

	/**
	 * The individual tool options defined in this pane
	 */
	protected ToolOption[] options;

	/**
	 * The name associated with this tool pane
	 * @since 4.1
	 */
	public String paneName;
	
	/**
	 * The name of the top-level tool workflow associated with this pane
	 */
	public String toolName;

	/**
	 * Added to the beginning of the opt string Default: empty
	 */
	public String prependOpts = "";

	/**
	 * String/character put before the first and after the last option in the
	 * option string Default: empty
	 */
	public String encloseOpts = "";

	/**
	 * String/Character placed between individual options Default: newline
	 */
	public String separateOpts = "\n";

	/**
	 * String/character placed around values Default: "
	 */
	public String encloseValues = "\"";

	/**
	 * String/character placed between option names and values Default: =
	 */
	public String separateNameValue = "=";
	/**
	 * This unique, generated ID is used to store the output of this pane in a
	 * launch configuration
	 */
	public String configID = "";

	public String configVarID = "";

	/**
	 * The type of tool the parameter defined by this pane goes to
	 */
	public int paneType = -1;

	/**
	 * Control the pane normally but don't the user must set it to be displayed
	 * manually in a plugin
	 * 
	 * @since 4.0
	 */
	public boolean embedded = false;

	protected ToolPane(boolean virtual) {
		this.virtual = virtual;
		if (!virtual) {
			browseListener = new MakeBrowseListener();
			checkListener = new ToolPaneListener(this);
			optString = new StringBuffer();
			varMap = new LinkedHashMap<String, String>();
		}
	}

	@SuppressWarnings("unused")
	private ToolPane() {
		this.virtual = false;
	}

	public String getArgument(ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(configID, "");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getEnvVars(ILaunchConfiguration configuration) {
		Map<String, String> nullmap = null;
		try {
			return configuration.getAttribute(configVarID, nullmap);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return nullmap;
	}

	public ToolOption getOption(String optID) {
		for (int i = 0; i < options.length; i++) {
			if (options[i].getID().equals(optID)) {
				return options[i];
			}
		}
		return null;
	}

	/**
	 * Returns the complete argument output of this pane, or the empty string if
	 * no input has been specified
	 * 
	 * @return
	 */
	// TODO: Make the empty-string output optional
	public String getOptionString() {
		String out = optString.toString();

		if (out.equals(prependOpts + encloseOpts + encloseOpts)) {
			return "";
		}

		return optString.toString();
	}

	public Map<String, String> getVarMap() {
		return varMap;
	}

	/**
	 * Initializes all tool values and enabled/disabled states with the contents
	 * of configuration, or their defaults
	 * 
	 * @param configuration
	 *            The configuration from which the current tool values are to be
	 *            extracted
	 * @throws CoreException
	 */
	public void initializePane(ILaunchConfiguration configuration) throws CoreException {
		String arg = "";
		for (int i = 0; i < options.length; i++) {
			if (options[i].unitCheck != null) {
				options[i].unitCheck.setSelection(configuration.getAttribute(options[i].confDefString, options[i].defState));
			}

			if (options[i].usesTextBox()) {
				arg = configuration.getAttribute(options[i].confArgString, options[i].defText);
				if (arg != null) {
					options[i].argbox.setText(arg);
				}
			}

			if (options[i].numopt != null) {
				options[i].numopt.setSelection(configuration.getAttribute(options[i].confArgString, options[i].defNum));
			}

			if (options[i].combopt != null) {
				arg = configuration.getAttribute(options[i].confArgString, options[i].defText);
				if (arg != null) {
					int dex = options[i].combopt.indexOf(arg);
					if (dex > -1) {
						options[i].combopt.select(dex);
					}
				}
			}

		}
		updateOptDisplay();
	}

	/**
	 * Creates the widgets and initializes the values for this ToolPane
	 * 
	 * @param comp
	 *            The composite that will contain the elements of this tool pane
	 */
	public void makeToolPane(Composite comp) {
		ToolMaker.makeToolPane(comp, this, browseListener, checkListener);
	}

	/**
	 * Creates the widgets and initializes the values for this ToolPane
	 * 
	 * @param comp
	 *            The composite that will contain the elements of this tool pane
	 * @param paneListener
	 *            The listener class that defines behavior when check boxes and
	 *            value fields are manipulated
	 */
	public void makeToolPane(Composite comp, ToolPaneListener paneListener) {
		ToolMaker.makeToolPane(comp, this, browseListener, paneListener);
	}

	/**
	 * For every option in this pane, if it is active/selected add its
	 * name/value string to the collection of active values for the whole pane
	 * 
	 */
	public void OptUpdate() {

		optString = new StringBuffer(this.prependOpts).append(this.encloseOpts);
		varMap = new LinkedHashMap<String, String>();

		for (int i = 0; i < options.length; i++) {
			if (options[i].unitCheck == null || options[i].unitCheck.getSelection()) {

				String text = options[i].getArg();
				if (options[i].getArg() == null) {
					text = "";
				}

				boolean useField = !options[i].fieldrequired || text.trim().length() > 0;

				if (options[i].isArgument) {
					if (useField) {
						optString.append(options[i].optionLine).append(this.separateOpts);
					}
				} else {
					if (options[i].getArg() != null) {

						if (useField) {
							varMap.put(options[i].optName, text);
						}
					} else {
						if (options[i].type == ToolOption.TOGGLE) {
							if (options[i].setOn != null) {
								varMap.put(options[i].optName, options[i].setOn);
							}
						}
					}
				}
				options[i].setWidgetsEnabled(true);
			} else {
				options[i].setWidgetsEnabled(false);
				if (options[i].type == ToolOption.TOGGLE) {
					if (options[i].setOff != null) {
						varMap.put(options[i].optName, options[i].setOff);
					}
				}
			}
		}
		optString.append(this.encloseOpts);

		updateOptDisplay();
	}

	/**
	 * Saves the current pane-state in the supplied configuration
	 * 
	 * @param configuration
	 *            The configuration where the pane-state is to be saved
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		for (int i = 0; i < options.length; i++) {
			boolean set = true;
			if (options[i].unitCheck != null&&!options[i].unitCheck.isDisposed()) {
				set = options[i].unitCheck.getSelection();
				configuration.setAttribute(options[i].confDefString, set);
				updateOptField(options[i].unitCheck);
			}
			if (options[i].usesTextBox()&&options[i].argbox!=null) {
				configuration.setAttribute(options[i].confArgString, options[i].argbox.getText());
				updateOptField(options[i].argbox);
			}

			if (options[i].numopt != null) {
				configuration.setAttribute(options[i].confArgString, options[i].numopt.getSelection());
				updateOptField(options[i].numopt);
			}

			if (options[i].combopt != null && options[i].combopt.getSelectionIndex() > -1) {
				configuration.setAttribute(options[i].confArgString, options[i].items[options[i].combopt.getSelectionIndex()]);
				updateOptField(options[i].combopt);
			}
			// if(options[i].type==ToolOption.TOGGLE){
			// String argVal=null;
			// if(set){
			// argVal=options[i].setOn;
			// }else{
			// argVal=options[i].setOff;
			// }
			// configuration.setAttribute(options[i].confArgString,argVal);
			// }
		}
		
	}

	/**
	 * Sets the default values as suppled for the tools in this pane in the
	 * given configuration
	 * 
	 * @param configuration
	 *            The configuration where the default values are set
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		for (int i = 0; i < options.length; i++) {
			if (options[i].visible && !options[i].required) {
				configuration.setAttribute(options[i].confDefString, options[i].defState);
			}
			if (options[i].usesTextBox()) {
				configuration.setAttribute(options[i].confArgString, options[i].defText);
			}
			if (options[i].numopt != null) {
				configuration.setAttribute(options[i].confArgString, options[i].defNum);
			}
			if (options[i].combopt != null) {
				configuration.setAttribute(options[i].confArgString, options[i].items[options[i].defNum]);
			}
		}
	}

	/**
	 * If the object is a tool in this pane update the associated strings and
	 * displays for the object
	 * 
	 * @param source
	 *            The object being searched for and updated if found
	 * @return True if the object is found, otherwise false
	 */
	public boolean updateOptField(Object source) {
		for (int i = 0; i < options.length; i++) {
			if (source.equals(options[i].argbox) || source.equals(options[i].numopt) || source.equals(options[i].combopt)) {
				OptArgUpdate(options[i]);
				updateOptDisplay();
				return true;
			}
		}
		return false;
	}

	/**
	 * If the given ToolOption has an assoicated value field, the name/value
	 * string is updated accordingly.
	 * 
	 * @param opt
	 *            The ToolOption being updated
	 */
	protected void OptArgUpdate(ToolOption opt) {
		String val = opt.getArg();
		if (val == null) {
			val = "";
		}
		// if (opt.type ==1||opt.type ==2||opt.type ==3) {
		// val = opt.argbox.getText();
		// }else if (opt.type==4){
		// val=opt.numopt.getText();
		// }
		opt.optionLine = new StringBuffer(opt.optName).append(this.separateNameValue).append(this.encloseValues).append(val)
				.append(this.encloseValues);

		OptUpdate();

	}

	protected void setName(String name) {
		paneName = name;
		configID = name + ToolsOptionsConstants.TOOL_PANE_ID_SUFFIX;
		configVarID = name + ToolsOptionsConstants.TOOL_PANE_VAR_ID_SUFFIX;
	}

	// /**
	// * Creates a new tool pane with the given name and list of tool options
	// * @param name The name associated with the new pane
	// * @param toptions A list of ToolOptions to be contained and displayed by
	// this pane
	// */
	// protected ToolPane(String name, List toptions) {
	// toolName = name;
	// configID=name+".performance.options.configuration_id";
	// options = new ToolOption[toptions.size()];
	// toptions.toArray(options);
	// }

	protected void setOptions(List<ToolOption> toptions) {
		options = new ToolOption[toptions.size()];
		toptions.toArray(options);
	}

	/**
	 * Places the current string of name/value pairs in the option-display text
	 * box
	 * @since 4.1
	 * 
	 */
	public void updateOptDisplay() {
		if (showOpts != null && optString != null) {
			showOpts.setText(optString.toString());
		}
	}

	/**
	 * @since 4.1
	 */
	public String getName() {
		return paneName;
	}

	/**
	 * @since 4.1
	 */
	public String getToolName() {
		return toolName;
	}

	/**
	 * @since 4.1
	 */
	public String getConfigID() {
		return configID;
	}

	/**
	 * @since 4.1
	 */
	public String getConfigVarID() {
		return configVarID;
	}

	/**
	 * @since 4.1
	 */
	public boolean isVirtual() {
		return virtual;
	}

	/**
	 * @since 4.1
	 */
	public boolean isEmbedded() {
		return embedded;
	}

}
