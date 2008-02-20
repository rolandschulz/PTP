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
package org.eclipse.ptp.perf.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.internal.BuildLaunchUtils;
import org.eclipse.ptp.perf.internal.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.toolopts.PerformanceTool;
import org.eclipse.ptp.perf.toolopts.ToolPane;
import org.eclipse.ptp.perf.toolopts.ToolPaneListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Defines the tab of the performance-analysis launch configuration system where performance-analysis options are selected
 * @author wspear
 *
 */
public class PerformanceAnalysisTab extends AbstractLaunchConfigurationTab implements IPerformanceLaunchConfigurationConstants {
	//private static final IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
	/**
	 * Determines if the launch configuration associated with this tab has access to the PTP
	 */
	protected boolean noPTP=false;
	
	protected final PerformanceTool[] tools=Activator.getTools();//null;

	protected Combo toolTypes;

	protected Button buildonlyCheck;
	
	protected Button noParallelRun;

	//protected Button nocleanCheck;

	protected Button keepprofsCheck;
	
	protected final ToolPane[] panes = Activator.getToolPanes();
	//protected Button relocateTools;
	
	/**
	 * Listens for action in tool options panes
	 * @author wspear
	 *
	 */
	protected class OptionsPaneListener extends ToolPaneListener{
		OptionsPaneListener(ToolPane tool) {
			super(tool);
		}
		protected void localAction(){
			updateLaunchConfigurationDialog();
		}
	}
	
	/**
	 * Sets weather or not it is possible to initiate a parallel launch from this tab
	 * @param noPar Availability of the PTP to this tab's launch configuration delegate
	 */
	public PerformanceAnalysisTab(boolean noPar) {
		noPTP=noPar;
//		File tauToolXML=null;
//		URL testURL=Activator.getDefault().getBundle().getEntry("toolxml"+File.separator+"tau_tool.xml");
//		try {
//			tauToolXML = new File(new URI(FileLocator.toFileURL(testURL).toString().replaceAll(" ", "%20")));
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		PerformanceTool[] tauTool=null;
//		PerformanceTool[] otherTools=null;
//		if(tauToolXML!=null&&tauToolXML.canRead())
//		{
//			tauTool=ToolMaker.makeTools(tauToolXML);
//		}
//		
//		File toolxml= new File(pstore.getString(XMLLOCID));
//		if(!toolxml.canRead())
//		{
//			String epath=BuildLaunchUtils.checkToolEnvPath("eclipse");
//			if(epath!=null)
//			{
//				toolxml=new File(epath);
//				if(toolxml.canRead())
//				{
//					toolxml=new File(toolxml.getPath()+File.separator+"tool.xml");
//					if(toolxml.canRead())
//					{
//						//tools=ToolMaker.makeTools(toolxml);
//						pstore.setValue(XMLLOCID, toolxml.getPath());
//					}
//				}
//			}
//		}
//		
//		if(toolxml.canRead())
//			otherTools=ToolMaker.makeTools(toolxml); //PerformanceTool.getSample();//new PerformanceTool[1];;
//		tools=new PerformanceTool[1+otherTools.length];
//		tools[0]=tauTool[0];
//		for(int i=0;i<otherTools.length;i++)
//		{
//			tools[i+1]=otherTools[i];
//		}
	}

	/**
	 * Listen for activity in the performance tool combo-box, or other options
	 * @author wspear
	 *
	 */
	protected class WidgetListener extends SelectionAdapter implements
			ModifyListener, IPropertyChangeListener {
		public void widgetSelected(SelectionEvent e) {

//			Object source = e.getSource();

//			if(source.equals(relocateTools))
//			{
//				BuildLaunchUtils.getAllToolPaths(tools, true);
//			}
			
			updateLaunchConfigurationDialog();
		}

		public void propertyChange(PropertyChangeEvent event) {
			updateLaunchConfigurationDialog();
		}

		public void modifyText(ModifyEvent evt) {

			updateLaunchConfigurationDialog();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	
	/**
	 * Generates the UI for the analyis tab, consisting of sub-tabs which may be dynamically generated
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		FillLayout topLayout = new FillLayout();
		comp.setLayout(topLayout);
		TabFolder tabParent = new TabFolder(comp, SWT.BORDER);

		/*
		 * 
		 * Analysis Options:  TAU Makefile options and PAPI counter selection
		 * 
		 * */
		TabItem toolTab = new TabItem(tabParent, SWT.NULL);
		toolTab.setText("Tool Selection");

		ScrolledComposite scrollTool = new ScrolledComposite(tabParent,SWT.V_SCROLL);
		
		Composite toolComp = new Composite(scrollTool, SWT.NONE);
		toolTab.setControl(scrollTool);

		toolComp.setLayout(createGridLayout(1, false, 0, 0));
		toolComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		

		/*
		 * The actual controls of AnaComp
		 * */
		createVerticalSpacer(toolComp, 1);

		Composite toolComboComp = new Composite(toolComp, SWT.NONE);
		toolComboComp.setLayout(createGridLayout(2, false, 0, 0));
		toolComboComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label makeLab = new Label(toolComboComp, 0);
		makeLab.setText("Select Analysis Tool:");
		
		toolTypes=new Combo(toolComboComp, SWT.DROP_DOWN|SWT.READ_ONLY|SWT.BORDER);
		toolTypes.addSelectionListener(listener);
		
		createVerticalSpacer(toolComp, 1);

		buildonlyCheck = createCheckButton(toolComp,
				"Build the instrumented executable but do not launch it");
		buildonlyCheck.addSelectionListener(listener);
//		nocleanCheck = createCheckButton(toolComp,
//				"Keep instrumented executable");
//		nocleanCheck.addSelectionListener(listener);

		toolComp.pack();
		int toolCompHeight=toolComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		
		scrollTool.setContent(toolComp);
		scrollTool.setMinSize(400, toolCompHeight);
		scrollTool.setExpandHorizontal(true);
		scrollTool.setExpandVertical(true);

		/*
		 * Dynamic Panes
		 */
		
		TabItem optionTab=null;
		ScrolledComposite scrollOption=null;
		Composite optionComp=null;
		int optionCompHeight=400;
		
		if(panes!=null)
		for(int i=0;i<panes.length;i++)
		{
			if(panes[i].virtual)
				continue;
			optionTab = new TabItem(tabParent, SWT.NULL);
			optionTab.setText(panes[i].toolName);

			scrollOption = new ScrolledComposite(tabParent,SWT.V_SCROLL);

			optionComp = new Composite(scrollOption, SWT.NONE);
			optionTab.setControl(scrollOption);

			optionComp.setLayout(createGridLayout(1, false, 0, 0));
			optionComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

			panes[i].makeToolPane(optionComp, new OptionsPaneListener(panes[i]));

			optionComp.pack();
			optionCompHeight=optionComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

			scrollOption.setContent(optionComp);
			scrollOption.setMinSize(400, optionCompHeight);
			scrollOption.setExpandHorizontal(true);
			scrollOption.setExpandVertical(true);
		}
		
		
//		for(int i=0;i<tools.length;i++)
//		{
//			if(tools[i].execUtils!=null&&tools[i].execUtils.length>0)  
//			{
//				for(int j=0;j<tools[i].execUtils.length;j++)
//				{
//					if(tools[i].execUtils[j].toolPanes!=null&&tools[i].execUtils[j].toolPanes.length>0)
//					{
//						for(int k=0;k<tools[i].execUtils[j].toolPanes.length;k++)
//						{
//							optionTab = new TabItem(tabParent, SWT.NULL);
//							optionTab.setText(tools[i].execUtils[j].toolPanes[k].toolName);
//
//							scrollOption = new ScrolledComposite(tabParent,SWT.V_SCROLL);
//
//							optionComp = new Composite(scrollOption, SWT.NONE);
//							optionTab.setControl(scrollOption);
//
//							optionComp.setLayout(createGridLayout(1, false, 0, 0));
//							optionComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
//
//
//							tools[i].execUtils[j].toolPanes[k].makeToolPane(optionComp, new OptionsPaneListener(tools[i].execUtils[j].toolPanes[k]));
//
//							optionComp.pack();
//							optionCompHeight=optionComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
//
//							scrollOption.setContent(optionComp);
//							scrollOption.setMinSize(400, optionCompHeight);
//							scrollOption.setExpandHorizontal(true);
//							scrollOption.setExpandVertical(true);
//						}
//					}
//				}
//			}
//		}

	}

	public void updateComboFromSelection() {
		System.out.println("change startup");
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if(panes!=null)
		for(int i=0;i<panes.length;i++)
		{
			panes[i].setDefaults(configuration);
		}
	}
	

	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		toolTypes.removeAll();
		//toolTypes.add("TAU");
		
		if(tools==null||tools.length==0)
		{
			toolTypes.add("Specify a valid tool configuration file in Performance Tool preferences");
			if(tools==null)
			{
				toolTypes.select(0);
				return;
			}
		}
		
		BuildLaunchUtils.getAllToolPaths(tools, false);
		
//		Shell ourshell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
//		Iterator eIt=null;
//		Map.Entry me = null;
		for(int i=0;i<tools.length;i++)
		{
//			eIt=tools[i].groupApp.entrySet().iterator();
//			while(eIt.hasNext())
//			{
//				me=(Map.Entry)eIt.next();
//				if(pstore.getString(TOOL_BIN_ID+"."+(String)me.getKey()).equals(""))
//				{
//					pstore.setValue(TOOL_BIN_ID+"."+(String)me.getKey(), BuildLaunchUtils.findToolBinPath((String)me.getValue(),null,tools[i].toolName,ourshell));//findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
//				}
//			}
//			
//			for(int j=0;j<tools[i].groupApp.size();j++)
//			{
//				
//				if(tools[i].groupApp.)
//				{
//					
//				}
//			}
//				
//			if(pstore.getString(TOOL_BIN_ID+"."+tools[i].toolID).equals(""))
//			{
//				pstore.setValue(TOOL_BIN_ID+"."+tools[i].toolID, BuildLaunchUtils.findToolBinPath(tools[i].compilerPathFinder,null,tools[i].toolName,ourshell));//findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
//			}
			
			toolTypes.add(tools[i].toolName);
		}
		toolTypes.select(0);
		try {
			int toolDex;
			toolDex = toolTypes.indexOf(configuration.getAttribute(SELECTED_TOOL, ""));
			if(toolDex>=0)
			{
				toolTypes.select(toolDex);
			}
			else
			{
				toolTypes.select(0);
				//This means the available tools have changed!
				if(configuration.getAttribute(SELECTED_TOOL, "").equals(""))
					updateLaunchConfigurationDialog();
			}
			
			buildonlyCheck.setSelection(configuration.getAttribute(BUILDONLY, false));
			//nocleanCheck.setSelection(configuration.getAttribute(NOCLEAN, false));
			if(panes!=null)
			for(int i=0;i<panes.length;i++)
			{
				panes[i].OptUpdate();
				panes[i].initializePane(configuration);
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	/**
//	 * Takes the set of build/launch parameters for the selected performance tool and
//	 * applies them to the current configuration
//	 * @param tool
//	 * @param configuration
//	 */
//	private void applyToolConfiguration(PerformanceTool tool, ILaunchConfigurationWorkingCopy configuration)
//	{
//		String binpath=pstore.getString(TOOL_BIN_ID+"."+tool.toolID)+File.separator;
//		
//		//Compilation phase attributes
//		configuration.setAttribute(PERF_RECOMPILE, tool.recompile);//TODO If this is false the rest of this section is moot
//		configuration.setAttribute(CC_COMPILER, binpath+tool.ccCompiler);
//		configuration.setAttribute(CXX_COMPILER, binpath+tool.cxxCompiler);
//		configuration.setAttribute(F90_COMPILER, binpath+tool.f90Compiler);
//		configuration.setAttribute(TOOLCONFNAME, tool.toolName);//TODO: We may want a more detailed build configuration tag
//		configuration.setAttribute(COMPILER_REPLACE, tool.replaceCompiler);
//		
//		//Execution phase attributes
//		configuration.setAttribute(USE_EXEC_UTIL, tool.prependExecution);
//		configuration.setAttribute(EXEC_UTIL_LIST, tool.execUtils);//binpath+tool.prependWith);
//		configuration.setAttribute(EXEC_UTIL_ARGS, tool.execUtilArgs);
//		
//		//Analysis phase attributes
//		configuration.setAttribute(TOOL_LIST, tool.analysisCommands);//TODO: May need multiple binpath entries for multiple tools
//		configuration.setAttribute(TOOL_ARGS, tool.analysisArgs);
//	}
	
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		String selectedtool=toolTypes.getItem(toolTypes.getSelectionIndex());

		configuration.setAttribute(SELECTED_TOOL, selectedtool);
		
//		if(toolTypes.getSelectionIndex()==0||tools==null||tools.length<1)
//		{
//			configuration.setAttribute(TAULAUNCH, true);
//			//configuration.setAttribute(PERF_RECOMPILE, true);
//			//configuration.setAttribute(USE_EXEC_UTIL, false);
//		}
//		else
		if(tools!=null&&tools.length>=1)
		{
			if(toolTypes.getSelectionIndex()==0)
				configuration.setAttribute(TAULAUNCH, true);
			else
				configuration.setAttribute(TAULAUNCH, false);

			configuration.setAttribute(USE_EXEC_UTIL, tools[toolTypes.getSelectionIndex()].prependExecution);
			configuration.setAttribute(PERF_RECOMPILE, tools[toolTypes.getSelectionIndex()].recompile);
//			for(int i=0;i<tools.length;i++)
//			{
//				if(tools[i].toolName.equals(selectedtool))
//				{
//					applyToolConfiguration(tools[i],configuration);
//					break;
//				}
//			}
		}

		configuration.setAttribute(BUILDONLY,
				buildonlyCheck.getSelection());
		//configuration.setAttribute(NOCLEAN,nocleanCheck.getSelection());
		if(panes!=null)
		for(int i=0;i<panes.length;i++)
		{
			panes[i].performApply(configuration);
			
			configuration.setAttribute(panes[i].configID, panes[i].getOptionString());
		}
	}

	protected String getFieldContent(IntegerFieldEditor editorField) {
		return getFieldContent(editorField.getStringValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid
	 * (org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);

		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Performance Analysis";
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog
	 * (ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

//	/**
//	 * @see ILaunchConfigurationTab#getImage()
//	 */
//	public Image getImage() {
//		return LaunchImages.getImage("org.eclipse.ptp.tau.core.tauLogo.gif");
//	}

	/**
	 * Produces a new GridLayout based on provided arguments
	 * @param columns
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

	/**
	 * Creates a new GridData based on provided style and space arguments
	 * @param style
	 * @param space
	 * @return
	 */
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

	/**
	 * Treats empty strings as null
	 * @param text
	 * @return Contents of text, or null if text is the empty string
	 */
	protected String getFieldContent(String text) {
		if ((text.trim().length() == 0) || text.equals("")) {
			return null;
		}

		return text;
	}
}