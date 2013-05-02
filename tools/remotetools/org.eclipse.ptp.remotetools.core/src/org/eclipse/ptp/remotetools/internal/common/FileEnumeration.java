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
package org.eclipse.ptp.remotetools.internal.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.core.messages.Messages;

public class FileEnumeration implements Enumeration<File> {

	public static void main(String[] args) {
		FileEnumeration enumeration = null;
		try {
			enumeration = new FileEnumeration("c:/command"); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		while (enumeration.hasMoreElements()) {
			// System.out.println(enumeration.nextElement().toString());
		}
	}

	private File[] files;
	private int currentFile;

	public FileEnumeration(File root) throws IOException {
		if (!root.exists()) {
			throw new FileNotFoundException();
		} else if (root.isFile()) {
			files = new File[1];
			files[0] = root;
		} else if (root.isDirectory()) {
			files = root.listFiles();
			if (files == null) {
				throw new IOException(NLS.bind(Messages.FileEnumeration_0, root.getAbsolutePath()));
			}
		} else {
			Assert.isTrue(false);
		}
		currentFile = 0;
	}

	/**
	 * @since 2.0
	 */
	public FileEnumeration(String root) throws IOException {
		this(new File(root));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return currentFile < files.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Enumeration#nextElement()
	 */
	/**
	 * @since 2.0
	 */
	public File nextElement() {
		if (currentFile < files.length) {
			return files[currentFile++];
		} else {
			return null;
		}
	}
}
