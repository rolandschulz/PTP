package org.eclipse.ptp.rm.jaxb.core.utils;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

public class FileUtils {

	public static void copy(IRemoteFileManager from, String source, IRemoteFileManager to, String target, SubMonitor progress)
			throws CoreException {
		if (from == null) {
			throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
					Messages.Copy_Operation_NullSourceFileManager));
		}
		if (to == null) {
			throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
					Messages.Copy_Operation_NullTargetFileManager));
		}
		if (source == null) {
			throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
					Messages.Copy_Operation_NullSource));
		}
		if (target == null) {
			throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
					Messages.Copy_Operation_NullTarget));
		}

		IFileStore lres = from.getResource(source);
		if (!lres.fetchInfo(EFS.NONE, progress.newChild(5)).exists()) {
			throw new CoreException(new Status(IStatus.ERROR, JAXBCorePlugin.getUniqueIdentifier(),
					Messages.Copy_Operation_Local_resource_does_not_exist));
		}
		IFileStore rres = to.getResource(target);
		lres.copy(rres, EFS.OVERWRITE, progress.newChild(5));
	}

	public static void export(String source, IRemoteFileManager to, String target, SubMonitor progress) {

	}

}
