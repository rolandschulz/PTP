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
package org.eclipse.ptp.rdt.ui.subsystems;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CalledByResult;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallsToResult;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIndexIncludeValue;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerProgress;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask;
import org.eclipse.ptp.internal.rdt.core.miners.CDTMiner;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraph;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteCIndexServiceProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.connectorservice.dstore.util.StatusMonitor;
import org.eclipse.rse.connectorservice.dstore.util.StatusMonitorFactory;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * An RSE subsystem which is used to provide C/C++ indexing services from a Miner
 * running on a remote host.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public class RemoteCIndexSubsystem extends SubSystem implements ICIndexSubsystem {

	private Map<IProject, String> fInitializedProjects = new HashMap<IProject, String>();
	private ProjectChangeListener fProjectOpenListener;
	private List<String> fErrorMessages = new ArrayList<String>();

	protected RemoteCIndexSubsystem(IHost host,
			IConnectorService connectorService) {
		super(host, connectorService);
		generateErrorMessages();
		setHidden(true);
	}
	
	private void generateErrorMessages() {				
//    	fErrorMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.inclusionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
//    	fErrorMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.definitionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fErrorMessages.add("Unresolved inclusion:"); //$NON-NLS-1$
    	fErrorMessages.add("Macro definition not found:"); //$NON-NLS-1$
	}	
	
	// index management
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void initializeSubSystem(IProgressMonitor monitor) {
		
		try {
			super.initializeSubSystem(monitor);
		} catch (SystemMessageException e) {
			RDTLog.logError(e);	
		}
		
		fProjectOpenListener = new ProjectChangeListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fProjectOpenListener);
		
		getDataStore().activateMiner("org.eclipse.ptp.internal.rdt.core.miners.CDTMiner"); //$NON-NLS-1$
	}

	@Override
	public void uninitializeSubSystem(IProgressMonitor monitor) {
		super.uninitializeSubSystem(monitor);
		
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fProjectOpenListener);
		//fInitializedProjects = null;
		fInitializedProjects.clear();
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#reindexScope(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask)
	 */
	public IStatus reindexScope(Scope scope, IRemoteIndexerInfoProvider provider, String indexLocation, IProgressMonitor monitor, RemoteIndexerTask task) {
		removeProblems(scope);
		DataStore dataStore = getDataStore();
		if(dataStore == null)
			return Status.OK_STATUS;
		
    	DataElement result = getDataStore().createObject(null, CDTMiner.T_INDEX_STATUS_DESCRIPTOR, "index"); //$NON-NLS-1$
     	StatusMonitor smonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(getConnectorService(), dataStore);
     	monitor.beginTask("Rebuilding indexing...", 100); //$NON-NLS-1$
   
        DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_INDEX_REINDEX);
        if (queryCmd != null) {
        	ArrayList<Object> args = new ArrayList<Object>();
 
        	args.add(dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName()));
        	
        	String serializedProvider = null;
        	try {
				serializedProvider = Serializer.serialize(provider);
			} catch (IOException e) {
				RDTLog.logError(e);
			}
			
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_SCANNER_INFO_PROVIDER, serializedProvider));
			args.add(dataStore.createObject(null, CDTMiner.T_SCOPE_CONFIG_LOCATION, indexLocation));
			
            DataElement status = dataStore.command(queryCmd, args, result);   

            //poll for progress information until the operation is done or canceled
            while (!status.getName().equals("done") && !status.getName().equals("cancelled") && !monitor.isCanceled()) { //$NON-NLS-1$ //$NON-NLS-2$
            	RemoteIndexerProgress progress = getIndexerProgress(status);
            	task.updateProgressInformation(progress);
            	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					RDTLog.logError(e);	
				}
            }
            
			try {
				try {
					smonitor.waitForUpdate(status, monitor);
				} catch (InterruptedException e) { // Canceled
					if (monitor.isCanceled()) 
						cancelOperation(status.getParent());
				}
			} catch (Exception e) {
				RDTLog.logError(e);	
			}
			
			if (status.getName().equals("done") || status.getName().equals("cancelled") || monitor.isCanceled()) { //$NON-NLS-1$//$NON-NLS-2$
				for (int i = 0; i < status.getNestedSize(); i ++ ){
					DataElement element = status.get(i);
			    	if (element != null && CDTMiner.T_INDEXING_ERROR.equals(element.getType())) { // Error occurred on the server
			    		String message = element.getAttribute(DE.A_NAME)+ ".  " + Messages.getString("RemoteCIndexSubsystem.11");  //$NON-NLS-1$//$NON-NLS-2$
			    		for (int j = 0; j < fErrorMessages.size(); j++) {
			    			if (message.indexOf(fErrorMessages.get(j)) > 0) {
					    		RDTLog.logWarning(message);
					    		reportProblem(scope, message);
			    			}
			    		}				    
			    	}
				}
			}
			monitor.done();
        }
	    
	    return Status.OK_STATUS;
	}
	
	
	protected void cancelOperation(DataElement command) {
		// send cancel command
		DataStore dataStore = command.getDataStore();
		DataElement cmdDescriptor = command.getDescriptor();
		DataElement cancelDescriptor = dataStore.localDescriptorQuery(cmdDescriptor, DataStoreSchema.C_CANCEL);
		if (cancelDescriptor != null) {
			dataStore.command(cancelDescriptor, command);
		}
	}
	
	
	protected void removeProblems(Scope scope) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject project = workspaceRoot.getProject(scope.getName());
		try {
			project.deleteMarkers("org.eclipse.ptp.rdt.ui.indexerproblemmarker", true, IResource.DEPTH_INFINITE); //$NON-NLS-1$
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
	}


	protected void reportProblem(Scope scope, String message) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject project = workspaceRoot.getProject(scope.getName());

		//parse for file name and line number
		int fileStart = message.indexOf("in file: "); //$NON-NLS-1$
		int fileEnd = message.indexOf(":", fileStart + 9); //$NON-NLS-1$
		String fileName = message.substring(fileStart + 9, fileEnd);
		
		int lineStart = fileEnd;
		int lineEnd = message.indexOf(".  Please", lineStart); //$NON-NLS-1$
		String lineNumber = message.substring(lineStart + 1, lineEnd);
		
		IFile file = null;
		String projectLocation = project.getLocationURI().getPath();
		fileStart = fileName.indexOf(projectLocation);
		if(fileStart == -1) {
			fileName = null;
		}
		else {
			fileName = fileName.substring(fileStart + projectLocation.length() + 1);
			IPath path = new Path(fileName);
			file = project.getFile(path);
		}
		
		
		if (file != null) {
			try {
				IMarker marker = file.createMarker("org.eclipse.ptp.rdt.ui.indexerproblemmarker"); //$NON-NLS-1$
				marker.setAttribute(IMarker.LINE_NUMBER, Integer.parseInt(lineNumber.replace(",", ""))); //$NON-NLS-1$ //$NON-NLS-2$
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				return;
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		} else {
			try {
				IMarker marker =  project.createMarker("org.eclipse.ptp.rdt.ui.indexerproblemmarker"); //$NON-NLS-1$
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#indexDelta(org.eclipse.ptp.internal.rdt.core.model.Scope, java.util.List, java.util.List, java.util.List, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus indexDelta(Scope scope, IRemoteIndexerInfoProvider provider, List<ICElement> newElements, 
			List<ICElement> changedElements, List<ICElement> deletedElements, IProgressMonitor monitor, RemoteIndexerTask task) {
		
		removeProblems(scope);
		DataStore dataStore = getDataStore();
		if(dataStore == null)
			return Status.OK_STATUS;

    	DataElement result = getDataStore().createObject(null, CDTMiner.T_INDEX_STATUS_DESCRIPTOR, "index"); //$NON-NLS-1$
     	StatusMonitor smonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(_connectorService, dataStore);
     	int workCount = newElements.size() + changedElements.size();
    	monitor.beginTask("Incrementally Indexing...", workCount); //$NON-NLS-1$
   
        DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_INDEX_DELTA);
        if (queryCmd != null) {
        	ArrayList<Object> args = new ArrayList<Object>();
        	
        	args.add(dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName()));
           	
           	String serializedProvider = null;
        	try {
				serializedProvider = Serializer.serialize(provider);
			} catch (IOException e) {
				RDTLog.logError(e);
			}
			
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_SCANNER_INFO_PROVIDER, serializedProvider));
			
           	for(ICElement element : newElements) {	
           		String remotePath = convertURIToRemotePath(element.getLocationURI());
               	args.add(dataStore.createObject(null, CDTMiner.T_INDEX_DELTA_ADDED, remotePath));
           	}
           	
           	for(ICElement element : changedElements) {	
           		String remotePath = convertURIToRemotePath(element.getLocationURI());
               	args.add(dataStore.createObject(null, CDTMiner.T_INDEX_DELTA_CHANGED, remotePath));
           	}
           	
           	for(ICElement element : deletedElements) {		
           		String remotePath = convertURIToRemotePath(element.getLocationURI());
               	args.add(dataStore.createObject(null, CDTMiner.T_INDEX_DELTA_REMOVED, remotePath));
           	}
        	
            DataElement status = dataStore.command(queryCmd, args, result);   
            
            //poll for progress information until the operation is done or canceled
            while (!status.getName().equals("done") && !status.getName().equals("cancelled") && !monitor.isCanceled()) { //$NON-NLS-1$ //$NON-NLS-2$
            	RemoteIndexerProgress progress = getIndexerProgress(status);
            	task.updateProgressInformation(progress);
            	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					RDTLog.logError(e);	
				}
            }
            
            try {
				try {
					smonitor.waitForUpdate(status, monitor);
				} catch (InterruptedException e) { // Canceled
					if (monitor.isCanceled()) 
						cancelOperation(status.getParent());
				}
            } catch(Exception e) {
            	RDTLog.logError(e);
            }
			
			if (status.getName().equals("done") || status.getName().equals("cancelled") || monitor.isCanceled()) { //$NON-NLS-1$//$NON-NLS-2$
				for (int i = 0; i < status.getNestedSize(); i ++ ){
					DataElement element = status.get(i);
					if (element != null && CDTMiner.T_INDEXING_ERROR.equals(element.getType())) { // Error occurred on the server
			    		String message = element.getAttribute(DE.A_NAME)+ ".  " + Messages.getString("RemoteCIndexSubsystem.11");  //$NON-NLS-1$//$NON-NLS-2$
			    		for (int j = 0; j < fErrorMessages.size(); j++) {
			    			if (message.indexOf(fErrorMessages.get(j)) > 0) {
					    		RDTLog.logWarning(message);
					    		reportProblem(scope, message);
			    			}
			    		}				    
			    	}
				}
			}

			monitor.done();
		}
	    
	    return Status.OK_STATUS;
	}
	
	
	private RemoteIndexerProgress getIndexerProgress(DataElement status) {
		int num = status.getNestedSize();
    	if (num > 0) {    	
    		boolean foundProgressInfo = false;
    		int counter = 1;
    		DataElement element = null;
    		while (!foundProgressInfo && counter <= num){
    			element = status.get(num-counter);
    			if(element != null && CDTMiner.T_INDEXER_PROGRESS_INFO.equals(element.getType())) {
    				foundProgressInfo = true;
    			}
    			counter++;
    		}			
			if(element != null && CDTMiner.T_INDEXER_PROGRESS_INFO.equals(element.getType())) {
	    		String data = element.getName();
	    		try
	    		{
	    			Object result = Serializer.deserialize(data);
	    			if (result == null || !(result instanceof RemoteIndexerProgress))
	    			{
	    				return null;
	    			}
	    			RemoteIndexerProgress info = (RemoteIndexerProgress) result;
	    			return info;
	    		} catch (IOException e) {
	    			RDTLog.logError(e);	
	    		} catch (ClassNotFoundException e) {
	    			RDTLog.logError(e);	
	    		}    		
			}
    	}
    	return null;
	}

	// scope management

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#registerScope(org.eclipse.ptp.internal.rdt.core.model.Scope, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus registerScope(Scope scope, List<ICElement> elements, String configLocation, IProgressMonitor monitor)
	{
		DataStore dataStore = getDataStore();
		   
	    if (dataStore != null)
	    {
	    	
	     	StatusMonitor smonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(getConnectorService(), dataStore);
	     	
	    	
	    	monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.3"), 100); //$NON-NLS-1$
	   
	        DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_SCOPE_REGISTER);
            if (queryCmd != null)
            {
                      	
            	ArrayList<Object> args = new ArrayList<Object>();
            	            	
            	// need to know the scope
            	DataElement scopeElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());
            	args.add(scopeElement);
            	
            	// need to know where to find the pdom file for the scope
            	DataElement configElement = dataStore.createObject(null, CDTMiner.T_SCOPE_CONFIG_LOCATION, configLocation);
            	args.add(configElement);
            
            	// add in the filenames
            	for(ICElement element : elements) {
            		addElement(dataStore, args, element);
            	}
            	
            	// execute the command
            	DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());
            	
            	try
                {
                	smonitor.waitForUpdate(status, monitor);
                }
                catch (Exception e)
                {                	
                	RDTLog.logError(e);
                }
            }	
	    }
	    
	    return Status.OK_STATUS;

	}


	/**
	 * @param dataStore
	 * @param args
	 * @param filePath
	 * @param element
	 */
	protected void addElement(DataStore dataStore, ArrayList<Object> args, ICElement element) {
		
   		String filePath = null;
		
		// if it's a translation unit, we can just add it
		if(element instanceof ITranslationUnit) {
			filePath = convertURIToRemotePath(element.getLocationURI());
		}
		
		else {
			// if it's a container or a project, add the child elements
			if(element instanceof ICContainer || element instanceof ICProject) {
				try {
					ICElement[] children = ((IParent) element).getChildren();
					
					for(int k = 0; k < children.length; k++)
						addElement(dataStore, args, children[k]);
					
				} catch (CModelException e) {
					RDTLog.logError(e);	
				}
			}
			
		}
		
		if (filePath != null) {
			DataElement fileElement = dataStore.createObject(null,
					CDTMiner.T_INDEX_FILENAME_DESCRIPTOR, filePath);
			args.add(fileElement);
		}
	}
	
	protected URI convertRemotePathToURI(String path) throws URISyntaxException {
		return new URI("rse", _host.getHostName(), path, null); //$NON-NLS-1$
	}
	
	protected String convertURIToRemotePath(URI locationURI) {
		// RSE URIs are of the form rse://host/path
		
		// it had better be an RSE URI
		assert(locationURI.getScheme().equals("rse")); //$NON-NLS-1$
		
		// the URI had better correspond to a location on the host that this subsystem is connected to
		assert(_host.getHostName().equals(locationURI.getHost()));
		
		return locationURI.getPath();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#unregisterScope(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus unregisterScope(Scope scope, IProgressMonitor monitor) {
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(scope.getName());
		fInitializedProjects.remove(project);
		
		DataStore dataStore = getDataStore();
		   
	    if (dataStore != null)
	    {
	     	
//	    	DataElement result = dataStore.createObject(null, CDTMiner.T_INDEX_STATUS_DESCRIPTOR, name);
//	    	DataElement statusDescriptor = dataStore.createObjectDescriptor(null, CDTMiner.T_INDEX_STATUS_DESCRIPTOR);
	    	
//	    	result.setDescriptor(statusDescriptor);
	    	
	     	StatusMonitor smonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(getConnectorService(), dataStore);
	     	
	    	
	    	monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.4"), 100); //$NON-NLS-1$
	   
	        DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_SCOPE_UNREGISTER);
            if (queryCmd != null)
            {
                      	
            	ArrayList<Object> args = new ArrayList<Object>();
            	            	
            	// need to know the scope
            	DataElement scopeElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());
            	args.add(scopeElement);
            
            	
            	// execute the command
            	//DataElement status = dataStore.command(queryCmd, dataStore.getDescriptorRoot(), true); 
            	DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());
            	
            	try
                {
                	smonitor.waitForUpdate(status, monitor);
                }
                catch (Exception e)
                {            
                	RDTLog.logError(e);
                }
            }
	    }
		
		return Status.OK_STATUS;
	}
	

	public IStatus removeIndexFile(Scope scope, IProgressMonitor monitor) {
		sendRequest(CDTMiner.C_REMOVE_INDEX_FILE, new Object[] {scope}, monitor);
		return Status.OK_STATUS;
	}	
	
	
	public String moveIndexFile(String scopeName, String newIndexLocation, IProgressMonitor monitor) {
		String actualLocation = sendRequestStringResult(CDTMiner.C_MOVE_INDEX_FILE, new Object[] {scopeName, newIndexLocation}, monitor);
		return actualLocation;
	}
	
	
	public OpenDeclarationResult openDeclaration(Scope scope, ITranslationUnit unit, String selectedText, int selectionStart, int selectionLength, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.9"), 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_NAVIGATION_OPEN_DECLARATION, new Object[] {scope, unit, selectedText, selectionStart, selectionLength}, monitor);
		if(result == null)
			return OpenDeclarationResult.failureUnexpectedError();
		return (OpenDeclarationResult)result;
	}
	
	
	// call hierarchy
	

	public CalledByResult getCallers(Scope scope, ICElement subject, IProgressMonitor monitor) {
    	monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.5") + subject, 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_CALLERS, new Object[] { scope, getHostName(), subject }, null);
		if (result == null) {
			return new CalledByResult();
		}
		return (CalledByResult) result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#getCallees(org.eclipse.ptp.internal.rdt.core.model.Scope, java.lang.String, java.lang.String, int, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CallsToResult getCallees(Scope scope, ICElement subject, IProgressMonitor monitor) {
    	monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.6") + subject, 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_CALLS, new Object[] { scope, getHostName(), subject }, null);
		if (result == null) {
			return new CallsToResult();
		}
		return (CallsToResult) result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#getCHDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope, java.lang.String, java.lang.String, int, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICElement[] getCHDefinitions(Scope scope, ICElement subject, IProgressMonitor monitor) {
    	monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.7") + subject, 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_ELEMENT, new Object[] { scope, getHostName(), subject }, null);
		if (result == null) {
			return new ICElement[0];
		}
		return (ICElement[]) result;
	}
	
	public ICElement[] getCHDefinitions(Scope scope, ITranslationUnit unit, int selectionStart, int selectionLength, IProgressMonitor monitor) {
    	monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.7") + unit, 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_WORKING_COPY, new Object[] { scope, getHostName(), unit, selectionStart, selectionLength }, null);
		if (result == null) {
			return new ICElement[0];
		}
		return (ICElement[]) result;
	}
	
	@SuppressWarnings("unchecked")
	public List<RemoteSearchMatch> runQuery(Scope scope, RemoteSearchQuery query, IProgressMonitor monitor) {
    	monitor.beginTask(Messages.getString("RemoteCIndexSubsystem.8") + query.getScopeDescription(), 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_SEARCH_RUN_QUERY, new Object[] { scope, getHostName(), query  }, null);
		if (result == null) {
			return Collections.emptyList();
		}
		return (List<RemoteSearchMatch>) result;
	}
	
	public List<Proposal> computeCompletionProposals(Scope scope, RemoteContentAssistInvocationContext context, ITranslationUnit unit) {
		DataStore dataStore = getDataStore();
	    if (dataStore == null)
	    {
	    	return Collections.emptyList();
	    }
	    
        DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_CONTENT_ASSIST_COMPUTE_PROPOSALS);
        
        if (queryCmd == null)
        {
	    	return Collections.emptyList();
        }

     	NullProgressMonitor monitor = new NullProgressMonitor();
     	StatusMonitor smonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(getConnectorService(), dataStore);
    	ArrayList<Object> args = new ArrayList<Object>();

    	// need to know the scope
    	DataElement dataElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());
    	args.add(dataElement);

    	// invocation context
    	args.add(createSerializableElement(dataStore, context));
    	
    	// translation unit
    	args.add(createSerializableElement(dataStore, unit));
    	
    	// execute the command
    	DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());
    	
    	try
        {
        	smonitor.waitForUpdate(status, monitor);
        }
        catch (Exception e)
        {
        	RDTLog.logError(e);	
        }
    	
    	DataElement element = status.get(0);
    	String data = element.getName();
		try
		{
			Object result = Serializer.deserialize(data);
			if (result == null || !(result instanceof List))
			{
				return Collections.emptyList();
			}
			return (List<Proposal>) result;
		} catch (IOException e) {
			RDTLog.logError(e);	
		} catch (ClassNotFoundException e) {
			RDTLog.logError(e);	
		}
    	return Collections.emptyList();
	}
	
	public THGraph computeTypeGraph(Scope scope, ICElement input, IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_TYPE_HIERARCHY_COMPUTE_TYPE_GRAPH, new Object[] { scope, getHostName(), input }, monitor);
		if (result == null) {
			return new THGraph();
		}
		return (THGraph) result;
	}
	
	public ICElement[] findTypeHierarchyInput(Scope scope, ICElement memberInput) {
		Object result = sendRequest(CDTMiner.C_TYPE_HIERARCHY_FIND_INPUT1, new Object[] { scope, getHostName(), memberInput }, null);
		if (result == null) {
			return new ICElement[] { null, null };
		}
		return (ICElement[]) result;
	}
	
	public ICElement[] findTypeHierarchyInput(Scope scope, ITranslationUnit unit, int selectionStart, int selectionLength) {
		Object result = sendRequest(CDTMiner.C_TYPE_HIERARCHY_FIND_INPUT2, new Object[] { scope, getHostName(), unit, new Integer(selectionStart), new Integer(selectionLength)}, null);
		if (result == null) {
			return new ICElement[] { null, null };
		}
		return (ICElement[]) result;
	}
	
	
	public Object sendRequest(String requestType, Object[] arguments, IProgressMonitor monitor) {
		return sendRequest(requestType, arguments, monitor, true);
	}
	
	public String sendRequestStringResult(String requestType, Object[] arguments, IProgressMonitor monitor) {
		return (String) sendRequest(requestType, arguments, monitor, false);
	}
	
	/**
	 * Sends a request in a set format of arguments.
	 * 
	 * @param deserializeResult If true the result will be deserialized, if false it will treat the result as a raw string.
	 */
	private Object sendRequest(String requestType, Object[] arguments, IProgressMonitor monitor, boolean deserializeResult) {
		DataStore dataStore = getDataStore();
	    if (dataStore == null)
	    	return null;
	    
        DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), requestType);
        if (queryCmd == null)
	    	return null;        

     	StatusMonitor statusMonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(getConnectorService(), dataStore);
    	ArrayList<Object> args = new ArrayList<Object>();

    	for (Object argument : arguments) {
    		if (argument instanceof Scope) {
    	    	DataElement dataElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, ((Scope) argument).getName());
    	    	args.add(dataElement);
    		} else if (argument instanceof String) {
            	DataElement dataElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, (String) argument);
            	args.add(dataElement);
    		} else if (argument instanceof Integer
    				|| argument instanceof Boolean
    				|| argument instanceof Character
    				|| argument instanceof Double
    				|| argument instanceof Float) {
            	DataElement dataElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, argument.toString());
            	args.add(dataElement);
    		} else {
    	    	args.add(createSerializableElement(dataStore, argument));
    		}
    	}
    	
    	// execute the command
    	DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());
    	
    	try
        {
    		monitor = monitor == null ? new NullProgressMonitor() : monitor;
        	statusMonitor.waitForUpdate(status, monitor);
        }
        catch (Exception e) {
        	RDTLog.logError(e);	
        }
    	
    	DataElement element = status.get(0);
    	if (element == null) {
    		return null;
    	}
    	
    	if(DataStoreResources.model_error.equals(element.getType())) { // Error occurred on the server
    		RDTLog.logError(status.getValue()); // prints the server error stack trace to the log
    		return null;
    	}
    	
    	String data = element.getName();
    	if(!deserializeResult)
    		return data;
    	
		try
		{
			Object result = Serializer.deserialize(data);
			return result;
		} catch (IOException e) {
			RDTLog.logError(e);	
		} catch (ClassNotFoundException e) {
			RDTLog.logError(e);	
		}
    	return null;
	}
	
	private DataElement createSerializableElement(DataStore dataStore, Object object) {
    	try {
        	String serialized = Serializer.serialize(object);
        	return dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, serialized);
    	} catch (IOException e) {
    		RDTLog.logError(e);	
    		return null;
    	}
	}

	protected DataStore getDataStore()
	{
		IConnectorService connectorService = getConnectorService();
		
		if(connectorService instanceof DStoreConnectorService) {
			return ((DStoreConnectorService) connectorService).getDataStore();

		}
		return null;
	}
	
	public void checkAllProjects(IProgressMonitor monitor) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();

		for (IProject project : workspaceRoot.getProjects()) {
			// is the project open? if not, there's not much we can do
			if (!project.isOpen())
				continue;

			// is this an RDT C/C++ project?
			// check the project natures... we care about the project if it has
			// both the remote nature and
			// at least one of the CDT natures
			try {
				if (!project.hasNature(RemoteNature.REMOTE_NATURE_ID)
						|| !(project.hasNature(CProjectNature.C_NATURE_ID)
						|| project.hasNature(CCProjectNature.CC_NATURE_ID)))
					continue;
				
				checkProject(project, monitor);
			} catch (Throwable e) {
				RDTLog.logError(e);	
			}
		}
	}
		
	public void checkProject(IProject project, IProgressMonitor monitor) {
		if (project == null){ 
			return;
		}
		
		if(fInitializedProjects.containsKey(project)){
			String projectURI = project.getLocationURI().toString();
			if(projectURI.equals(fInitializedProjects.get(project))){
				return;
			}else{
				//the project's uri is changed, so we need to initialize it again.
				//no need to unregister its scope, since initializeScope handles it.
			}
		}
		try {
			initializeScope(project, monitor);
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
	}

	private void initializeScope(IProject project, IProgressMonitor monitor) throws CoreException {
		// get the service model configuration for this project
		final ServiceModelManager serviceModelManager = ServiceModelManager.getInstance();
		IServiceConfiguration config = serviceModelManager.getActiveConfiguration(project);

		// is the indexing service associated with our service provider?
		IService service = serviceModelManager.getService(RemoteCIndexServiceProvider.SERVICE_ID);
		IServiceProvider provider = config.getServiceProvider(service);


		// if so, initialize a scope for the project consisting of all
		// its translation units
		final List<ICElement> cElements = new LinkedList<ICElement>();

		IResourceVisitor fileCollector = new IResourceVisitor() {

			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					// add the path
					ITranslationUnit tu = CoreModelUtil.findTranslationUnit((IFile) resource);
					if (tu != null) {
						cElements.add(tu);
						return false;
					}
				}
				return true;
			}
		};

		// collect the translation units
		project.accept(fileCollector);

		String configLocation = ((IIndexServiceProvider)provider).getIndexLocation();
		Scope scope = new Scope(project.getName());

		// unregister the scope if there already is one
		unregisterScope(scope, monitor);

		// register the new scope
		registerScope(scope, cElements, configLocation, monitor);
		
		String projectURI = project.getLocationURI().toString();
		fInitializedProjects.put(project, projectURI);

	}



	public IIndexIncludeValue[] findIncludesTo(Scope scope, IIndexFileLocation location, IProgressMonitor monitor)
	{
		Object result = sendRequest(CDTMiner.C_INCLUDES_FIND_INCLUDES_TO, new Object[] { scope, getHostName(), location }, monitor);
		if (result == null) 
		{
			return new IIndexIncludeValue[0];
		}
	
		return (IIndexIncludeValue[]) result;
	}

	public IIndexIncludeValue[] findIncludedBy(Scope scope, IIndexFileLocation location, IProgressMonitor monitor)
	{
		Object result = sendRequest(CDTMiner.C_INCLUDES_FIND_INCLUDED_BY, new Object[] { scope, getHostName(), location }, monitor);
		if (result == null) 
		{
			return new IIndexIncludeValue[0];
		}
	
		return (IIndexIncludeValue[]) result;
	}


	public boolean isIndexed(Scope scope, IIndexFileLocation location, IProgressMonitor monitor)
	{
		Object result = sendRequest(CDTMiner.C_INCLUDES_IS_INDEXED, new Object[] { scope, getHostName(), location }, monitor);
		if (result != null) 
		{
			return Boolean.parseBoolean(result.toString());
		}
		
		return false;
	}
	
	public IIndexIncludeValue findInclude(Scope scope, IIndexFileLocation location, String name, int offset, IProgressMonitor monitor)
	{
		Object result = sendRequest(CDTMiner.C_INCLUDES_FIND_INCLUDE, new Object[] { scope, getHostName(), location, name, offset}, monitor);
		if (result == null) 
		{
			return null;
		}
	
		return (IIndexIncludeValue) result;
	}
	
	public ITranslationUnit getModel(ITranslationUnit unit, IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_MODEL_BUILDER, new Object[] {unit}, monitor);
		if (result == null) 
		{
			return null;
		}
		
		//the working copy	
		return (ITranslationUnit) result;
	}
	
}
