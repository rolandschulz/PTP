/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.log;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.gig.messages.Messages;

public class ThreadInfo {
	// keep a memo of which filename goes with which IFile resource
	private static Map<String, IFile> fileResolver = new HashMap<String, IFile>();

	/*
	 * Resets the file Resolver, should be called everytime a new GkleeLog object is created
	 */
	public static void reset() {
		fileResolver.clear();
	}

	private final IFile logFile;
	private final String block, thread, filename, line;
	// these are lazily parsed as it is easier to deal with empty strings then
	private Integer blockInt, threadInt, lineInt;
	private IFile file;

	/*
	 * No guarantees of the format of these strings are required. Warnings are given in methods that may have exceptions
	 */
	public ThreadInfo(String block, String thread, String file, String line, IFile logFile) {
		this.block = block;
		this.thread = thread;
		this.filename = file;
		this.line = line;
		this.logFile = logFile;
	}

	/*
	 * This is very likely to throw a NumberFormatException
	 */
	public int getBlock() {
		if (blockInt == null) {
			return blockInt = Integer.parseInt(block);
		}
		return blockInt;
	}

	/*
	 * This should only be called after getLabel has been called. Internally, after getFilename has lazily loaded the file.
	 */
	public IFile getFile() {
		return file;
	}

	/*
	 * This recursively cuts down on the filePath to try and find the file in the container.
	 * There is no guarantee that the file is actually there, so null is returned if it gives up.
	 */
	private IFile getFile(IContainer container, String filePath) {
		final int splitPoint = filePath.indexOf('/');
		if (splitPoint == -1) {
			if (container instanceof IProject) {
				final IProject project = (IProject) container;
				return project.getFile(filePath);
			}
			else if (container instanceof IFolder) {
				final IFolder folder = (IFolder) container;
				return folder.getFile(filePath);
			}
		}
		final String folderName = filePath.substring(0, splitPoint);
		filePath = filePath.substring(splitPoint + 1);
		if (container instanceof IProject) {
			final IProject project = (IProject) container;
			final IFolder folder = project.getFolder(folderName);
			return getFile(folder, filePath);
		}
		else if (container instanceof IFolder) {
			final IFolder parent = (IFolder) container;
			final IFolder folder = parent.getFolder(folderName);
			return getFile(folder, filePath);
		}
		return null;
	}

	/*
	 * attempts to get the file specified by this.filename in the project given
	 */
	private IFile getFile(IProject project) {
		// we recompute the same files again and again, so check the memo for if it is already computed
		if (fileResolver.containsKey(filename)) {
			return fileResolver.get(filename);
		}
		// now try computing it based from server, then from local verification
		final String serverHomeDirectory = "/home/bgibson/GIGServer/"; //$NON-NLS-1$
		if (filename.startsWith(serverHomeDirectory)) {
			// log file must have been generated on the server, and server file paths follow eclipse file structure
			String filePath = filename.substring(serverHomeDirectory.length());
			filePath = filePath.substring(filePath.indexOf('/') + 1);
			final IFile ret = getFile(project, filePath);
			fileResolver.put(filename, ret);
			return ret;
		}
		else {
			// log file must have been generated locally, we do know that the source file should be near the log file in both
			String logFilePath = this.logFile.getLocation().toOSString();
			String filePath = filename;
			int fileSeparator, logSeparator;
			String filePrefix, logPrefix;

			// truncate the common ancestry
			while (true) {
				fileSeparator = filePath.indexOf('/');
				logSeparator = logFilePath.indexOf('/');
				if (fileSeparator == -1 || logSeparator == -1) {
					break;
				}
				filePrefix = filePath.substring(0, fileSeparator);
				final String tempFile = filePath.substring(fileSeparator + 1);
				logPrefix = logFilePath.substring(0, logSeparator);
				final String tempLogFile = logFilePath.substring(logSeparator + 1);
				if (filePrefix.equals(logPrefix)) {
					filePath = tempFile;
					logFilePath = tempLogFile;
				}
				else {
					break;
				}
			}

			// find the common ancestor
			IResource curr = this.logFile;
			logSeparator = logFilePath.lastIndexOf('/');
			while (logSeparator != -1) {
				curr = curr.getParent();
				logFilePath = logFilePath.substring(0, logSeparator);
				logSeparator = logFilePath.lastIndexOf('/');
			}
			IContainer parentContainer = curr.getParent();

			// find the container that contains the target file
			fileSeparator = filePath.indexOf('/');
			while (fileSeparator != -1) {
				filePrefix = filePath.substring(0, fileSeparator);
				filePath = filePath.substring(fileSeparator + 1);
				if (parentContainer instanceof IProject) {
					final IProject parent = (IProject) parentContainer;
					parentContainer = parent.getFolder(filePrefix);
				}
				else if (parentContainer instanceof IFolder) {
					final IFolder parent = (IFolder) parentContainer;
					parentContainer = parent.getFolder(filePrefix);
				}
				else {
					return null;
				}
			}

			final IFile ret;
			if (parentContainer instanceof IProject) {
				final IProject parent = (IProject) parentContainer;
				ret = parent.getFile(filePath);
			}
			else if (parentContainer instanceof IFolder) {
				final IFolder parent = (IFolder) parentContainer;
				ret = parent.getFile(filePath);
			}
			else {
				return null;
			}

			if (ret.exists()) {
				fileResolver.put(filename, ret);
				return ret;
			}

			return null;
		}
	}

	/*
	 * Lazily loads the file and gives the name. If no file can be found, the filename is returned.
	 */
	private String getFilename(IProject project) {
		if (file == null) {
			file = getFile(project);
			if (file != null) {
				return file.getName();
			}
			else {
				return filename;
			}
		}
		return file.getName();
	}

	/*
	 * This gives a good label/string representation of this object, if it doesn't have an exception.
	 * Lazily parses/loads all the data of this object, so the odds of a NumberFormatException are high.
	 */
	public String getLabel(IProject project) {
		return String
				.format(Messages.THREAD_INFO_FORMAT, getBlock(), getThread(), getLine(), getFilename(project));
	}

	/*
	 * Lazily parses the line number; has a high chance of NumberFormatException
	 */
	public int getLine() {
		if (lineInt == null) {
			return lineInt = Integer.parseInt(line);
		}
		return lineInt;
	}

	/*
	 * This is very likely to throw a NumberFormatException
	 */
	public int getThread() {
		if (threadInt == null) {
			return threadInt = Integer.parseInt(thread);
		}
		return threadInt;
	}
}
