/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.photran.cdtinterface.CDTInterfacePlugin;

/**
 * Wizard to create a new Fortran source folder.
 * 
 * @author Jeff Overbey
 */
public class NewSourceFolderCreationWizard extends org.eclipse.cdt.ui.wizards.NewSourceFolderCreationWizard
{
	public NewSourceFolderCreationWizard()
	{
		super();
		setDefaultPageImageDescriptor(CDTInterfacePlugin.getImageDescriptor("icons/wizban/newsrcfldr_wiz.gif"));
		setWindowTitle("New Source Folder");
	}
}
