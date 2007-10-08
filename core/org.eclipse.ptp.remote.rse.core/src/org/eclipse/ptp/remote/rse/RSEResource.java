/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.rse;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remote.AbstractRemoteResource;
import org.eclipse.ptp.remote.IRemoteResource;
import org.eclipse.ptp.remote.IRemoteResourceInfo;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileContext;


public class RSEResource extends AbstractRemoteResource {

	private IRemoteFile rseFile;
	private URI rseUri;
	private RSEFileManager fileMgr;
	
	public RSEResource(RSEFileManager mgr, IRemoteFile rseFile) {
		this.fileMgr = mgr;
		this.rseFile = rseFile;
		try {
			this.rseUri = new URI("rse", rseFile.getSystemConnection().getHostName(), rseFile.getAbsolutePath(), null);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor)
			throws CoreException {
		String[] names;
		
		IRemoteFileSubSystem subSys = rseFile.getParentRemoteFileSubSystem();
		if (!rseFile.isStale() && rseFile.hasContents(RemoteChildrenContentsType.getInstance()) 
				&& !(subSys instanceof IFileServiceSubSystem))
		{
			Object[] children = rseFile.getContents(RemoteChildrenContentsType.getInstance());
			names = new String[children.length];
			                
			for (int i = 0; i < children.length; i++)
			{
				names[i] = ((IRemoteFile)children[i]).getName();
			}
		}
		else
		{
			try {
				IRemoteFile[] children = null;
				
				if (subSys instanceof FileServiceSubSystem) {
					FileServiceSubSystem fileServiceSubSystem = ((FileServiceSubSystem)subSys);
					IHostFile[] results = fileServiceSubSystem.getFileService().getFilesAndFolders(rseFile.getAbsolutePath(), "*", monitor); //$NON-NLS-1$
					IRemoteFileSubSystemConfiguration config = subSys.getParentRemoteFileSubSystemConfiguration();
					RemoteFileFilterString filterString = new RemoteFileFilterString(config, rseFile.getAbsolutePath(), "*"); //$NON-NLS-1$
					filterString.setShowFiles(true);
					filterString.setShowSubDirs(true);
					RemoteFileContext context = new RemoteFileContext(subSys, rseFile, filterString);
					children = fileServiceSubSystem.getHostFileToRemoteFileAdapter().convertToRemoteFiles(fileServiceSubSystem, context, rseFile, results);
				}
				else {
					children = subSys.listFoldersAndFiles(rseFile, "*", monitor); //$NON-NLS-1$
				}
				
				names = new String[children.length];
				
				for (int i = 0; i < children.length; i++) {
					names[i] = (children[i]).getName();
				}		
			}
			catch (SystemMessageException e) {
				names = new String[0];
			}
		}
		
		return names;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor)
			throws CoreException {
		IRemoteFileSubSystem subSys = rseFile.getParentRemoteFileSubSystem();
		try {
			boolean success = subSys.delete(rseFile, monitor);
			if (!success) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						"Could not delete file"));
			}
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"Could not delete file", e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IRemoteResourceInfo fetchInfo(int options, IProgressMonitor monitor)
			throws CoreException {
		String classification = (rseFile==null) ? null : rseFile.getClassification();
		
		FileInfo info = new FileInfo(rseFile.getName());
		if (rseFile == null || !rseFile.exists()) {
			info.setExists(false);
			//broken symbolic link handling
			if (classification!=null && classification.startsWith("broken symbolic link")) { //$NON-NLS-1$
				info.setAttribute(IRemoteResource.ATTRIBUTE_SYMLINK, true);
				int i1 = classification.indexOf('\'');
				if (i1>0) {
					int i2 = classification.indexOf('´');
					if (i2>i1) {
						info.setStringAttribute(IRemoteResource.ATTRIBUTE_LINK_TARGET, classification.substring(i1+1,i2));
					}
				}
			}
			return new RSEResourceInfo(info);
		}
		
		info.setExists(true);
		info.setLastModified(rseFile.getLastModified());
		boolean isDir = rseFile.isDirectory();
		info.setDirectory(isDir);
		info.setAttribute(IRemoteResource.ATTRIBUTE_READ_ONLY, !rseFile.canWrite());
		info.setAttribute(IRemoteResource.ATTRIBUTE_EXECUTABLE, rseFile.isExecutable());
		info.setAttribute(IRemoteResource.ATTRIBUTE_ARCHIVE, rseFile.isArchive());
		info.setAttribute(IRemoteResource.ATTRIBUTE_HIDDEN, rseFile.isHidden());
		if (classification!=null && classification.startsWith("symbolic link")) { //$NON-NLS-1$
			info.setAttribute(IRemoteResource.ATTRIBUTE_SYMLINK, true);
			int idx = classification.indexOf(':');
			if (idx>0) {
				info.setStringAttribute(IRemoteResource.ATTRIBUTE_LINK_TARGET, classification.substring(idx+1));
			}
		}

		if (!isDir) {
			info.setLength(rseFile.getLength());
		}
		
		return new RSEResourceInfo(info);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getChild(java.lang.String)
	 */
	@Override
	public IRemoteResource getChild(String name, IProgressMonitor monitor) {
		URI uri = toURI();
		IPath childPath = new Path(uri.getPath()).append(name);
		
		IRemoteResource resource = fileMgr.lookup(childPath);
		if (resource != null) {
			return resource;
		}
		
		IRemoteFileSubSystem subSys = rseFile.getParentRemoteFileSubSystem();
		if (!rseFile.isStale() && rseFile.hasContents(RemoteChildrenContentsType.getInstance()) 
				&& !(subSys instanceof IFileServiceSubSystem))
		{
			Object[] children = rseFile.getContents(RemoteChildrenContentsType.getInstance());
			                
			for (int i = 0; i < children.length; i++)
			{
				IRemoteFile file = (IRemoteFile)children[i];
				if (file.getName().equals(name)) {
					IRemoteResource rem = new RSEResource(fileMgr, file);
					fileMgr.cache(childPath, rem);
					return rem;
				}
			}
		}
		else
		{
			try {
				IRemoteFile[] children = null;
				
				if (subSys instanceof FileServiceSubSystem) {
					FileServiceSubSystem fileServiceSubSystem = ((FileServiceSubSystem)subSys);
					IHostFile[] results = fileServiceSubSystem.getFileService().getFilesAndFolders(rseFile.getAbsolutePath(), "*", monitor); //$NON-NLS-1$
					IRemoteFileSubSystemConfiguration config = subSys.getParentRemoteFileSubSystemConfiguration();
					RemoteFileFilterString filterString = new RemoteFileFilterString(config, rseFile.getAbsolutePath(), "*"); //$NON-NLS-1$
					filterString.setShowFiles(true);
					filterString.setShowSubDirs(true);
					RemoteFileContext context = new RemoteFileContext(subSys, rseFile, filterString);
					children = fileServiceSubSystem.getHostFileToRemoteFileAdapter().convertToRemoteFiles(fileServiceSubSystem, context, rseFile, results);
				}
				else {
					children = subSys.listFoldersAndFiles(rseFile, "*", monitor); //$NON-NLS-1$
				}
				
				for (IRemoteFile child : children) {
					if (child.getName().equals(name)) {
						IRemoteResource rem = new RSEResource(fileMgr, child);
						fileMgr.cache(childPath, rem);
						return rem;
					}
				}		
			}
			catch (SystemMessageException e) {
				return null;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getName()
	 */
	@Override
	public String getName() {
		return rseFile.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getParent()
	 */
	@Override
	public IRemoteResource getParent() {
		return new RSEResource(fileMgr, rseFile.getParentRemoteFile());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IRemoteResource mkdir(int options, IProgressMonitor monitor)
			throws CoreException {
		if (rseFile==null) {
			throw new CoreException(new Status(IStatus.ERROR, 
					Activator.getDefault().getBundle().getSymbolicName(),
					"Could not get remote file"));
		}
		IRemoteFileSubSystem subSys = rseFile.getParentRemoteFileSubSystem();
		if (!rseFile.exists()) {
			try {
				rseFile = subSys.createFolder(rseFile, monitor);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(), 
						"The directory could not be created", e));
			}
			return this;
		}
		else if (rseFile.isFile()) {
			throw new CoreException(new Status(IStatus.ERROR, 
					Activator.getDefault().getBundle().getSymbolicName(),
					"A file of that name already exists"));
		}
		else {
			return this;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#openInputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		IRemoteFileSubSystem subSys = rseFile.getParentRemoteFileSubSystem();
		
		if (rseFile.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR, 
					Activator.getDefault().getBundle().getSymbolicName(),
					"The file store represents a directory"));
		}
		
		if (rseFile.isFile()) {
			try {
				return subSys.getInputStream(rseFile.getParentPath(), rseFile.getName(), true, monitor);
			}
			catch (SystemMessageException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						"Could not get input stream", e));
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		if (rseFile==null) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"Could not get remote file"));
		}
		IRemoteFileSubSystem subSys = rseFile.getParentRemoteFileSubSystem();
		if (!rseFile.exists()) {
			try {
				rseFile = subSys.createFile(rseFile, monitor);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						"Could not create file", e));
			} 
		}
			
		if (rseFile.isFile()) {
			try {
				return subSys.getOutputStream(rseFile.getParentPath(), rseFile.getName(), true, monitor);
			}
			catch (SystemMessageException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						"Could not get output stream", e));
			}
		}
		else if (rseFile.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"This is a directory"));
		}
		else {
			//TODO check what to do for symbolic links and other strange stuff
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#putInfo(org.eclipse.ptp.remote.IRemoteResourceInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IRemoteResourceInfo info, int options,
			IProgressMonitor monitor) throws CoreException {
		IRemoteFileSubSystem subSys = rseFile.getParentRemoteFileSubSystem();
		boolean success = true;
		try {
			if ((options & IRemoteResource.SET_ATTRIBUTES) != 0) {
				//We cannot currently write isExecutable(), isHidden()
				success &= subSys.setReadOnly(rseFile, info.getAttribute(IRemoteResource.ATTRIBUTE_READ_ONLY), monitor);
			}
			if ((options & IRemoteResource.SET_LAST_MODIFIED) != 0) {
				success &= subSys.setLastModified(rseFile, info.getLastModified(), monitor);
			}
		} catch(Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					e.getMessage(), e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#toURI()
	 */
	@Override
	public URI toURI() {
		return rseUri;
	}
}
