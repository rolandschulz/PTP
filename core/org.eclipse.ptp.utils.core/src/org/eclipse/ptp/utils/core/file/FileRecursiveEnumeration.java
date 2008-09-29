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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;


public class FileRecursiveEnumeration implements Enumeration {

	Stack roots;
	FileEnumeration currentDirectory;
	File nextFile;
	LinkedList exceptionList = new LinkedList();

	public FileRecursiveEnumeration(String root) {
		this(new File(root));
	}
	
	public FileRecursiveEnumeration(File root) {
		if (! root.exists()) {
			throw new IllegalArgumentException();
		}
		roots = new Stack();
		roots.add(root);
		fetchNextFile();
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
	
	public boolean hasMoreExceptions() {
		return exceptionList.size() > 0;
	}
	
	public Exception nextException() {
		if (exceptionList.size() == 0) {
			throw new NoSuchElementException();
		}
		return (Exception) exceptionList.removeFirst();
	}
	
	public boolean hasMoreElements() {
		return nextFile != null;
	}

	public Object nextElement() {
		File result = nextFile;
		fetchNextFile();
		return result;
	}

	public static void main(String[] args) {
		Enumeration enumeration = new FileRecursiveEnumeration("c:/command/gs"); //$NON-NLS-1$
		while (enumeration.hasMoreElements()) {
			// System.out.println(enumeration.nextElement().toString());
		}
	}
}
