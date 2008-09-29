/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.linux.packagemanager.rpm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.ptp.cell.utils.debug.Debug;
import org.eclipse.ptp.cell.utils.packagemanager.PackageManager;
import org.eclipse.ptp.cell.utils.process.ProcessController;


/**
 * This class represents the RPM Package Management System used in various Linux
 * distributions (e.g. Fedora and RHEL). You should not create an instance of
 * it. The correct way of using this class is to get an instance from
 * getDefault() or, even better, using
 * PackageManagementSystemManager.getPackageManager to get the correct Package
 * Management System for your system
 * 
 * @author laggarcia
 * @since 1.3.1
 */
public class RPMPackageManager implements PackageManager {

	protected class PackageInformation {

		private String name;

		private boolean isInstalled;

		private long expireDate;

		public PackageInformation(String name, boolean isInstalled,
				long expireDate) {
			this.name = name;
			this.isInstalled = isInstalled;
			this.expireDate = expireDate;
		}

		public long getExpireDate() {
			return this.expireDate;
		}

		public boolean isInstalled() {
			return this.isInstalled;
		}

		public String getName() {
			return this.name;
		}

	}

	protected static final String PROCESS_NAME = "RPM"; //$NON-NLS-1$

	protected static final String RPM = "rpm"; //$NON-NLS-1$

	protected static final String RPM_QUERY_FLAG = "-q"; //$NON-NLS-1$

	protected static final String RPM_QUERY_FORMAT = "--queryformat %{NAME}\\n"; //$NON-NLS-1$

	protected static final String RPM_QUERY_LIST_FLAG = "-ql"; //$NON-NLS-1$

	protected static final long TIMEOUT = 2000;

	protected static final long CACHE_EXPIRE_DATE = 30000; // Cache information
															// expires in 30
															// seconds

	private static RPMPackageManager instance = null;

	private Map packageInformationCache;

	private RPMPackageManager() {
		this.packageInformationCache = new HashMap();
	}

	private StringBuffer addPackage(StringBuffer packages, String pack) {
		if (packages == null) {
			packages = new StringBuffer();
		} else {
			packages.append(WHITESPACE);
		}
		return packages.append(pack);
	}

	public static synchronized RPMPackageManager getDefault() {
		if (instance == null) {
			instance = new RPMPackageManager();
		}
		return instance;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public boolean query(String pack) {
		Debug.POLICY.enter(Debug.DEBUG_LINUX, pack);
		String[] arg = new String[1];
		arg[0] = pack;
		boolean result = queryAll(arg);
		Debug.POLICY.exit(Debug.DEBUG_LINUX, result);
		return result;
	}

	public boolean queryAll(String[] packs) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_LINUX, packs.toString());
		
		PackageInformation packageInformation;
		/*
		 * This doesn't need to be extremely accurate, hence, we are going to
		 * calculate the current time only here and use the same value in the
		 * rest of the method.
		 */
		long currentTime = System.currentTimeMillis(); 
		StringBuffer packages = null;

		for (int i = 0; i < packs.length; i++) {
			if ((packageInformation = (PackageInformation) this.packageInformationCache
					.get(packs[i])) != null) {
				// The package information is cached.
				if (packageInformation.getExpireDate() > currentTime) {
					Debug.POLICY.trace(Debug.DEBUG_LINUX, "Query package: {0} (cached, recent)", packs[i]); //$NON-NLS-1$
					// The package information is still valid.
					if (!packageInformation.isInstalled()) {
						// The package is not installed. We can return.
						Debug.POLICY.trace(Debug.DEBUG_LINUX, "Known that package is not installed {0}", packs[i]); //$NON-NLS-1$
						Debug.POLICY.exit(Debug.DEBUG_LINUX, false);
						return false;
					}
					Debug.POLICY.trace(Debug.DEBUG_LINUX, "Known that package is installed {0}", packs[i]); //$NON-NLS-1$
					// The package is installed. Continue to search other
					// packages.
				} else {
					Debug.POLICY.trace(Debug.DEBUG_LINUX, "Query package: {0} (cached, expired)", packs[i]); //$NON-NLS-1$
					// The package information is not valid anymore. We have to
					// check it with rpm command line tool.
					packages = addPackage(packages, packs[i]);
				}
			} else {
				Debug.POLICY.trace(Debug.DEBUG_LINUX, "Query package: {0} (not cached)", packs[i]); //$NON-NLS-1$
				// The package information is not cached. We have to check it
				// with rpm command line tool.
				packages = addPackage(packages, packs[i]);
			}
		}

		if (packages == null) {
			// All the packages information are cached and all of them are
			// installed.
			Debug.POLICY.exit(Debug.DEBUG_LINUX, true);
			return true;
		}

		String command = RPM + WHITESPACE + RPM_QUERY_FLAG + WHITESPACE
				+ RPM_QUERY_FORMAT + WHITESPACE + packages;

		try {
			Debug.POLICY.trace(Debug.DEBUG_LINUX, command);
			Process process = ProcessFactory.getFactory().exec(command);
			// The process controller thread will destory the process if it
			// blocks for more than TIMEOUT seconds or if the user cancel the
			// operation.
			ProcessController processController = new ProcessController(
					PROCESS_NAME, process, TIMEOUT);
			processController.start();
			
			BufferedReader processOutput = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line;
			Pattern p = Pattern
					.compile(RPMPackageManagerMessages.rpmIsNotInstalledErrorMessagePattern);
			line = processOutput.readLine();
			Debug.POLICY.trace(Debug.DEBUG_LINUX_MORE, line);
			while (line != null) {
				Matcher m = p.matcher(line);
				if (m.matches()) {
					// Package is not installed. Update package information
					// cache.
					this.packageInformationCache.put(m.group(1),
							new PackageInformation(m.group(1), false,
									currentTime + CACHE_EXPIRE_DATE));
					Debug.POLICY.trace(Debug.DEBUG_LINUX, "Package is not installed: {0}", m.group(1)); //$NON-NLS-1$
					Debug.POLICY.exit(Debug.DEBUG_LINUX, false);
					return false;
				}
				// Package is installed. Update package information cache.
				this.packageInformationCache.put(line, new PackageInformation(
						line, true, currentTime + CACHE_EXPIRE_DATE));
				Debug.POLICY.trace(Debug.DEBUG_LINUX, "Package is installed: {0}", line); //$NON-NLS-1$
				line = processOutput.readLine();
				Debug.POLICY.trace(Debug.DEBUG_LINUX_MORE, line);
			}
		} catch (IOException e) {
			Debug.POLICY.error(Debug.DEBUG_LINUX, e.getMessage());
			Debug.POLICY.exit(Debug.DEBUG_LINUX, false);
			Debug.POLICY.logError(e, RPMPackageManagerMessages.RPMPackageManager_FailedExcecution, command);
			return false;
		}

		// The process suceeded and its output not indicated the package is not
		// installed
		Debug.POLICY.exit(Debug.DEBUG_LINUX, true);
		return true;

	}

	public boolean queryAll(String packs, String delimiter) {
		return queryAll(packs.split(delimiter));
	}

	public List queryList(String pack) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_LINUX, pack);
		String command = RPM + WHITESPACE + RPM_QUERY_LIST_FLAG + WHITESPACE
				+ pack;
		List fileList;

		try {
			Debug.POLICY.trace(Debug.DEBUG_LINUX, command);
			Process process = ProcessFactory.getFactory().exec(command);
			// The process controller thread will destory the process if it
			// blocks for more than TIMEOUT seconds or if the user cancel the
			// operation.
			ProcessController processController = new ProcessController(
					PROCESS_NAME, process, TIMEOUT);
			processController.start();

			BufferedReader processOutput = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			// Read first line and check if the package is installed
			String line = processOutput.readLine();
			Debug.POLICY.trace(Debug.DEBUG_LINUX_MORE, line);
			if (line
					.endsWith(RPMPackageManagerMessages.rpmIsNotInstalledErrorMessage)) {
				Debug.POLICY.trace(Debug.DEBUG_LINUX, "Package {0} is not installed", pack); //$NON-NLS-1$
				Debug.POLICY.exit(Debug.DEBUG_LINUX);
				return new ArrayList();
			}
			fileList = new ArrayList();
			// The package is installed. Read the other outputed lines
			fileList.add(line);
			while ((line = processOutput.readLine()) != null) {
				fileList.add(line);
			}
			Debug.POLICY.trace(Debug.DEBUG_LINUX, "Added {0} files to list", fileList.size()); //$NON-NLS-1$

			if (process.waitFor() != 0) {
				Debug.POLICY.trace(Debug.DEBUG_LINUX, "Process failed with exit code {0}", process.exitValue()); //$NON-NLS-1$
				// If the process fail, there is no way to make sure the package
				// is installed
				processController.interrupt();
				Debug.POLICY.exit(Debug.DEBUG_LINUX);
				return new ArrayList();
			}
			processController.interrupt();
		} catch (InterruptedException e) {
			Debug.POLICY.error(Debug.DEBUG_LINUX, e);
			Debug.POLICY.exit(Debug.DEBUG_LINUX);
			return new ArrayList();
		} catch (IOException e) {
			Debug.POLICY.error(Debug.DEBUG_LINUX, e);
			Debug.POLICY.exit(Debug.DEBUG_LINUX);
			Debug.POLICY.logError(e, RPMPackageManagerMessages.RPMPackageManager_FailedExcecution, command);
			return new ArrayList();
		}

		// The process suceeded and its output not indicated the package is not
		// installed
		Debug.POLICY.exit(Debug.DEBUG_LINUX, fileList.toString());
		return fileList;
	}

	public String searchFileInQueryListAndReturnInitialPathSegments(
			String pack, String pathEnd) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_LINUX, pack, pathEnd);
		Iterator files = queryList(pack).iterator();
		while (files.hasNext()) {
			String file = (String) files.next();
			int lastIndexOfLastSegments = file.lastIndexOf(pathEnd);
			if (lastIndexOfLastSegments != -1) {
				String result = file.substring(0, lastIndexOfLastSegments);
				Debug.POLICY.exit(Debug.DEBUG_LINUX, result);
				return result;
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_LINUX, null);
		return null;

	}

	public String searchFileInQueryListAndReturnFullPath(String pack,
			String pathEnd) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_LINUX, pack, pathEnd);
		Iterator files = queryList(pack).iterator();
		while (files.hasNext()) {
			String file = (String) files.next();
			if (file.endsWith(pathEnd)) {
				Debug.POLICY.exit(Debug.DEBUG_LINUX, file);
				return file;
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_LINUX, null);
		return null;
	}

}
