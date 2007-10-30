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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexFileNameFilter implements FilenameFilter {
	Pattern pattern;
	Matcher matcher;
	RegexFileNameFilter(String regex) {
		this.pattern = Pattern.compile(regex);
	}
	public boolean accept(File dir, String name) {
		this.matcher = pattern.matcher(name);			
		return matcher.matches();
	}
}