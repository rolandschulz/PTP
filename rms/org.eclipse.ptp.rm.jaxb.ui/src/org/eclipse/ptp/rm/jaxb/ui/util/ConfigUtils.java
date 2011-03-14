/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ConfigUtils implements IJAXBUINonNLSConstants {

	private ConfigUtils() {
	}

	/**
	 * Open a dialog that allows the user to choose a project.
	 * 
	 * @return selected project
	 */
	public static File chooseLocalProject(Shell shell) {
		IProject[] projects = getLocalProjects();
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, labelProvider);
		dialog.setTitle(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Title);
		dialog.setMessage(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Message);
		dialog.setElements(projects);
		if (dialog.open() == Window.OK) {
			IProject project = (IProject) dialog.getFirstResult();
			return new File(project.getLocationURI());
		}
		return null;
	}

	public static File exportResource(String resource, Shell shell) throws Throwable {
		if (resource == null || ZEROSTR.equals(resource)) {
			return null;
		}
		URL fUrl = FileLocator.toFileURL(JAXBInitializationUtils.getURL(resource));
		URI uri = fUrl.toURI();
		File source = new File(uri);
		FileDialog fileDialog = new FileDialog(shell, SWT.SINGLE | SWT.SAVE);
		fileDialog.setText(Messages.ConfigUtils_exportResourceTitle);
		String path = fileDialog.open();
		if (path == null) {
			return null;
		}
		File target = new File(path);
		if (target.equals(source)) {
			throw new IllegalArgumentException(Messages.ConfigUtils_exportResourceError_0);
		}

		FileInputStream fis = new FileInputStream(source);
		FileOutputStream fos = new FileOutputStream(target);

		long total = 0;
		long size = source.length();
		int recvd = 0;
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		try {
			while (size == UNDEFINED || total < size) {
				recvd = fis.read(buffer, 0, COPY_BUFFER_SIZE);
				if (recvd == UNDEFINED) {
					break;
				}
				if (recvd > 0) {
					fos.write(buffer, 0, recvd);
					total += recvd;
				}
			}
		} catch (IOException ioe) {
			throw new Throwable(Messages.ConfigUtils_exportResourceError_1, ioe);
		} finally {
			try {
				fos.flush();
				fos.getFD().sync();
				fos.close();
				fis.close();
			} catch (IOException ignore) {
			}
		}

		return target;
	}

	public static String getFileContents(File file) throws Throwable {
		StringBuffer buffer = new StringBuffer();
		if (file.exists() && file.isFile()) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try {
				while (true) {
					try {
						String line = reader.readLine();
						if (line == null) {
							break;
						}
						buffer.append(line).append(LINE_SEP);
					} catch (EOFException eof) {
						break;
					}
				}
			} finally {
				reader.close();
			}
		}
		return buffer.toString();
	}

	public static File getUserHome() {
		return new File(System.getProperty(JAVA_USER_HOME));
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static String writeContentsToFile(Shell shell, String contents, File file) throws Throwable {
		FileDialog fileDialog = new FileDialog(shell, SWT.SINGLE | SWT.SAVE);
		fileDialog.setText(Messages.ConfigUtils_exportResourceTitle);
		fileDialog.setOverwrite(true);
		fileDialog.setFileName(file.getName());
		String path = fileDialog.open();
		if (path == null) {
			return null;
		}

		FileWriter fw = new FileWriter(path, false);
		fw.write(contents);
		fw.flush();
		fw.close();
		return path;
	}

	private static IProject[] getLocalProjects() {
		IProject[] all = getWorkspaceRoot().getProjects();
		List<IProject> local = new ArrayList<IProject>();
		for (IProject p : all) {
			if (FILE_SCHEME.equals(p.getLocationURI().getScheme())) {
				local.add(p);
			}
		}
		return local.toArray(new IProject[0]);
	}
}
