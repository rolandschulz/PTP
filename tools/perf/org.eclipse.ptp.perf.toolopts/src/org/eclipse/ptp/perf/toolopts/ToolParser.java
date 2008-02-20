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

import java.util.ArrayList;
import java.util.Stack;

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
	private static final String ALLCOMP = "allcompilers";
	
	/**
	 * Top level element of the execution phase of analysis
	 */
	private static final String EXECUTE="execute";
	private static final String UTILITY="utility";
	
	
	/**
	 * Top level element of the analysis phase of analysis
	 */
	private static final String ANALYZE = "analyze";
	
	private static final String PROCESS = "process";
	private static final String VIEW = "view";
	
	/**
	 * Top level element of options pane definition structure
	 */
	private static final String OPTIONPANE = "optionpane";
	private static final String TOGOPT = "togoption";
	private static final String LABEL = "label";
	private static final String TIP = "tooltip";
	private static final String NAME = "optname";
	private static final String VALUE = "optvalue";
	private static final String DEFAULT = "default";
	private static final String DEFSTATE = "defstate";
	
	/**
	 * Element to specify an argument to a compiler, run utility or analysis tool
	 */
	private static final String ARGUMENT="argument";

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
	
	private Stack tagStack = new Stack();
	protected ArrayList performanceTools= new ArrayList();
	private PerformanceTool currentTool;
	/**
	 * Contains the list of tool panes for the current tool app
	 */
	private ArrayList toolPanes;// = new ArrayList();
	private ToolPane currentPane;
	
	/**
	 * Contains the list of tool apps (compilers, exec utils or analysis tools) for the current subheading
	 */
	private ArrayList toolApps;
	/**
	 * Contains the list of argument strings for the current tool.
	 */
	private ArrayList currentArgs;
	/**
	 * The tool app currently being worked on
	 */
	private ToolApp currentApp;
	
	private ArrayList toolOptions;
	private ToolOption actOpt;
	private Stack content = new Stack();
	
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
			parseOldStart(uri, localName, name,atts);
			return;
		}
		if(name.equals(TOOL))
		{
			inTool=true;
			currentTool=new PerformanceTool();
			currentTool.toolID=currentTool.toolName=getAttribute("name",atts);
		}
		else if(name.equals(COMPILE))
		{
			if(inTool&&currentTool!=null)
			{
				inCompilation=true;
				currentTool.recompile=true;
				currentTool.replaceCompiler=getBooleanAttribute("replace",false,atts);
			}
		}
		else if(name.equals(EXECUTE)&&!inExecution)
		{
			inExecution=true;
			toolApps=new ArrayList();
		}
		else if(name.equals(ANALYZE)&&!inAnalysis)
		{
			inAnalysis=true;
			toolApps=new ArrayList();
		}
		else if(name.equals(CC)||name.equals(CXX)||name.equals(F90)||name.equals(ALLCOMP)||name.equals(UTILITY))
		{
			if(inTool&&currentTool!=null)
			{
				currentApp=new ToolApp();
				currentApp.toolCommand=getAttribute("command",atts);
				currentApp.toolGroup=getAttribute("group",atts);
				if(currentApp.toolGroup!=null&&currentApp.toolCommand!=null)
					currentTool.groupApp.put(currentApp.toolGroup, currentApp.toolCommand);
				if(inExecution)
					currentTool.prependExecution=true;
			}
		}
		else if(name.equals(ARGUMENT))
		{
			if(currentArgs==null)
				currentArgs=new ArrayList();
			currentArgs.add(getAttribute("value",atts));
		}
		else if(name.equals(OPTIONPANE))
		{
			boolean virtual=getBooleanAttribute("virtual",false,atts);
			//TODO: Make -absolutely- certain that nothing ever tries to greate a UI instance of a virtual pane!
			
			toolOptions=new ArrayList();
			currentPane=new ToolPane(virtual);
			currentPane.setName(getAttribute("title",atts));
			int optdex = atts.getIndex("prependwith");
			if(optdex>=0)
			{
				currentPane.prependOpts=atts.getValue(optdex);
			}
			optdex = atts.getIndex("enclosewith");
			if(optdex>=0)
			{
				currentPane.encloseOpts=atts.getValue(optdex);
			}
			optdex = atts.getIndex("seperatewith");
			if(optdex>=0)
			{
				currentPane.separateOpts=atts.getValue(optdex);
			}
		}
		else if(name.equals(TOGOPT))
		{
			actOpt=new ToolOption();
			
			actOpt.optLabel=getAttribute(LABEL, atts);
			actOpt.optName=getAttribute(NAME,atts);
			actOpt.toolTip=getAttribute(TIP,atts);
			actOpt.defState=getBooleanAttribute(DEFSTATE,false,atts);
		}
		else if(name.equals(VALUE))
		{
			if(actOpt!=null&&tagStack.peek().equals(TOGOPT))
			{
				actOpt.useEquals=getBooleanAttribute("equals",true,atts);
				actOpt.defText=getAttribute(DEFAULT,atts);
				String type=getAttribute("type",atts);
				if(type!=null)
				{
					type = type.toLowerCase();
					if(type.equals("text"))
						actOpt.type=ToolOption.TEXT;
					else if(type.equals("dir"))
						actOpt.type=ToolOption.DIR;
					else if(type.equals("file"))
						actOpt.type=ToolOption.FILE;
					else if(type.equals("number"))
						actOpt.type=ToolOption.NUMBER;
					else if(type.equals("combo"))
						actOpt.type=ToolOption.COMBO;
				}
			}
		}
		tagStack.push(name.toLowerCase());
		content.push(new StringBuffer());
	}
	
	private ToolApp finishApp()
	{
		if(currentArgs!=null&&currentArgs.size()>0)
		{
			currentApp.arguments=new String[currentArgs.size()];
			currentArgs.toArray(currentApp.arguments);
		}
		if(toolPanes!=null&&toolPanes.size()>0)
		{
			currentApp.toolPanes=new ToolPane[toolPanes.size()];
			toolPanes.toArray(currentApp.toolPanes);
		}
		currentArgs=null;
		toolPanes=null;
		return currentApp;
	}
	
	public void endElement(String uri, String localName, String name) throws SAXException {
		name = name.toLowerCase();
		
		if(oldParser)
		{
			parseOldEnd(uri,localName, name);
			return;
		}
		
		if(name.equals(TOOL))
		{
			inTool=false;
			performanceTools.add(currentTool);
		}
		//Compilation specific attributes
		else if(name.equals(CC)&&inCompilation)
		{
			if(currentTool!=null)
			{
				currentTool.ccCompiler=finishApp();
			}
		}
		else if(name.equals(CXX)&&inCompilation)
		{
			if(currentTool!=null)
			{
				currentTool.cxxCompiler=finishApp();
			}
		}
		else if(name.equals(F90)&&inCompilation)
		{
			if(currentTool!=null)
			{
				currentTool.f90Compiler=finishApp();
			}
		}
		else if(name.equals(ALLCOMP)&&inCompilation)
		{
			if(currentTool!=null)
			{
				currentTool.allCompilers=finishApp();
			}
		}
		else if(name.equals(COMPILE))
		{
			inCompilation=false;
		}
		//Execution specific attributes
		else if(name.equals(EXECUTE)&&inExecution)
		{
			currentTool.execUtils=new ToolApp[toolApps.size()];
			toolApps.toArray(currentTool.execUtils);
			inExecution=false;
		}
		else if(name.equals(UTILITY))
		{
			toolApps.add(finishApp());
			currentApp=null;
		}
		else if(name.equals(ANALYZE)&&inAnalysis)
		{
			currentTool.analysisCommands=new ToolApp[toolApps.size()];
			toolApps.toArray(currentTool.analysisCommands);
			inAnalysis=false;
		}

		//Options-related tags
		else if(name.equals(OPTIONPANE))
		{
			if(toolOptions!=null)
			{
				if(toolPanes==null)
					toolPanes=new ArrayList();
				currentPane.setOptions(toolOptions);
				toolPanes.add(currentPane);
			}
		}
		else if(name.equals(TOGOPT))
		{
			if(actOpt!=null)
			{
				actOpt=ToolMaker.finishToolOption(actOpt);
				if(actOpt!=null)
					toolOptions.add(actOpt);
			}
		}
		
		tagStack.pop();
		content.pop();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * depricated...someday
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	private ToolApp oldFinishApp()
	{
		if(currentArgs!=null&&currentArgs.size()>0)
		{
			currentApp.arguments=new String[currentArgs.size()];
			currentArgs.toArray(currentApp.arguments);
		}
		if(toolPanes!=null&&toolPanes.size()>0)
		{
			currentApp.toolPanes=new ToolPane[toolPanes.size()];
			toolPanes.toArray(currentApp.toolPanes);
		}
		currentArgs=null;
		toolPanes=null;
		currentApp.toolCommand=content.peek().toString().trim();
		return currentApp;
	}
	
	private boolean inExecUtil=false;
	private boolean inAnaTool=false;
	void parseOldStart(String uri, String localName, String name, Attributes atts)
	{	
		name=name.toLowerCase();
		
		if(name.equals(TOOL))
		{
			inTool=true;
			currentTool=new PerformanceTool();
		}
		else if(name.equals(COMPILE))
		{
			if(inTool&&currentTool!=null)
			{
				inCompilation=true;
				currentTool.recompile=true;
				int repdex = atts.getIndex("replace");
				if(repdex>=0)
				{
					if(atts.getValue(repdex).toLowerCase().equals("true"))
						currentTool.replaceCompiler=true;
				}
			}
		}
		else if(name.equals(CC)||name.equals(CXX)||name.equals(F90)||name.equals(ALLCOMP))
		{
			if(inCompilation&&currentTool!=null)
			{
				currentApp=new ToolApp();
			}
		}
		else if(name.equals(EXECUTE)&&!inExecution)
		{
			inExecution=true;
			toolApps=new ArrayList();
		}
		else if(name.equals(UTILITY)&&!inExecUtil)
		{
			inExecUtil=true;
			currentApp=new ToolApp();
		}
		else if(name.equals(ANALYZE)&&!inAnalysis)
		{
			inAnalysis=true;
			toolApps=new ArrayList();
		}
		else if((name.equals(PROCESS)||name.equals(VIEW))&&!inAnaTool)
		{
			inAnaTool=true;
			currentApp=new ToolApp();
		}
		else if(name.equals(ARGUMENT))
		{
			currentArgs=new ArrayList();
		}
		else if(name.equals(OPTIONPANE))
		{
			boolean virtual=false;
			//TODO: Make -absolutely- certain that nothing ever tries to greate a UI instance of a virtual pane!
			int optdex = atts.getIndex("virtual");
			if(optdex>=0)
			{
				if(atts.getValue(optdex).equals("true"))
				{
					virtual=true;
				}
			}
			
			toolOptions=new ArrayList();
			currentPane=new ToolPane(virtual);
			optdex = atts.getIndex("prependwith");
			if(optdex>=0)
			{
				currentPane.prependOpts=atts.getValue(optdex);
			}
			optdex = atts.getIndex("enclosewith");
			if(optdex>=0)
			{
				currentPane.encloseOpts=atts.getValue(optdex);
			}
			optdex = atts.getIndex("seperatewith");
			if(optdex>=0)
			{
				currentPane.separateOpts=atts.getValue(optdex);
			}
		}
		else if(name.equals(TOGOPT))
		{
			actOpt=new ToolOption();
			int defdex = atts.getIndex("defstate");
			if(defdex>=0)
			{
				if(atts.getValue(defdex).toLowerCase().equals("on"))
					actOpt.defState=true;
			}
		}
		else if(name.equals(NAME))
		{
			if(actOpt!=null&&tagStack.peek().equals(TOGOPT))
			{
				int eqdex = atts.getIndex("equals");
				if(eqdex>=0)
				{
					String useeq = atts.getValue(eqdex).toLowerCase();
					if(useeq.equals("false"))
					{
						actOpt.useEquals=false;
					}
				}
			}
		}
		else if(name.equals(VALUE))
		{
			if(actOpt!=null&&tagStack.peek().equals(TOGOPT))
			{
				int typedex = atts.getIndex("type");
				if(typedex>=0)
				{
					String type = atts.getValue(typedex).toLowerCase();
					if(type.equals("text"))
						actOpt.type=ToolOption.TEXT;
					else if(type.equals("dir"))
						actOpt.type=ToolOption.DIR;
					else if(type.equals("file"))
						actOpt.type=ToolOption.FILE;
					else if(type.equals("number"))
						actOpt.type=ToolOption.NUMBER;
					else if(type.equals("combo"))
						actOpt.type=ToolOption.COMBO;
				}
			}
		}

		tagStack.push(name.toLowerCase());
		content.push(new StringBuffer());
	}

	void parseOldEnd(String uri, String localName, String name)
	{
		name = name.toLowerCase();
		if(name.equals(TOOL))
		{
			currentTool.toolID=currentTool.toolName=content.peek().toString().trim();
			performanceTools.add(currentTool);
		}
		//Compilation specific attributes
		else if(name.equals(CC)&&inCompilation)
		{
			if(currentTool!=null)
			{
				//currentTool.prependExecution=true;
				currentTool.ccCompiler=oldFinishApp();
			}
		}
		else if(name.equals(CXX)&&inCompilation)
		{
			if(currentTool!=null)
			{
				//currentTool.prependExecution=true;
				currentTool.cxxCompiler=oldFinishApp();
			}
		}
		else if(name.equals(F90)&&inCompilation)
		{
			if(currentTool!=null)
			{
				//currentTool.prependExecution=true;
				currentTool.f90Compiler=oldFinishApp();
			}
		}
		else if(name.equals(ALLCOMP)&&inCompilation)
		{
			if(currentTool!=null)
			{
				//currentTool.prependExecution=true;
				currentTool.allCompilers=oldFinishApp();
			}
		}
		else if(name.equals(COMPILE))
		{
			inCompilation=false;
		}
		//Execution specific attributes
		else if(name.equals(EXECUTE)&&inExecution)
		{
			currentTool.execUtils=new ToolApp[toolApps.size()];
			toolApps.toArray(currentTool.execUtils);
			inExecution=false;
		}
		else if(name.equals(UTILITY)&&inExecUtil)
		{
			inExecUtil=false;
			toolApps.add(oldFinishApp());
			currentTool.prependExecution=true;
			currentApp=null;
		}
		else if((name.equals(PROCESS)||name.equals(VIEW))&&inAnaTool)
		{
			inAnaTool=false;
			toolApps.add(oldFinishApp());
			currentApp=null;
		}
		else if(name.equals(ANALYZE)&&inAnalysis)
		{
			currentTool.analysisCommands=new ToolApp[toolApps.size()];
			toolApps.toArray(currentTool.analysisCommands);
			inAnalysis=false;
		}
		else if(name.equals(ARGUMENT))
		{
			currentArgs.add(content.peek().toString().trim());
		}
		
		//Options-related tags
		else if(name.equals(OPTIONPANE))
		{
			if(toolOptions!=null)
			{
				if(toolPanes==null)
					toolPanes=new ArrayList();
				currentPane.setName(content.peek().toString().trim());
				currentPane.setOptions(toolOptions);
				toolPanes.add(currentPane);
			}
		}
		else if(name.equals(TOGOPT))
		{
			if(actOpt!=null)
			{
				actOpt=ToolMaker.finishToolOption(actOpt);
				toolOptions.add(actOpt);
			}
		}
		else if(name.equals(TIP))
		{
			if(actOpt!=null)
				actOpt.toolTip=content.peek().toString().trim();
		}
		else if(name.equals(LABEL))
		{
			if(actOpt!=null)
				actOpt.optLabel=content.peek().toString().trim();
		}
		else if(name.equals(NAME))
		{
			if(actOpt!=null)
			{
				actOpt.optName=content.peek().toString().trim();
			}
		}
		else if(name.equals(DEFAULT))
		{
			if(actOpt!=null)
				actOpt.optLabel=content.peek().toString().trim();
		}
		
		tagStack.pop();
		content.pop();
	}
	
}
