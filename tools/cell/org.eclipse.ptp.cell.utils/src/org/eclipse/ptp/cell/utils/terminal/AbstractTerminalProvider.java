/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.utils.terminal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.ptp.cell.utils.debug.Debug;


/**
 * Implements IterminalProvider, enabling listeners and data forwarding
 * of received data to the listeners.
 * @author Daniel Felix Ferber
 *
 */
public abstract class AbstractTerminalProvider implements ITerminalProvider {

	Collection terminalReceivers = new HashSet();

	public void addListener(ITerminalReceiver receiver) {
		terminalReceivers.add(receiver);
	}

	public void removeListener(ITerminalReceiver receiver) {
		terminalReceivers.remove(receiver);
	}

	public void receiveMetaMessage(String message) {
		Iterator iterator = terminalReceivers.iterator();
		while (iterator.hasNext()) {
			ITerminalReceiver listener = (ITerminalReceiver) iterator.next();
			try {
				listener.receiveMetaMessage(message);
			} catch (Exception ee) {
				Debug.POLICY.error(Debug.DEBUG_STREAM, ee);
				Debug.POLICY.logError(ee, Messages.AbstractTerminalProvider_FailedDelegateMethod);
			}
		}
	}

	public void receiveDataFromTerminal(byte[] bytes, int length) {
		Iterator iterator = terminalReceivers.iterator();
		while (iterator.hasNext()) {
			ITerminalReceiver listener = (ITerminalReceiver) iterator.next();
			try {
				listener.receiveData(bytes, length);
			} catch (Exception ee) {
				Debug.POLICY.error(Debug.DEBUG_STREAM, ee);
				Debug.POLICY.logError(ee, Messages.AbstractTerminalProvider_1);
			}
		}
	}

	public void receiveErrorFromTerminal(byte[] bytes, int length) {
		Iterator iterator = terminalReceivers.iterator();
		while (iterator.hasNext()) {
			ITerminalReceiver listener = (ITerminalReceiver) iterator.next();
			try {
				listener.receiveError(bytes, length);
			} catch (Exception ee) {
				Debug.POLICY.error(Debug.DEBUG_STREAM, ee);
				Debug.POLICY.logError(ee, Messages.AbstractTerminalProvider_2);
			}
		}
	}

	public abstract void writeDataToTerminal(byte[] bytes, int length);

}
