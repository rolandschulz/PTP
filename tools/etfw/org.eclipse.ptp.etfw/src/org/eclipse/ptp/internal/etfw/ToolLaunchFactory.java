package org.eclipse.ptp.internal.etfw;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;

public class ToolLaunchFactory implements ILaunchFactory {

	public String getType() {
		return SEQUENTIAL;
	}

	public ILaunch makeLaunch(ILaunchConfiguration config, String mode, ISourceLocator isl) {
		return new Launch(config, mode, isl);
	}

}
