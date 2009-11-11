package org.eclipse.ptp.etfw.internal;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

public interface ILaunchFactory {
	public static final String PARALLEL="parallel_launch";
	public static final String SEQUENTIAL="sequential_launch";
	public ILaunch makeLaunch(ILaunchConfiguration config, String mode, ISourceLocator isl);
	public String getType();
}
