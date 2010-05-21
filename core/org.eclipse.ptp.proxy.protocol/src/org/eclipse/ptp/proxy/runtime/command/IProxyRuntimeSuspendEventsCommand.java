/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.proxy.runtime.command;

/**
 * Interface definition for SUSPEND_EVENTS command. The SUSPEND_EVENTS command
 * is used in proxy flow control to inform the proxy that it must suspend
 * sending event notifications to the client.
 * 
 * @author David Wootton
 * @since 4.0
 */
public interface IProxyRuntimeSuspendEventsCommand extends IProxyRuntimeCommand {

}
