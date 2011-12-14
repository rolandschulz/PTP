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

import org.eclipse.ui.IMemento;

/**
 * A simple pattern matcher to match against strings contained in a certain path (files in a subdirectory, for example).
 * Note that this does exact matching only. This class should be used when the user wants to filter a specific, literal path.
 * Using RegexPatternMatcher could cause trouble with directory and file names containing wildcard characters.
 */
public class PathPatternMatcher extends PatternMatcher {
	private static final String ATTR_PATH = "path"; //$NON-NLS-1$
	String path;

	public PathPatternMatcher(String p) {
		if (p == null) {
			path = ""; //$NON-NLS-1$
		} else {
			path = p;
		}
	}

	public boolean match(String candidate) {
		return candidate.startsWith(path);
	}

	public String toString() {
		return path;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PathPatternMatcher)) {
			return false;
		}
		PathPatternMatcher other = (PathPatternMatcher) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

	/**
	 * Place needed data for recreating inside the memento
	 */
	@Override
	public void savePattern(IMemento memento) {
		super.savePattern(memento);
		memento.putString(ATTR_PATH, path);
	}
	
	/**
	 * Recreate instance from memento
	 * 
	 * @param memento
	 * @return the recreated instance
	 * @throws NoSuchElementException
	 * 				if expected data is not in the memento.
	 */
	public static PatternMatcher loadPattern(IMemento memento) throws NoSuchElementException {
		String p = memento.getString(ATTR_PATH);
		if (p == null) {
			throw new NoSuchElementException("Path not found in memento"); //$NON-NLS-1$
		}
		return new PathPatternMatcher(memento.getString(ATTR_PATH));
	}
}
