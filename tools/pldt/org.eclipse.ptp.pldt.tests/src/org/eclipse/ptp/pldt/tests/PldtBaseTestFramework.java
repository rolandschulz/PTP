/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.pldt.internal.common.IDs;
import org.eclipse.ptp.pldt.tests.infrastructure.BaseTestFramework;
import org.osgi.framework.Bundle;

/**
 * Basic Test framework for PLDT tests, extends that of CDT
 * 
 * @author Beth Tibbitts
 * 
 */
public abstract class PldtBaseTestFramework extends BaseTestFramework {
	private static HashMap<String, ArrayList<Integer>> lineMaps = new HashMap<String, ArrayList<Integer>>();

	/**
	 * Return a file imported for use in the tests. Includes determining if the
	 * file exists
	 */
	protected IFile importFile(String srcDir, String filename) throws Exception {
		assertTrue("Missing file: " + filename, testExists(srcDir, filename));
		IFile result = super.importFile(filename, readTestFile(srcDir, filename));
		return result;
	}

	/**
	 * Determine if a file exists
	 * 
	 * @param srcDir
	 *            source directory in which the file should be located
	 * @param filename
	 *            file name of file to find
	 * @return
	 */
	private boolean testExists(String srcDir, String filename) {
		String fullname = srcDir + File.separator + filename;
		IPath path = new Path(fullname);
		Activator a = Activator.getDefault();
		Bundle bundle = a.getBundle();
		URL url = FileLocator.find(bundle, path, null);
		if (url == null) {
			// System.out.println(filename+" **NOT FOUND***");
			return false;
		}
		return true;

	}

	protected String readTestFile(String srcDir, String filename) throws IOException, URISyntaxException {
		ArrayList<Integer> lineMap = new ArrayList<Integer>(50);
		lineMaps.put(filename, lineMap);
		lineMap.add(0); // Offset of line 1
		return readStream(lineMap, getClass().getResourceAsStream("/" + srcDir + "/" + filename));
	}

	protected String readStream(ArrayList<Integer> lineMap, InputStream inputStream) throws IOException {
		StringBuffer sb = new StringBuffer(4096);
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		for (int offset = 0, ch = in.read(); ch >= 0; ch = in.read()) {
			sb.append((char) ch);
			offset++;

			if (ch == '\n' && lineMap != null) {
				// System.out.println("Line " + (lineMap.size()+1) +
				// " starts at offset " + offset);
				lineMap.add(offset);
			}
		}
		in.close();
		return sb.toString();
	}

	protected String readStream(InputStream inputStream) throws IOException {
		return readStream(null, inputStream);
	}

	protected String readWorkspaceFile(String filename) throws IOException, CoreException {
		return readStream(project.getFile(filename).getContents());
	}

	/**
	 * @param filename
	 * @param line
	 *            line number, starting at 1
	 * @param col
	 *            column number, starting at 1
	 */
	protected int getLineColOffset(String filename, int line, int col) {
		return lineMaps.get(filename).get(line - 1) + (col - 1);
	}

	/**
	 * Convenience class for sorting artifacts so we compare them in an expected
	 * order
	 * 
	 * @author beth
	 * 
	 */
	public class ArtifactWithLine implements Comparable {
		public int getLineNo() {
			return lineNo;
		}

		public String getName() {
			return name;
		}

		public IMarker getMarker() {
			return marker;
		}

		private int lineNo;
		private String name;
		private IMarker marker;

		public ArtifactWithLine(int line, String nam) {
			lineNo = line;
			name = nam;
		}

		public ArtifactWithLine(IMarker marker) {
			try {
				String nam = marker.getAttribute(IDs.NAME).toString();
				String line = getLineNoAttr(marker);
				Integer ii = Integer.decode(line);
				int theInt = ii.intValue();
				this.lineNo = theInt;
				this.name = nam;
				this.marker = marker;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public boolean equals(ArtifactWithLine other) {
			boolean a = this.lineNo == other.lineNo;
			boolean b = this.name.equals(other.name);
			return a && b;
		}

		public String toString() {
			return lineNo + ": " + name;
		}

		boolean traceOn = false;

		public int compareTo(Object o) {
			int result;
			String sign = "=";
			ArtifactWithLine other = (ArtifactWithLine) o;
			if (this.lineNo < other.lineNo) {
				result = -1;
				if (traceOn)
					sign = "<";
			} else if (this.lineNo > other.lineNo) {
				result = 1;
				if (traceOn)
					sign = ">";
			}
			// lineNo's equal, must compare name
			else {
				result = this.name.compareTo(other.name);
			}
			// System.out.println("CompareTo: "+this+" -to- "+other+"; result is: "+result);
			if (traceOn)
				System.out.println("ArtifactWithLine.compareTo: " + this.lineNo + sign + other.lineNo);
			return result;
		}
	}

	public String getLineNoAttr(IMarker marker) throws CoreException {
		return marker.getAttribute(IMarker.LINE_NUMBER).toString();
	}

	public String getNameAttr(IMarker marker) throws CoreException {
		return (String) marker.getAttribute(IDs.NAME);
	}

	public String getMethodName() {
		String name = (new Exception().getStackTrace()[1].getMethodName());
		return name;
	}
}
