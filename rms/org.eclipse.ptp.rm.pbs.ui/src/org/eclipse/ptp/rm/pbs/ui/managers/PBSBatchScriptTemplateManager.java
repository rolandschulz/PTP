/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - implementation
 *  				- reworked 05/11/2010
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.data.PBSBatchScriptTemplate;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigUtils;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigUtils.SuffixFilter;
import org.osgi.framework.Bundle;

/**
 * Controls the selection and configuration of batch script templates.
 * 
 * @see org.eclipse.ptp.rm.pbs.ui.data.PBSBatchScriptTemplate
 * 
 * @author arossi
 * 
 */
public class PBSBatchScriptTemplateManager {
	private static final String DEFAULT_TEMPLATE = Messages.PBSBatchScriptTemplateManager_defaultTemplate;
	private static final String FULL_TEMPLATE = Messages.PBSBatchScriptTemplateManager_fullTemplate;
	private static final String RESOURCE_PATH = Messages.PBSBatchScriptTemplateManager_resourcePath
			+ System.getProperty("file.separator");
	private static final String SUFFIX = Messages.PBSBatchScriptTemplateManager_templateSuffix;

	private PBSBatchScriptTemplate current;

	public PBSBatchScriptTemplateManager() throws Throwable {
		initializeStore();
	}

	/**
	 * Looks in the known locations for all <code>_template</code> files and
	 * loads their names.
	 * <p>
	 * If the two standard files are not present in the template dir, they are
	 * copied there.
	 * </p>
	 */
	public String[] findAvailableTemplates() {
		List<String> templateNames = new ArrayList<String>();
		try {
			File attrconf = PBSUIPlugin.getDefault().getStateLocation()
					.toFile();
			SuffixFilter filter = new SuffixFilter(SUFFIX);
			File[] properties = attrconf.listFiles(filter);
			for (int i = 0; i < properties.length; i++)
				templateNames.add(properties[i].getName());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return templateNames.toArray(new String[0]);
	}

	public PBSBatchScriptTemplate getCurrent() {
		return current;
	}

	/**
	 * Constructs and template and calls load using a stream fro the file
	 * corresponding to the choice parameter. Sets the configuration before
	 * loading.
	 * 
	 * @param choice
	 *            name of the template file
	 * @param config
	 *            current launch configuration
	 * @return populated template object
	 */
	public PBSBatchScriptTemplate loadTemplate(String choice,
			ILaunchConfiguration config) {
		current = new PBSBatchScriptTemplate();
		if (choice == null || ConfigUtils.EMPTY_STRING.equals(choice)) {
			return current;
		}
		try {
			File f = new File(PBSUIPlugin.getDefault().getStateLocation()
					.toFile(), choice);
			if (!f.exists())
				return current;
			current.setConfiguration(config);
			current.load(new FileInputStream(f));
			current.setName(choice);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return current;
	}

	/**
	 * Checks first to make sure user is not attempting to remove one of the
	 * fixed lists.
	 * 
	 * @param name
	 *            of template to remove
	 * @throws IllegalAccessError
	 */
	public void removeTemplate(String name) throws IllegalAccessError {
		if (DEFAULT_TEMPLATE.equals(name) || FULL_TEMPLATE.equals(name))
			throw new IllegalAccessError(name
					+ Messages.PBSAttributeTemplateManager_removeError);

		File configFile = new File(PBSUIPlugin.getDefault().getStateLocation()
				.toFile(), name);
		if (configFile.exists())
			configFile.delete();
	}

	/**
	 * Called after Edit action. Validates and writes content to persistent
	 * store location.
	 * 
	 * @param editedContent
	 *            of current template
	 * @param fileName
	 *            to which to write contents
	 * @throws IOException
	 * @throws NoSuchElementException
	 * @throws IllegalAccessError
	 */
	public void storeTemplate(String editedContent, String fileName)
			throws IOException, NoSuchElementException, IllegalAccessError {
		FileWriter fw = null;
		try {
			File configFile = new File(PBSUIPlugin.getDefault()
					.getStateLocation().toFile(), fileName);
			if (configFile.exists())
				configFile.delete();
			fw = new FileWriter(configFile, false);
			fw.write(editedContent);
			fw.flush();
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException t) {
					t.printStackTrace();
				}
		}
	}

	/**
	 * Checks to make sure user is not attempting to overwrite one of the fixed
	 * lists.
	 * 
	 * @param fileName
	 *            for the template
	 * @return the fileName if valid
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessError
	 */
	public String validateTemplateNameForEdit(String fileName)
			throws IllegalArgumentException, IllegalAccessError {
		if (fileName.length() == 0)
			throw new IllegalArgumentException(
					Messages.PBSRMLaunchConfigEditChoose_illegalArgument);
		if (!fileName
				.endsWith(Messages.PBSBatchScriptTemplateManager_templateSuffix))
			fileName = fileName
					+ Messages.PBSBatchScriptTemplateManager_templateSuffix;

		if (DEFAULT_TEMPLATE.equals(fileName) || FULL_TEMPLATE.equals(fileName))
			throw new IllegalAccessError(fileName
					+ Messages.PBSAttributeTemplateManager_storeError);
		return fileName;
	}

	/*
	 * Instantiates the two preconfigured templates from plugin resources.
	 */
	private PBSBatchScriptTemplate getResourceTemplate(String name)
			throws Throwable {
		Bundle bundle = PBSUIPlugin.getDefault().getBundle();
		URL url = FileLocator
				.find(bundle, new Path(RESOURCE_PATH + name), null);
		if (url == null)
			return null;
		InputStream s = null;
		PBSBatchScriptTemplate template = null;
		try {
			s = url.openStream();
			template = new PBSBatchScriptTemplate();
			template.load(s);
			template.setName(name);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
			}
		}
		return template;
	}

	/*
	 * Checks to make sure the read-only templates have been stored in the
	 * persistent location.
	 */
	private void initializeStore() throws Throwable {
		File attrconf = PBSUIPlugin.getDefault().getStateLocation().toFile();
		if (!attrconf.exists())
			attrconf.mkdirs();
		String[] found = findAvailableTemplates();
		Set<String> readOnly = new HashSet<String>();
		readOnly.add(DEFAULT_TEMPLATE);
		readOnly.add(FULL_TEMPLATE);
		for (int i = 0; i < found.length; i++)
			if (DEFAULT_TEMPLATE.equals(found[i])
					|| FULL_TEMPLATE.equals(found[i])) {
				readOnly.remove(found[i]);
				if (readOnly.isEmpty())
					return;
			}

		for (Iterator<String> i = readOnly.iterator(); i.hasNext();) {
			String name = i.next();
			PBSBatchScriptTemplate template = getResourceTemplate(name);
			if (template != null)
				storeTemplate(template.getText(), name);
		}
	}
}