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
package org.eclipse.ptp.rm.pbs.ui.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.ui.IPBSAttributeToTemplateConverter;
import org.eclipse.ptp.rm.pbs.ui.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.data.PBSBatchScriptTemplate;
import org.eclipse.ptp.rm.pbs.ui.launch.PBSRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigUtils;
import org.eclipse.ptp.rm.pbs.ui.utils.PBSAttributeToTemplateConverterFactory;
import org.eclipse.swt.widgets.Shell;

/**
 * Controls the selection and configuration of batch script templates.
 * 
 * @see org.eclipse.ptp.rm.pbs.ui.data.PBSBatchScriptTemplate
 * 
 * @author arossi
 * 
 */
public class PBSBatchScriptTemplateManager implements IPBSNonNLSConstants {

	private PBSBatchScriptTemplate current;

	private final PBSRMLaunchConfigurationDynamicTab launchTab;
	private final IPBSAttributeToTemplateConverter converter;

	public PBSBatchScriptTemplateManager(PBSRMLaunchConfigurationDynamicTab launchTab) throws Throwable {
		this.launchTab = launchTab;
		this.converter = PBSAttributeToTemplateConverterFactory.getConverter(getRMConfig());
		configureConverter();
	}

	public void addImportedTemplate(final File imported) throws Throwable {
		String name = imported.getName();
		name = validateTemplateNameForEdit(name);
		String template = ConfigUtils.readFull(imported, PBSBatchScriptTemplate.BUFFER_SIZE);
		if (ZEROSTR.equals(template))
			throw new Throwable(Messages.PBSRMLaunchConfigTemplate_zerostringError);
		ByteArrayInputStream bais = new ByteArrayInputStream(template.getBytes());
		new PBSBatchScriptTemplate(converter).load(bais);
		getRMConfig().addTemplate(name, template);
	}

	public void exportTemplate(final String dir, final String original, final String renamed) throws Throwable {
		FileWriter fw = null;
		try {
			String validated = validateTemplateNameForEdit(renamed);
			String template = getRMConfig().getTemplate(original);
			if (ZEROSTR.equals(template))
				throw new Throwable(Messages.PBSRMLaunchConfigTemplate_zerostringError);
			File export = new File(dir, validated);
			fw = new FileWriter(export, false);
			fw.write(template);
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
	 * Looks in the resource manager configuration for all
	 * <code>_template</code> strings and loads their names.
	 */
	public String[] findAvailableTemplates() {
		IPBSResourceManagerConfiguration c = getRMConfig();
		if (c == null)
			return new String[0];
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
		if (current != null)
			return current.getName();
		IPBSResourceManagerConfiguration c = getRMConfig();
		if (c != null)
			return c.getCurrentTemplateName();
		return FULL_TEMPLATE;
	}

	public PBSResourceManager getRM() {
		return launchTab.getResourceManager();
	}

	public IPBSResourceManagerConfiguration getRMConfig() {
		PBSResourceManager rm = getRM();
		if (rm == null)
			return null;
		return (IPBSResourceManagerConfiguration) rm.getConfiguration();
	}

	/**
	 * The is the full template, with all valid attributes mapped to qsub flags.
	 * 
	 * @throws Throwable
	 */
	public boolean handleBaseTemplates() throws Throwable {
		PBSResourceManager rm = getRM();
		IPBSResourceManagerConfiguration c = getRMConfig();
		Shell shell = PBSUIPlugin.getActiveWorkbenchShell();
		if (rm == null || !rm.getState().equals(ResourceManagerAttributes.State.STARTED)) {
			MessageDialog dialog = new MessageDialog(shell, Messages.PBSAttributeTemplateManager_requestStartTitle, null,
					Messages.PBSAttributeTemplateManager_requestStartMessage, MessageDialog.QUESTION, new String[] {
							Messages.PBSAttributeTemplateManager_requestStartContinue,
							Messages.PBSAttributeTemplateManager_requestStartCancel }, 1);
			if (MessageDialog.CANCEL == dialog.open())
				return false;
		}
		if (!configureConverter()) {
			new MessageDialog(shell, Messages.PBSAttributeTemplateManager_requestInitializeTitle, null,
					Messages.PBSAttributeTemplateManager_requestInitializeMessage, MessageDialog.WARNING,
					new String[] { Messages.PBSAttributeTemplateManager_requestStartCancel }, 0).open();
			return false;
		}
		String fullTemplate = converter.generateFullBatchScriptTemplate();
		if (fullTemplate == null || ZEROSTR.equals(fullTemplate)) {
			if (c != null)
				fullTemplate = c.getTemplate(FULL_TEMPLATE);
			if (fullTemplate == null || ZEROSTR.equals(fullTemplate)) {
				new MessageDialog(shell, Messages.PBSAttributeTemplateManager_requestInitializeTitle, null,
						Messages.PBSAttributeTemplateManager_requestInitializeMessage, MessageDialog.WARNING,
						new String[] { Messages.PBSAttributeTemplateManager_requestStartCancel }, 0).open();
				return false;
			}
			return true;
		}
		if (c != null) {
			c.addTemplate(FULL_TEMPLATE, fullTemplate);
			String minTemplate = converter.generateMinBatchScriptTemplate();
			if (minTemplate != null && !ZEROSTR.equals(minTemplate))
				c.addTemplate(MIN_TEMPLATE, minTemplate);
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
		IPBSResourceManagerConfiguration c = getRMConfig();
		if (c == null)
			return null;
		PBSBatchScriptTemplate template = null;
		if (choice == null || ConfigUtils.ZEROSTR.equals(choice))
			choice = c.getCurrentTemplateName();
		try {
			String serialized = c.getTemplate(choice);
			if (serialized == null)
				return null;
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
		if (name.equals(FULL_TEMPLATE) || name.equals(MIN_TEMPLATE))
			throw new IllegalAccessError(name + Messages.PBSAttributeTemplateManager_removeError);
		IPBSResourceManagerConfiguration c = getRMConfig();
		if (c != null)
			c.removeTemplate(name);
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
		IPBSResourceManagerConfiguration c = getRMConfig();
		if (c != null)
			c.addTemplate(name, editedContent);
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
		if (name.length() == 0)
			throw new IllegalArgumentException(Messages.PBSRMLaunchConfigEditChoose_illegalArgument);
		if (!name.endsWith(TEMPLATE_SUFFIX))
			name = name + TEMPLATE_SUFFIX;
		else if (name.equals(FULL_TEMPLATE) || name.equals(MIN_TEMPLATE))
			throw new IllegalAccessError(name + Messages.PBSAttributeTemplateManager_storeError);
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
			IPBSResourceManagerConfiguration c = getRMConfig();
			if (c != null)
				stored = c.getValidAttributeSet();
			if (stored == null)
				return false;
			ByteArrayInputStream bais = new ByteArrayInputStream(stored.getBytes());
			converter.getData().deserialize(bais);
		} else
			converter.setAttributeDefinitions(modelAttributes);
		converter.initialize();
		storeValidAttributeSet(true);
		return true;
	}

	private IAttributeDefinition<?, ?, ?>[] getModelAttributeDefinitions() {
		PBSResourceManager rm = getRM();
		if (rm == null)
			return null;
		IAttributeDefinition<?, ?, ?>[] defs = rm.getAttributeDefinitionManager().getAttributeDefinitions();

		if (defs.length == 0)
			return null;
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
		IPBSResourceManagerConfiguration c = getRMConfig();
		if (c == null)
			return;
		if (!force && c.getValidAttributeSet() != null)
			return;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
		converter.getData().serialize(baos);
		c.setValidAttributeSet(baos.toString());
	}
}