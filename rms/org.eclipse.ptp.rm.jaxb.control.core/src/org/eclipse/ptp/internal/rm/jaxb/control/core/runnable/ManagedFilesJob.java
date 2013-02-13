/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.runnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.LaunchController;
import org.eclipse.ptp.internal.rm.jaxb.control.core.data.LineImpl;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.LineType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFileType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFilesType;

/**
 * A managed file is a client-side file which needs to be moved to the resource to which the job will be submitted. This class wraps
 * the Job runnable for staging these files. <br>
 * <br>
 * There are two possible operations, copy and delete. In the former case, all files in the list are copied serially to the target
 * resource; in the latter, only those files with delete of the target marked as true are deleted.
 * 
 * @author arossi
 * 
 */
public class ManagedFilesJob extends Job {

	public enum Operation {
		COPY, DELETE
	};

	private final String uuid;
	private final ILaunchController control;
	private final List<ManagedFileType> files;

	private RemoteServicesDelegate delegate;
	private IVariableMap rmVarMap;
	private String stagingDir;
	private boolean success;
	private Operation operation;

	/**
	 * 
	 * @param uuid
	 *            internal job identifier (the job has not yet been submitted)
	 * @param files
	 *            JAXB data element
	 * @param control
	 *            callback to resource manager control
	 * @throws CoreException
	 */
	public ManagedFilesJob(String uuid, ManagedFilesType files, ILaunchController control) throws CoreException {
		super(Messages.ManagedFilesJob);
		this.uuid = uuid;
		this.control = control;
		stagingDir = files.getFileStagingLocation();
		this.files = files.getFile();
	}

	/**
	 * @return whether the staging succeeded
	 */
	public boolean getSuccess() {
		return success;
	}

	/**
	 * Either copy or delete
	 * 
	 * @param operation
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	/**
	 * First checks to see if the file references in-memory content, and if so, writes out a temporary source file. It then copies
	 * the file and places a property in the environment mapping the name of the ManagedFile object against its target path.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			try {
				delegate = JAXBUtils.getRemoteServicesDelegate(control.getRemoteServicesId(), control.getConnectionName(),
						progress.newChild(2));
				IRemoteConnection conn = delegate.getRemoteConnection();
				LaunchController.checkConnection(conn, progress);
				if (delegate.getRemoteFileManager() == null) {
					throw new Throwable(Messages.UninitializedRemoteServices);
				}
			} catch (Throwable t) {
				return CoreExceptionUtils.getErrorStatus(Messages.ManagedFilesJobError, t);
			}

			rmVarMap = control.getEnvironment();
			success = false;
			try {
				if (operation == Operation.COPY) {
					doCopy(monitor);
				} else if (operation == Operation.DELETE) {
					doDelete(monitor);
				}
				success = true;
				return Status.OK_STATUS;
			} catch (Throwable t) {
				return CoreExceptionUtils.getErrorStatus(Messages.ManagedFilesJobError, t);
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Copy local data from the local host to the remote host.
	 * 
	 * @param localPath
	 *            source file
	 * @param remotePath
	 *            target file
	 * @param monitor
	 * @throws CoreException
	 */
	private void copyFileToRemoteHost(String localPath, String remotePath, IProgressMonitor monitor) throws CoreException {
		/*
		 * EFS.NONE means mkdir -p on the parent directory (EFS.SHALLOW is mkdir parent, UNDEFINED is no mkdir).
		 */
		RemoteServicesDelegate.copy(delegate.getLocalFileManager(), localPath, delegate.getRemoteFileManager(), remotePath,
				EFS.NONE, monitor);
	}

	/**
	 * Executes copy operation.
	 * 
	 * @param monitor
	 */
	private void doCopy(IProgressMonitor monitor) throws Throwable {
		stagingDir = rmVarMap.getString(uuid, stagingDir);
		boolean localTarget = delegate.getLocalFileManager() == delegate.getRemoteFileManager();
		SubMonitor progress = SubMonitor.convert(monitor, files.size() * 10);
		/*
		 * for now we handle the files serially. NOTE: no support for Windows as target ...
		 */
		for (ManagedFileType file : files) {
			File localFile = maybeWriteFile(file);
			progress.worked(5);
			String fileName = localFile.getName();
			if (fileName == null || fileName.length() == 0) {
				continue;
			}
			String pathSep = localTarget ? JAXBControlConstants.PATH_SEP : JAXBControlConstants.REMOTE_PATH_SEP;
			String target = stagingDir + pathSep + fileName;
			SubMonitor m = progress.newChild(5);
			copyFileToRemoteHost(localFile.getAbsolutePath(), target, m);
			if (file.isDeleteSourceAfterUse()) {
				localFile.delete();
			}
			if (m.isCanceled()) {
				break;
			}
			AttributeType a = new AttributeType();
			a.setName(file.getName());
			if (localTarget) {
				a.setValue(new File(System.getProperty(JAXBControlConstants.JAVA_USER_HOME), target).getAbsolutePath());
			} else {
				a.setValue(target);
			}
			a.setVisible(false);
			rmVarMap.put(a.getName(), a);
			progress.worked(5);
		}
	}

	/**
	 * Deletes files where delete target is indicated.
	 * 
	 * @param monitor
	 */
	private void doDelete(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, files.size() * 15);
		/*
		 * for now we handle the files serially. NOTE: no support for Windows as target ...
		 */
		for (ManagedFileType file : files) {
			if (!file.isDeleteTargetAfterUse()) {
				progress.worked(15);
				continue;
			}
			AttributeType a = rmVarMap.get(file.getName());
			IFileStore store = delegate.getRemoteFileManager().getResource(String.valueOf(a.getValue()));
			try {
				if (store.fetchInfo(EFS.NONE, progress.newChild(5)).exists()) {
					store.delete(EFS.NONE, progress.newChild(10));
				}
			} catch (CoreException t) {
				JAXBControlCorePlugin.log(t);
			}
		}
	}

	/**
	 * If there is already a path defined for this file, this is returned. Else a temporary source file is created to be deleted on
	 * completion. If the file contents is a reference to a string in the environment, the normal VariableResolver can be bypassed
	 * by setting <code>resolveContents</code> to false; this avoids recursive resolution which might falsely interpret shell
	 * symbols (${...}) as referring to the Eclipse default string resolver. Otherwise, the &lt;line&gt; arguments are processed as
	 * in the script.
	 * 
	 * @param file
	 *            JAXB data element
	 * @return the written or the pre-existent file
	 * @throws IOException
	 * @throws CoreException
	 */
	private File maybeWriteFile(ManagedFileType file) throws IOException, CoreException {
		String path = file.getPath();
		if (path != null) {
			/*
			 * We need to dereference here; added 06/11/2011
			 */
			path = rmVarMap.getString(uuid, path);
			return new File(path);
		}
		String name = rmVarMap.getString(uuid, file.getName());
		if (file.isUniqueIdPrefix()) {
			name = UUID.randomUUID() + name;
		}
		File sourceDir = new File(System.getProperty(JAXBControlConstants.JAVA_TMP_DIR));
		File localFile = new File(sourceDir, name);
		String contents = file.getContents();
		List<LineType> lines = file.getLine();
		FileWriter fw = null;
		try {
			if (!lines.isEmpty()) {
				StringBuffer buffer = new StringBuffer();
				String s = null;
				for (LineType line : lines) {
					s = new LineImpl(uuid, line, rmVarMap).getResolved();
					if (!JAXBControlConstants.ZEROSTR.equals(s)) {
						buffer.append(s).append(JAXBControlConstants.REMOTE_LINE_SEP);
					}
				}
				contents = buffer.toString();
			} else if (file.isResolveContents()) {
				contents = rmVarMap.getString(uuid, contents);
			} else {
				/*
				 * magic to avoid attempted resolution of unknown shell variables
				 */
				int start = contents.indexOf(JAXBControlConstants.OPENVRM);
				int end = contents.length();
				if (start >= 0) {
					start += JAXBControlConstants.OPENVRM.length();
					end = contents.indexOf(JAXBControlConstants.PD);
					if (end < 0) {
						end = contents.indexOf(JAXBControlConstants.CLOSV);
					}
					String key = contents.substring(start, end);
					contents = String.valueOf(rmVarMap.get(key).getValue());
				}
			}
			fw = new FileWriter(localFile, false);
			fw.write(contents);
			fw.flush();
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException t) {
				JAXBControlCorePlugin.log(t);
			}
		}
		return localFile;
	}
}
