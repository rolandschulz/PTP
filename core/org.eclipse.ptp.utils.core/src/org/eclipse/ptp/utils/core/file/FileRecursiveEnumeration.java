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
package org.eclipse.ptp.utils.core.file;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;


public class FileRecursiveEnumeration implements Enumeration<File> {

	public static void main(String[] args) {
		Enumeration<File> enumeration = new FileRecursiveEnumeration("c:/command/gs"); //$NON-NLS-1$
		while (enumeration.hasMoreElements()) {
			// System.out.println(enumeration.nextElement().toString());
		}
	}
	
	private Stack<File> roots;
	private FileEnumeration currentDirectory;
	private File nextFile;
	private LinkedList<Exception> exceptionList = new LinkedList<Exception>();
	
	public FileRecursiveEnumeration(File root) {
		if (! root.exists()) {
			throw new IllegalArgumentException();
		}
		roots = new Stack<File>();
		roots.add(root);
		fetchNextFile();
	}
	
	public FileRecursiveEnumeration(String root) {
		this(new File(root));
	}
	
	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return nextFile != null;
	}
	
	public boolean hasMoreExceptions() {
		return exceptionList.size() > 0;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public File nextElement() {
		File result = nextFile;
		fetchNextFile();
		return result;
	}

	public Exception nextException() {
		if (exceptionList.size() == 0) {
			throw new NoSuchElementException();
		}
		return (Exception) exceptionList.removeFirst();
	}

	private void fetchNextFile() {
		nextFile = null;
		while (true) {
			if (currentDirectory == null) {
				if (roots.empty()) {
					return;
				}
				File root = (File)roots.pop();
				try {
					currentDirectory = new FileEnumeration(root);
				} catch (IOException e) {
					exceptionList.addLast(e);
				}
			} else {
				if (currentDirectory.hasMoreElements()) {
					nextFile = (File)currentDirectory.nextElement();
					if (nextFile.isDirectory()) {
						roots.add(nextFile);
					}
					return;
				} else {
					currentDirectory = null;
				}
			}	
		}		
	}
}
