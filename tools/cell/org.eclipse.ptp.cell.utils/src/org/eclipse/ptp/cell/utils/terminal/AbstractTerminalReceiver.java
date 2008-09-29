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

/**
 * Implements ITerminalReceiver with ability to write data back to the associated 
 * ITerminalProvider, as it were data typed in into the terminal.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public abstract class AbstractTerminalReceiver implements ITerminalReceiver {
	ITerminalProvider terminalProvider;
	
	public AbstractTerminalReceiver(ITerminalProvider terminalProvider) {
		this.terminalProvider = terminalProvider;
	}
	
	public ITerminalProvider getTerminalProvider() {
		return terminalProvider;
	}

	public abstract void receiveData(byte[] bytes, int length);

	public abstract void receiveMetaMessage(String message);

	public abstract void receiveError(byte[] bytes, int length);
	
	public void writeData(byte[] bytes, int length) {
		terminalProvider.writeDataToTerminal(bytes, length);
	}


}
