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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * An individual element encapsulating a single option for display and
 * user-manipulation/selection in an option pane
 * 
 * @author wspear
 * 
 */
public class ToolOption {
	public static final int BOOL = 0;
	public static final int TEXT = 1;
	public static final int DIR = 2;
	public static final int FILE = 3;
	public static final int COMBO = 4;
	public static final int NUMBER = 5;
	public static final int SUBOPT = 6;
	public static final int TOGGLE = 7;

	/**
	 * List of items availabel in the combo widget
	 * 
	 * @since 5.0
	 */
	public String[] items;

	/**
	 * The check button to activate/deactivate this option if it is optional
	 * 
	 * @since 5.0
	 */
	public Button unitCheck;

	/**
	 * The label used if this option is required
	 * 
	 * @since 5.0
	 */
	public Label reqLabel;

	/**
	 * Holds/displays text arguments if any
	 * 
	 * @since 5.0
	 */
	public Text argbox;

	/**
	 * Launches file/directory browser for argbox
	 * 
	 * @since 5.0
	 */
	public Button browser;

	/*
	 * The following should be defined in the factory
	 */

	/**
	 * Sets the default state of the check button
	 * 
	 * @since 5.0
	 */
	public boolean defState;

	/**
	 * Specifies the type of option(bool,text,dir,file or combo)
	 * 
	 * @since 5.0
	 */
	public int type;

	/**
	 * The name, or prefix of the option
	 * 
	 * @since 5.0
	 */
	public String optName;

	/**
	 * @since 5.0
	 */
	public String optID;

	/**
	 * The default value for the argument, if any
	 * 
	 * @since 5.0
	 */
	public String defText;

	/**
	 * The default numerical value for numerical arguments, if any
	 * 
	 * @since 5.0
	 */
	public int defNum;

	/**
	 * The minimum numerical value for numerical arguments, if any
	 * 
	 * @since 5.0
	 */
	public int minNum;

	/**
	 * The maximum numerical value for numerical arguments, if any
	 * 
	 * @since 5.0
	 */
	public int maxNum;

	/**
	 * Text for the option button
	 * 
	 * @since 5.0
	 */
	public String optLabel;

	/**
	 * Context sensitive help info
	 * 
	 * @since 5.0
	 */
	public String toolTip;

	/**
	 * Context sensitive help info for entry widget
	 * 
	 * @since 5.0
	 */
	public String valueToolTip;

	/**
	 * Connect the option name and value with an equals if true otherwise use
	 * whitespace.
	 */
	// private boolean useEquals;

	/**
	 * If true this option is always used and has just a title rather than a
	 * checkbox
	 */
	public boolean required = false;

	/**
	 * If false this option can not be seen or manipulated in the UI.
	 */
	public boolean visible = true;

	/**
	 * @since 5.0
	 */
	public Combo combopt;

	/**
	 * @since 5.0
	 */
	public Spinner numopt;

	/**
	 * If true this option is for an argument, if false it is for an environment
	 * variable
	 * 
	 * @since 5.0
	 */
	public boolean isArgument = true;

	/**
	 * @since 5.0
	 */
	public String setOn = ""; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public String setOff = null;
	/**
	 * @since 5.0
	 */
	public boolean fieldrequired = false;

	/*
	 * These values should be defined internally
	 */
	/**
	 * The whole option as sent to the system, up to the argument component
	 * 
	 * @since 5.0
	 */
	public StringBuffer optionLine;

	// This will eventually be used to hold subordinate options.
	// protected ArrayList subopts;

	/**
	 * Name of the configuration value associated with the toggle button state
	 * for this option
	 * 
	 * @since 5.0
	 */
	public String confStateString;

	/**
	 * Name of the configuration value associated with the argument of this
	 * option, if any
	 * 
	 * @since 5.0
	 */
	public String confArgString;

	/**
	 * Name of the configuration value associated with the default argument of
	 * this option, if any
	 * 
	 * @since 5.0
	 */
	public String confDefString;

	public String fileLike = null;

	/**
	 * Creates a new ToolOption
	 * 
	 * @since 5.0
	 * 
	 */
	public ToolOption() {
		type = 0;
		defState = false;
		defText = ""; //$NON-NLS-1$
		// useEquals=true;
	}

	/**
	 * @since 4.0
	 */
	public String getArg() {
		if (argbox != null) {
			return argbox.getText();
		} else if (numopt != null) {
			return numopt.getText();
		} else if (combopt != null && combopt.getSelectionIndex() != -1) {
			return combopt.getItem(combopt.getSelectionIndex());
		}
		return null;
	}

	public String getID() {
		return optID;
	}

	public String getName() {
		return optName;
	}

	public boolean getSelected() {
		if (unitCheck == null) {
			return false;
		}
		return unitCheck.getSelection();
	}

	/**
	 * @since 4.0
	 */
	public void setArg(int arg) {
		if (numopt != null) {
			boolean isOn = numopt.getEnabled();
			numopt.setSelection(arg);
			numopt.setEnabled(isOn);
		} else if (argbox != null) {
			setArg("" + arg); //$NON-NLS-1$
		} else if (combopt != null) {
			combopt.select(arg);
		}
	}

	public void setArg(String arg) {
		if (argbox != null) {
			boolean isOn = argbox.getEnabled();
			argbox.setText(arg);
			argbox.setEnabled(isOn);
		} else if (numopt != null) {
			int argnum = Integer.parseInt(arg);
			setArg(argnum);
		} else if (combopt != null) {
			combopt.select(Integer.parseInt(arg));
		}
	}

	public void setEnabled(boolean set) {
		if (unitCheck != null) {
			unitCheck.setEnabled(set);
		}

		if (browser != null) {
			browser.setEnabled(set);
		}

		if (argbox != null) {
			argbox.setEnabled(set);
		}

		if (numopt != null) {
			numopt.setEnabled(set);
		}

		if (combopt != null) {
			combopt.setEnabled(set);
		}

	}

	/**
	 * Sets if this is an argument or an env variable.
	 * 
	 * @param isarg
	 * @since 5.0
	 */
	public void setIsArgument(boolean isarg) {
		isArgument = isarg;
	}

	/**
	 * Sets this option's selected status. Returns true upon success or false if
	 * there is no widget to set.
	 * 
	 * @param set
	 * @return
	 */
	public boolean setSelected(boolean set) {
		if (unitCheck == null) {
			return false;
		}
		unitCheck.setSelection(set);
		return true;
	}

	/**
	 * Enable or disable (true or false) whichever widget this option uses to
	 * take its argument value
	 * 
	 * @param bool
	 * @since 4.0
	 */
	public void setWidgetsEnabled(boolean bool) {
		if (numopt != null) {
			numopt.setEnabled(bool);
		}
		if (combopt != null) {
			combopt.setEnabled(bool);
		}
		if (argbox != null) {
			argbox.setEnabled(bool);
		}
		if (browser != null) {
			browser.setEnabled(bool);
		}
	}

	/**
	 * Determines if this option includes a text box
	 * 
	 * @return returns if this option includes a text field
	 */
	protected boolean usesTextBox() {
		return (type == ToolOption.DIR || type == ToolOption.TEXT || type == ToolOption.FILE);
	}

}
