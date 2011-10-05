package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class MergeConflictException extends CoreException {
	private static final long serialVersionUID = 1L;
	private static final String pluginID = "org.eclipse.ptp.rdt.sync.git.core"; //$NON-NLS-1$
	
	public MergeConflictException(Throwable arg0) {
		super(new Status(IStatus.ERROR, pluginID, null, arg0));
	}
	
	public MergeConflictException(String arg0, Throwable arg1) {
		super(new Status(IStatus.ERROR, pluginID, arg0, arg1));
	}

	public MergeConflictException(IStatus status) {
		super(status);
	}
}
