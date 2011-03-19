/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - implementation
 *  				- reworked 05/11/2010
 *                  - version 5.0: now writes to the resourceManager config
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.templates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.ConfigUtils;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

/**
 * Controls the selection and configuration of batch script templates.
 * 
 * @see org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplate
 * 
 * @author arossi
 * @since 5.0
 * 
 */
public class PBSBatchScriptTemplateManager implements IPBSNonNLSConstants {

	private PBSBatchScriptTemplate current;

	private final PBSResourceManager resourceManager;
	private final IPBSAttributeToTemplateConverter converter;

	public PBSBatchScriptTemplateManager(PBSResourceManager rm) throws Throwable {
		this.resourceManager = rm;
		this.converter = PBSAttributeToTemplateConverterFactory.getConverter(getRMConfig());
		configureConverter();
	}

	public void addImportedTemplate(final File imported) throws Throwable {
		String name = imported.getName();
		name = validateTemplateNameForEdit(name);
		String template = ConfigUtils.readFull(imported, PBSBatchScriptTemplate.BUFFER_SIZE);
		if (ZEROSTR.equals(template)) {
			throw new Throwable(Messages.PBSBatchScriptTemplateManager_zerostringError);
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(template.getBytes());
		new PBSBatchScriptTemplate(converter).load(bais);
		getRMConfig().addTemplate(name, template);
	}

	public void exportTemplate(final String dir, final String original, final String renamed) throws Throwable {
		FileWriter fw = null;
		try {
			String validated = validateTemplateNameForEdit(renamed);
			String template = getRMConfig().getTemplate(original);
			if (ZEROSTR.equals(template)) {
				throw new Throwable(Messages.PBSBatchScriptTemplateManager_zerostringError);
			}
			File export = new File(dir, validated);
			fw = new FileWriter(export, false);
			fw.write(template);
			fw.flush();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException t) {
					t.printStackTrace();
				}
			}
		}
	}

	/**
	 * Looks in the resource manager configuration for all
	 * <code>_template</code> strings and loads their names.
	 */
	public String[] findAvailableTemplates() {
		PBSResourceManagerConfiguration c = getRMConfig();
		if (c == null) {
			return new String[0];
		}
		return c.getTemplateNames();
	}

	public PBSBatchScriptTemplate getCurrent() {
		return current;
	}

	/**
	 * Either returns the name of the loaded template, or looks in the
	 * configuration for the last stored one; if both are undefined, it returns
	 * "base_template".
	 * 
	 * @return name
	 */
	public String getCurrentTemplateName() {
		if (current != null) {
			return current.getName();
		}
		PBSResourceManagerConfiguration c = getRMConfig();
		if (c != null) {
			return c.getCurrentTemplateName();
		}
		return FULL_TEMPLATE;
	}

	public PBSResourceManagerConfiguration getRMConfig() {
		return (PBSResourceManagerConfiguration) resourceManager.getConfiguration();
	}

	/**
	 * The is the full template, with all valid attributes mapped to qsub flags.
	 * 
	 * @throws Throwable
	 */
	public boolean handleBaseTemplates() throws Throwable {
		PBSResourceManagerConfiguration c = getRMConfig();
		if (!configureConverter()) {
			return false;
		}
		String fullTemplate = converter.generateFullBatchScriptTemplate();
		if (fullTemplate == null || ZEROSTR.equals(fullTemplate)) {
			if (c != null) {
				fullTemplate = c.getTemplate(FULL_TEMPLATE);
			}
			if (fullTemplate == null || ZEROSTR.equals(fullTemplate)) {
				return false;
			}
			return true;
		}
		if (c != null) {
			c.addTemplate(FULL_TEMPLATE, fullTemplate);
			String minTemplate = converter.generateMinBatchScriptTemplate();
			if (minTemplate != null && !ZEROSTR.equals(minTemplate)) {
				c.addTemplate(MIN_TEMPLATE, minTemplate);
			}
		}
		return true;
	}

	/**
	 * Constructs a template and calls load using a stream from the serialized
	 * string stored in the resource manager configuration corresponding to the
	 * choice parameter. Sets the launch configuration before loading.
	 * 
	 * @param choice
	 *            name of the template file
	 * @param config
	 *            current launch configuration
	 * @return populated template object
	 */
	public PBSBatchScriptTemplate loadTemplate(String choice, ILaunchConfiguration config) {
		PBSResourceManagerConfiguration c = getRMConfig();
		if (c == null) {
			return null;
		}
		PBSBatchScriptTemplate template = null;
		if (choice == null || ConfigUtils.ZEROSTR.equals(choice)) {
			choice = c.getCurrentTemplateName();
		}
		try {
			String serialized = c.getTemplate(choice);
			if (serialized == null) {
				return null;
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(serialized.getBytes());
			template = new PBSBatchScriptTemplate(converter);
			template.setConfiguration(config);
			template.load(bais);
			template.setName(choice);
			if (config != null) {
				c.setCurrentTemplateName(choice);
				current = template;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return template;
	}

	/**
	 * Checks first to make sure user is not attempting to remove the base
	 * template.
	 * 
	 * @param name
	 *            of template to remove
	 * @throws IllegalAccessError
	 */
	public void removeTemplate(String name) throws IllegalAccessError {
		if (name.equals(FULL_TEMPLATE) || name.equals(MIN_TEMPLATE)) {
			throw new IllegalAccessError(name + Messages.PBSBatchScriptTemplateManager_removeError);
		}
		PBSResourceManagerConfiguration c = getRMConfig();
		if (c != null) {
			c.removeTemplate(name);
		}
	}

	/**
	 * Called after Edit action. Writes content to resource manager
	 * configuration.
	 * 
	 * @param editedContent
	 *            of current template
	 * @param name
	 *            to which to write contents
	 */
	public void storeTemplate(String editedContent, String name) {
		validateTemplateNameForEdit(name);
		PBSResourceManagerConfiguration c = getRMConfig();
		if (c != null) {
			c.addTemplate(name, editedContent);
		}
	}

	/**
	 * Checks to make sure user is not attempting to overwrite the base
	 * template.
	 * 
	 * @param name
	 *            for the template
	 * @return the name if valid
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessError
	 */
	public String validateTemplateNameForEdit(String name) throws IllegalArgumentException, IllegalAccessError {
		if (name.length() == 0) {
			throw new IllegalArgumentException(Messages.PBSBatchScriptTemplateManager_illegalArgument);
		}
		if (!name.endsWith(TEMPLATE_SUFFIX)) {
			name = name + TEMPLATE_SUFFIX;
		} else if (name.equals(FULL_TEMPLATE) || name.equals(MIN_TEMPLATE)) {
			throw new IllegalAccessError(name + Messages.PBSBatchScriptTemplateManager_storeError);
		}
		return name;
	}

	/*
	 * If we are offline, check for the last configuration of attributes for
	 * this resource manager and use that.
	 */
	private boolean configureConverter() throws Throwable {
		IAttributeDefinition<?, ?, ?>[] modelAttributes = getModelAttributeDefinitions();
		if (modelAttributes == null) {
			String stored = null;
			PBSResourceManagerConfiguration c = getRMConfig();
			if (c != null) {
				stored = c.getValidAttributeSet();
			}
			if (stored == null) {
				return false;
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(stored.getBytes());
			converter.getData().deserialize(bais);
		} else {
			converter.setAttributeDefinitions(modelAttributes);
		}
		converter.initialize();
		storeValidAttributeSet(true);
		return true;
	}

	private IAttributeDefinition<?, ?, ?>[] getModelAttributeDefinitions() {
		IRuntimeSystem rts = resourceManager.getRuntimeSystem();
		if (rts == null) {
			return null;
		}
		IAttributeDefinition<?, ?, ?>[] defs = rts.getAttributeDefinitionManager().getAttributeDefinitions();

		if (defs.length == 0) {
			return null;
		}
		return defs;
	}

	/*
	 * This is useful for avoiding having to read in the model definition or
	 * contact the resource manager everytime we reload the manager.
	 * 
	 * @param force overwrite of current list. If <code>false</code> and the
	 * list exists, this method simply returns.
	 */
	private void storeValidAttributeSet(boolean force) throws Throwable {
		PBSResourceManagerConfiguration c = getRMConfig();
		if (c == null) {
			return;
		}
		if (!force && c.getValidAttributeSet() != null) {
			return;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
		converter.getData().serialize(baos);
		c.setValidAttributeSet(baos.toString());
	}
}