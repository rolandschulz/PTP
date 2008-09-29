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

package org.eclipse.ptp.cell.utils.stream;

/**
 * A listener that is called when an entire line is read from a stream.
 * A ILineStreamListener is an entity that can receive one line of text at time.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public interface ILineStreamListener {
	void newLine(String line);
	void streamClosed();
	void streamError(Exception e);
}
