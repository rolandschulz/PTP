package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;

public interface ICommandJobStreamsProxy extends IStreamsProxy, IStreamsProxy2 {

	void close();

	void setErrMonitor(ICommandJobStreamMonitor err);

	void setOutMonitor(ICommandJobStreamMonitor out);

	void startMonitors();
}
