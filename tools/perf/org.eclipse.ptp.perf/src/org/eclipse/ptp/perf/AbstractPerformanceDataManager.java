package org.eclipse.ptp.perf;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public abstract class AbstractPerformanceDataManager {
	public abstract String getName();
	public abstract void cleanup();
	public void process(String projname, ILaunchConfiguration configuration, String projectLocation)throws CoreException
	{};
	public abstract void view();
	
}
