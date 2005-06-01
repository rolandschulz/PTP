package org.eclipse.ptp.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

public class PCDIDebugModel {
	public static IPDebugTarget newDebugTarget(final ILaunch launch, final IPCDITarget target) throws DebugException {
		PDebugTarget debugTarget = new PDebugTarget(launch, target);
		return debugTarget;
	}
}
