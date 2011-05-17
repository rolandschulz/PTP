/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.runnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFileType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFilesType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;

/**
 * A managed file is a client-side file which needs to be moved to the resource
 * to which the job will be submitted. This class wraps the Job runnable for
 * staging these files. All files in the list are copied serially to the target
 * resource.
 * 
 * @author arossi
 * 
 */
public class ManagedFilesJob extends Job {

	private final String uuid;
	private final IJAXBResourceManagerControl control;
	private final List<ManagedFileType> files;

	private RemoteServicesDelegate delegate;
	private IVariableMap rmVarMap;
	private String stagingDir;
	private boolean success;

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
	public ManagedFilesJob(String uuid, ManagedFilesType files, IJAXBResourceManagerControl control) throws CoreException {
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
	 * First checks to see if the file references in-memory content, and if so,
	 * writes out a temporary source file. It then copies the file and places a
	 * property in the environment mapping the name of the ManagedFile object
	 * against its target path.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			delegate = control.getRemoteServicesDelegate(monitor);
		} catch (Throwable t) {
			return CoreExceptionUtils.getErrorStatus(Messages.ManagedFilesJobError, t);
		}

		rmVarMap = control.getEnvironment();
		stagingDir = rmVarMap.getString(uuid, stagingDir);

		boolean localTarget = delegate.getLocalFileManager() == delegate.getRemoteFileManager();
		success = false;
		SubMonitor progress = SubMonitor.convert(monitor, files.size() * 10);
		/*
		 * for now we handle the files serially. NOTE: no support for Windows as
		 * target ...
		 */
		for (ManagedFileType file : files) {
			try {
				File localFile = maybeWriteFile(file);
				progress.worked(5);
				String fileName = localFile.getName();
				if (file.isUniqueIdPrefix()) {
					fileName = UUID.randomUUID() + fileName;
				}
				String pathSep = localTarget ? JAXBControlConstants.PATH_SEP : JAXBControlConstants.REMOTE_PATH_SEP;
				String target = stagingDir + pathSep + fileName;
				SubMonitor m = progress.newChild(5);
				copyFileToRemoteHost(localFile.getAbsolutePath(), target, m);
				if (file.isDeleteAfterUse()) {
					localFile.delete();
				}
				if (m.isCanceled()) {
					break;
				}
				PropertyType p = new PropertyType();
				p.setName(file.getName());
				if (localTarget) {
					p.setValue(new File(System.getProperty(JAXBControlConstants.JAVA_USER_HOME), target).getAbsolutePath());
				} else {
					p.setValue(target);
				}
				p.setVisible(false);
				rmVarMap.put(p.getName(), p);
			} catch (Throwable t) {
				progress.done();
				return CoreExceptionUtils.getErrorStatus(Messages.ManagedFilesJobError, t);
			}
			progress.worked(5);
		}
		progress.done();
		success = true;
		return Status.OK_STATUS;
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
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			progress.newChild(5);
			/*
			 * EFS.NONE means mkdir -p on the parent directory (EFS.SHALLOW is
			 * mkdir parent, UNDEFINED is no mkdir).
			 */
			RemoteServicesDelegate.copy(delegate.getLocalFileManager(), localPath, delegate.getRemoteFileManager(), remotePath,
					EFS.NONE, progress);
		} finally {
			progress.done();
		}
	}

	/**
	 * If there is already a path defined for this file, this is returned. Else
	 * a temporary source file is created to be deleted on completion. If the
	 * file contents is a reference to a string in the environment, the normal
	 * VariableResolver can be bypassed by setting <code>resolveContents</code>
	 * to false; this avoids recursive resolution which might falsely interpret
	 * shell symbols (${...}) as referring to the Eclipse default string
	 * resolver.
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
			return new File(path);
		}
		String name = rmVarMap.getString(uuid, file.getName());
		File sourceDir = new File(System.getProperty(JAXBControlConstants.JAVA_TMP_DIR));
		File localFile = new File(sourceDir, name);
		String contents = file.getContents();
		FileWriter fw = null;
		try {
			if (contents == null) {
				if (!localFile.exists() || !localFile.isFile()) {
					throw new FileNotFoundException(localFile.getAbsolutePath());
				}
			} else {
				if (file.isResolveContents()) {
					contents = rmVarMap.getString(uuid, contents);
				} else {
					/*
					 * magic to avoid attempted resolution of unknown shell
					 * variables
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
						Object o = rmVarMap.get(key);
						if (o instanceof PropertyType) {
							contents = String.valueOf(((PropertyType) o).getValue());
						} else if (o instanceof AttributeType) {
							contents = String.valueOf(((AttributeType) o).getValue());
						}
					}
				}
				fw = new FileWriter(localFile, false);
				fw.write(contents);
				fw.flush();
			}
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
