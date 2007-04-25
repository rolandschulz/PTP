package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.command.IProxyCommand;

/**
 * @deprecated
 */
public interface IProxyEventHandler {

	/**
	 * Handle (process) the given event.
	 * 
	 * @param event - the event to process
	 */
	public abstract void handleEvent(IProxyCommand command, IProxyEvent event);

}

