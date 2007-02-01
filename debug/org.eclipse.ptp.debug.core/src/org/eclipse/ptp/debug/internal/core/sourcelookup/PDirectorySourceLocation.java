/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.sourcelookup;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.resources.FileStorage;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Clement chu
 * 
 */
public class PDirectorySourceLocation implements IDirectorySourceLocation {
	private static final String ELEMENT_NAME = "cDirectorySourceLocation";
	private static final String ATTR_DIRECTORY = "directory";
	private static final String ATTR_ASSOCIATION = "association";
	private static final String ATTR_SEARCH_SUBFOLDERS = "searchSubfolders";
	private IPath fDirectory;
	private IPath fAssociation = null;
	private boolean fSearchForDuplicateFiles = false;
	private boolean fSearchSubfolders = false;
	private File[] fFolders = null;

	public PDirectorySourceLocation() {}

	public PDirectorySourceLocation(IPath directory, IPath association, boolean searchSubfolders) {
		setDirectory(directory);
		setAssociation(association);
		setSearchSubfolders(searchSubfolders);
	}
	public Object findSourceElement(String name) throws CoreException {
		Object result = null;
		if (!isEmpty(name) && getDirectory() != null) {
			File file = new File(name);
			if (file.isAbsolute())
				result = findFileByAbsolutePath(name);
			else
				result = findFileByRelativePath(name);
			if (result == null && getAssociation() != null) {
				IPath path = new Path(name);
				if (path.segmentCount() > 1 && getAssociation().isPrefixOf(path)) {
					path = getDirectory().append(path.removeFirstSegments(getAssociation().segmentCount()));
					result = findFileByAbsolutePath(path.toOSString());
				}
			}
		}
		return result;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPSourceLocation.class))
			return this;
		if (adapter.equals(PDirectorySourceLocation.class))
			return this;
		if (adapter.equals(IPath.class))
			return getDirectory();
		return null;
	}
	private void setDirectory(IPath directory) {
		fDirectory = directory;
	}
	public IPath getDirectory() {
		return fDirectory;
	}
	public void getDirectory(IPath path) {
		fDirectory = path;
	}
	public void setAssociation(IPath association) {
		fAssociation = association;
	}
	public IPath getAssociation() {
		return fAssociation;
	}
	private Object findFileByAbsolutePath(String name) {
		File file = new File(name);
		if (!file.isAbsolute())
			return null;
		File[] folders = getFolders();
		if (folders != null) {
			LinkedList list = new LinkedList();
			for (int i = 0; i < folders.length; ++i) {
				Object result = findFileByAbsolutePath(folders[i], name);
				if (result instanceof List) {
					if (searchForDuplicateFiles())
						list.addAll((List) result);
					else
						return list.getFirst();
				} else if (result != null) {
					if (searchForDuplicateFiles())
						list.add(result);
					else
						return result;
				}
			}
			if (list.size() > 0)
				return (list.size() == 1) ? list.getFirst() : list;
		}
		return null;
	}
	private Object findFileByAbsolutePath(File folder, String name) {
		File file = new File(name);
		if (!file.isAbsolute())
			return null;
		IPath filePath = new Path(name);
		IPath path = new Path(folder.getAbsolutePath());
		IPath association = getAssociation();
		if (!isPrefix(path, filePath) || path.segmentCount() + 1 != filePath.segmentCount()) {
			if (association != null && isPrefix(association, filePath) && association.segmentCount() + 1 == filePath.segmentCount())
				filePath = path.append(filePath.removeFirstSegments(association.segmentCount()));
			else
				return null;
		}
		// Try for a file in another workspace project
		IFile[] wsFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(filePath);
		LinkedList list = new LinkedList();
		for (int j = 0; j < wsFiles.length; ++j)
			if (wsFiles[j].exists()) {
				if (!searchForDuplicateFiles())
					return wsFiles[j];
				list.add(wsFiles[j]);
			}
		if (list.size() > 0)
			return (list.size() == 1) ? list.getFirst() : list;
		file = filePath.toFile();
		if (file.exists() && file.isFile()) {
			return createExternalFileStorage(filePath);
		}
		return null;
	}
	private Object findFileByRelativePath(String fileName) {
		File[] folders = getFolders();
		if (folders != null) {
			LinkedList list = new LinkedList();
			for (int i = 0; i < folders.length; ++i) {
				Object result = findFileByRelativePath(folders[i], fileName);
				if (result instanceof List) {
					if (searchForDuplicateFiles())
						list.addAll((List) result);
					else
						return list.getFirst();
				} else if (result != null) {
					if (searchForDuplicateFiles())
						list.add(result);
					else
						return result;
				}
			}
			if (list.size() > 0)
				return (list.size() == 1) ? list.getFirst() : list;
		}
		return null;
	}
	private Object findFileByRelativePath(File folder, String fileName) {
		IPath path = new Path(folder.getAbsolutePath());
		path = path.append(fileName);
		File file = path.toFile();
		if (file.exists() && file.isFile()) {
			path = new Path(file.getAbsolutePath());
			IFile[] wsFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
			LinkedList list = new LinkedList();
			for (int j = 0; j < wsFiles.length; ++j)
				if (wsFiles[j].exists()) {
					if (!searchForDuplicateFiles())
						return wsFiles[j];
					list.add(wsFiles[j]);
				}
			if (list.size() > 0)
				return (list.size() == 1) ? list.getFirst() : list;
			return createExternalFileStorage(path);
		}
		return null;
	}
	private IStorage createExternalFileStorage(IPath path) {
		return new FileStorage(path);
	}
	public String getMemento() throws CoreException {
		Document document = null;
		Throwable ex = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element node = document.createElement(ELEMENT_NAME);
			document.appendChild(node);
			node.setAttribute(ATTR_DIRECTORY, getDirectory().toOSString());
			if (getAssociation() != null)
				node.setAttribute(ATTR_ASSOCIATION, getAssociation().toOSString());
			node.setAttribute(ATTR_SEARCH_SUBFOLDERS, new Boolean(searchSubfolders()).toString());
			return PDebugUtils.serializeDocument(document);
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		} catch (TransformerException e) {
			ex = e;
		}
		abort(MessageFormat.format(InternalSourceLookupMessages.getString("PDirectorySourceLocation.0"), new String[] { getDirectory().toOSString() }), ex);
		// execution will not reach here
		return null;
	}
	public void initializeFrom(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
			String dir = root.getAttribute(ATTR_DIRECTORY);
			if (isEmpty(dir)) {
				abort(InternalSourceLookupMessages.getString("PDirectorySourceLocation.1"), null);
			} else {
				IPath path = new Path(dir);
				if (path.isValidPath(dir) && path.toFile().isDirectory() && path.toFile().exists()) {
					setDirectory(path);
				} else {
					abort(MessageFormat.format(InternalSourceLookupMessages.getString("PDirectorySourceLocation.2"), new String[] { dir }), null);
				}
			}
			dir = root.getAttribute(ATTR_ASSOCIATION);
			if (isEmpty(dir)) {
				setAssociation(null);
			} else {
				IPath path = new Path(dir);
				if (path.isValidPath(dir)) {
					setAssociation(path);
				} else {
					setAssociation(null);
				}
			}
			setSearchSubfolders(Boolean.valueOf(root.getAttribute(ATTR_SEARCH_SUBFOLDERS)).booleanValue());
			return;
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(InternalSourceLookupMessages.getString("PDirectorySourceLocation.3"), ex);
	}
	private void abort(String message, Throwable e) throws CoreException {
		IStatus s = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, message, e);
		throw new CoreException(s);
	}
	private boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
	public boolean equals(Object obj) {
		if (obj instanceof IDirectorySourceLocation) {
			IPath dir = ((IDirectorySourceLocation) obj).getDirectory();
			IPath association = ((IDirectorySourceLocation) obj).getAssociation();
			if (dir == null)
				return false;
			boolean result = dir.equals(getDirectory());
			if (result) {
				if (association == null && getAssociation() == null)
					return true;
				if (association != null)
					return association.equals(getAssociation());
			}
		}
		return false;
	}
	private boolean isPrefix(IPath prefix, IPath path) {
		int segCount = prefix.segmentCount();
		if (segCount >= path.segmentCount())
			return false;
		String prefixString = prefix.toOSString();
		String pathString = path.removeLastSegments(path.segmentCount() - segCount).toOSString();
		return prefixString.equalsIgnoreCase(pathString);
	}
	public void setSearchForDuplicateFiles(boolean search) {
		fSearchForDuplicateFiles = search;
	}
	public boolean searchForDuplicateFiles() {
		return fSearchForDuplicateFiles;
	}
	public boolean searchSubfolders() {
		return fSearchSubfolders;
	}
	public void setSearchSubfolders(boolean search) {
		resetFolders();
		fSearchSubfolders = search;
	}
	protected File[] getFolders() {
		if (fFolders == null)
			initializeFolders();
		return fFolders;
	}
	protected void resetFolders() {
		fFolders = null;
	}
	private void initializeFolders() {
		if (getDirectory() != null) {
			ArrayList list = new ArrayList();
			File root = getDirectory().toFile();
			list.add(root);
			if (searchSubfolders())
				list.addAll(getFileFolders(root));
			fFolders = (File[]) list.toArray(new File[list.size()]);
		}
	}
	private List getFileFolders(File file) {
		ArrayList list = new ArrayList();
		File[] folders = file.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		list.addAll(Arrays.asList(folders));
		for (int i = 0; i < folders.length; ++i)
			list.addAll(getFileFolders(folders[i]));
		return list;
	}
	public String toString() {
		return (getDirectory() != null) ? getDirectory().toOSString() : "";
	}
	public void dispose() {}
}
