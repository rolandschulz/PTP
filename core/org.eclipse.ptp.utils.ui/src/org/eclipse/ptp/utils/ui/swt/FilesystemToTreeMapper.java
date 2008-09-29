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
import java.util.LinkedList;
import java.util.List;

/**
 * This class receives a top directory and maps all files and subdirectories
 * (recursively) to a tree structure where each node correspond to a file or directory. 
 * 
 * Main use for this method is to generate a tree to be displayed in a 
 * ElementTreeSelectionDialog whose content provider is a TreeNodeContentProvider
 * 
 * 
 * @author Richard Maciel
 *
 */
public class FilesystemToTreeMapper {
	public FilesystemTreeNode filesystemTreeFactory(File basedir) {
		
		if(!basedir.isDirectory())
			return null;
		
		// Create a node for the basedir
		FilesystemTreeNode baseNode = new FilesystemTreeNode(basedir);
		
		// Sweep the filesystem under basedir
		baseNode.setChildren(generateFilesystemTree(baseNode));
		
		return baseNode;
	}

	private FilesystemTreeNode[] generateFilesystemTree(FilesystemTreeNode parentNode) {
		
		List<FilesystemTreeNode> children = new LinkedList<FilesystemTreeNode>();
		
		// List dir entries
		//File dir = new File(directory.getAbsolutePath());
		File [] dirChildren = parentNode.getFile().listFiles();
		
		// Iterates over all items
		for(int i=0; i < dirChildren.length; i++) {
			// TODO: filter here before processing
			
			FilesystemTreeNode node = new FilesystemTreeNode(dirChildren[i]);
			node.setParent(parentNode);
			if(dirChildren[i].isDirectory()) {
				// If directory, generate directory information
				node.setChildren(generateFilesystemTree(node));
			} else {
				node.setChildren(null);
			}
			children.add(node);
		}
		
		// Transform it into an array
		FilesystemTreeNode [] temp = new FilesystemTreeNode[children.size()];
		children.toArray(temp);
		
		return temp;
	}
}
