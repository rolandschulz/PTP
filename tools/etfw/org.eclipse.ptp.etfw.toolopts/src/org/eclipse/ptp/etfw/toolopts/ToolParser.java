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

import java.util.ArrayList;
import java.util.Stack;

import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess.Parametric;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A sax parser handler implementation that generates a tool-pane from a supplied XML file
 * @author wspear
 *
 */
public class ToolParser extends DefaultHandler{
	
	private static final String TOOLSET="toolset";
	boolean oldParser=false;
	
	/**
	 * Top level element of a single analysis tool
	 */
	private static final String TOOL = "tool";
	
	/**
	 * Top level element of the compilation phase of analysis
	 */
	private static final String COMPILE = "compile";
	
	private static final String CC = "cc";
	private static final String CXX = "cxx";
	private static final String F90 = "f90";
	private static final String UPC = "upc";
	private static final String ALLCOMP = "allcompilers";
	
	/**
	 * Top level element of the execution phase of analysis
	 */
	private static final String EXECUTE="execute";
	private static final String UTILITY="utility";
	
	/**
	 * Indicates the use of a top level 'false' tool, to hold environment variables for the entire workflow step
	 */
	private static final String GLOBAL="global";
	
	
	/**
	 * Top level element of the analysis phase of analysis
	 */
	private static final String ANALYZE = "analyze";
	
	//private static final String PROCESS = "process";
	//private static final String VIEW = "view";
	
	/**
	 * Top level element of options pane definition structure
	 */
	private static final String OPTIONPANE = "optionpane";
	private static final String TOGOPT = "togoption";
	private static final String LABEL = "label";
	private static final String ID = "optid";
	private static final String TIP = "tooltip";
	private static final String NAME = "optname";
	private static final String VALUE = "optvalue";
	private static final String DEFAULT = "default";
	private static final String DEFAULTNUM = "defaultnum";
	private static final String MINIMUM = "minimum";
	private static final String MAXIMUM = "maximum";
	private static final String DEFSTATE = "defstate";
	
	private static final String SETON="seton";
	private static final String SETOFF="setoff";
	
	/**
	 * If this is true for a ui option it will only be used if its text field is not empty
	 */
	private static final String FIELDREQUIRED="fieldrequired";
	
	/**
	 * Use of this attribute in a tool option indicates it is always activated
	 * Default is false.
	 */
	private static final String REQUIRED="required";
	
	/**
	 * Use of this attribute in a tool option indicates that it is not visible
	 * and always used.
	 */
	private static final String VISIBLE="visible";
	
	/**
	 * Use of this attribute in an options pane indicates the options-tally display will be provided
	 * Defaults to true.
	 */
	private static final String DISPLAYOPTIONS="displayoptions";
	

	private static final String ENCLOSEVALS="enclosevalues";
	/**
	 * The entire string of generated values will be enclosed in this string)
	 * tag: enclosevalues
	 * TODO: create enclose start and enclose end values? (e.g for parentheses)
	 */
	private static final String ENCLOSEWITH="enclosewith";
	
	/**
	 * A string put between all generated options
	 */
	private static final String SEPARATEWITH="separatewith";
	
	/**
	 * A string prepended to the string of generated options
	 */
	private static final String PREPENDWITH="prependwith";
	/**
	 * The string between a flag and an associated option
	 */
	private static final String SEPARATEVAL="separatevalues";
	
	/**
	 * Element to specify an argument to a compiler, run utility or analysis tool
	 */
	private static final String ARGUMENT="argument";
	private static final String ENVVAR="envvar";
	
	private static final String PARAMETRIC="parametric";
	private static final String WEAKSCALING="weakscaling";
	private static final String NUMPROCS="numprocs";
	private static final String COMPILEROPT="compileropt";
	private boolean inParametric=false;
	

	private boolean inTool=false;
	/**
	 * If true we are within the compilation definition section
	 */
	private boolean inCompilation=false;

	/**
	 * If true we are within the execution definition section
	 */
	private boolean inExecution=false;

	/**
	 * If true we are within the analysis definition section
	 */
	private boolean inAnalysis=false;
	
	private Stack<String> tagStack = new Stack<String>();
	protected ArrayList<ExternalToolProcess> externalToolList= new ArrayList<ExternalToolProcess>();
	private ExternalToolProcess currentTool;
	private BuildTool buildTool;
	private ExecTool execTool;
	private PostProcTool ppTool;
	/**
	 * Contains the list of tool panes for the current tool app
	 */
	private ArrayList<ToolPane> toolPanes;// = new ArrayList();
	private ToolPane currentPane;
	
	/**
	 * Contains the list of tool apps (compilers, exec utils or analysis tools) for the current subheading
	 */
	private ArrayList<ToolApp> toolApps;
	/**
	 * Contains the list of argument strings for the current tool.
	 */
	//private ArrayList<ToolArgument> currentArgs;
	
	/**
	 * Contains the list of environment variable strings for the current tool.
	 */
	//private ArrayList<ToolArgument> currentVars;
	
	/**
	 * The tool app currently being worked on
	 */
	private ToolApp currentApp;
	
	
	private ArrayList<IAppInput> appInput;
	//private ArrayList<IAppInput> appVars;
	
	private ArrayList<ToolOption> toolOptions;
	private ToolOption actOpt;
	private Stack<StringBuffer> content = new Stack<StringBuffer>();
	
	public void characters(char[] chars, int start, int len)
	{
		((StringBuffer)content.peek()).append(chars, start, len);
	}
	
	private static String getAttribute(String name, Attributes atts)
	{
		int repdex = atts.getIndex(name);
		if(repdex>=0)
		{
			return atts.getValue(repdex);
		}
		else
			return null;
	}
	
	private static int getIntAttribute(String name, int defValue, Attributes atts){
		String str = getAttribute(name,atts);
		if(str == null)
			return defValue;
		int ret = Integer.parseInt(str);
		return ret;
		
	}
	
	private static boolean getBooleanAttribute(String name, boolean defValue, Attributes atts)
	{
		String boolAtt=getAttribute(name,atts);
		if(boolAtt==null)
			return defValue;
		if(boolAtt.toLowerCase().equals("true"))
			return true;
		else if(boolAtt.toLowerCase().equals("false"))
			return false;
		return defValue;
	}
	
	public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		name=name.toLowerCase();
		if(name.equals(TOOLSET))
		{
			oldParser=true;
			System.out.println("XML Format Not Supported");
			//parseOldStart(uri, localName, name,atts);
			return;
		}
		if(name.equals(TOOL))
		{
			inTool=true;
			currentTool=new ExternalToolProcess();
			currentTool.toolName=getAttribute("name",atts);
			currentTool.toolID= getAttribute("id",atts);
			currentTool.explicitExecution=getBooleanAttribute("explicitexecution",false,atts);
			if(currentTool.toolID==null)
			{
				currentTool.toolID=currentTool.toolName;
			}
			
		}
		else if(name.equals(COMPILE))
		{
			if(inTool&&currentTool!=null)
			{
				inCompilation=true;
				currentTool.recompile=true;
				buildTool=new BuildTool();
				buildTool.toolID=currentTool.toolID;
				buildTool.replaceCompiler=getBooleanAttribute("replace",false,atts);
				buildTool.requireTrue=getAttribute("if",atts);
			}
		}
		else if(name.equals(EXECUTE)&&!inExecution)
		{
			inExecution=true;
			execTool=new ExecTool();
			toolApps=new ArrayList<ToolApp>();
			execTool.replaceExecution=getBooleanAttribute("replace_execution",false,atts);
			execTool.requireTrue=getAttribute("if",atts);
		}
		else if(name.equals(ANALYZE)&&!inAnalysis)
		{
			inAnalysis=true;
			ppTool=new PostProcTool();
			toolApps=new ArrayList<ToolApp>();
			ppTool.requireTrue=getAttribute("if",atts);
			ppTool.useDefaultLocation=getBooleanAttribute("defaultloc",false,atts);
			ppTool.forAllLike=getAttribute("foralllike",atts);
			ppTool.useLatestFileOnly=getBooleanAttribute("uselatestonly",true,atts);
			String depthS=getAttribute("depth",atts);
			if(depthS!=null){
				int depth=Integer.parseInt(depthS);
				ppTool.depth=depth;
			}
			
		}
		else if(name.equals(PARAMETRIC)&&inTool&&!inParametric&&currentTool!=null){
			inParametric=true;
			currentTool.para=new Parametric();
			currentTool.para.runParametric=true;
			currentTool.para.weakScaling=getBooleanAttribute(WEAKSCALING,false,atts);
			currentTool.para.mpiProcs=getAttribute(NUMPROCS,atts);
			currentTool.para.compileropt=getAttribute(COMPILEROPT,atts);
		}
		else if(name.equals(CC)||name.equals(CXX)||name.equals(F90)||name.equals(UPC)||name.equals(ALLCOMP)||name.equals(UTILITY))
		{
			if(inTool&&currentTool!=null)
			{
				currentApp=new ToolApp();
				currentApp.toolCommand=getAttribute("command",atts);
				currentApp.toolID=getAttribute("id",atts);
				currentApp.toolGroup=getAttribute("group",atts);
				currentApp.outToFile=getAttribute("outtofile",atts);
				if(currentApp.toolGroup!=null&&currentApp.toolCommand!=null)
					currentTool.groupApp.put(currentApp.toolGroup, currentApp.toolCommand);
				if(inExecution)
					currentTool.prependExecution=true;
				
				currentApp.isVisualizer=getBooleanAttribute("visualizer",false,atts);
			}
		}
		else if(name.equals(GLOBAL)){
			if(inTool&&currentTool!=null)
			{
				currentApp=new ToolApp();
				//currentApp.toolCommand="GLOBAL";
				currentApp.toolID=getAttribute("command",atts);
			}
		}
		else if(name.equals(ENVVAR)){
			if(inParametric)
			{
				currentTool.para.varNames.add(getAttribute("name",atts));
				currentTool.para.varValues.add(getAttribute("values",atts));
				
				String weak ="0";
				if(getBooleanAttribute(WEAKSCALING,false,atts)){
					weak="1";
				}
				currentTool.para.varWeakBools.add(weak);
			}
			else{
//				if(currentVars==null){
//					currentVars=new ArrayList<ToolArgument>();
//				}
//				if(appVars==null){
//					appVars=new ArrayList<IAppInput>();
//				}
				if(appInput==null)
					appInput=new ArrayList<IAppInput>();
				
				boolean local=getBooleanAttribute("localdir",false,atts);
				
				String flag=getAttribute("flag",atts);
				String val=getAttribute("value",atts);
				String sep=getAttribute("separator",atts);
				
				String cval=getAttribute("confvalue",atts);
				
				ToolArgument tArg=new ToolArgument(flag,val,sep,local);
				if(cval!=null)
				{
					tArg.setUseConfValue(true);
					tArg.setConfValue(cval);
				}
				
				//currentVars.add(tArg);
				tArg.setType(ToolArgument.VAR);
				appInput.add(tArg);
				
			}
		}
		else if(name.equals(ARGUMENT))
		{
			if(inParametric)
			{
				currentTool.para.argNames.add(getAttribute("name",atts));
				currentTool.para.argValues.add(getAttribute("values",atts));
				
				String weak ="0";
				if(getBooleanAttribute(WEAKSCALING,false,atts)){
					weak="1";
				}
				currentTool.para.argWeakBools.add(weak);
			}
			else{
//			if(currentArgs==null)
//				currentArgs=new ArrayList<ToolArgument>();
			if(appInput==null)
				appInput=new ArrayList<IAppInput>();
			
			boolean local=getBooleanAttribute("localdir",false,atts);
			
			String flag=getAttribute("flag",atts);
			String val=getAttribute("value",atts);
			String sep=getAttribute("separator",atts);
			
			String cval=getAttribute("confvalue",atts);
			
			boolean needsVal=getBooleanAttribute(FIELDREQUIRED,false,atts);
			
//			if(cval!=null)
//				val=cval;
			
			ToolArgument tArg=new ToolArgument(flag,val,sep,local);
			if(cval!=null)
			{
				tArg.setUseConfValue(true);
				tArg.setConfValue(cval);
			}
			
			tArg.setRequireValue(needsVal);
			
//			if(local){
//				arg=ToolsOptionsConstants.PROJECT_LOCATION+File.separator+arg;//Must be the same as IToolLaunchConfigurationConstants.PROJECT_LOCATION
//			}
			//currentArgs.add(tArg);
			appInput.add(tArg);
			}
		}
		else if(name.equals(OPTIONPANE))
		{
			boolean virtual=getBooleanAttribute("virtual",false,atts);
			boolean embedded = getBooleanAttribute("embedded",false,atts);
			//TODO: Make -absolutely- certain that nothing ever tries to create a UI instance of a virtual pane!
			
			toolOptions=new ArrayList<ToolOption>();
			currentPane=new ToolPane(virtual);
			currentPane.setName(getAttribute("title",atts));
			currentPane.embedded=embedded;
			int optdex = atts.getIndex(PREPENDWITH);
			if(optdex>=0)
			{
				currentPane.prependOpts=atts.getValue(optdex);
			}
			optdex = atts.getIndex(ENCLOSEWITH);
			if(optdex>=0)
			{
				currentPane.encloseOpts=atts.getValue(optdex);
			}
			optdex = atts.getIndex(SEPARATEWITH);
			if(optdex>=0)
			{
				currentPane.separateOpts=atts.getValue(optdex);
			}
			optdex = atts.getIndex(ENCLOSEVALS);
			if(optdex>=0)
			{
				currentPane.encloseValues=atts.getValue(optdex);
			}
			optdex = atts.getIndex(SEPARATEVAL);
			if(optdex>=0)
			{
				currentPane.separateNameValue=atts.getValue(optdex);
			}
			optdex = atts.getIndex(DISPLAYOPTIONS);
			if(optdex>=0)
			{
				String abool=atts.getValue(optdex);
				abool=abool.toLowerCase();
				if(abool.equals("false"))
					currentPane.displayOptions=false;
			}
		}
		else if(name.equals(TOGOPT))
		{
			actOpt=new ToolOption();
			//actOpt.isArgument=getBooleanAttribute(ARGUMENT,true,atts);
			actOpt.isArgument=!getBooleanAttribute(ENVVAR,false,atts);
			
			actOpt.optLabel=getAttribute(LABEL, atts);
			actOpt.optName=getAttribute(NAME,atts);
			actOpt.optID=getAttribute(ID,atts);
			if(actOpt.optID==null){
				actOpt.optID=actOpt.optName;
			}
			actOpt.toolTip=getAttribute(TIP,atts);
			actOpt.defState=getBooleanAttribute(DEFSTATE,false,atts);
			actOpt.required=getBooleanAttribute(REQUIRED,false,atts);
			actOpt.visible=getBooleanAttribute(VISIBLE,true,atts);
		}
		else if(name.equals(VALUE))
		{
			if(actOpt!=null&&tagStack.peek().equals(TOGOPT))
			{
				//actOpt.useEquals=getBooleanAttribute("equals",true,atts);
				actOpt.defText=getAttribute(DEFAULT,atts);
				actOpt.defNum=getIntAttribute(DEFAULTNUM,0,atts);
				actOpt.minNum=getIntAttribute(MINIMUM,0,atts);
				actOpt.maxNum=getIntAttribute(MAXIMUM,Integer.MAX_VALUE,atts);
				String type=getAttribute("type",atts);
				actOpt.valueToolTip=getAttribute("TIP",atts);
				if(type!=null)
				{
					type = type.toLowerCase();
					if(type.equals("text"))
						actOpt.type=ToolOption.TEXT;
					else if(type.equals("dir"))
						actOpt.type=ToolOption.DIR;
					else if(type.equals("file"))
					{
						actOpt.type=ToolOption.FILE;
						actOpt.fileLike=getAttribute("filelike",atts);
					}
					else if(type.equals("number"))
						actOpt.type=ToolOption.NUMBER;
					else if(type.equals("combo"))
					{
						actOpt.type=ToolOption.COMBO;
						String items = getAttribute("items",atts);
						if(items!=null)
							actOpt.items=parseItems(items);
					}
					else if(type.equals("toggle")){
						actOpt.type=ToolOption.TOGGLE;
						String hold=getAttribute(SETON,atts);
						if(hold==null){
							hold="1";
						}
						actOpt.setOn=hold;
						actOpt.setOff=getAttribute(SETOFF,atts);
					}
					
					actOpt.fieldrequired=getBooleanAttribute(FIELDREQUIRED,false,atts);
				}
			}
		}
		tagStack.push(name.toLowerCase());
		content.push(new StringBuffer());
	}
	
	private static String[] parseItems(String in){
		return in.split(":::");
	}
	
	private ToolApp finishApp()
	{
		if(appInput!=null&&appInput.size()>0){
			currentApp.allInput=new IAppInput[appInput.size()];
			appInput.toArray(currentApp.allInput);
		}
//		if(appVars!=null&&appVars.size()>0){
//			currentApp.allVars=new IAppInput[appVars.size()];
//			appVars.toArray(currentApp.allVars);
//		}
//		if(currentArgs!=null&&currentArgs.size()>0)
//		{
//			currentApp.arguments=new ToolArgument[currentArgs.size()];
//			currentArgs.toArray(currentApp.arguments);
//		}
//		if(currentVars!=null&&currentVars.size()>0)
//		{
//			currentApp.envVars=new ToolArgument[currentVars.size()];
//			currentVars.toArray(currentApp.envVars);
//		}
		if(toolPanes!=null&&toolPanes.size()>0)
		{
			currentApp.toolPanes=new ToolPane[toolPanes.size()];
			toolPanes.toArray(currentApp.toolPanes);
		}
		if(currentApp.toolID==null){
			currentApp.toolID=currentTool.toolID;
		}
		//currentArgs=null;
		toolPanes=null;
		appInput=null;
		return currentApp;
	}
	
	public void endElement(String uri, String localName, String name) throws SAXException {
		name = name.toLowerCase();
		
		if(oldParser)
		{
			System.out.println("XML Format not supported");
			//parseOldEnd(uri,localName, name);
			return;
		}
		
		if(name.equals(TOOL))
		{
			inTool=false;
			externalToolList.add(currentTool);
		}
		//Compilation specific attributes
		else if(name.equals(CC)&&inCompilation)
		{
			if(currentTool!=null&&buildTool!=null)
			{
				buildTool.ccCompiler=finishApp();
			}
		}
		else if(name.equals(CXX)&&inCompilation)
		{
			if(currentTool!=null&&buildTool!=null)
			{
				buildTool.cxxCompiler=finishApp();
			}
		}
		else if(name.equals(F90)&&inCompilation)
		{
			if(currentTool!=null&&buildTool!=null)
			{
				buildTool.f90Compiler=finishApp();
			}
		}
		else if(name.equals(UPC)&&inCompilation)
		{
			if(currentTool!=null&&buildTool!=null)
			{
				buildTool.upcCompiler=finishApp();
			}
		}
		else if(name.equals(PARAMETRIC)){
			inParametric=false;
		}
		else if(name.equals(ALLCOMP)&&inCompilation)
		{
			if(currentTool!=null&&buildTool!=null)
			{
				buildTool.allCompilers=finishApp();
			}
		}
		else if(name.equals(COMPILE))
		{
			if(currentTool!=null&&buildTool!=null)
			{
				currentTool.externalTools.add(buildTool);
			}
			inCompilation=false;
		}
		//Execution specific attributes
		else if(name.equals(EXECUTE)&&inExecution)
		{
			
			execTool.execUtils=new ToolApp[toolApps.size()];
			toolApps.toArray(execTool.execUtils);
			currentTool.externalTools.add(execTool);
			inExecution=false;
		}
		else if(name.equals(UTILITY))
		{
			toolApps.add(finishApp());
			currentApp=null;
		}
		else if(name.equals(ANALYZE)&&inAnalysis)
		{
			ppTool.analysisCommands=new ToolApp[toolApps.size()];
			toolApps.toArray(ppTool.analysisCommands);
			currentTool.externalTools.add(ppTool);
			inAnalysis=false;
		}

		//Options-related tags
		else if(name.equals(OPTIONPANE))
		{
			if(toolOptions!=null)
			{
				if(toolPanes==null)
					toolPanes=new ArrayList<ToolPane>();
				if(appInput==null)
					appInput=new ArrayList<IAppInput>();
				currentPane.setOptions(toolOptions);
				toolPanes.add(currentPane);
				appInput.add(currentPane);
			}
		}
		else if(name.equals(TOGOPT))
		{
			if(actOpt!=null)
			{
				String panename="";
				if(currentPane!=null&&currentPane.configID!=null)
					panename=currentPane.configID;
				actOpt=ToolMaker.finishToolOption(actOpt,panename);
				if(actOpt!=null)
					toolOptions.add(actOpt);
			}
		}
		else if(name.equals(GLOBAL)){
			ToolApp gApp = finishApp();
			
			if(inCompilation&&buildTool!=null){
				buildTool.global=gApp;
			}
			else if(inExecution&&execTool!=null){
				execTool.global=gApp;
			}
			else if(inAnalysis&&ppTool!=null){
				ppTool.global=gApp;
			}
			
			currentApp=null;
		}
		tagStack.pop();
		content.pop();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	/*
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * depricated...someday
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 */
//	
//	
//	private ToolApp oldFinishApp()
//	{
//		if(currentArgs!=null&&currentArgs.size()>0)
//		{
//			currentApp.arguments=new String[currentArgs.size()];
//			currentArgs.toArray(currentApp.arguments);
//		}
//		if(toolPanes!=null&&toolPanes.size()>0)
//		{
//			currentApp.toolPanes=new ToolPane[toolPanes.size()];
//			toolPanes.toArray(currentApp.toolPanes);
//		}
//		currentArgs=null;
//		toolPanes=null;
//		currentApp.toolCommand=content.peek().toString().trim();
//		return currentApp;
//	}
//	
//	private boolean inExecUtil=false;
//	private boolean inAnaTool=false;
//	void parseOldStart(String uri, String localName, String name, Attributes atts)
//	{	
//		name=name.toLowerCase();
//		
//		if(name.equals(TOOL))
//		{
//			inTool=true;
//			currentTool=new ExternalToolProcess();
//		}
//		else if(name.equals(COMPILE))
//		{
//			if(inTool&&currentTool!=null)
//			{
//				inCompilation=true;
//				currentTool.recompile=true;
//				int repdex = atts.getIndex("replace");
//				if(repdex>=0)
//				{
//					if(atts.getValue(repdex).toLowerCase().equals("true"))
//						currentTool.replaceCompiler=true;
//				}
//			}
//		}
//		else if(name.equals(CC)||name.equals(CXX)||name.equals(F90)||name.equals(ALLCOMP))
//		{
//			if(inCompilation&&currentTool!=null)
//			{
//				currentApp=new ToolApp();
//			}
//		}
//		else if(name.equals(EXECUTE)&&!inExecution)
//		{
//			inExecution=true;
//			toolApps=new ArrayList<ToolApp>();
//		}
//		else if(name.equals(UTILITY)&&!inExecUtil)
//		{
//			inExecUtil=true;
//			currentApp=new ToolApp();
//		}
//		else if(name.equals(ANALYZE)&&!inAnalysis)
//		{
//			inAnalysis=true;
//			toolApps=new ArrayList<ToolApp>();
//		}
//		else if((name.equals(PROCESS)||name.equals(VIEW))&&!inAnaTool)
//		{
//			inAnaTool=true;
//			currentApp=new ToolApp();
//		}
//		else if(name.equals(ARGUMENT))
//		{
//			currentArgs=new ArrayList<String>();
//		}
//		else if(name.equals(OPTIONPANE))
//		{
//			boolean virtual=false;
//			//TODO: Make -absolutely- certain that nothing ever tries to greate a UI instance of a virtual pane!
//			int optdex = atts.getIndex("virtual");
//			if(optdex>=0)
//			{
//				if(atts.getValue(optdex).equals("true"))
//				{
//					virtual=true;
//				}
//			}
//			
//			toolOptions=new ArrayList<ToolOption>();
//			currentPane=new ToolPane(virtual);
//			optdex = atts.getIndex(PREPENDWITH);
//			if(optdex>=0)
//			{
//				currentPane.prependOpts=atts.getValue(optdex);
//			}
//			optdex = atts.getIndex(ENCLOSEWITH);
//			if(optdex>=0)
//			{
//				currentPane.encloseOpts=atts.getValue(optdex);
//			}
//			optdex = atts.getIndex(SEPARATEWITH);
//			if(optdex>=0)
//			{
//				currentPane.separateOpts=atts.getValue(optdex);
//			}
//		}
//		else if(name.equals(TOGOPT))
//		{
//			actOpt=new ToolOption();
//			int defdex = atts.getIndex("defstate");
//			if(defdex>=0)
//			{
//				if(atts.getValue(defdex).toLowerCase().equals("on"))
//					actOpt.defState=true;
//			}
//		}
//		else if(name.equals(NAME))
//		{
//			if(actOpt!=null&&tagStack.peek().equals(TOGOPT))
//			{
//				int eqdex = atts.getIndex("equals");
//				if(eqdex>=0)
//				{
//					String useeq = atts.getValue(eqdex).toLowerCase();
//					if(useeq.equals("false"))
//					{
//						actOpt.useEquals=false;
//					}
//				}
//			}
//		}
//		else if(name.equals(VALUE))
//		{
//			if(actOpt!=null&&tagStack.peek().equals(TOGOPT))
//			{
//				int typedex = atts.getIndex("type");
//				if(typedex>=0)
//				{
//					String type = atts.getValue(typedex).toLowerCase();
//					if(type.equals("text"))
//						actOpt.type=ToolOption.TEXT;
//					else if(type.equals("dir"))
//						actOpt.type=ToolOption.DIR;
//					else if(type.equals("file"))
//						actOpt.type=ToolOption.FILE;
//					else if(type.equals("number"))
//						actOpt.type=ToolOption.NUMBER;
//					else if(type.equals("combo"))
//						actOpt.type=ToolOption.COMBO;
//				}
//			}
//		}
//
//		tagStack.push(name.toLowerCase());
//		content.push(new StringBuffer());
//	}
//
//	void parseOldEnd(String uri, String localName, String name)
//	{
//		name = name.toLowerCase();
//		if(name.equals(TOOL))
//		{
//			currentTool.toolID=currentTool.toolName=content.peek().toString().trim();
//			externalToolList.add(currentTool);
//		}
//		//Compilation specific attributes
//		else if(name.equals(CC)&&inCompilation)
//		{
//			if(currentTool!=null)
//			{
//				//currentTool.prependExecution=true;
//				currentTool.ccCompiler=oldFinishApp();
//			}
//		}
//		else if(name.equals(CXX)&&inCompilation)
//		{
//			if(currentTool!=null)
//			{
//				//currentTool.prependExecution=true;
//				currentTool.cxxCompiler=oldFinishApp();
//			}
//		}
//		else if(name.equals(F90)&&inCompilation)
//		{
//			if(currentTool!=null)
//			{
//				//currentTool.prependExecution=true;
//				currentTool.f90Compiler=oldFinishApp();
//			}
//		}
//		else if(name.equals(ALLCOMP)&&inCompilation)
//		{
//			if(currentTool!=null)
//			{
//				//currentTool.prependExecution=true;
//				currentTool.allCompilers=oldFinishApp();
//			}
//		}
//		else if(name.equals(COMPILE))
//		{
//			inCompilation=false;
//		}
//		//Execution specific attributes
//		else if(name.equals(EXECUTE)&&inExecution)
//		{
//			currentTool.execUtils=new ToolApp[toolApps.size()];
//			toolApps.toArray(currentTool.execUtils);
//			inExecution=false;
//		}
//		else if(name.equals(UTILITY)&&inExecUtil)
//		{
//			inExecUtil=false;
//			toolApps.add(oldFinishApp());
//			currentTool.prependExecution=true;
//			currentApp=null;
//		}
//		else if((name.equals(PROCESS)||name.equals(VIEW))&&inAnaTool)
//		{
//			inAnaTool=false;
//			toolApps.add(oldFinishApp());
//			currentApp=null;
//		}
//		else if(name.equals(ANALYZE)&&inAnalysis)
//		{
//			currentTool.analysisCommands=new ToolApp[toolApps.size()];
//			toolApps.toArray(currentTool.analysisCommands);
//			inAnalysis=false;
//		}
//		else if(name.equals(ARGUMENT))
//		{
//			currentArgs.add(content.peek().toString().trim());
//		}
//		
//		//Options-related tags
//		else if(name.equals(OPTIONPANE))
//		{
//			if(toolOptions!=null)
//			{
//				if(toolPanes==null)
//					toolPanes=new ArrayList<ToolPane>();
//				currentPane.setName(content.peek().toString().trim());
//				currentPane.setOptions(toolOptions);
//				toolPanes.add(currentPane);
//			}
//		}
//		else if(name.equals(TOGOPT))
//		{
//			if(actOpt!=null)
//			{
//				actOpt=ToolMaker.finishToolOption(actOpt,"");
//				toolOptions.add(actOpt);
//			}
//		}
//		else if(name.equals(TIP))
//		{
//			if(actOpt!=null)
//				actOpt.toolTip=content.peek().toString().trim();
//		}
//		else if(name.equals(LABEL))
//		{
//			if(actOpt!=null)
//				actOpt.optLabel=content.peek().toString().trim();
//		}
//		else if(name.equals(NAME))
//		{
//			if(actOpt!=null)
//			{
//				actOpt.optName=content.peek().toString().trim();
//			}
//		}
//		else if(name.equals(DEFAULT))
//		{
//			if(actOpt!=null)
//				actOpt.optLabel=content.peek().toString().trim();
//		}
//		
//		tagStack.pop();
//		content.pop();
//	}
	
}
