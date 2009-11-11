package org.eclipse.ptp.etfw.parallel;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.etfw.internal.ILaunchFactory;

public class ParallelLaunchFactory implements ILaunchFactory {

	public ILaunch makeLaunch(ILaunchConfiguration config, String mode,ISourceLocator isl) {
		return new PLaunch(config, mode, isl);
	}
	public String getType(){
		return PARALLEL;
	}
}
