/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCMarkerGenerator;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rdt.sync.core.remotemake.SyncCommandLauncher;

public class SyncScannerInfoProvider implements IExternalScannerInfoProvider {
    public static final String SPECS_FILE_PATH_VAR = "${specs_file_path}"; //$NON-NLS-1$
    public static final String SPECS_FOLDER_NAME = ".specs"; //$NON-NLS-1$
    private static final String EXTERNAL_SI_PROVIDER_CONSOLE_ID = MakeCorePlugin.getUniqueIdentifier()
            + ".ExternalScannerInfoProviderConsole"; //$NON-NLS-1$;
    
    private IProgressMonitor monitor = null;
    private InfoContext context = null;
    private IProject project = null;
    private String providerId = null;
    private IScannerConfigBuilderInfo2 buildInfo = null;
    private IScannerInfoCollector collector = null;
    private Properties env = null;

	@Override
	public boolean invokeProvider(IProgressMonitor monitor, IResource resource, String providerId, IScannerConfigBuilderInfo2
			buildInfo, IScannerInfoCollector collector) {
        InfoContext context = new InfoContext(resource.getProject());
        return invokeProvider(monitor, resource, context, providerId, buildInfo, collector, null);
	}

	@Override
	public boolean invokeProvider(IProgressMonitor mon, IResource resource, InfoContext cont, String provId,
			IScannerConfigBuilderInfo2 bInfo, IScannerInfoCollector coll, Properties props) {
		monitor = mon;
		context = cont;
		IProject project = resource.getProject();
		providerId = provId;
		buildInfo = bInfo;
		collector = coll;
		env = props;
		try {
			this.createSpecsFile();
			List<String> scanCommandLine = this.getScanCommandLine(project);
			this.runCommand(scanCommandLine, true);
			this.deleteSpecsFile();
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
		}
		return true;
	}
	
	// TODO: Externalize strings
	// TODO: What if file exists?
	private void createSpecsFile() throws CoreException {
		List<String> commandLine = new ArrayList<String>();
		commandLine.add("touch"); //$NON-NLS-1$
		commandLine.add(this.getSpecsFileName(project));
		this.runCommand(commandLine, false);
	}
	
	private void deleteSpecsFile() throws CoreException {
		List<String> commandLine = new ArrayList<String>();
		commandLine.add("rm"); //$NON-NLS-1$
		commandLine.add("-f"); //$NON-NLS-1$
		commandLine.add(this.getSpecsFileName(project));
		this.runCommand(commandLine, false);
	}
	
	private void runCommand(List<String> commandLine, boolean attachSISniffer) throws CoreException {
		SyncCommandLauncher launcher = new SyncCommandLauncher();
		IPath command = new Path(commandLine.get(0));
		IPath workingDirectory = project.getLocation();
		Process p = launcher.execute(command, commandLine.toArray(new String[commandLine.size()]), null, workingDirectory, null);
		if (attachSISniffer) {
			this.attachSISniffer(p);
		}
		launcher.waitAndRead(null, null);
	}

	// Copied from:
	// org.eclipse.ptp.rdt.core.remotemake.RemoteSpecsRunSIProvider.getCommand(IProject project, String providerId, IScannerConfigBuilderInfo2 buildInfo)
	private List<String> getScanCommandLine(IProject project) throws CoreException {
		// get the command that is provided in the extension point
		String gcc = buildInfo.getProviderRunCommand(providerId);

		// resolve macros in the run command
		try {
			gcc = ManagedBuildManager.getBuildMacroProvider().resolveValue(gcc, "", null, //$NON-NLS-1$
					IBuildMacroProvider.CONTEXT_CONFIGURATION, ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration());
		} catch (BuildMacroException e) {
			RDTSyncCorePlugin.log(e);
			return null;
		}

		String args = buildInfo.getProviderRunArguments(providerId);
		String specsFileName = getSpecsFileName(project);

		if (gcc == null || args == null || specsFileName == null)
			return null;


		List<String> command = new ArrayList<String>();
		command.add(gcc);
		for (String arg : args.split(" ")) //$NON-NLS-1$
		command.add(arg);

		return command;
	}

	private String getSpecsFileName(IProject project) throws CoreException {
			if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
				return GCCScannerConfigUtil.CPP_SPECS_FILE;
			} else if (project.hasNature(CProjectNature.C_NATURE_ID)) {
				return GCCScannerConfigUtil.C_SPECS_FILE;
			} else {
				// TODO: Handle this case
			}
		return null;
	}
	
	private void attachSISniffer(Process p) throws CoreException {
        SCMarkerGenerator markerGenerator = new SCMarkerGenerator();

        // the sniffer parses the results of the command and adds them to the collector
        OutputStream scanOutput = p.getOutputStream();
        ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.getESIProviderOutputSniffer(scanOutput, scanOutput, project,
        		context, providerId, buildInfo, collector, markerGenerator);
	}
}