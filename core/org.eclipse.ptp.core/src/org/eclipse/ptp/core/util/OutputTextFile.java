/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.core.PreferenceConstants;

/**
 *
 */
public class OutputTextFile implements PreferenceConstants {
	private String filename = "";

	private File file = null;

	private String outputPath = null;

	private int storeLine = 0;

	private int lineCounter = 0;

	public OutputTextFile(String name, String type, String outputPath, int storeLine) {
		this.filename = name + "." + type;
		this.outputPath = outputPath;
		this.storeLine = storeLine;
		init();
	}

	private void init() {
		file = getFilePath();
	}

	private File getFilePath() {
		IPath filePath = new Path(outputPath).append(filename);
		File tmpFile = filePath.toFile();
		try {
			tmpFile.createNewFile();
		} catch (IOException e) {
			System.out.println("OutputTextFile - getFilePath err: "
					+ e.getMessage());
		}
		return tmpFile;
	}

	public void write(String text) {
		if (lineCounter == storeLine) {
			lineCounter = 0;
			write(text, false);
		} else
			write(text, true);
	}

	public void write(String text, boolean isAppend) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, isAppend);
			fos.write(text.getBytes());
			lineCounter++;
		} catch (FileNotFoundException e) {
			System.out.println("OutputTextFile - append file err: "
					+ e.getMessage());
		} catch (IOException ioe) {
			System.out.println("OutputTextFile - append io err: "
					+ ioe.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe) {
					System.out.println("OutputTextFile - append close err: "
							+ ioe.getMessage());
				}
			}
			fos = null;
		}
	}

	public void delete() {
		if (file.exists())
			file.delete();

		file = null;
	}

	public String getContents() {
		if (file == null)
			return null;

		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
			return readString(is, ResourcesPlugin.getEncoding());
		} catch (FileNotFoundException e) {
			System.out.println("OutputTextFile - read file err: "
					+ e.getMessage());
		} finally {
			is = null;
		}
		return null;
	}

	private String readString(InputStream is, String encoding) {
		if (is == null)
			return null;

		BufferedReader reader = null;
		try {
			StringBuffer buffer = new StringBuffer();
			char[] part = new char[2048];
			int read = 0;
			reader = new BufferedReader(new InputStreamReader(is, encoding));
			while ((read = reader.read(part)) != -1)
				buffer.append(part, 0, read);

			return buffer.toString();
		} catch (IOException ex) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}
}
