package org.eclipse.ptp.bluegene.db;

import java.util.List;

public interface IBGEventListener {
	public void handleJobChangedEvent(List<JobInfo> jobs);
}
