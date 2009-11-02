/*******************************************************************************
 * Copyright (c) 2009, University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.photran.core.FortranCorePlugin;

/**
 * Wizard page for the Create New Fortran Source File wizard ({@link NewSourceFileCreationWizard}).
 * 
 * @author Jeff Overbey
 * 
 * @see NewSourceFileCreationWizard
 */
@SuppressWarnings("restriction")
public class NewSourceFileCreationWizardPage extends org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileCreationWizardPage {
	protected Template[] getApplicableTemplates() {
		return StubUtility.getFileTemplatesForContentTypes(
				new String[] {
				    FortranCorePlugin.FREE_FORM_CONTENT_TYPE,
				    FortranCorePlugin.FIXED_FORM_CONTENT_TYPE }, null);
	}
}
