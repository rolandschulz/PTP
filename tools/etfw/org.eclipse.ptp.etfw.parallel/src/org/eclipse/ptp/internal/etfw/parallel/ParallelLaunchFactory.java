package org.eclipse.ptp.internal.etfw.parallel;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.internal.debug.core.launch.PLaunch;
import org.eclipse.ptp.internal.etfw.ILaunchFactory;

public class ParallelLaunchFactory implements ILaunchFactory {

	public ILaunch makeLaunch(ILaunchConfiguration config, String mode, ISourceLocator isl) {
		return new PLaunch(config, mode, isl);
	}

	public String getType() {
		return PARALLEL;
	}
}
