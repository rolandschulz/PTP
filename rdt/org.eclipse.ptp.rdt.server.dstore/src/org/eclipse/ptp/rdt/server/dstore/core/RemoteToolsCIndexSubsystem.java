/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.server.dstore.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserMessages;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.RemoteProjectResourcesUtil;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CalledByResult;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallsToResult;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteDefaultCodeFormatterOptions;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteMultiTextEdit;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteReplaceEdit;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteTextEdit;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIndexIncludeValue;
import org.eclipse.ptp.internal.rdt.core.index.IRemoteFastIndexerUpdateEvent;
import org.eclipse.ptp.internal.rdt.core.index.IRemoteFastIndexerUpdateEvent.EventType;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerProgress;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask;
import org.eclipse.ptp.internal.rdt.core.miners.CDTMiner;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.navigation.FoldingRegionsResult;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraph;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.subsystems.ProjectChangeListener;
import org.eclipse.ptp.rdt.ui.subsystems.StatusMonitor;
import org.eclipse.ptp.remote.core.server.RemoteServerManager;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * An Remote Tools subsystem which is used to provide C/C++ indexing services
 * from a Miner running on a remote host. <strong>EXPERIMENTAL</strong>. This
 * class or interface has been added as part of a work in progress. There is no
 * guarantee that this API will work or that it will remain the same. Please do
 * not use this API without consulting with the RDT team.
 * 
 * @author crecoskie
 * 
 */
public class RemoteToolsCIndexSubsystem implements ICIndexSubsystem {

	private final Map<IProject, String> fInitializedProjects = new HashMap<IProject, String>();
	private final ProjectChangeListener fProjectOpenListener = new ProjectChangeListener(this);
	private final RemoteToolsCIndexServiceProvider fProvider;
	private final List<String> fErrorMessages = new ArrayList<String>();
	private DStoreServer fDStoreServer = null;

	public RemoteToolsCIndexSubsystem(RemoteToolsCIndexServiceProvider provider) {
		fProvider = provider;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fProjectOpenListener);
		generateErrorMessages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#
	 * checkAllProjects(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void checkAllProjects(IProgressMonitor monitor) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();

		for (IProject project : workspaceRoot.getProjects()) {
			// is the project open? if not, there's not much we can do
			if (!project.isOpen()) {
				continue;
			}

			// is this an RDT C/C++ project?
			// check the project natures... we care about the project if it has
			// both the remote nature and
			// at least one of the CDT natures
			try {
				if (!project.hasNature(RemoteNature.REMOTE_NATURE_ID)
						|| !(project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID))) {
					continue;
				}

				checkProject(project, monitor);
			} catch (Throwable e) {
				RDTLog.logError(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#checkProject
	 * (org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void checkProject(IProject project, IProgressMonitor monitor) {
		if (project == null) {
			return;
		}

		if (fInitializedProjects.containsKey(project)) {
			String projectURI = project.getLocationURI().toString();
			if (projectURI.equals(fInitializedProjects.get(project))) {
				return;
			} else {
				// the project's uri is changed, so we need to initialize it
				// again.
				// no need to unregister its scope, since initializeScope
				// handles it.
			}
		}
		try {
			initializeScope(project, monitor);
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#
	 * computeCompletionProposals(org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.ptp.internal.rdt.core.contentassist.
	 * RemoteContentAssistInvocationContext,
	 * org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	public List<Proposal> computeCompletionProposals(Scope scope, RemoteContentAssistInvocationContext context,
			ITranslationUnit unit) {
		checkAllProjects(new NullProgressMonitor());
		String path = EFSExtensionManager.getDefault().getPathFromURI(unit.getLocationURI());
		DataStore dataStore = getDataStore(null);
		if (dataStore == null) {
			return Collections.emptyList();
		}

		DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(),
				CDTMiner.C_CONTENT_ASSIST_COMPUTE_PROPOSALS);

		if (queryCmd == null) {
			return Collections.emptyList();
		}

		NullProgressMonitor monitor = new NullProgressMonitor();
		StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);
		ArrayList<Object> args = new ArrayList<Object>();

		// need to know the scope
		DataElement dataElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());
		args.add(dataElement);

		// invocation context
		args.add(createSerializableElement(dataStore, context));

		// translation unit
		args.add(createSerializableElement(dataStore, unit));

		// path to translation unit
		dataElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, path);
		args.add(dataElement);

		// execute the command
		DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());

		try {
			smonitor.waitForUpdate(status, monitor);
		} catch (Exception e) {
			RDTLog.logError(e);
		}

		DataElement element = status.get(0);
		String data = element.getName();
		try {
			Object result = Serializer.deserialize(data);
			if (result == null || !(result instanceof List<?>)) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#
	 * computeTypeGraph(org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.model.ICElement,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public THGraph computeTypeGraph(Scope scope, ICElement input, IProgressMonitor monitor) {
		String path = EFSExtensionManager.getDefault().getPathFromURI(input.getLocationURI());
		Object result = sendRequest(CDTMiner.C_TYPE_HIERARCHY_COMPUTE_TYPE_GRAPH,
				new Object[] { scope, getHostName(), input, path }, monitor);
		if (result == null) {
			return new THGraph();
		}
		return (THGraph) result;
	}

	/**
	 * Shutdown and clean up the subsystem
	 */
	public void dispose() {
		if (fDStoreServer != null) {
			fDStoreServer.cancel();
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fProjectOpenListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#findInclude
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.index.IIndexFileLocation, java.lang.String, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IIndexIncludeValue findInclude(Scope scope, IIndexFileLocation location, String name, int offset,
			IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_INCLUDES_FIND_INCLUDE,
				new Object[] { scope, getHostName(), location, name, offset }, monitor);
		if (result == null) {
			return null;
		}

		return (IIndexIncludeValue) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#findIncludedBy
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.index.IIndexFileLocation,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IIndexIncludeValue[] findIncludedBy(Scope scope, IIndexFileLocation location, IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_INCLUDES_FIND_INCLUDED_BY, new Object[] { scope, getHostName(), location }, monitor);
		if (result == null) {
			return new IIndexIncludeValue[0];
		}

		return (IIndexIncludeValue[]) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#findIncludesTo
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.index.IIndexFileLocation,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IIndexIncludeValue[] findIncludesTo(Scope scope, IIndexFileLocation location, IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_INCLUDES_FIND_INCLUDES_TO, new Object[] { scope, getHostName(), location }, monitor);
		if (result == null) {
			return new IIndexIncludeValue[0];
		}

		return (IIndexIncludeValue[]) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#
	 * findTypeHierarchyInput(org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.model.ICElement)
	 */
	public ICElement[] findTypeHierarchyInput(Scope scope, ICElement memberInput) {
		String path = EFSExtensionManager.getDefault().getPathFromURI(memberInput.getLocationURI());
		Object result = sendRequest(CDTMiner.C_TYPE_HIERARCHY_FIND_INPUT1,
				new Object[] { scope, getHostName(), memberInput, path }, null);
		if (result == null) {
			return new ICElement[] { null, null };
		}
		return (ICElement[]) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#
	 * findTypeHierarchyInput(org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.model.ITranslationUnit, int, int)
	 */
	public ICElement[] findTypeHierarchyInput(Scope scope, ITranslationUnit unit, int selectionStart, int selectionLength) {
		String path = EFSExtensionManager.getDefault().getPathFromURI(unit.getLocationURI());
		Object result = sendRequest(CDTMiner.C_TYPE_HIERARCHY_FIND_INPUT2, new Object[] { scope, getHostName(), unit, path,
				new Integer(selectionStart), new Integer(selectionLength) }, null);
		if (result == null) {
			return new ICElement[] { null, null };
		}
		return (ICElement[]) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#getCallees
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope, java.lang.String,
	 * java.lang.String, int, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CallsToResult getCallees(Scope scope, ICElement subject, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.6") + subject, 100); //$NON-NLS-1$
		String path = EFSExtensionManager.getDefault().getPathFromURI(subject.getLocationURI());
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_CALLS, new Object[] { scope, getHostName(), subject, path }, null);
		if (result == null) {
			return new CallsToResult();
		}
		return (CallsToResult) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#getCallers
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.model.ICElement,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CalledByResult getCallers(Scope scope, ICElement subject, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.5") + subject, 100); //$NON-NLS-1$
		String path = EFSExtensionManager.getDefault().getPathFromURI(subject.getLocationURI());
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_CALLERS, new Object[] { scope, getHostName(), subject, path },
				null);
		if (result == null) {
			return new CalledByResult();
		}
		return (CalledByResult) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#
	 * getCHDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * java.lang.String, java.lang.String, int, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICElement[] getCHDefinitions(Scope scope, ICElement subject, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.7") + subject, 100); //$NON-NLS-1$
		String path = EFSExtensionManager.getDefault().getPathFromURI(subject.getLocationURI());
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_ELEMENT, new Object[] { scope, getHostName(),
				subject, path }, null);
		if (result == null) {
			return new ICElement[0];
		}
		return (ICElement[]) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#
	 * getCHDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.model.ITranslationUnit, int, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICElement[] getCHDefinitions(Scope scope, ITranslationUnit unit, int selectionStart, int selectionLength,
			IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.7") + unit, 100); //$NON-NLS-1$
		String path = EFSExtensionManager.getDefault().getPathFromURI(unit.getLocationURI());
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_WORKING_COPY, new Object[] { scope,
				getHostName(), unit, path, selectionStart, selectionLength }, null);
		if (result == null) {
			return new ICElement[0];
		}
		return (ICElement[]) result;
	}

	/**
	 * @since 2.0
	 */
	public Map<String, ICElement[]> findOverriders(Scope scope, ICElement subject, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.7") + subject, 100); //$NON-NLS-1$
		String path = EFSExtensionManager.getDefault().getPathFromURI(subject.getLocationURI());
		Object result = sendRequest(CDTMiner.C_CALL_HIERARCHY_GET_OVERRIDERS, new Object[] { scope, getHostName(), subject, path },
				null);
		if (result == null) {
			return new HashMap<String, ICElement[]>();
		}
		return (Map<String, ICElement[]>) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#getModel
	 * (org.eclipse.cdt.core.model.ITranslationUnit,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITranslationUnit getModel(ITranslationUnit unit, IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_MODEL_BUILDER, new Object[] { unit }, monitor);
		if (result == null) {
			return null;
		}

		// the working copy
		return (ITranslationUnit) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#indexDelta
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope, java.util.List,
	 * java.util.List, java.util.List,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus indexDelta(Scope scope, IRemoteIndexerInfoProvider provider, List<ICElement> newElements,
			List<ICElement> changedElements, List<ICElement> deletedElements, IProgressMonitor monitor, RemoteIndexerTask task) {

		removeProblems(scope);
		DataStore dataStore = getDataStore(monitor);
		if (dataStore == null) {
			return Status.OK_STATUS;
		}

		DataElement result = getDataStore(monitor).createObject(null, CDTMiner.T_INDEX_STATUS_DESCRIPTOR, "index"); //$NON-NLS-1$
		StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);
		int workCount = newElements.size() + changedElements.size();
		monitor.beginTask("Incrementally Indexing...", workCount); //$NON-NLS-1$

		DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_INDEX_DELTA);
		if (queryCmd != null) {
			ArrayList<Object> args = new ArrayList<Object>();

			args.add(dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName()));
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getScheme()));
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getRootPath()));
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getMappedPath()));
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getHost()));

			String serializedProvider = null;
			try {
				serializedProvider = Serializer.serialize(provider);
			} catch (IOException e) {
				RDTLog.logError(e);
			}

			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_SCANNER_INFO_PROVIDER, serializedProvider));

			for (ICElement element : newElements) {
				String remotePath = convertURIToRemotePath(element.getLocationURI());
				args.add(dataStore.createObject(null, CDTMiner.T_INDEX_DELTA_ADDED, remotePath));
			}

			for (ICElement element : changedElements) {
				String remotePath = convertURIToRemotePath(element.getLocationURI());
				args.add(dataStore.createObject(null, CDTMiner.T_INDEX_DELTA_CHANGED, remotePath));
			}

			for (ICElement element : deletedElements) {
				String remotePath = convertURIToRemotePath(element.getLocationURI());
				args.add(dataStore.createObject(null, CDTMiner.T_INDEX_DELTA_REMOVED, remotePath));
			}

			DataElement status = dataStore.command(queryCmd, args, result);

			// poll for progress information until the operation is done or
			// canceled
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
					if (monitor.isCanceled()) {
						cancelOperation(status.getParent());
					}
				}
			} catch (Exception e) {
				RDTLog.logError(e);
			}

			if (status.getName().equals("done") || status.getName().equals("cancelled") || monitor.isCanceled()) { //$NON-NLS-1$//$NON-NLS-2$
				for (int i = 0; i < status.getNestedSize(); i++) {
					DataElement element = status.get(i);
					if (element != null && CDTMiner.T_INDEXING_ERROR.equals(element.getType())) {
						// Error occurred on the server
						String message = element.getAttribute(DE.A_NAME) + ".  "; //$NON-NLS-1$
						for (int j = 0; j < fErrorMessages.size(); j++) {
							if (message.indexOf(fErrorMessages.get(j)) > 0) {
								String msg = reportProblem(scope, message);
								RDTLog.logWarning(msg);
							}
						}
					}
				}
			}

			monitor.done();
		}

		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#isIndexed
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.index.IIndexFileLocation,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean isIndexed(Scope scope, IIndexFileLocation location, IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_INCLUDES_IS_INDEXED, new Object[] { scope, getHostName(), location }, monitor);
		if (result != null) {
			return Boolean.parseBoolean(result.toString());
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#moveIndexFile
	 * (java.lang.String, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String moveIndexFile(String scopeName, String newIndexLocation, IProgressMonitor monitor) {
		String actualLocation = sendRequestStringResult(CDTMiner.C_MOVE_INDEX_FILE, new Object[] { scopeName, newIndexLocation },
				monitor);
		return actualLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#openDeclaration
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.cdt.core.model.ITranslationUnit, java.lang.String, int, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OpenDeclarationResult openDeclaration(Scope scope, ITranslationUnit unit, String selectedText, int selectionStart,
			int selectionLength, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.9"), 100); //$NON-NLS-1$
		String path = EFSExtensionManager.getDefault().getPathFromURI(unit.getLocationURI());
		Object result = sendRequest(CDTMiner.C_NAVIGATION_OPEN_DECLARATION, new Object[] { scope, unit, path, selectedText,
				selectionStart, selectionLength }, monitor);
		if (result == null) {
			return OpenDeclarationResult.failureUnexpectedError();
		}
		return (OpenDeclarationResult) result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#registerScope
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope, java.lang.String[],
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus registerScope(Scope scope, List<ICElement> elements, String configLocation, IProgressMonitor monitor) {
		DataStore dataStore = getDataStore(monitor);

		if (dataStore != null) {

			StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);

			monitor.beginTask(Messages.getString("RSECIndexSubsystem.3"), 100); //$NON-NLS-1$

			DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_SCOPE_REGISTER);
			if (queryCmd != null) {

				ArrayList<Object> args = new ArrayList<Object>();

				// need to know the scope
				DataElement scopeElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());
				args.add(scopeElement);

				// scheme for scope
				DataElement dataElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getScheme());
				args.add(dataElement);

				// host
				DataElement hostElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getHost());
				args.add(hostElement);

				// root path for scope on server
				DataElement rootPath = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getRootPath());
				args.add(rootPath);

				// mapped path for scope on local machine
				DataElement mappedPath = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getMappedPath());
				args.add(mappedPath);

				// need to know where to find the pdom file for the scope
				DataElement configElement = dataStore.createObject(null, CDTMiner.T_SCOPE_CONFIG_LOCATION, configLocation);
				args.add(configElement);

				// add in the filenames
				for (ICElement element : elements) {
					addElement(dataStore, args, element);
				}

				// execute the command
				DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());

				try {
					smonitor.waitForUpdate(status, monitor);
				} catch (Exception e) {
					RDTLog.logError(e);
				}
			}
		}

		return Status.OK_STATUS;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#reindexScope
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider,
	 * org.eclipse.core.runtime.IProgressMonitor,
	 * org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask)
	 */
	public IStatus reindexScope(Scope scope, IRemoteIndexerInfoProvider provider, String indexLocation, IProgressMonitor monitor,
			RemoteIndexerTask task) {
		removeProblems(scope);
		DataStore dataStore = getDataStore(monitor);
		if (dataStore == null) {
			return Status.OK_STATUS;
		}

		DataElement result = getDataStore(monitor).createObject(null, CDTMiner.T_INDEX_STATUS_DESCRIPTOR, "index"); //$NON-NLS-1$
		StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);
		monitor.beginTask("Rebuilding indexing...", 100); //$NON-NLS-1$

		DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_INDEX_REINDEX);
		if (queryCmd != null) {
			ArrayList<Object> args = new ArrayList<Object>();

			args.add(dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName()));
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getScheme()));
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getRootPath()));
			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, scope.getMappedPath()));
			args.add(dataStore.createObject(null, CDTMiner.T_SCOPE_CONFIG_LOCATION, indexLocation));

			String serializedProvider = null;
			try {
				serializedProvider = Serializer.serialize(provider);
			} catch (IOException e) {
				RDTLog.logError(e);
			}

			args.add(dataStore.createObject(null, CDTMiner.T_INDEX_SCANNER_INFO_PROVIDER, serializedProvider));

			DataElement status = dataStore.command(queryCmd, args, result);

			// poll for progress information until the operation is done or
			// canceled
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
					if (monitor.isCanceled()) {
						cancelOperation(status.getParent());
					}
				}
			} catch (Exception e) {
				RDTLog.logError(e);
			}

			if (status.getName().equals("done") || status.getName().equals("cancelled") || monitor.isCanceled()) { //$NON-NLS-1$//$NON-NLS-2$
				for (int i = 0; i < status.getNestedSize(); i++) {
					DataElement element = status.get(i);
					if (element != null && CDTMiner.T_INDEXING_ERROR.equals(element.getType())) {
						// Error occurred on the server
						String message = element.getAttribute(DE.A_NAME) + ".  "; //$NON-NLS-1$
						for (int j = 0; j < fErrorMessages.size(); j++) {
							if (message.indexOf(fErrorMessages.get(j)) > 0) {
								String msg = reportProblem(scope, message);
								RDTLog.logWarning(msg);
							}
						}
					}
				}
			}
			monitor.done();
		}

		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#removeIndexFile
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus removeIndexFile(Scope scope, IProgressMonitor monitor) {
		sendRequest(CDTMiner.C_REMOVE_INDEX_FILE, new Object[] { scope }, monitor);
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#runQuery
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	public List<RemoteSearchMatch> runQuery(Scope scope, RemoteSearchQuery query, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.8") + query.getScopeDescription(), 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_SEARCH_RUN_QUERY, new Object[] { scope, getHostName(), query }, null);
		if (result == null) {
			return Collections.emptyList();
		}
		return (List<RemoteSearchMatch>) result;
	}

	/**
	 * @see org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#runQuery2
	 * @since 2.0
	 */
	public RemoteSearchQuery runQuery2(Scope scope, RemoteSearchQuery query, IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("RSECIndexSubsystem.8") + query.getScopeDescription(), 100); //$NON-NLS-1$
		Object result = sendRequest(CDTMiner.C_SEARCH_RUN_QUERY2, new Object[] { scope, getHostName(), query }, null);
		if (result == null) {
			return null;
		}
		return (RemoteSearchQuery) result;
	}

	/**
	 * @param requestType
	 * @param arguments
	 * @param monitor
	 * @return
	 */
	public Object sendRequest(String requestType, Object[] arguments, IProgressMonitor monitor) {
		return sendRequest(requestType, arguments, monitor, true);
	}

	/**
	 * @param requestType
	 * @param arguments
	 * @param monitor
	 * @return
	 */
	public String sendRequestStringResult(String requestType, Object[] arguments, IProgressMonitor monitor) {
		return (String) sendRequest(requestType, arguments, monitor, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem#unregisterScope
	 * (org.eclipse.ptp.internal.rdt.core.model.Scope,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus unregisterScope(Scope scope, IProgressMonitor monitor) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(scope.getName());
		fInitializedProjects.remove(project);

		DataStore dataStore = getDataStore(monitor);

		if (dataStore != null) {

			// DataElement result = dataStore.createObject(null,
			// CDTMiner.T_INDEX_STATUS_DESCRIPTOR, name);
			// DataElement statusDescriptor =
			// dataStore.createObjectDescriptor(null,
			// CDTMiner.T_INDEX_STATUS_DESCRIPTOR);

			// result.setDescriptor(statusDescriptor);

			StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);

			monitor.beginTask(Messages.getString("RSECIndexSubsystem.4"), 100); //$NON-NLS-1$

			DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), CDTMiner.C_SCOPE_UNREGISTER);
			if (queryCmd != null) {

				ArrayList<Object> args = new ArrayList<Object>();

				// need to know the scope
				DataElement scopeElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());
				args.add(scopeElement);

				// execute the command
				// DataElement status = dataStore.command(queryCmd,
				// dataStore.getDescriptorRoot(), true);
				DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());

				try {
					smonitor.waitForUpdate(status, monitor);
				} catch (Exception e) {
					RDTLog.logError(e);
				}
			}
		}

		return Status.OK_STATUS;
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

	private void generateErrorMessages() {
		//    	fErrorMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.inclusionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
		//    	fErrorMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.definitionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
		fErrorMessages.add("Unresolved inclusion:"); //$NON-NLS-1$
		fErrorMessages.add("Macro definition not found:"); //$NON-NLS-1$
	}

	private RemoteIndexerProgress getIndexerProgress(DataElement status) {
		int num = status.getNestedSize();
		if (num > 0) {
			boolean foundProgressInfo = false;
			int counter = 1;
			DataElement element = null;
			while (!foundProgressInfo && counter <= num) {
				element = status.get(num - counter);
				if (element != null && CDTMiner.T_INDEXER_PROGRESS_INFO.equals(element.getType())) {
					foundProgressInfo = true;
				}
				counter++;
			}
			if (element != null && CDTMiner.T_INDEXER_PROGRESS_INFO.equals(element.getType())) {
				String data = element.getName();
				try {
					Object result = Serializer.deserialize(data);
					if (result == null || !(result instanceof RemoteIndexerProgress)) {
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

	private void initializeScope(IProject project, IProgressMonitor monitor) throws CoreException {
		// get the service model configuration for this project
		final ServiceModelManager serviceModelManager = ServiceModelManager.getInstance();
		IServiceConfiguration config = serviceModelManager.getActiveConfiguration(project);

		// is the indexing service associated with our service provider?
		IService service = serviceModelManager.getService(RemoteToolsCIndexServiceProvider.SERVICE_ID);
		IServiceProvider provider = config.getServiceProvider(service);

		// if so, initialize a scope for the project consisting of all
		// its translation units
		final List<ICElement> cElements = RemoteProjectResourcesUtil.getCElements(project);

		String configLocation = ((IIndexServiceProvider) provider).getIndexLocation();
		Scope scope = new Scope(project);

		// unregister the scope if there already is one
		unregisterScope(scope, monitor);

		// register the new scope
		registerScope(scope, cElements, configLocation, monitor);

		String projectURI = project.getLocationURI().toString();
		fInitializedProjects.put(project, projectURI);

	}

	/**
	 * Sends a request in a set format of arguments.
	 * 
	 * @param deserializeResult
	 *            If true the result will be deserialized, if false it will
	 *            treat the result as a raw string.
	 */
	private Object sendRequest(String requestType, Object[] arguments, IProgressMonitor monitor, boolean deserializeResult) {
		DataStore dataStore = getDataStore(monitor);
		if (dataStore == null) {
			return null;
		}

		DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), requestType);
		if (queryCmd == null) {
			return null;
		}

		StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);
		ArrayList<Object> args = new ArrayList<Object>();

		for (Object argument : arguments) {
			if (argument instanceof Scope) {
				DataElement dataElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR,
						((Scope) argument).getName());
				args.add(dataElement);

				dataElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, ((Scope) argument).getScheme());
				args.add(dataElement);

				// root path for scope on server
				DataElement rootPath = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR,
						((Scope) argument).getRootPath());
				args.add(rootPath);

				// path mappings for scope
				DataElement pathElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR,
						((Scope) argument).getMappedPath());
				args.add(pathElement);
			} else if (argument instanceof String) {
				DataElement dataElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, (String) argument);
				args.add(dataElement);
			} else if (argument instanceof Integer || argument instanceof Boolean || argument instanceof Character
					|| argument instanceof Double || argument instanceof Float) {
				DataElement dataElement = dataStore.createObject(null, CDTMiner.T_INDEX_STRING_DESCRIPTOR, argument.toString());
				args.add(dataElement);
			} else {
				args.add(createSerializableElement(dataStore, argument));
			}
		}

		// execute the command
		DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());

		try {
			monitor = monitor == null ? new NullProgressMonitor() : monitor;
			smonitor.waitForUpdate(status, monitor);
		} catch (Exception e) {
			RDTLog.logError(e);
		}

		DataElement element = status.get(0);
		if (element == null) {
			return null;
		}

		if (DataStoreResources.model_error.equals(element.getType())) {
			// Error occurred on the server
			RDTLog.logError(status.getValue()); // prints the server error stack
												// trace to the log
			return null;
		}

		String data = element.getName();
		if (!deserializeResult) {
			return data;
		}

		try {
			Object result = Serializer.deserialize(data);
			return result;
		} catch (IOException e) {
			RDTLog.logError(e);
		} catch (ClassNotFoundException e) {
			RDTLog.logError(e);
		}
		return null;
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
		if (element instanceof ITranslationUnit) {
			filePath = convertURIToRemotePath(element.getLocationURI());
		}

		else {
			// if it's a container or a project, add the child elements
			if (element instanceof ICContainer || element instanceof ICProject) {
				try {
					ICElement[] children = ((IParent) element).getChildren();

					for (ICElement element2 : children) {
						addElement(dataStore, args, element2);
					}

				} catch (CModelException e) {
					RDTLog.logError(e);
				}
			}

		}

		if (filePath != null) {
			DataElement fileElement = dataStore.createObject(null, CDTMiner.T_INDEX_FILENAME_DESCRIPTOR, filePath);
			args.add(fileElement);
		}
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

	protected URI convertRemotePathToURI(String path) throws URISyntaxException {
		return fProvider.getConnection().getRemoteServices().getFileManager(fProvider.getConnection()).toURI(path);
	}

	protected String convertURIToRemotePath(URI locationURI) {
		String path = EFSExtensionManager.getDefault().getPathFromURI(locationURI);
		return path;
	}

	protected String getHostName() {
		return fProvider.getConnectionName();
	}

	protected DataStore getDataStore(IProgressMonitor monitor) {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		try {
			if (fDStoreServer == null) {
				fDStoreServer = (DStoreServer) RemoteServerManager.getServer(DStoreServer.SERVER_ID, fProvider.getConnection());
			}
			fDStoreServer.setWorkDir(fProvider.getIndexLocation());
			return fDStoreServer.getDataStore();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
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

	protected String reportProblem(Scope scope, String message) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject project = workspaceRoot.getProject(scope.getName());

		// parser for error message
		int errorMessageStart = message.indexOf("Indexer: "); //$NON-NLS-1$
		int errorMessageEnd = message.indexOf(": ", errorMessageStart + 9); //$NON-NLS-1$
		String errorMessage = message.substring(errorMessageStart + 9, errorMessageEnd);

		boolean includeError = true;
		if (errorMessage.indexOf("inclusion") < 0) {
			includeError = false;
		}

		// parser for include/macro name
		int includeStart = errorMessageEnd + 2;
		int includeEnd = message.indexOf(" in file: ", includeStart); //$NON-NLS-1$
		String include = message.substring(includeStart, includeEnd);

		// parse for file name and line number
		int fileStart = includeEnd + 10;
		int fileEnd = message.indexOf(":", fileStart); //$NON-NLS-1$
		String fileName = message.substring(fileStart, fileEnd);

		int lineStart = fileEnd;
		int lineEnd = message.indexOf(".  ", lineStart); //$NON-NLS-1$
		String lineNumber = message.substring(lineStart + 1, lineEnd);

		// put error message back together
		Object[] args = new Object[] { include, fileName, new Integer(lineNumber.replace(",", "")) }; //$NON-NLS-1$ //$NON-NLS-2$
		String info = ParserMessages.getFormattedString("BaseProblemFactory.problemPattern", args); //$NON-NLS-1$
		if (includeError) {
			info = ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.inclusionNotFound", info); //$NON-NLS-1$
		} else {
			info = ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.definitionNotFound", info); //$NON-NLS-1$
		}

		String infoMsg = Messages.getString("RSECIndexSubsystem.11"); //$NON-NLS-1$
		String wholeMessage = NLS.bind(Messages.getString("RSECIndexSubsystem.12"), new Object[] { info }) + "  " + infoMsg; //$NON-NLS-1$ //$NON-NLS-2$

		IFile file = null;
		String projectLocation = project.getLocationURI().getPath();
		fileStart = fileName.indexOf(projectLocation);
		if (fileStart == -1) {
			fileName = null;
		} else {
			fileName = fileName.substring(fileStart + projectLocation.length() + 1);
			IPath path = new Path(fileName);
			file = project.getFile(path);
		}

		if (file != null) {
			try {
				IMarker marker = file.createMarker("org.eclipse.ptp.rdt.ui.indexerproblemmarker"); //$NON-NLS-1$
				marker.setAttribute(IMarker.LINE_NUMBER, Integer.parseInt(lineNumber.replace(",", ""))); //$NON-NLS-1$ //$NON-NLS-2$
				marker.setAttribute(IMarker.MESSAGE, wholeMessage);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		} else {
			try {
				IMarker marker = project.createMarker("org.eclipse.ptp.rdt.ui.indexerproblemmarker"); //$NON-NLS-1$
				marker.setAttribute(IMarker.MESSAGE, wholeMessage);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
		return wholeMessage;
	}

	/**
	 * @since 3.0
	 */
	public EventType getReIndexEventType() {
		return IRemoteFastIndexerUpdateEvent.EventType.EVENT_REINDEX;
	}

	/**
	 * @since 3.0
	 */
	public String computeHighlightPositions(ITranslationUnit targetUnit) {
		// If something goes wrong, return an empty string.

		checkAllProjects(new NullProgressMonitor());
		DataStore dataStore = getDataStore(null);
		if (dataStore == null) {
			return ""; //$NON-NLS-1$
		}
		DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(),
				CDTMiner.C_SEMANTIC_HIGHTLIGHTING_COMPUTE_POSITIONS);
		if (queryCmd == null) {
			return ""; //$NON-NLS-1$
		}
		NullProgressMonitor monitor = new NullProgressMonitor();
		StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);
		ArrayList<Object> args = new ArrayList<Object>();
		Scope scope = new Scope(targetUnit.getCProject().getProject());
		DataElement dataElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());

		args.add(dataElement);
		args.add(createSerializableElement(dataStore, targetUnit));

		// execute the command
		DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());

		try {
			smonitor.waitForUpdate(status, monitor);
		} catch (Exception e) {
			RDTLog.logError(e);
		}

		DataElement element = status.get(0);
		String data = element.getName();
		try {
			Object result = Serializer.deserialize(data);
			if (result == null || !(result instanceof String)) {
				return ""; //$NON-NLS-1$;
			}
			return (String) result;
		} catch (IOException e) {
			RDTLog.logError(e);
		} catch (ClassNotFoundException e) {
			RDTLog.logError(e);
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @since 3.2
	 */
	public String computeInactiveHighlightPositions(ITranslationUnit targetUnit) {
		DataStore dataStore = getDataStore(null);
		if (dataStore == null) {
			return ""; //$NON-NLS-1$
		}
		DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(),
				CDTMiner.C_INACTIVE_HIGHTLIGHTING_COMPUTE_POSITIONS);
		if (queryCmd == null) {
			return ""; //$NON-NLS-1$
		}

		Scope scope = new Scope(targetUnit.getCProject().getProject());

		ArrayList<Object> args = new ArrayList<Object>();
		args.add(dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName()));
		args.add(createSerializableElement(dataStore, targetUnit));

		DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());

		StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);
		try {
			smonitor.waitForUpdate(status, new NullProgressMonitor());
		} catch (Exception e) {
			RDTLog.logError(e);
		}

		DataElement element = status.get(0);
		if (element == null) {
			return ""; //$NON-NLS-1$
		}
		String result = element.getName();
		return result == null ? "" : result; //$NON-NLS-1$
	}

	/**
	 * @since 3.0
	 */
	public FoldingRegionsResult computeFoldingRegions(ITranslationUnit targetUnit, int docLength,
			boolean fPreprocessorBranchFoldingEnabled, boolean fStatementsFoldingEnabled) {
		checkAllProjects(new NullProgressMonitor());
		DataStore dataStore = getDataStore(null);
		if (dataStore == null) {
			return null;
		}
		DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(),
				CDTMiner.C_CODE_FOLDING_COMPUTE_REGIONS);
		if (queryCmd == null) {
			return null;
		}
		NullProgressMonitor monitor = new NullProgressMonitor();
		StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(fProvider.getConnection(), dataStore);
		ArrayList<Object> args = new ArrayList<Object>();
		Scope scope = new Scope(targetUnit.getCProject().getProject());
		DataElement dataElement = dataStore.createObject(null, CDTMiner.T_SCOPE_SCOPENAME_DESCRIPTOR, scope.getName());

		args.add(dataElement);
		args.add(createSerializableElement(dataStore, targetUnit));
		args.add(dataStore.createObject(null, CDTMiner.T_INDEX_INT_DESCRIPTOR, Integer.toString(docLength)));
		args.add(dataStore.createObject(null, CDTMiner.T_INDEX_BOOLEAN_DESCRIPTOR,
				Boolean.toString(fPreprocessorBranchFoldingEnabled)));
		args.add(dataStore.createObject(null, CDTMiner.T_INDEX_BOOLEAN_DESCRIPTOR, Boolean.toString(fStatementsFoldingEnabled)));

		// execute the command
		DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());

		try {
			smonitor.waitForUpdate(status, monitor);
		} catch (Exception e) {
			RDTLog.logError(e);
		}

		DataElement element = status.get(0);

		String data = element.getName();
		try {
			Object result = Serializer.deserialize(data);
			if (result == null || !(result instanceof FoldingRegionsResult)) {
				return null;
			}
			return (FoldingRegionsResult) result;
		} catch (IOException e) {
			RDTLog.logError(e);
		} catch (ClassNotFoundException e) {
			RDTLog.logError(e);
		}
		return null;
	}

	/**
	 * @since 3.2
	 */
	public TextEdit computeCodeFormatting(Scope scope, ITranslationUnit targetUnit, String source,
			RemoteDefaultCodeFormatterOptions preferences, int offset, int length, IProgressMonitor monitor) {
		Object result = sendRequest(CDTMiner.C_CODE_FORMATTING, new Object[] { scope.getName(), targetUnit, source, preferences,
				offset, length }, monitor);
		if (result == null) {
			return null;
		}

		return transformEdit((RemoteTextEdit) result);
	}

	private TextEdit transformEdit(RemoteTextEdit remoteEdit) {
		TextEdit edit = null;
		if (remoteEdit instanceof RemoteMultiTextEdit) {
			RemoteMultiTextEdit source = (RemoteMultiTextEdit) remoteEdit;
			edit = new MultiTextEdit(source.getOffset(), source.getLength());

		} else if (remoteEdit instanceof RemoteReplaceEdit) {
			RemoteReplaceEdit source = (RemoteReplaceEdit) remoteEdit;
			edit = new ReplaceEdit(source.getOffset(), source.getLength(), source.getText());
		}

		if (edit != null) {
			RemoteTextEdit[] children = remoteEdit.getChildren();
			for (RemoteTextEdit element : children) {
				edit.addChild(transformEdit(element));
			}
		}
		return edit;
	}

}
