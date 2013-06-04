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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * Class for filtering files during synchronization. Instead of a constructor, the user can create an empty filter or a filter that
 * has decent default behavior, filtering, for example, configuration files like .project and .cproject.
 * <p>
 * Facilities are then provided for adding and removing files and directories from filtering.
 * <p>
 * Note that a SyncFileFilter can include several patterns which each have a ResourceMatcher. A pattern has a type (exclude or
 * include) and a ResourceMatcher (e.g. path, regex, or wildcard matcher). A single SyncFileFilter is used for a project.
 * SyncFileFilters are saved in preferences.
 * 
 * @since 3.0
 */
public abstract class AbstractSyncFileFilter {
	
	/* needs to implement:
	 * toString: should return pattern valid for addPattern(String)
	 */
	public abstract class AbstractIgnoreRule {
		public abstract boolean isMatch(IResource target);
		public abstract boolean isMatch(String target, boolean isFolder);
		public int hashCode() { return toString().hashCode(); }
		public boolean equals(Object o) { return o == this || 				
				(o != null && o.getClass() == this.getClass() && toString().equals(o.toString())); }
		/**@return true if the target is to be ignored, false otherwise.*/
		public abstract boolean getResult(); 
		/** @return String representing the full pattern (e.g. including an encoding for exclude)*/
		public abstract String toString(); 
		/** @return String without encoding of extra flags (i.e. exclude)*/
		public abstract String getPattern(); 
	}
	
	/* highest precedence is last rule (as for Git - reverse from PTP Juno) */
	public List<AbstractIgnoreRule> rules = new ArrayList<AbstractIgnoreRule>();

	public void addPattern(String pattern, boolean exclude) {
		addPattern(pattern,exclude,rules.size());
	}
	
	
	public void addPattern(IResource resource, boolean exclude) {
		addPattern(resource,exclude,rules.size());
	}
	
	/**
	 * pattern format is provider dependent
	 */
	public abstract void addPattern(String pattern, boolean exclude, int index);
	
	/**
	 * add rule only matching exactly this resource (not file/path with same name)
	 * 
	 * @param true: exclude resource, false: include resource
	 */
	public abstract void addPattern(IResource resource, boolean exclude, int index);
	
//	/**
//	 * Add the common, default list of paths to be filtered. This should not be called until the files
//	 * filtered already exist.
//	 */
//	public void addDefaults() {
//		for (String pattern: getDefaults()) {
//			addPattern(ResourcesPlugin.getWorkspace().getRoot().findMember(pattern), true);
//		}
//	}

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
			Collections.swap(rules, oldIndex, oldIndex+1);
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
			Collections.swap(rules, oldIndex, oldIndex-1);
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
		addPattern(newPattern,exclude,index);
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
		//If there is a rule to ignore a folder all members are ignored
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
		//If there is a rule to ignore a folder all members are ignored
		for (int i = rules.size() - 1; i > -1; i--) {
			if (rules.get(i).isMatch(path, isFolder)) {
				return rules.get(i).getResult();
			}
		}
		return false;
	}

	abstract public void saveFilter() throws IOException;
	
	abstract public AbstractSyncFileFilter clone();
}
