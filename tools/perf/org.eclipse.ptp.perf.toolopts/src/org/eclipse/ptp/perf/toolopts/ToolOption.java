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
package org.eclipse.ptp.perf.toolopts;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * An individual element encapsulating a single option for display and user-manipulation/selection in an option pane
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
	
	/**
	 * The check button to activate/deactivate this option if it is optional
	 */
	protected Button unitCheck;
	
	/**
	 * The label used if this option is required
	 */
	protected Label reqLabel;
	
	/**
	 * Holds/displays text arguments if any
	 */
	protected Text argbox;
	
	/**
	 * Launches file/directory browser for argbox
	 */
	protected Button browser;

	/*
	 * The following should be defined in the factory
	 */
	
	/**
	 * Sets the default state of the check button
	 */
	protected boolean defState;
	
	/**
	 * Specifies the type of option(bool,text,dir,file or combo)
	 */
	protected int type;
	
	/**
	 * The name, or prefix of the option
	 */
	protected String optName;
	
	/**
	 * The default value for the argument, if any
	 */
	protected String defText;
	
	/**
	 * Text for the option button
	 */
	protected String optLabel;

	/**
	 * Context sensitive help info
	 */
	protected String toolTip;
	
	/**
	 * Connect the option name and value with an equals if true
	 * otherwise use whitespace.
	 */
	protected boolean useEquals;
	

	/**
	 * If true this option is always used and has just a title rather than a checkbox
	 */
	public boolean required = false;
	
	/**
	 * If false this option can not be seen or manipulated in the UI.
	 */
	public boolean visible=true;
	
	protected Combo combopt;
	
	//This will eventually be used to hold subordinate options.
//	protected ArrayList subopts;

	
	/*
	 * These values should be defined internally
	 */
	/**
	 * The whole option as sent to the system, up to the argument component
	 */
	protected StringBuffer optionLine;
	
	/**
	 * Name of the configuration value associated with the 
	 * toggle button state for this option
	 */
	protected String confStateString;
	
	/**
	 * Name of the configuration value associated with the
	 * argument of this option, if any
	 */
	protected String confArgString;
	
	/**
	 * Name of the configuration value associated with the
	 * default argument of this option, if any
	 */
	protected String confDefString;

	/**
	 * Determines if this option includes a text box
	 * @return returns if this option includes a text field
	 */
	protected boolean usesTextBox()
	{
		return(type==ToolOption.DIR||type==ToolOption.TEXT||type==ToolOption.FILE);
	}
	
	/**
	 * Creates a new ToolOption
	 *
	 */
	protected ToolOption(){
		type=0;
		defState=false;
		defText="";
		useEquals=true;
	}
	

}
