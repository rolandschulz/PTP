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
 * Offers capabilities to be informed from one ITerminalProvider about
 * data received from a terminal.
 * For further documentation, see @link ITerminalProvider.
 * @author Daniel Felix Ferber
 *
 */
public interface ITerminalReceiver {
	void receiveMetaMessage(String message);
	void receiveData(byte bytes[], int length);
	void receiveError(byte bytes[], int length);
}
