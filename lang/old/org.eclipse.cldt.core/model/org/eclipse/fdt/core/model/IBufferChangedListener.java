package org.eclipse.fdt.core.model;


/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

/**
 * A listener, which gets notified when the contents of a specific buffer
 * have changed, or when the buffer is closed.
 * When a buffer is closed, the listener is notified <em>after</em> the buffer has been closed.
 * A listener is not notified when a buffer is saved.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * This interface is similar to the JDT IBufferChangedListener interface
 */
public interface IBufferChangedListener {
	/** 
	 * Notifies that the given event has occurred.
	 *
	 * @param event the change event
	 */
	public void bufferChanged(BufferChangedEvent event);

}
