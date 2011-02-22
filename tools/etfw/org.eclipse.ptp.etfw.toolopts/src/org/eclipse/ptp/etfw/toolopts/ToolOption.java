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
	public static final int TOGGLE = 7;
	
	/**
	 * List of items availabel in the combo widget
	 */
	protected String[] items;
	
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
	
	protected String optID;
	
	/**
	 * The default value for the argument, if any
	 */
	protected String defText;
	
	/**
	 * The default numerical value for numerical arguments, if any
	 */
	protected int defNum;
	
	/**
	 * The minimum numerical value for numerical arguments, if any
	 */
	protected int minNum;
	
	/**
	 * The maximum numerical value for numerical arguments, if any
	 */
	protected int maxNum;
	
	/**
	 * Text for the option button
	 */
	protected String optLabel;

	/**
	 * Context sensitive help info
	 */
	protected String toolTip;
	
	/**
	 * Context sensitive help info for entry widget
	 */
	protected String valueToolTip;
	
	/**
	 * Connect the option name and value with an equals if true
	 * otherwise use whitespace.
	 */
	//private boolean useEquals;
	

	/**
	 * If true this option is always used and has just a title rather than a checkbox
	 */
	public boolean required = false;
	
	/**
	 * If false this option can not be seen or manipulated in the UI.
	 */
	public boolean visible=true;
	
	protected Combo combopt;
	
	protected Spinner numopt;
	
	/**
	 * If true this option is for an argument, if false it is for an environment variable
	 */
	protected boolean isArgument=true;
	
	protected String setOn="";
	protected String setOff=null;
	protected boolean fieldrequired=false;
	
	/**
	 * Sets if this is an argument or an env variable.
	 * @param isarg
	 */
	public void setIsArgumetn(boolean isarg){
		isArgument=isarg;
	}
	
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
	public String fileLike=null;

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
		//useEquals=true;
	}
	
	public String getName(){
		return optName;
	}
	
	public String getID(){
		return optID;
	}
	
	public boolean getSelected(){
		if(unitCheck==null){
			return false;
		}
		return unitCheck.getSelection();
	}
	
	/**
	 * Sets this option's selected status.  Returns true upon success or false if there is no widget to set.
	 * @param set
	 * @return
	 */
	public boolean setSelected(boolean set){
		if(unitCheck==null){
			return false;
		}
		unitCheck.setSelection(set);
		return true;
	}
	public void setEnabled(boolean set){
		if(unitCheck!=null){
			unitCheck.setEnabled(set);
		}
		
		if(browser!=null){
			browser.setEnabled(set);
		}
		
		if(argbox!=null){
			argbox.setEnabled(set);
		}
		
		if(numopt!=null){
			numopt.setEnabled(set);
		}
		
		if(combopt!=null){
			combopt.setEnabled(set);
		}
		
	}
	public void setArg(String arg){
		if(argbox!=null){
			boolean isOn=argbox.getEnabled();
			argbox.setText(arg);
			argbox.setEnabled(isOn);
		}
		else if(numopt!=null){
			int argnum=Integer.parseInt(arg);
			setArg(argnum);
		}
		else if(combopt!=null){
			combopt.select(Integer.parseInt(arg));
		}
	}
	public void setArg(int arg){
		if(numopt!=null){
			boolean isOn=numopt.getEnabled();
			numopt.setSelection(arg);
			numopt.setEnabled(isOn);
		}
		else if(argbox!=null){
			setArg(""+arg);
		}
		else if(combopt!=null){
			combopt.select(arg);
		}
	}
	public String getArg(){
		if(argbox!=null){
			return argbox.getText();
		}
		else if (numopt!=null){
			return numopt.getText();
		}
		else if(combopt!=null){
			return combopt.getItem(combopt.getSelectionIndex());
		}
		return null;
	}

	/**
	 * Enable or disable (true or false) whichever widget this option uses to take its argument value
	 * @param bool
	 */
	public void setWidgetsEnabled(boolean bool){
		if(numopt!=null){
			numopt.setEnabled(bool);
		}
		if(combopt!=null){
			combopt.setEnabled(bool);
		}
		if(argbox!=null){
			argbox.setEnabled(bool);
		}
		if(browser!=null){
			browser.setEnabled(bool);
		}
	}
	
}
