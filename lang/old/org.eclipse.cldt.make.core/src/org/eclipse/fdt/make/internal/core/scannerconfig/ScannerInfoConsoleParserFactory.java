/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.make.internal.core.scannerconfig;

import java.io.OutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IMarkerGenerator;
import org.eclipse.fdt.make.core.MakeCorePlugin;
import org.eclipse.fdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.fdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.fdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.fdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.fdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.fdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;

/**
 * A factory that creates a ConsoleOutputStreamSniffer,
 * ScannerInfoConsoleParser and optionally a ScannerInfoConsoleParserUtility.
 * 
 * @author vhirsl
 */
public class ScannerInfoConsoleParserFactory {

    /**
     * Creates a ConsoleOutputStreamSniffer, make builder scanner info console parser
     * and a utility.
     * 
     * @param outputStream
     * @param errorStream
     * @param currentProject
     * @param providerId 
     * @param scBuildInfo
     * @param markerGenerator
     * @return ConsoleOutputSniffer
     */
    public static ConsoleOutputSniffer getESIProviderOutputSniffer(
                                            OutputStream outputStream,
                                            OutputStream errorStream,
                                            IProject currentProject,
                                            String providerId,
                                            IScannerConfigBuilderInfo2 scBuildInfo,
                                            IScannerInfoCollector collector,
                                            IMarkerGenerator markerGenerator) {
        if (scBuildInfo.isProviderOutputParserEnabled(providerId)) {
            // get the ESIProvider console parser 
            SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                    getSCProfileInstance(currentProject, scBuildInfo.getSelectedProfileId());
            IScannerInfoConsoleParser clParser = profileInstance.createExternalScannerInfoParser(providerId);
            clParser.startup(currentProject, currentProject.getLocation(), collector, markerGenerator);
            // create an output stream sniffer
            return new ConsoleOutputSniffer(outputStream, errorStream, new 
                IScannerInfoConsoleParser[] {clParser});
        }
        return null;
    }

	/**
	 * Creates a ConsoleOutputStreamSniffer, ESI provider scanner info console parser
	 * and a utility.
	 * 
	 * @param outputStream
     * @param errorStream
	 * @param currentProject
	 * @param workingDirectory
     * @param buildInfo
	 * @param markerGenerator
     * @param IScannerInfoCollector2
	 * @return ConsoleOutputSniffer
	 */
	public static ConsoleOutputSniffer getMakeBuilderOutputSniffer(
											OutputStream outputStream,
											OutputStream errorStream,
											IProject currentProject,
											IPath workingDirectory,
                                            IScannerConfigBuilderInfo2 scBuildInfo,
											IMarkerGenerator markerGenerator,
                                            IScannerInfoCollector collector) {
		try {
			// get the SC builder settings
			if (currentProject.hasNature(ScannerConfigNature.NATURE_ID)) {
				if (scBuildInfo == null) {
    				try {
    					scBuildInfo = ScannerConfigProfileManager.
    						createScannerConfigBuildInfo2(currentProject);
    				}
    				catch (CoreException e) {
    					// builder not installed or disabled
    				}
                }
				if (scBuildInfo != null && 
						scBuildInfo.isAutoDiscoveryEnabled() &&
						scBuildInfo.isBuildOutputParserEnabled()) {
					// get the make builder console parser 
					SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
							getSCProfileInstance(currentProject, scBuildInfo.getSelectedProfileId());
					IScannerInfoConsoleParser clParser = profileInstance.createBuildOutputParser();
                    if (collector == null) {
                        collector = profileInstance.getScannerInfoCollector();
                    }
					clParser.startup(currentProject, workingDirectory, collector,
                            scBuildInfo.isProblemReportingEnabled() ? markerGenerator : null);
					// create an output stream sniffer
					return new ConsoleOutputSniffer(outputStream, errorStream, new 
						IScannerInfoConsoleParser[] {clParser});
				}
			}
		} 
		catch (CoreException e) {
			MakeCorePlugin.log(e.getStatus());
		}
		return null;
	}
}
