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
import java.util.HashSet;
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
 *
 */
public class ScannerInfoUtility {
	public static  ConsoleOutputSniffer createBuildOutputSniffer(OutputStream outputStream,
			OutputStream errorStream,
			IProject project,
			IConfiguration cfg,
			IPath workingDirectory,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector){
		ICfgScannerConfigBuilderInfo2Set container = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
		Map map = container.getInfoMap();
		List clParserList = new ArrayList();
		
		if(container.isPerRcTypeDiscovery()){
			IResourceInfo[] rcInfos = cfg.getResourceInfos();
			for(int q = 0; q < rcInfos.length; q++){
				IResourceInfo rcInfo = rcInfos[q];
				ITool tools[];
				if(rcInfo instanceof IFileInfo){
					tools = ((IFileInfo)rcInfo).getToolsToInvoke();
				} else {
					tools = ((IFolderInfo)rcInfo).getFilteredTools();
				}
				for(int i = 0; i < tools.length; i++){
					ITool tool = tools[i];
					IInputType[] types = tool.getInputTypes();
					
					if(types.length != 0){
						for(int k = 0; k < types.length; k++){
							IInputType type = types[k];
							CfgInfoContext c = new CfgInfoContext(rcInfo, tool, type);
							contributeToConsoleParserList(project, map, c, workingDirectory, markerGenerator, collector, clParserList);
						}
					} else {
						CfgInfoContext c = new CfgInfoContext(rcInfo, tool, null);
						contributeToConsoleParserList(project, map, c, workingDirectory, markerGenerator, collector, clParserList);
					}
				}
			}
		} 
		
		if(clParserList.size() == 0){
			contributeToConsoleParserList(project, map, new CfgInfoContext(cfg), workingDirectory, markerGenerator, collector, clParserList);
		}
		
		if(clParserList.size() != 0){
			return new ConsoleOutputSniffer(outputStream, errorStream, 
					(IScannerInfoConsoleParser[])clParserList.toArray(new IScannerInfoConsoleParser[clParserList.size()]));
		}
		
		return null;
	}
	
	public static boolean contributeToConsoleParserList(
			IProject project, 
			Map map, 
			CfgInfoContext context, 
			IPath workingDirectory,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector,
			List parserList){
		IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2)map.get(context);
		InfoContext ic = context.toInfoContext();
		boolean added = false;
		if (info != null && 
				info.isAutoDiscoveryEnabled() &&
				info.isBuildOutputParserEnabled()) {
			
			Set<String> profileIDSet = new HashSet<String>();
			
			// add profiles from tool
			ITool tool = context.getTool();
			if(tool != null) {
				IInputType[] inputTypes = tool.getInputTypes();
				
				for(IInputType inputType : inputTypes) {
					InputType realInputType = (InputType) inputType;
					String profileIDString = realInputType.getDiscoveryProfileIdAttribute();
					
					String[] profileIDs = profileIDString.split("\\|"); //$NON-NLS-1$
					
					for(String profileID : profileIDs) {
						profileIDSet.add(profileID);
					}
				}
			
			}

			
			for (String id : profileIDSet) {

				// String id = info.getSelectedProfileId();
				ScannerConfigProfile profile = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(id);
				if (profile.getBuildOutputProviderElement() != null) {
					// get the make builder console parser
					SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().getSCProfileInstance(
							project, ic, id);

					IScannerInfoConsoleParser clParser = profileInstance.createBuildOutputParser();
					if (collector == null) {
						collector = profileInstance.getScannerInfoCollector();
					}
					if (clParser != null) {
						clParser.startup(project, workingDirectory, collector,
								info.isProblemReportingEnabled() ? markerGenerator : null);
						parserList.add(clParser);
						added = true;
					}

				}
			}
		}

		return added;
	}
}
