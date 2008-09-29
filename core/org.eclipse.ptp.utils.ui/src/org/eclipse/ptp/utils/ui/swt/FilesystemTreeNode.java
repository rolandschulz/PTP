/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.utils.ui.swt;

import java.io.File;

import org.eclipse.jface.viewers.TreeNode;

/**
 * A node of a filesystem tree structure
 * 
 * @author Richard Maciel
 *
 */
public class FilesystemTreeNode extends TreeNode {
	
	public FilesystemTreeNode(File fileOrDirectory) {
		super(fileOrDirectory);
	}
	
	/**
	 * Convenience method to get the path object stored into the tree node.
	 * 
	 * @return An IPath object
	 */
	public File getFile() {
		return (File)getValue();
	}
	
	@Override
	public String toString() {
		if(value != null) {
			return getFile().getName();
		} else {
			return super.toString();
		}
	}
}
