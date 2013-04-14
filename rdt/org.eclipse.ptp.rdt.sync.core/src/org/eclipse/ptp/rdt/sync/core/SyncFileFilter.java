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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Class for filtering files during synchronization. Instead of a constructor, the user can create an empty filter or a filter that
 * has decent default behavior, filtering, for example, configuration files like .project and .cproject.
 * <p>
 * Facilities are then provided for adding and removing files and directories from filtering.
 * <p>
 * Note that a SyncFileFilter can include several patterns which each have a ResourceMatcher. A pattern has a type (exclude or
 * include) and a ResourceMatcher (e.g. path, regex, or wildcard matcher). A single SyncFileFilter is used for a project.
 * SyncFileFilters are saved in preferences.
 */
public class SyncFileFilter {
	private static final String PATTERN_NODE_NAME = "pattern"; //$NON-NLS-1$
	private static final String PATTERN_TYPE_KEY = "pattern-type"; //$NON-NLS-1$
	private static final String NUM_PATTERNS_KEY = "num-patterns"; //$NON-NLS-1$

	private final LinkedList<ResourceMatcher> filteredPaths = new LinkedList<ResourceMatcher>();
	private final Map<ResourceMatcher, PatternType> patternToTypeMap = new HashMap<ResourceMatcher, PatternType>();

	public enum PatternType {
		EXCLUDE, INCLUDE
	}

	// Private constructor - create instances with "create" methods.
	private SyncFileFilter() {
	}

	// Copy constructor
	public SyncFileFilter(SyncFileFilter oldFilter) {
		filteredPaths.addAll(oldFilter.filteredPaths);
		patternToTypeMap.putAll(oldFilter.patternToTypeMap);
	}

	/**
	 * Constructor for an empty filter. Most clients will want to use "createDefaultFilter"
	 * 
	 * @return the new filter
	 */
	public static SyncFileFilter createEmptyFilter() {
		return new SyncFileFilter();
	}

	/**
	 * Constructor for a filter with a standard set of defaults. Note that this is a "default default". It may be overwritten if the
	 * user has
	 * altered the default global filter.
	 * 
	 * @return the new filter
	 */
	public static SyncFileFilter createBuiltInDefaultFilter() {
		SyncFileFilter sff = new SyncFileFilter();
		sff.addDefaults();
		return sff;
	}

	/**
	 * Get all patterns for this filter
	 * 
	 * @return patterns
	 */
	public ResourceMatcher[] getPatterns() {
		return filteredPaths.toArray(new ResourceMatcher[filteredPaths.size()]);
	}

	/**
	 * Get the pattern type for the pattern
	 * 
	 * @param pattern
	 * @return the type or null if this pattern is unknown in this filter.
	 */
	public PatternType getPatternType(ResourceMatcher pattern) {
		return patternToTypeMap.get(pattern);
	}

	/**
	 * Add the common, default list of paths to be filtered.
	 */
	public void addDefaults() {
		// In bug 370491, we decided not to filter binaries by default
		// this.addPattern(new BinaryResourceMatcher(), PatternType.EXCLUDE);
		this.addPattern(new PathResourceMatcher(new Path(".project")), PatternType.EXCLUDE); //$NON-NLS-1$
		this.addPattern(new PathResourceMatcher(new Path(".cproject")), PatternType.EXCLUDE); //$NON-NLS-1$
		this.addPattern(new PathResourceMatcher(new Path(".settings")), PatternType.EXCLUDE); //$NON-NLS-1$
		// TODO: This Git-specific directory is defined in multiple places - need to refactor.
		this.addPattern(new PathResourceMatcher(new Path(".ptp-sync")), PatternType.EXCLUDE); //$NON-NLS-1$
	}

	/**
	 * Add pattern to front of list (calls addPattern with position 0)
	 * 
	 * @param pattern
	 * @param type
	 */
	public void addPattern(ResourceMatcher pattern, PatternType type) {
		this.addPattern(pattern, type, 0);
	}

	/**
	 * Add a new pattern to the filter of the specified type at the specified position
	 * This function and others that manipulate the pattern list must enforce the invariant that no pattern appears more than once.
	 * This invariant is assumed by other functions.
	 * 
	 * @param pattern
	 * @param type
	 * @param pos
	 * @throws IndexOutOfBoundsException
	 *             if position is out of range
	 */
	public void addPattern(ResourceMatcher pattern, PatternType type, int pos) {
		if (patternToTypeMap.get(pattern) != null) {
			filteredPaths.remove(pattern);
		}
		filteredPaths.add(pos, pattern);
		patternToTypeMap.put(pattern, type);
	}

	/**
	 * Remove a pattern from the filter
	 * Assumes pattern appears no more than once
	 * 
	 * @param pattern
	 */
	public void removePattern(ResourceMatcher pattern) {
		filteredPaths.remove(pattern);
		patternToTypeMap.remove(pattern);
	}

	/**
	 * Swap a pattern with its lower-index neighbor
	 * Assumes pattern only appears once
	 * 
	 * @param pattern
	 * @return whether pattern was actually promoted
	 */
	public boolean promote(ResourceMatcher pattern) {
		int oldIndex = filteredPaths.indexOf(pattern);
		if (oldIndex > 0) {
			filteredPaths.remove(oldIndex);
			filteredPaths.add(oldIndex - 1, pattern);
			return true;
		}

		return false;
	}

	/**
	 * Swap a pattern with its higher-index neighbor
	 * Assumes pattern appears no more than once
	 * 
	 * @param pattern
	 * @return whether pattern was actually demoted
	 */
	public boolean demote(ResourceMatcher pattern) {
		int oldIndex = filteredPaths.indexOf(pattern);
		if (oldIndex > -1 && oldIndex < filteredPaths.size() - 1) {
			filteredPaths.remove(oldIndex);
			filteredPaths.add(oldIndex + 1, pattern);
			return true;
		}

		return false;
	}

	/**
	 * Replace pattern with another - useful for when existing patterns are edited
	 * 
	 * @param oldPattern
	 * @param newPattern
	 * @param type
	 * @return whether replace was successful (old pattern was found)
	 */
	public boolean replacePattern(ResourceMatcher oldPattern, ResourceMatcher newPattern, PatternType type) {
		int index = filteredPaths.indexOf(oldPattern);
		if (index == -1) {
			return false;
		}
		this.removePattern(oldPattern);
		this.addPattern(newPattern, type, index);
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
		// Cannot ignore a folder if it contains members that should not be ignored.
		if (r instanceof IFolder) {
			try {
				for (IResource member : ((IFolder) r).members()) {
					if (!this.shouldIgnore(member)) {
						return false;
					}
				}
			} catch (CoreException e) {
				// Could mean folder doesn't exist, which is fine. Continue with testing.
			}
		}

		for (ResourceMatcher pm : filteredPaths) {
			if (pm.match(r)) {
				PatternType type = patternToTypeMap.get(pm);
				assert (pm != null);
				if (type == PatternType.EXCLUDE) {
					return true;
				} else {
					return false;
				}
			}
		}

		return false;
	}

	/**
	 * Store filter in the given preference node
	 * 
	 * @param preference
	 *            node
	 */
	public void saveFilter(Preferences prefRootNode) {
		// To clear pattern information, remove node, flush parent, and then recreate the node
		try {
			prefRootNode.node(PATTERN_NODE_NAME).removeNode();
			prefRootNode.flush();
		} catch (BackingStoreException e) {
			RDTSyncCorePlugin.log(Messages.SyncFileFilter_2, e);
			return;
		}
		Preferences prefPatternNode = prefRootNode.node(PATTERN_NODE_NAME);
		prefPatternNode.putInt(NUM_PATTERNS_KEY, filteredPaths.size());
		int i = 0;
		for (ResourceMatcher pm : filteredPaths) {
			Preferences prefMatcherNode = prefPatternNode.node(Integer.toString(i));
			// Whether pattern is exclusive or inclusive
			prefMatcherNode.put(PATTERN_TYPE_KEY, patternToTypeMap.get(pm).name());
			pm.saveMatcher(prefMatcherNode);
			i++;
		}
	}

	/**
	 * Load filter from the given preference node
	 * 
	 * @param preference
	 *            node
	 * @return the restored filter or null if the node does not contain a filter or if there are problems reading the filter
	 */
	public static SyncFileFilter loadFilter(Preferences prefRootNode) {
		try {
			if (!prefRootNode.nodeExists(PATTERN_NODE_NAME)) {
				return null;
			}
			Preferences prefPatternNode = prefRootNode.node(PATTERN_NODE_NAME);
			int numPatterns = prefPatternNode.getInt(NUM_PATTERNS_KEY, -1);
			if (numPatterns == -1) {
				RDTSyncCorePlugin.log(Messages.SyncFileFilter_1);
				return null;
			}

			SyncFileFilter filter = createEmptyFilter();
			for (int i = numPatterns - 1; i >= 0; i--) {
				if (!prefPatternNode.nodeExists(Integer.toString(i))) {
					RDTSyncCorePlugin.log(Messages.SyncFileFilter_1);
					return null;
				}

				Preferences prefMatcherNode = prefPatternNode.node(Integer.toString(i));

				// Load matcher type (whether pattern is exclusive or inclusive)
				String typeName = prefMatcherNode.get(PATTERN_TYPE_KEY, null);
				if (typeName == null) {
					RDTSyncCorePlugin.log(Messages.SyncFileFilter_1);
					return null;
				}
				SyncFileFilter.PatternType type = SyncFileFilter.PatternType.valueOf(typeName);

				// Load the actual matcher
				ResourceMatcher pm = ResourceMatcher.loadMatcher(prefMatcherNode);

				filter.addPattern(pm, type);
			}
			return filter;
		} catch (BackingStoreException e) {
			RDTSyncCorePlugin.log(Messages.SyncFileFilter_1, e);
			return null;
		} catch (InvocationTargetException e) {
			RDTSyncCorePlugin.log(Messages.SyncFileFilter_1, e);
			return null;
		} catch (ParserConfigurationException e) {
			RDTSyncCorePlugin.log(Messages.SyncFileFilter_1, e);
			return null;
		}
	}
}
