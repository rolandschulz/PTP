/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests.miner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

final class FileManager {

	private static final String RESOURCE_DIR = "resources/indexer/";	
	private static final String SCOPE_DIRECTORY = "standalonetest";
	
	public static final String PROPERTY_INCLUDE_PATHS_FILE = "minertest.includePathsFile";
	
	private static String[] FILES_TO_EXTRACT = {
		"DocumentManager.cpp", 
		"DocumentManager.h", 
		"extramail.cpp", 
		"mail.cpp", 
		"reftest.cpp"
	};
	
	
	private static File testDirectory = null;
	
	private FileManager() {}
	
	
	/**
	 * Returns the directory where the index will be created and the files will be located.
	 */
	public static synchronized File getTestDirectory() {
		if(testDirectory == null) {
			String userHome = System.getProperties().getProperty("user.home"); //$NON-NLS-1$
			File userDir = new File(userHome + File.separator + SCOPE_DIRECTORY);
			if(userDir.exists())
				return testDirectory = userDir;
			if(userDir.mkdir()) // creates the directory if possible
				return testDirectory = userDir;
			
			return null;
		}
		
		return testDirectory;
	}
	

	public static List<String> copyProjectContent() throws IOException  {
		List<String> tus = new LinkedList<String>();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File outputDirectory = getTestDirectory();
		
		for(String resource : FILES_TO_EXTRACT) {
			InputStream in = classLoader.getResourceAsStream(RESOURCE_DIR + resource);
			File destination = new File(outputDirectory, resource);
			OutputStream out = new FileOutputStream(destination);
			
			byte[] buffer = new byte[1000];
			int length;
			while((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			
			in.close();
			out.close();
			
			tus.add(destination.getAbsolutePath());
		}
		
		return tus;
	}
	
	
	public static String[] getIncludePaths() throws IOException {
		String includePathsFile = System.getProperty(PROPERTY_INCLUDE_PATHS_FILE);
		if(includePathsFile == null) {
			System.out.println("No include paths file given.");
			System.out.println("Provide a file of include paths using the system property '" + PROPERTY_INCLUDE_PATHS_FILE + "'");
			return new String[0];
		}
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(includePathsFile));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			return new String[0];
		}
		
		List<String> includePaths = new LinkedList<String>();
		String line;
		while((line = reader.readLine()) != null) {
			System.out.println("Include path: '" + line + "'");
			includePaths.add(line);
		}
		return includePaths.toArray(new String[0]);
	}
	
}
