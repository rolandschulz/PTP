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

import org.eclipse.cldt.internal.core.CharOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;


/**
 * An <code>IFileDocument</code> represents an IFile.
 */

public class IFileDocument extends PropertyDocument {
	protected IFile file;

	// cached contents if needed - only one of them is used at a time
	protected char[] charContents;
	protected byte[] byteContents;
	/**
	 * IFileDocument constructor comment.
	 */
	public IFileDocument(IFile file) {
		this(file, (char[])null);
	}
	/**
	 * IFileDocument constructor comment.
	 */
	public IFileDocument(IFile file, byte[] byteContents) {
		this.file= file;
		this.byteContents= byteContents;
	}
	/**
	 * IFileDocument constructor comment.
	 */
	public IFileDocument(IFile file, char[] charContents) {
		this.file= file;
		this.charContents= charContents;
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
	 */
	public byte[] getByteContent() throws IOException {
		if (byteContents != null) return byteContents;
		IPath location = file.getLocation();
		if (location == null) return new byte[0];
		return byteContents = org.eclipse.cldt.internal.core.Util.getFileByteContent(location.toFile());
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getCharContent()
	 */
	public char[] getCharContent() throws IOException {
		if (charContents != null) return charContents;
		IPath location = file.getLocation();
		if (location == null) return CharOperation.NO_CHAR;
		return charContents = org.eclipse.cldt.internal.core.Util.getFileCharContent(
					location.toFile(), 
					getEncoding());
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getEncoding()
	 */
	public String getEncoding() {
		//TODO: Indexer - get encoding
	    return null;
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getName()
	 */
	public String getName() {
		return file.getFullPath().toString();
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getStringContent()
	 */
	public String getStringContent() throws java.io.IOException {
		return new String(getCharContent());
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getType()
	 */
	public String getType() {
		String extension= file.getFileExtension();
		if (extension == null)
			return ""; //$NON-NLS-1$
		return extension;
	}
}
