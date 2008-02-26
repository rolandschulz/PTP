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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

/**
 * A factory-class for the generation and management of PerformanceTools and their subordinate elements
 * @author wspear
 *
 */
public class ToolMaker {
	
	/**
	 * Creates PerformanceTools
	 * @param tooldef The xml file containing the definition of one or more tool-panes
	 * @return The array of defined but uninitialized ToolPanes defined in the provided xml file
	 */
	public static PerformanceTool[] makeTools(File tooldef){
		SAXParserFactory factory = SAXParserFactory.newInstance();
		//factory.setValidating(false);
		ToolParser tparser = new ToolParser();
		try {
			factory.newSAXParser().parse(tooldef, tparser);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PerformanceTool[] tparr = new PerformanceTool[tparser.performanceTools.size()];
		tparser.performanceTools.toArray(tparr);
		return tparr;
	}
	
	/**
	 * Finishes the initialization of a ToolOption
	 * @param toolopt The ToolOption to be finished
	 * @return The finished ToolOption
	 */
	protected static ToolOption finishToolOption(ToolOption toolopt){
		
		if(toolopt.type>5)
			toolopt.type=0;
		String upname;
		if(toolopt.optName!=null)
		{
		
		}
		else if(toolopt.optLabel!=null)
		{
			toolopt.optName=toolopt.optLabel;
		}
		else
		{
			return null;
		}
		upname = toolopt.optName.toUpperCase();

		toolopt.confArgString=upname+"_ARGUMENT_SAVED";
		toolopt.confStateString=upname+"_BUTTON_STATE";
		toolopt.confDefString=upname+"_ARGUMENT_DEFAULT";
		
		toolopt.optionLine=new StringBuffer(toolopt.optName);
		if(toolopt.type>0)
			toolopt.optionLine.append("=\"\"");//  +="=\"\"";
		toolopt.optionLine.append(' ');//+=' ';
		
		return toolopt;
	}
	
	
	/**
	 * Initializes a tool pane within a composite provided by the user
	 * @param comp  The composite where the tool pane will be created
	 * @param pane  The pane containing the elements to be displayed
	 * @param browseListener  The listener for the browse buttons used in the pane
	 * @param checkListener  The listener for the check boxes and value fields used in the pane
	 */
	protected static void makeToolPane(Composite comp, ToolPane pane, SelectionListener browseListener, ToolPaneListener checkListener)
	{
		comp.setLayout(createGridLayout(3, false, 0, 0));
		comp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		createVerticalSpacer(comp, 3);
		
		if(pane.displayOptions)
		{
			pane.showOpts = new Text(comp,SWT.BORDER|SWT.WRAP|SWT.MULTI|SWT.V_SCROLL);
			GridData showOptGD = new GridData();
			showOptGD.horizontalAlignment=SWT.FILL;
			showOptGD.verticalAlignment=SWT.FILL;
			showOptGD.horizontalSpan=3;
			showOptGD.grabExcessHorizontalSpace=true;
			//showOptGD.grabExcessVerticalSpace=true;
			showOptGD.minimumHeight=pane.showOpts.getLineHeight()*3;
			showOptGD.heightHint=pane.showOpts.getLineHeight()*3;

			pane.showOpts.setEditable(false);
			pane.showOpts.setLayoutData(showOptGD);
		}
		
		Composite invis=new Composite(comp, SWT.NONE);
		invis.setVisible(false);
		
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
	    gridData.horizontalSpan = 3;
	    gridData.horizontalAlignment = GridData.FILL;
	    invis.setLayoutData(gridData);
		
		
		for(int i=0;i<pane.options.length;i++)
		{
			if(pane.options[i].visible)
				displayToolOption(comp,pane.options[i], pane.browseListener,checkListener);
			else
				displayToolOption(invis,pane.options[i], pane.browseListener,checkListener);
		}
		invis.setSize(0, 0);
		invis.moveBelow(null);
		//createVerticalSpacer(comp, 3);
	}
	
	private static void initializeCheckLabel(Composite comp, ToolOption toolOpt)
	{
		if(!toolOpt.required)
		{
			toolOpt.unitCheck = createCheckButton(comp, toolOpt.optLabel);
			toolOpt.unitCheck.setToolTipText(toolOpt.toolTip);
		}
		else
		{
			toolOpt.reqLabel=new Label(comp, SWT.NONE);
			toolOpt.reqLabel.setText(toolOpt.optLabel);
			toolOpt.reqLabel.setToolTipText(toolOpt.toolTip);
		}
	}
	
	/**
	 * Initializes a single tool option
	 * @param comp  The composite where the ToolOption is to be displayed
	 * @param toolOpt  The ToolOption to be displayed
	 * @param browseListener  The listener that defines behavior for this tool's browse buttons, if any
	 * @param checkListener  The listener that defines behavior for this tool's check boxe and value field, if any
	 */
	protected static void displayToolOption(Composite comp, ToolOption toolOpt, SelectionListener browseListener, ToolPaneListener checkListener){
		if (toolOpt.type == ToolOption.BOOL) {
			initializeCheckLabel(comp,toolOpt);
			new Label(comp, SWT.NULL);
			new Label(comp, SWT.NULL);
		} else if (toolOpt.type == ToolOption.TEXT) {
			initializeCheckLabel(comp,toolOpt);

			toolOpt.argbox = new Text(comp, SWT.BORDER | SWT.SINGLE);
			toolOpt.argbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			if(checkListener!=null)
				toolOpt.argbox.addModifyListener((ModifyListener)checkListener);

			new Label(comp, SWT.NULL);
		} else {
			initializeCheckLabel(comp,toolOpt);

			toolOpt.argbox = new Text(comp, SWT.BORDER | SWT.SINGLE);
			toolOpt.argbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			if(checkListener != null)
			toolOpt.argbox.addModifyListener((ModifyListener)checkListener);
			toolOpt.browser = createPushButton(comp, "Browse");
			if(browseListener!=null)
				toolOpt.browser.addSelectionListener(browseListener);
		}
		if(checkListener!=null&&toolOpt.unitCheck!=null)
			toolOpt.unitCheck.addSelectionListener(checkListener);
	}
	
	/**
	 * Creates a checkbox
	 * @param parent The composite where the checkbox is created
	 * @param label The label of the checkbox
	 * @return The created checkbox
	 */
	protected static Button createCheckButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		button.setFont(parent.getFont());
		//SWTUtil.setButtonDimensionHint(button);
		return button;
	}
	
	/**
	 * Creates a button
	 * @param parent  The composite where the button is created
	 * @param label  The label of the button
	 * @return  The created button
	 */
	protected static Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);	
		//SWTUtil.setButtonDimensionHint(button);
		return button;	
	}
	
	/**
	 * Creates either a file or directory broswer window, depending on the type of ToolOption, and puts the selected value in the option's value field
	 * @param opt The option whose value is being browsed for
	 */
	protected static void optBrowse(ToolOption opt){
		if(opt.type==ToolOption.DIR){
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setText(opt.toolTip);

			String correctPath = opt.argbox.getText();// getFieldContent(tauArch.getText());
			if (correctPath != null) {
				File path = new File(correctPath);
				if (path.exists()) {
					dialog.setFilterPath(path.isFile() ? correctPath : path
							.getParent());
				}
			}

			String selectedPath = dialog.open();
			if (selectedPath != null) {
				opt.argbox.setText(selectedPath);
			}
		}
		else
		if(opt.type==ToolOption.FILE)
		{
			FileDialog dialog = new FileDialog(getShell());
			dialog.setText(opt.toolTip);

			String correctPath = opt.argbox.getText();// getFieldContent(tauArch.getText());
			if (correctPath != null) {
				File path = new File(correctPath);
				if (path.exists()) {
					dialog.setFilterPath(path.isFile() ? correctPath : path
							.getParent());
				}
			}

			String selectedPath = dialog.open();
			if (selectedPath != null) {
				opt.argbox.setText(selectedPath);
			}
		}
		
	}
	
	/**
	 * Creatues a new shell from the workbench's current display
	 * @return A fresh shell
	 */
	protected static Shell getShell(){
		Display thisDisplay = PlatformUI.getWorkbench().getDisplay();//.getCurrent();//.getDefault();
		return new Shell(thisDisplay.getActiveShell());
	}
	
	/**
	 * Returns a new GridLayout
	 * @param columns Number of columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected static GridLayout createGridLayout(int columns, boolean isEqual, int mh,
			int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}
	
	protected static GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}
	
	protected static void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
		label.setFont(comp.getFont());
	}	
}
