/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.make.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.fdt.core.CCorePlugin;
import org.eclipse.fdt.make.core.IMakeBuilderInfo;
import org.eclipse.fdt.make.core.MakeBuilder;
import org.eclipse.fdt.make.core.MakeCorePlugin;
import org.eclipse.fdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.fdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.fdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.fdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;


public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID, true);
		try {
			info.setBuildCommand(new Path("make")); //$NON-NLS-1$
			info.setBuildLocation(new Path("")); //$NON-NLS-1$
			info.setStopOnError(false);
			info.setUseDefaultBuildCmd(true);
			info.setAutoBuildEnable(false);
			info.setAutoBuildTarget("all"); //$NON-NLS-1$
			info.setIncrementalBuildEnable(true);
			info.setIncrementalBuildTarget("all"); //$NON-NLS-1$
			info.setFullBuildEnable(true);
			info.setFullBuildTarget("clean all"); //$NON-NLS-1$
			info.setCleanBuildEnable(true);
			info.setCleanBuildTarget("clean"); //$NON-NLS-1$
			info.setErrorParsers(CCorePlugin.getDefault().getAllErrorParsersIDs());
		} catch (CoreException e) {
		}
		MakeCorePlugin.getDefault().getPluginPreferences().setDefault(CCorePlugin.PREF_BINARY_PARSER, CCorePlugin.PLUGIN_ID + ".ELF"); //$NON-NLS-1$

		// default plugin preferences for scanner configuration discovery
		IScannerConfigBuilderInfo scInfo = MakeCorePlugin.createScannerConfigBuildInfo(MakeCorePlugin.getDefault().getPluginPreferences(), ScannerConfigBuilder.BUILDER_ID, true);
		try {
			scInfo.setAutoDiscoveryEnabled(true);
			scInfo.setMakeBuilderConsoleParserEnabled(true);
			scInfo.setESIProviderCommandEnabled(true);
			scInfo.setUseDefaultESIProviderCmd(true);
			scInfo.setESIProviderCommand(new Path("gcc")); //$NON-NLS-1$
			scInfo.setESIProviderArguments("-E -P -v -dD ${plugin_state_location}/${specs_file}");	//$NON-NLS-1$
			scInfo.setESIProviderConsoleParserId(MakeCorePlugin.GCC_SPECS_CONSOLE_PARSER_ID);
			scInfo.setMakeBuilderConsoleParserId(MakeCorePlugin.GCC_SCANNER_INFO_CONSOLE_PARSER_ID);
			scInfo.setSIProblemGenerationEnabled(true);
		} catch (CoreException e) {
		}

        // default plugin preferences for new scanner configuration discovery
        IScannerConfigBuilderInfo2 scInfo2 = ScannerConfigProfileManager.
                createScannerConfigBuildInfo2(MakeCorePlugin.getDefault().getPluginPreferences(),
                        ScannerConfigProfileManager.NULL_PROFILE_ID, true);
        scInfo2.setAutoDiscoveryEnabled(true);
        scInfo2.setProblemReportingEnabled(true);
        scInfo2.setSelectedProfileId(ScannerConfigProfileManager.DEFAULT_SI_PROFILE_ID);
        scInfo2.setBuildOutputFileActionEnabled(false);
        scInfo2.setBuildOutputFilePath(""); //$NON-NLS-1$
        scInfo2.setBuildOutputParserEnabled(true);
        String providerId = "specsFile";    //$NON-NLS-1$
        scInfo2.setProviderOpenFilePath(providerId, "");//$NON-NLS-1$
        scInfo2.setProviderRunCommand(providerId, "gcc");   //$NON-NLS-1$
        scInfo2.setProviderRunArguments(providerId, "-E -P -v -dD ${plugin_state_location}/${specs_file}");//$NON-NLS-1$
        scInfo2.setProviderOutputParserEnabled(providerId, true);
        scInfo2.setProblemReportingEnabled(true);
        try {
            scInfo2.save();
        }
        catch (CoreException e) {
        }
        
		// Store default for makefile
		MakeCorePlugin.getDefault().getPluginPreferences().setDefault(MakeCorePlugin.MAKEFILE_STYLE, "GNU"); //$NON-NLS-1$
	}

}
