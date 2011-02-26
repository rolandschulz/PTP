package org.eclipse.ptp.rm.jaxb.core.runnable;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFile;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.rm.JAXBResourceManager;

public class ManagedFileJob extends Job {

	private final ManagedFile file;
	private final ManagedFiles files;
	private final JAXBResourceManager rm;

	public ManagedFileJob(ManagedFile file, ManagedFiles files, JAXBResourceManager rm) {
		super(file.getName());
		this.files = files;
		this.file = file;
		this.rm = rm;
	}

	/**
	 * Copy local data from a path (can be a file or directory) from the local
	 * host to the remote host.
	 * 
	 * @param localPath
	 * @param remotePath
	 * @param configuration
	 * @throws CoreException
	 */
	protected void copyFileToRemoteHost(String localPath, String remotePath, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			IRemoteFileManager localFileManager = rm.getLocalFileManager();
			IRemoteFileManager remoteFileManager = rm.getRemoteFileManager();
			progress.newChild(5);
			if (progress.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
						Messages.Copy_Operation_cancelled_by_user, null));
			}
			if (remoteFileManager == null) {
				throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
						Messages.Copy_Operation_Null_FileManager));
			}

			IFileStore lres = localFileManager.getResource(localPath);
			if (!lres.fetchInfo(EFS.NONE, progress.newChild(5)).exists()) {
				// Local file not found!
				throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
						Messages.Copy_Operation_Local_resource_does_not_exist));
			}
			IFileStore rres = remoteFileManager.getResource(remotePath);

			// Copy file
			lres.copy(rres, EFS.OVERWRITE, progress.newChild(5));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// figure out if the file needs to be written out
		// contruct paths
		// copy
		return null;
	}
}
