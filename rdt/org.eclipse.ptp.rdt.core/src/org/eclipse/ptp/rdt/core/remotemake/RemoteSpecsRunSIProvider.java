/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.remotemake;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * Gets the command to run gcc from the scanner discovery extension point and
 * prepares it to run.
 * 
 * @author Mike Kucera
 */
public class RemoteSpecsRunSIProvider extends RemoteRunSIProvider {

	public static final String SPECS_FILE_PATH_VAR = "${specs_file_path}"; //$NON-NLS-1$
	public static final String SPECS_FOLDER_NAME = ".specs"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	@Override
	protected List<String> getCommand(IProject project, String providerId, IScannerConfigBuilderInfo2 buildInfo) {
		// get the command that is provided in the extension point
		String gcc = buildInfo.getProviderRunCommand(providerId);

		// resolve macros in the run command
		try {
			gcc = ManagedBuildManager.getBuildMacroProvider().resolveValue(gcc, EMPTY_STRING, null,
					IBuildMacroProvider.CONTEXT_CONFIGURATION, ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration());
		} catch (BuildMacroException e1) {
			RDTLog.logError(e1);
			return null;
		}

		String args = buildInfo.getProviderRunArguments(providerId);
		String specsFileName = getSpecsFileName(project);

		if (gcc == null || args == null || specsFileName == null)
			return null;

		IFileStore specsFilestore;
		try {
			specsFilestore = createSpecsFile(project, specsFileName, null);
		} catch (CoreException e) {
			RDTLog.logError(e);
			return null;
		} catch (IOException e) {
			RDTLog.logError(e);
			return null;
		}

		String specsFilePath = EFSExtensionManager.getDefault().getPathFromURI(specsFilestore.toURI());
		args = args.replace(SPECS_FILE_PATH_VAR, specsFilePath);

		List<String> command = new ArrayList<String>();
		command.add(gcc);
		for (String arg : args.split(" ")) //$NON-NLS-1$
			command.add(arg);

		return command;
	}

	/**
	 * Create an empty "specs" file in the server folder using EFS.
	 * 
	 * @param monitor
	 * @throws IOException
	 * @since 2.0
	 */
	protected static IFileStore createSpecsFile(IProject project, String specsFileName, IProgressMonitor monitor)
			throws CoreException, IOException {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
		IServiceProvider provider = serviceConfig.getServiceProvider(buildService);
		IRemoteExecutionServiceProvider executionProvider = null;
		if (provider instanceof IRemoteExecutionServiceProvider) {
			executionProvider = (IRemoteExecutionServiceProvider) provider;
		}

		if (executionProvider != null) {

			IRemoteServices remoteServices = executionProvider.getRemoteServices();
			if (remoteServices == null)
				return null;
			
			IRemoteConnection connection = executionProvider.getConnection();

			if (!connection.isOpen())
				try {
					connection.open(null);
				} catch (RemoteConnectionException e) {
					RDTLog.logError(e);
				}

			// get the config dir
			IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, ""); //$NON-NLS-1$
			String configPath = executionProvider.getConfigLocation();
			IRemoteFileManager remoteFileManager = remoteServices.getFileManager(connection);
			IFileStore workingDir = remoteFileManager.getResource(configPath);

			// set the working directory for the process to be the config
			// directory
			processBuilder.directory(workingDir);

			IFileStore specsFile = workingDir.getChild(specsFileName);

			IFileInfo fileInfo = specsFile.fetchInfo();

			if (!fileInfo.exists()) {
				try {
					// If the working directory doesn't exist, create it. 
					if (!workingDir.fetchInfo().exists()) {
						workingDir.mkdir(0, monitor);
					}

					InputStream is = new ByteArrayInputStream("\n".getBytes()); //$NON-NLS-1$
					OutputStream os = specsFile.openOutputStream(EFS.NONE, null);

					int data = is.read();
					while (data != -1) {
						os.write(data);
						data = is.read();
					}
					
					is.close();
					os.close();
				} catch (IOException e) {
					RDTLog.logError(e);
				}
			}

			return specsFile;

		}

		return null;
	}

	/**
	 * @since 2.0
	 */
	protected static String getSpecsFileName(IProject project) {
		try {
			if (project.hasNature(CCProjectNature.CC_NATURE_ID))
				return GCCScannerConfigUtil.CPP_SPECS_FILE;
			else if (project.hasNature(CProjectNature.C_NATURE_ID))
				return GCCScannerConfigUtil.C_SPECS_FILE;
		} catch (CoreException e) {
		}

		return null;
	}

	@Override
	protected IPath getWorkingDirectory(IProject project) {

		ServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
		IServiceProvider provider = serviceConfig.getServiceProvider(buildService);
		IRemoteExecutionServiceProvider executionProvider = null;
		if (provider instanceof IRemoteExecutionServiceProvider) {
			executionProvider = (IRemoteExecutionServiceProvider) provider;
		}

		if (executionProvider != null) {

			IRemoteServices remoteServices = executionProvider.getRemoteServices();
			if (remoteServices == null) 
				return null;
			
			IRemoteConnection connection = executionProvider.getConnection();

			if (!connection.isOpen())
				try {
					connection.open(null);
				} catch (RemoteConnectionException e) {
					RDTLog.logError(e);
				}
			
		// get the CWD
		IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, ""); //$NON-NLS-1$
		IFileStore workingDir = processBuilder.directory();
		
		IPath path = new Path(EFSExtensionManager.getDefault().getPathFromURI(workingDir.toURI()));
		return path;
		}
		return null;
	}
}
