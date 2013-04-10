/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.util.NoSuchElementException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.osgi.service.prefs.Preferences;

/**
 * A simple resource matcher to test for resources contained inside a specific directory path.
 * This should be used to filter resources in a specific, named path. (As opposed to RegexPatternMatcher, which may not work with
 * directory and file names that contain wildcard characters.)
 */
public class PathResourceMatcher extends ResourceMatcher {
	private static final String ATTR_PATH = "path"; //$NON-NLS-1$
	IPath path;

	public PathResourceMatcher(IPath p) {
		if (p == null) {
			path = new Path(""); //$NON-NLS-1$
		} else {
			path = p;
		}
	}

	public boolean match(IResource candidate) {
		if (candidate == null) {
			return false;
		}
		return path.isPrefixOf(candidate.getProjectRelativePath());
	}

	public String toString() {
		return path.toOSString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return path.toOSString().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PathResourceMatcher)) {
			return false;
		}
		PathResourceMatcher other = (PathResourceMatcher) obj;
		if (!path.toOSString().equals(other.path.toOSString())) {
			return false;
		}
		return true;
	}

	/**
	 * Place needed data for recreating inside the preference node
	 */
	@Override
	public void saveMatcher(Preferences prefRootNode) {
		super.saveMatcher(prefRootNode);
		prefRootNode.put(ATTR_PATH, path.toPortableString());
	}
	
	/**
	 * Recreate instance from preference node
	 * 
	 * @param preference node
	 * @return the recreated instance
	 * @throws NoSuchElementException
	 * 				if expected data is not in the preference node.
	 */
	public static ResourceMatcher loadMatcher(Preferences prefRootNode) throws NoSuchElementException {
		String p = prefRootNode.get(ATTR_PATH, null);
		if (p == null) {
			throw new NoSuchElementException(Messages.PathResourceMatcher_0);
		}
		return new PathResourceMatcher(Path.fromPortableString(p));
	}
}
