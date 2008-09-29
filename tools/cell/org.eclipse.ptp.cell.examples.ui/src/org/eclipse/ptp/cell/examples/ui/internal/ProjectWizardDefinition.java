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
package org.eclipse.ptp.cell.examples.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @author laggarcia
 * @since 1.1.1
 * 
 */
public class ProjectWizardDefinition {

	private static final String NAME = "name"; //$NON-NLS-1$

	private static final String SRC_ZIP = "srcZip"; //$NON-NLS-1$

	private static final String ZIP_INTERNAL_RELATIVE_PATH = "zipInternalRelativePath"; //$NON-NLS-1$

	private static final String OPEN = "open"; //$NON-NLS-1$

	private static final String PAGE_TITLE = "pageTitle"; //$NON-NLS-1$

	private static final String PAGE_DESCRIPTION = "pageDescription"; //$NON-NLS-1$

	private static final String PAGE_LABEL = "pageLabel"; //$NON-NLS-1$

	private String contributor;

	private String name;

	private String srcZip;

	private String zipInternalRelativePath;

	private String fileToOpen;

	private String pageTitle;

	private String pageDescription;

	private String pageLabel;

	/**
	 * 
	 */
	public ProjectWizardDefinition(
			IConfigurationElement projectWizardDefinitionElement) {
		contributor = projectWizardDefinitionElement.getContributor().getName();
		name = projectWizardDefinitionElement.getAttribute(NAME);
		srcZip = projectWizardDefinitionElement.getAttribute(SRC_ZIP);
		zipInternalRelativePath = projectWizardDefinitionElement
				.getAttribute(ZIP_INTERNAL_RELATIVE_PATH);
		fileToOpen = projectWizardDefinitionElement.getAttribute(OPEN);
		pageTitle = projectWizardDefinitionElement.getAttribute(PAGE_TITLE);
		pageDescription = projectWizardDefinitionElement
				.getAttribute(PAGE_DESCRIPTION);
		pageLabel = projectWizardDefinitionElement.getAttribute(PAGE_LABEL);
	}

	public String getContributingBundle() {
		return contributor;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the path to the zip file containing the Eclipse Project to be
	 * imported. This path is relative to the plugin root directory.
	 * 
	 * @return the source zip file path relative to the plug-in root folder.
	 */
	public String getSourceZip() {
		return srcZip;
	}

	/**
	 * Get the path of the folder inside the zip file that will be used as the
	 * root in the import proccess. This path is relative to the root of the zip
	 * file.
	 * 
	 * @return the path of the folder inside the zip file that will be used as
	 *         the root in the import process.
	 */
	public String getZipInternalRelativePath() {
		return zipInternalRelativePath;
	}

	public String getFileToOpen() {
		return fileToOpen;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public String getPageDescription() {
		return pageDescription;
	}

	public String getPageLabel() {
		return pageLabel;
	}

}
