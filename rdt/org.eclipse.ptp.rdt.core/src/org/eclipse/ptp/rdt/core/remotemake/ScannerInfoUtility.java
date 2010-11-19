/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.remotemake;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * @author crecoskie
 * @since 2.0
 * 
 */
public class ScannerInfoUtility {
	public static ConsoleOutputSniffer createBuildOutputSniffer(OutputStream outputStream, OutputStream errorStream,
			IProject project, IConfiguration cfg, IPath workingDirectory, IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector) {
		ICfgScannerConfigBuilderInfo2Set container = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
		Map<CfgInfoContext, IScannerConfigBuilderInfo2>  map = container.getInfoMap();
		List<IScannerInfoConsoleParser> clParserList = null;
		Map<String, Set<CfgInfoContext>> profileIDToConfigContextMap = new HashMap<String, Set<CfgInfoContext>>();
		if (container.isPerRcTypeDiscovery()) {
			IResourceInfo[] rcInfos = cfg.getResourceInfos();
			for (int q = 0; q < rcInfos.length; q++) {
				IResourceInfo rcInfo = rcInfos[q];
				ITool tools[];
				if (rcInfo instanceof IFileInfo) {
					tools = ((IFileInfo) rcInfo).getToolsToInvoke();
				} else {
					tools = ((IFolderInfo) rcInfo).getFilteredTools();
				}
				for (int i = 0; i < tools.length; i++) {
					ITool tool = tools[i];
					IInputType[] types = tool.getInputTypes();

					if (types.length != 0) {
						for (int k = 0; k < types.length; k++) {
							IInputType type = types[k];
							CfgInfoContext c = new CfgInfoContext(rcInfo, tool, type);
							contributeToProfileSet(project, map, c, workingDirectory, profileIDToConfigContextMap);
						}
					} else {
						CfgInfoContext c = new CfgInfoContext(rcInfo, tool, null);
						contributeToProfileSet(project, map, c, workingDirectory, profileIDToConfigContextMap);
					}
				}
			}
		}

		if (profileIDToConfigContextMap.size() == 0) {
			contributeToProfileSet(project, map, new CfgInfoContext(cfg), workingDirectory, profileIDToConfigContextMap);
		}
		
		if (profileIDToConfigContextMap.size() != 0) {
			clParserList = contributeToConsoleParserList(project, map, workingDirectory, markerGenerator, collector, profileIDToConfigContextMap);
		}

		if (clParserList.size() != 0) {
			return new ConsoleOutputSniffer(outputStream, errorStream,
					(IScannerInfoConsoleParser[]) clParserList.toArray(new IScannerInfoConsoleParser[clParserList.size()]));
		}

		return null;
	}
	private static void contributeToProfileSet(IProject project, Map<CfgInfoContext, IScannerConfigBuilderInfo2> map, CfgInfoContext context, IPath workingDirectory, Map<String, Set<CfgInfoContext>> profileIDToConfigInfoMap) {
		
		IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2) map.get(context);
		if (info != null && info.isAutoDiscoveryEnabled() && info.isBuildOutputParserEnabled()) {

			// add profiles from tool
			ITool tool = context.getTool();
			if (tool != null) {
				IInputType[] inputTypes = tool.getInputTypes();

				for (IInputType inputType : inputTypes) {
					if(inputType!=null){
						InputType realInputType = (InputType) inputType;
						String profileIDString = realInputType.getDiscoveryProfileIdAttribute();
	
						String[] profileIDs = profileIDString.split("\\|"); //$NON-NLS-1$
	
						for (String profileID : profileIDs) {
							if(profileID!=null){
								Set<CfgInfoContext> thisProfileContextSet = profileIDToConfigInfoMap.get(profileID);
								if(thisProfileContextSet==null){
									thisProfileContextSet= new HashSet<CfgInfoContext>();
								}
								thisProfileContextSet.add(context);
								profileIDToConfigInfoMap.put(profileID, thisProfileContextSet);
							}
						}
					}
				}

			}

			
		}

	}
	
	private static List<IScannerInfoConsoleParser> contributeToConsoleParserList(IProject project, Map<CfgInfoContext, IScannerConfigBuilderInfo2> map, IPath workingDirectory, IMarkerGenerator markerGenerator,IScannerInfoCollector collector, Map<String, Set<CfgInfoContext>> profileIDToConfigInfoMap){
		
		List<IScannerInfoConsoleParser> clParserList = new ArrayList<IScannerInfoConsoleParser>();
		
		for (String profileId : profileIDToConfigInfoMap.keySet()) {

			if(profileId!=null){
				ScannerConfigProfile profile = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(profileId);
				if (profile.getBuildOutputProviderElement() != null) {
					
					Set<CfgInfoContext> profileConextSet = profileIDToConfigInfoMap.get(profileId);
					List<CfgInfoContext> handledProfileConextList = new ArrayList<CfgInfoContext>();
					Iterator<CfgInfoContext> orginalProfileConextIter = profileConextSet.iterator();
					if(orginalProfileConextIter.hasNext()){
						//add first context
						CfgInfoContext firstConext = orginalProfileConextIter.next();
						handledProfileConextList.add(firstConext);
						IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2) map.get(firstConext);
						InfoContext ic = firstConext.toInfoContext();
						// get the make builder console parser
						SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().getSCProfileInstance(project, ic,
								profileId);
		
						IScannerInfoConsoleParser clParser = profileInstance.createBuildOutputParser();
						if (collector == null) {
							collector = profileInstance.getScannerInfoCollector();
						}
		     			clParser.startup(project, workingDirectory, collector, info.isProblemReportingEnabled() ? markerGenerator
									: null);
						clParserList.add(clParser);
					}
					//starting from 2nd context, check each one to see if it is the same one with any of already handled context  
					while(orginalProfileConextIter.hasNext()){
						CfgInfoContext thisContext = orginalProfileConextIter.next();
						boolean skipThisContext = false;
						for(CfgInfoContext handledContext : handledProfileConextList){
							if(doContextsGenerateSameParser(project, map, collector, profileId, handledContext, thisContext)){
								skipThisContext = true;
								break;
							}
						}
						if(!skipThisContext){
							handledProfileConextList.add(thisContext);
							//create parser
							IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2) map.get(thisContext);
							InfoContext ic = thisContext.toInfoContext();
							// get the make builder console parser
							SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().getSCProfileInstance(project, ic,
									profileId);
			
							IScannerInfoConsoleParser clParser = profileInstance.createBuildOutputParser();
							if (collector == null) {
								collector = profileInstance.getScannerInfoCollector();
							}
			     			clParser.startup(project, workingDirectory, collector, info.isProblemReportingEnabled() ? markerGenerator
										: null);
							clParserList.add(clParser);
						}
					}
					
				}
	
			}
		}
		
		return clParserList;
	}
	
	private static boolean doContextsGenerateSameParser(IProject project, Map<CfgInfoContext, IScannerConfigBuilderInfo2> map, IScannerInfoCollector collector, String profileID, CfgInfoContext context_1, CfgInfoContext context_2){
		
		InfoContext ic_1 = context_1.toInfoContext();
		SCProfileInstance profileInstance_1 = ScannerConfigProfileManager.getInstance().getSCProfileInstance(project, ic_1,
				profileID);
		IScannerInfoConsoleParser clParser_1 = profileInstance_1.createBuildOutputParser();
		
		InfoContext ic_2 = context_2.toInfoContext();
		SCProfileInstance profileInstance_2 = ScannerConfigProfileManager.getInstance().getSCProfileInstance(project, ic_2,
				profileID);
		IScannerInfoConsoleParser clParser_2 = profileInstance_2.createBuildOutputParser();
		
		if(!clParser_1.getClass().getName().equals(clParser_2.getClass().getName())){
			return false;
		}
		
		if (collector == null) {
			IScannerInfoCollector collector_1 = profileInstance_1.getScannerInfoCollector();
			IScannerInfoCollector collector_2 = profileInstance_2.getScannerInfoCollector();
			if(!collector_1.getClass().getName().equals(collector_2.getClass().getName())){
				return false;
			}
		}
		IScannerConfigBuilderInfo2 info_1 = (IScannerConfigBuilderInfo2) map.get(context_1);
		IScannerConfigBuilderInfo2 info_2 = (IScannerConfigBuilderInfo2) map.get(context_2);
		
		if(info_1.isProblemReportingEnabled()!=info_2.isProblemReportingEnabled()){
			return false;
		}
		return true;
	}
	
	/*
	public static boolean contributeToConsoleParserList(IProject project, Map map, CfgInfoContext context, IPath workingDirectory,
			IMarkerGenerator markerGenerator, IScannerInfoCollector collector, List parserList) {
		IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2) map.get(context);
		InfoContext ic = context.toInfoContext();
		boolean added = false;
		if (info != null && info.isAutoDiscoveryEnabled() && info.isBuildOutputParserEnabled()) {

			Set<String> profileIDSet = new HashSet<String>();

			// add profiles from tool
			ITool tool = context.getTool();
			if (tool != null) {
				IInputType[] inputTypes = tool.getInputTypes();

				for (IInputType inputType : inputTypes) {
					InputType realInputType = (InputType) inputType;
					String profileIDString = realInputType.getDiscoveryProfileIdAttribute();

					String[] profileIDs = profileIDString.split("\\|"); //$NON-NLS-1$

					for (String profileID : profileIDs) {
						profileIDSet.add(profileID);
					}
				}

			}

			for (String id : profileIDSet) {

				// String id = info.getSelectedProfileId();
				ScannerConfigProfile profile = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(id);
				if (profile.getBuildOutputProviderElement() != null) {
					// get the make builder console parser
					SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().getSCProfileInstance(project, ic,
							id);

					IScannerInfoConsoleParser clParser = profileInstance.createBuildOutputParser();
					if (collector == null) {
						collector = profileInstance.getScannerInfoCollector();
					}
					if (clParser != null) {
						clParser.startup(project, workingDirectory, collector, info.isProblemReportingEnabled() ? markerGenerator
								: null);
						parserList.add(clParser);
						added = true;
					}

				}
			}
		}

		return added;
	}
	*/

}
