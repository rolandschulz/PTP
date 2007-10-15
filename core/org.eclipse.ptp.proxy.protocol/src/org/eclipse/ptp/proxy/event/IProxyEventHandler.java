package org.eclipse.ptp.proxy.event;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyEvent;

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

