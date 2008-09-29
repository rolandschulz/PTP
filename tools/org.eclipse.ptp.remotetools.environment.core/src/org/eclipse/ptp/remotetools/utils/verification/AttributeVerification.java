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
package org.eclipse.ptp.remotetools.utils.verification;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

public class AttributeVerification {
	
	public static IStatus createStatus(String pluginID, Throwable e, int severity,
			String attributeName, String message, String value) {
		if (value == null) {
			value = Messages.AttributeVerification_NoValue;
		}
		String errorMessage = NLS.bind(Messages.AttributeVerification_ErrorMessage, new String[] { attributeName, message, value});
		return new Status(IStatus.ERROR, pluginID, 0,
				errorMessage, e);
	}
	
	/**
	 * Generic convenience method for raising core exception for errors.
	 * 
	 * @param pluginID
	 *            see CoreException
	 * @param e
	 *            see CoreException
	 * @param attributeName
	 *            see CoreException
	 * @param message
	 *            see CoreException
	 * @param value
	 *            see CoreException
	 * @throws CoreException
	 */
	public static void throwAttributeException(String pluginID, Throwable e,
			String attributeName, String message, String value)
			throws CoreException {
		throw new CoreException(createStatus(pluginID, e, IStatus.ERROR, attributeName, message, value));
	}

	/**
	 * Generic convenience method for raising core exception for errors.
	 * 
	 * @param pluginID
	 *            see CoreException
	 * @param attributeName
	 *            see CoreException
	 * @param message
	 *            see CoreException
	 * @param value
	 *            see CoreException
	 * @throws CoreException
	 */
	public static void throwAttributeException(String pluginID,
			String attributeName, String message, String value)
			throws CoreException {
		throwAttributeException(pluginID, (Throwable) null, attributeName,
				message, value);
	}

	String pluginID = null;

	public AttributeVerification(String pluginID) {
		super();
		this.pluginID = pluginID;
	}

	/**
	 * Throws a CoreException for the plug-in associated with this instance of
	 * AttributeVerification.
	 * 
	 * @param e
	 *            see CoreException
	 * @param attributeName
	 *            see CoreException
	 * @param message
	 *            see CoreException
	 * @param value
	 *            see CoreException
	 * @throws CoreException
	 *             see CoreException
	 */
	public void throwAttributeException(Throwable e,
			String attributeName, String message, String value)
			throws CoreException {
		if (value == null) {
			value = Messages.AttributeVerification_NoValue;
		}
		String errorMessage = NLS.bind(Messages.AttributeVerification_ErrorMessage, new String[] { attributeName, message, value});
		throw new CoreException(new Status(IStatus.ERROR, pluginID, 0,
				errorMessage, e));
	}

	/**
	 * Throws a CoreException for the plug-in associated with this instance of
	 * AttributeVerification.
	 * 
	 * @param attributeName
	 *            see CoreException
	 * @param message
	 *            see CoreException
	 * @param value
	 *            see CoreException
	 * @throws CoreException
	 *             see CoreException
	 */
	public void throwAttributeException(String attributeName, String message, String value) throws CoreException {
		throwAttributeException((Throwable)null, attributeName, message, value);
	}

	/**
	 * If the string represents a valid parsable path, returns this path.
	 * @param attributeName Name of the attribute
	 * @param stringValue String that represents the path
	 * @param pathType Class used to create path
	 * @return The path represented by the string
	 * @throws CoreException
	 */
	public IPath verifyPath(String attributeName, String stringValue) throws CoreException {
		IPath path = new Path(stringValue);
		
		// Verify path
		// Very strange design for IPath interface for testing validity.
		if (! path.isValidPath(stringValue)) {
			throwAttributeException(attributeName, Messages.AttributeVerification_InvalidPath, stringValue);
		}
		
		return path;
	}
	
	public static final int EXIST = 1;
	public static final int WRITEABLE = 2;
	public static final int DIRECTORY = 4;
	public static final int EXECUTABLE = 8;
	public static final int FILE = 16;
	public static final int EXISTING_FILE = EXIST | FILE;
	public static final int EXISTING_EXECUTABLE = EXIST | FILE | EXECUTABLE;
	public static final int EXISTING_DIRECTORY = EXIST | DIRECTORY;
	
	public IStatus checkPath(String attributeName, IPath path) {
		return checkPath(attributeName, path, IStatus.ERROR, EXIST);
	}
			
	public IStatus checkPath(String attributeName, IPath path, int severity) {
		return checkPath(attributeName, path, severity, EXIST);
	}
	
	public IStatus checkPath(String attributeName, IPath path, int severity, int options) {
		/*
		 * Path must be absolute for safe check, if not, would check against
		 * some unknown current working directory.
		 */
		if (! path.isAbsolute()) {
			return createStatus(pluginID, null, severity, attributeName, Messages.AttributeVerification_NotAnAbsolutePath, path.toOSString());
		}

		URI uri = URIUtil.toURI(path);
		IFileStore file;
		try {
			file = EFS.getStore(uri);
		} catch (CoreException e) {
			return e.getStatus();
		}
		
		IFileInfo info = file.fetchInfo();
		
		if ((options & EXIST) != 0) {
			if (! info.exists()) {
				return createStatus(pluginID, null, severity, attributeName, Messages.AttributeVerification_PathDoesNotExist, path.toOSString());
			}
		}
		
		if ((options & DIRECTORY) != 0) {
			if (! info.isDirectory()) {
				return createStatus(pluginID, null, severity, attributeName, Messages.AttributeVerification_PathIsNotDir, path.toOSString());			
			}
		}

		if ((options & EXECUTABLE) != 0) {
			/*
			 * On PPC, it is known that the EFS.ATTRIBUTE_EXECUTABLE is never set,
			 * event if the path is an executable fiel.
			 */
			if (Platform.getOSArch().equals(Platform.ARCH_PPC)) {
				// Simple ignore and assume it is an executable.
			} else {
				if (! info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE)) {
					return createStatus(pluginID, null, severity, attributeName, Messages.AttributeVerification_PathIsNotExecutableFile, path.toOSString());
				}
			}
		}

		if ((options & WRITEABLE) != 0) {
			if (info.getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
				return createStatus(pluginID, null, severity, attributeName, Messages.AttributeVerification_PathIsNotWritable, path.toOSString());
			}
		}
		
		if ((options & FILE) != 0) {
			if (info.isDirectory()) {
				return createStatus(pluginID, null, severity, attributeName, Messages.AttributeVerification_PathIsNotFile, path.toOSString());
			}
		}
		
		return null;
	}
	
	public IPath verifyPluginRelativePath(String attributeName, Plugin plugin, IPath path) throws CoreException {
		if (path.isAbsolute()) {
			return path;
		} else {
			URL url = FileLocator.find(plugin.getBundle(), path, null);
			if (url == null) {
				throwAttributeException(attributeName, Messages.AttributeVerification_PathNotFound, path.toOSString());
			}
			try {
				url = FileLocator.resolve(url);
				URI uri = new URI(url.toString());
				File file = new File(uri);
				return new Path(file.getAbsolutePath());
			} catch (IOException e1) {
				throwAttributeException(attributeName, Messages.AttributeVerification_PathNotFound, path.toOSString());
			} catch (URISyntaxException e) {
				throwAttributeException(attributeName, Messages.AttributeVerification_MustBeLocalFile, path.toOSString());
			}
		}
		return null;
	}
	
//	public IPath verifyWorkspaceRelativePath(String attributeName, Plugin plugin, IPath path) {
//		// TODO: Implement
//
//	}

	public IStatus createResultStatus(List errors) {
		/*
		 * Remove null elements from list.
		 */
		List oldList = errors;
		errors = new ArrayList();
		Iterator iterator = oldList.iterator(); 
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object != null) {
				errors.add(object);
			}
		}
	
		if (errors.isEmpty()) {
			return new Status(IStatus.OK, pluginID, 0, Messages.AttributeVerification_ConfigurationOK, null);
		} else {
			/*
			 * TODO: make code better, without iterator
			 */ 
			MultiStatus result = new MultiStatus(pluginID, 0, Messages.AttributeVerification_InvalidConfiguration, null);
			Iterator iterator2 = errors.iterator(); 
			while (iterator2.hasNext()) {
				IStatus object = (IStatus) iterator2.next();
				result.add(object);
			}
			return result;
		}
	}
}
