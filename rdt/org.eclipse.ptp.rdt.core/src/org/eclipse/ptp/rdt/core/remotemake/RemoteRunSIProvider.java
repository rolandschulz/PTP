/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCMarkerGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.remotemake.RemoteProcessClosure;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.messages.Messages;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;

/**
 * A remove scanner info provider that runs the discovery command remotely.
 * 
 * @author Mike Kucera
 */
public abstract class RemoteRunSIProvider implements IExternalScannerInfoProvider {
	
	
	/**
	 * Subclasses need to provide the actual command to run.
	 */
	protected abstract List<String> getCommand(IProject project, String providerId, IScannerConfigBuilderInfo2 buildInfo);

	
	
	public boolean invokeProvider(IProgressMonitor monitor, IResource resource,
			String providerId, IScannerConfigBuilderInfo2 buildInfo,
			IScannerInfoCollector collector) {

		InfoContext context = new InfoContext(resource.getProject());
		return invokeProvider(monitor, resource, context, providerId, buildInfo, collector, null);
	}

	
	public boolean invokeProvider(IProgressMonitor monitor, IResource resource,
			InfoContext context, String providerId,
			IScannerConfigBuilderInfo2 buildInfo,
			IScannerInfoCollector collector, Properties env) {
		
		monitor = (monitor == null) ? new NullProgressMonitor() : monitor;
		monitor.beginTask(Messages.RemoteRunSiProvider_taskName, 5);
		
		try {
			return doInvoke(monitor, resource, context, providerId, buildInfo, collector, env);
		} catch (Exception e) {
			RDTLog.logError(e);
			return false;
		} finally {
			monitor.done(); // called before return
		}
	}
	
	
	
	private boolean doInvoke(IProgressMonitor monitor, IResource resource,
			InfoContext context, String providerId,
			IScannerConfigBuilderInfo2 buildInfo,
			IScannerInfoCollector collector, Properties env) throws Exception {
		
		System.out.println(Messages.RemoteRunSiProvider_taskName);
		
		IProject project = resource.getProject();
		
		IRemoteExecutionServiceProvider executionProvider = getExecutionServiceProvider(project);
		if(executionProvider == null || monitor.isCanceled())
			return false;
		
		IRemoteConnection connection = executionProvider.getConnection();
		if(!connection.isOpen())
			connection.open(monitor); // throws RemoteConnectionException
		
		monitor.worked(1);
		
		// prepare the command to run
		List<String> runCommand = getCommand(project, providerId, buildInfo);
		if(runCommand == null || runCommand.isEmpty() || monitor.isCanceled())
			return false;
		
		IRemoteServices remoteServices = executionProvider.getRemoteServices();
		IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, runCommand);
		
		monitor.worked(1);
		
		// the output of the command goes to the console
		IConsole console = CCorePlugin.getDefault().getConsole();
		console.start(project);
		OutputStream cos = new StreamMonitor(new SubProgressMonitor(monitor, 70), console.getOutputStream(), 100);
		SCMarkerGenerator markerGenerator = new SCMarkerGenerator();
		
		// the sniffer parses the results of the command and adds them to the collector
		ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.getESIProviderOutputSniffer(
                cos, cos, project, context, providerId, buildInfo, collector, markerGenerator);
        OutputStream consoleOut = sniffer == null ? cos : sniffer.getOutputStream();
        OutputStream consoleErr = sniffer == null ? cos : sniffer.getErrorStream();
		
        if(monitor.isCanceled())
        	return false;
        
        monitor.worked(1);
        
        // run the command and wait for it to produce output
		IRemoteProcess remoteProcess = processBuilder.start();
		RemoteProcessClosure remoteProcessClosure = new RemoteProcessClosure(remoteProcess, consoleOut, consoleErr);
		remoteProcessClosure.runNonBlocking();
		remoteProcess.waitFor();
		
		// we're done
		consoleOut.close();
		consoleErr.close();
		cos.close();
		
		monitor.worked(1);
		
        return true;
	}
	
	
	private static IRemoteExecutionServiceProvider getExecutionServiceProvider(IProject project) {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		if(serviceConfig == null)
			return null;
		
		IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
		IServiceProvider provider = serviceConfig.getServiceProvider(buildService);
		
		if(provider instanceof IRemoteExecutionServiceProvider)
			return (IRemoteExecutionServiceProvider) provider;
		
		return null;
	}
}
