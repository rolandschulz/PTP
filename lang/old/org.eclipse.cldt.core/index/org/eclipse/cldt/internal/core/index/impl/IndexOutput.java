/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.core.index.impl;

import java.io.IOException;

/**
 * An indexOutput is used to write an index into a different object (a File, ...). 
 */
public abstract class IndexOutput {
	/**
	 * Adds a File to the destination.
	 */
	public abstract void addFile(IndexedFile file) throws IOException;
	/**
	 * Adds a word to the destination.
	 */
	public abstract void addWord(WordEntry word) throws IOException;
	/**
	 * Closes the output, releasing the resources it was using.
	 */
	public abstract void close() throws IOException;
	/**
	 * Flushes the output.
	 */
	public abstract void flush() throws IOException;
	/**
	 * Returns the Object the output is writing to. It can be a file, another type of index, ... 
	 */
	public abstract Object getDestination();
	/**
	 * Opens the output, before writing any information.
	 */
	public abstract void open() throws IOException;
}
