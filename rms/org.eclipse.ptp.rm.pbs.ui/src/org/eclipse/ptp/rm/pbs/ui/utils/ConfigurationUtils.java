/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * Various utility methods for configuring preferences.
 * 
 * @author arossi
 */
public class ConfigurationUtils {
	public static class PrefixFilter implements FilenameFilter {
		private final String prefix;

		public PrefixFilter(String prefix) {
			this.prefix = prefix;
		}

		public boolean accept(File dir, String name) {
			return name.startsWith(prefix);
		}

	}

	public static class SuffixFilter implements FilenameFilter {
		private final String suffix;

		public SuffixFilter(String suffix) {
			this.suffix = suffix;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(suffix);
		}
	}

	private static class VersionSorter implements Comparator<File> {
		public int compare(File s1, File s2) {
			return s1.getName().compareTo(s2.getName()) * -1;
		}
	}

	private static VersionSorter vs = new VersionSorter();

	private ConfigurationUtils() {
	}

	/**
	 * Searches the Platform install location for the "features" dir, and then
	 * for the feature plugins corresponding to the prefix in that dir; sorts
	 * the features by decreasing version number, and returns the first.
	 * 
	 * @param prefix
	 *            for the plugin name
	 * @return
	 * @throws URISyntaxException
	 */
	public static File findFeatureDir(String prefix) throws URISyntaxException {
		Location loc = Platform.getInstallLocation();
		File dir = new File(new URI(loc.getURL().toString()));
		dir = new File(dir, "features"); //$NON-NLS-1$
		PrefixFilter pf = new PrefixFilter(prefix);
		File[] features = dir.listFiles(pf);
		if (features.length == 0)
			return null;
		Arrays.sort(features, vs);
		return features[0];
	}
}
