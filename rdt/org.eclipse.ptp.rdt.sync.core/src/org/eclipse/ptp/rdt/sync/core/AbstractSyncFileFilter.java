/*******************************************************************************
 * Copyright (c) 2011, 2013 Oak Ridge National Laboratory, University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen & Roland Schulz - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * Class for filtering files during synchronization. The non-abstract class is provided
 * by the sync provider. All read operations (e.g. shouldIgnore) are thread safe (/need to
 * implemented thread safe by the subclasses).
 * 
 * @since 3.0
 */
public abstract class AbstractSyncFileFilter {

	/**
	 * Abstract class for ignore rules
	 * 
	 */
	public abstract class AbstractIgnoreRule {
		public abstract boolean isMatch(IResource target);

		public abstract boolean isMatch(String target, boolean isFolder);

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o == this || (o != null && o.getClass() == this.getClass() && toString().equals(o.toString()));
		}

		/** @return true if the target is to be ignored, false otherwise. */
		public abstract boolean getResult();

		/** @return String representing the full pattern (e.g. including an encoding for exclude) */
		@Override
		public abstract String toString();

		/** @return String without encoding of extra flags which are querryable separately (currently only exclude) */
		public abstract String getPattern();
	}

	protected final List<AbstractIgnoreRule> rules = new ArrayList<AbstractIgnoreRule>();

	/**
	 * Get the rules in this filter. Highest precedence is last rule (as for Git - reverse from PTP Juno)
	 * 
	 * @return
	 */
	public List<AbstractIgnoreRule> getRules() {
		return rules;
	}

	/**
	 * Add the pattern
	 * 
	 * @param pattern
	 *            pattern to add
	 * @param exclude
	 *            exclude flag
	 */
	public void addPattern(String pattern, boolean exclude) {
		addPattern(pattern, exclude, rules.size());
	}

	/**
	 * Add the resource pattern
	 * 
	 * @param resource
	 *            resource
	 * @param exclude
	 *            exclude flag
	 */
	public void addPattern(IResource resource, boolean exclude) {
		addPattern(resource, exclude, rules.size());
	}

	/**
	 * Add pattern. Pattern format is provider dependent
	 * 
	 * @param pattern
	 *            pattern to add
	 * @param exclude
	 *            exclude flag
	 * @param index
	 *            location of pattern in the list
	 */
	public abstract void addPattern(String pattern, boolean exclude, int index);

	/**
	 * Add resource pattern. Add rule only matching exactly this resource (not file/path with same name)
	 * 
	 * @param resource
	 *            resource to add
	 * @param exclude
	 *            true: exclude resource, false: include resource
	 * @param index
	 *            location of pattern in the list
	 */
	public abstract void addPattern(IResource resource, boolean exclude, int index);

	/**
	 * Swap a pattern with its higher-index neighbor
	 * Assumes pattern appears no more than once
	 * 
	 * @param pattern
	 * @return whether pattern was actually demoted
	 */
	public boolean demote(AbstractIgnoreRule pattern) {
		int oldIndex = rules.indexOf(pattern);
		if (oldIndex > -1 && oldIndex < rules.size() - 1) {
			Collections.swap(rules, oldIndex, oldIndex + 1);
			return true;
		}
		return false;
	}

	/**
	 * Get all patterns for this filter
	 * 
	 * @return patterns
	 */
	public List<AbstractIgnoreRule> getPatterns() {
		return Collections.unmodifiableList(rules);
	}

	/**
	 * Swap a pattern with its lower-index neighbor
	 * Assumes pattern only appears once
	 * 
	 * @param pattern
	 * @return whether pattern was actually promoted
	 */
	public boolean promote(AbstractIgnoreRule pattern) {
		int oldIndex = rules.indexOf(pattern);
		if (oldIndex > 0) {
			Collections.swap(rules, oldIndex, oldIndex - 1);
			return true;
		}
		return false;
	}

	/**
	 * Remove a pattern from the filter
	 * Assumes pattern appears no more than once
	 * 
	 * @param pattern
	 */
	public void removePattern(AbstractIgnoreRule pattern) {
		rules.remove(pattern);
	}

	/**
	 * Replace pattern with another - useful for when existing patterns are edited
	 * 
	 * @param oldRule
	 * @param newPattern
	 * @param type
	 * @return whether replace was successful (old pattern was found)
	 */
	public boolean replacePattern(AbstractIgnoreRule oldRule, String newPattern, boolean exclude) {
		int index = rules.indexOf(oldRule);
		if (index == -1) {
			return false;
		}
		rules.remove(index);
		addPattern(newPattern, exclude, index);
		return true;
	}

	/**
	 * Apply the filter to the given string
	 * 
	 * @param s
	 *            - the string
	 * @return whether the string should be ignored
	 */
	public boolean shouldIgnore(IResource r) {
		// If there is a rule to ignore a folder all members are ignored
		for (int i = rules.size() - 1; i > -1; i--) {
			if (rules.get(i).isMatch(r)) {
				return rules.get(i).getResult();
			}
		}
		return false;
	}

	/**
	 * Apply the filter to the given string
	 * 
	 * @param s
	 *            - the string
	 * @return whether the string should be ignored
	 */
	public boolean shouldIgnore(String path, boolean isFolder) {
		// If there is a rule to ignore a folder all members are ignored
		for (int i = rules.size() - 1; i > -1; i--) {
			if (rules.get(i).isMatch(path, isFolder)) {
				return rules.get(i).getResult();
			}
		}
		return false;
	}

	/**
	 * Save filters
	 * 
	 * @throws IOException
	 */
	public abstract void saveFilter() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract AbstractSyncFileFilter clone();
}
