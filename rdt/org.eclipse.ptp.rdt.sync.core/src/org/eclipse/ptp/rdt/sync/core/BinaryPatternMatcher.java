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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * Matcher for a binary file. Note that it is conservative. It works only for files recognized by CDT and is not yet a general
 * binary file matcher.
 */
public class BinaryPatternMatcher extends PatternMatcher {
	private static final String ATTR_PROJECT_NAME = "project-name"; //$NON-NLS-1$
	private final IProject project;
	
	public BinaryPatternMatcher(IProject p) {
		project = p;
	}

	public boolean match(String candidate) {
		try {
			ICElement fileElement = CoreModel.getDefault().create(project.getFile(candidate));
			if (fileElement == null) {
				return false;
			}
			int resType = fileElement.getElementType();
			if (resType == ICElement.C_BINARY) {
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException e) {
			// CDT throws this exception for files not recognized. For now, be conservative and allow these files.
			return false;
		}
	}

	public String toString() {
		return Messages.BinaryPatternMatcher_0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
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
		if (!(obj instanceof BinaryPatternMatcher)) {
			return false;
		}
		BinaryPatternMatcher other = (BinaryPatternMatcher) obj;
		if (project == null) {
			if (other.project != null) {
				return false;
			}
		} else if (!project.equals(other.project)) {
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
		memento.putString(ATTR_PROJECT_NAME, project.getName());
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
		String r = memento.getString(ATTR_PROJECT_NAME);
		if (r == null) {
			throw new NoSuchElementException("Project name not found in memento"); //$NON-NLS-1$
		}
		String projectName = memento.getString(ATTR_PROJECT_NAME);

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null) {
			throw new NoSuchElementException("Project unknown: " + projectName); //$NON-NLS-1$
		}

		return new BinaryPatternMatcher(project);
	}
}
