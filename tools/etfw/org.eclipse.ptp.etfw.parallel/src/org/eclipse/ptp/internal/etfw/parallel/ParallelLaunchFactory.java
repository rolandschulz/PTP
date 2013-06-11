package org.eclipse.ptp.internal.etfw.parallel;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.internal.debug.core.launch.PLaunch;
import org.eclipse.ptp.internal.etfw.ILaunchFactory;

public class ParallelLaunchFactory implements ILaunchFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.etfw.ILaunchFactory#getType()
	 */
	public String getType() {
		return PARALLEL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.etfw.ILaunchFactory#makeLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.model.ISourceLocator)
	 */
	public ILaunch makeLaunch(ILaunchConfiguration config, String mode, ISourceLocator isl) {
		return new PLaunch(config, mode, isl);
	}
}
