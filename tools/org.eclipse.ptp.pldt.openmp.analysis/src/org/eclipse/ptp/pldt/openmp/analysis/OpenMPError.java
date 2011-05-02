/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis;

/**
 * @author pazel
 */
public class OpenMPError
{
	protected String description_ = "";
	protected String filename_ = "";
	protected String path_ = "";
	protected int lineno_ = 0;
	protected int severity_ = INFO;

	// severity codes
	public static final int INFO = 0;
	public static final int WARN = 1;
	public static final int ERROR = 2;

	/**
	 * OpenMPError - holds error information
	 * 
	 * @param description
	 *            - of problem
	 * @param filename
	 *            - of problem
	 * @param path
	 *            - of filename
	 * @param lineno
	 *            - of problem
	 * @param severity
	 *            - of problem
	 */
	public OpenMPError(String description, String filename, String path, int lineno, int severity)
	{
		description_ = description;
		filename_ = filename;
		path_ = path;
		lineno_ = lineno;
		severity_ = severity;
	}

	/**
	 * OpenMPError - holds error information
	 * 
	 * @param description
	 *            - String
	 * @param fqn
	 *            - String (path+filename)
	 * @param lineno
	 *            - of problem
	 * @param severity
	 *            - of problem
	 */
	public OpenMPError(String description, String fqn, int lineno, int severity)
	{
		description_ = description;

		int l1 = fqn.lastIndexOf('/');
		int l2 = fqn.lastIndexOf('\\');
		int lastIndex = Math.max(l1, l2);
		if (lastIndex == -1 || lastIndex > fqn.length() + 1)
			filename_ = fqn;
		else {
			filename_ = fqn.substring(lastIndex + 1);
			path_ = fqn.substring(0, lastIndex);
		}

		lineno_ = lineno;
		severity_ = severity;
	}

	// accessors

	public String getDescription() {
		return description_;
	}

	public String getFilename() {
		return filename_;
	}

	public String getPath() {
		return path_;
	}

	public int getLineno() {
		return lineno_;
	}

	public int getSeverity() {
		return severity_;
	}
}
