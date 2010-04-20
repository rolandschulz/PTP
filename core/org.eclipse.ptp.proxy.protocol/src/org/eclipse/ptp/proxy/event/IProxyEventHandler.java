package org.eclipse.ptp.proxy.event;

import org.eclipse.ptp.proxy.command.IProxyCommand;

/**
 * @deprecated
 */
@Deprecated
public interface IProxyEventHandler {

	/**
	 * Handle (process) the given event.
	 * 
	 * @param event
	 *            - the event to process
	 */
	public abstract void handleEvent(IProxyCommand command, IProxyEvent event);

}
