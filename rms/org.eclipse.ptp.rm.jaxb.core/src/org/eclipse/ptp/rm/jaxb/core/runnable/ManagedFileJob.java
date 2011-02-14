package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFile;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;

public class ManagedFileJob extends Job {

	private final ManagedFile file;
	private final ManagedFiles files;
	private final IRemoteConnection connection;
	private final Map<String, Object> env;

	public ManagedFileJob(ManagedFile file, ManagedFiles files, IRemoteConnection connection, Map<String, Object> env) {
		super(file.getName());
		this.files = files;
		this.file = file;
		this.connection = connection;
		this.env = env;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return null;
	}
}
