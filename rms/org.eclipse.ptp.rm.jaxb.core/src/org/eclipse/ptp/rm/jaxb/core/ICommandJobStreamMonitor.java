package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.debug.core.model.IStreamMonitor;

public interface ICommandJobStreamMonitor extends IStreamMonitor {

	public void close();

	public void setBufferLimit(int limit);

	public void startMonitoring();
}
