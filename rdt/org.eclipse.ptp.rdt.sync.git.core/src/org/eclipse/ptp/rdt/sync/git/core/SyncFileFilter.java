package org.eclipse.ptp.rdt.sync.git.core;

/**
 * A simple filtering interface intended for sync providers to pass to the underlying sync tool to indicate files that should not
 * by sync'ed. This may include Eclipse files, like .project, binary files, or files indicated by the user. 
 */
public interface SyncFileFilter {
	public boolean shouldIgnore(String fileName);
}
