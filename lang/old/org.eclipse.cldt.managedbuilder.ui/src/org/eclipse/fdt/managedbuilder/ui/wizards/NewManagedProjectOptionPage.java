/**********************************************************************
 * Copyright (c) 2002,2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
package org.eclipse.fdt.managedbuilder.ui.wizards;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.fdt.managedbuilder.internal.ui.ErrorParserBlock;
import org.eclipse.fdt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.fdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.fdt.managedbuilder.internal.ui.ManagedProjectOptionBlock;
import org.eclipse.fdt.ui.dialogs.ICOptionPage;
import org.eclipse.fdt.ui.dialogs.IndexerBlock;
import org.eclipse.fdt.ui.dialogs.ReferenceBlock;
import org.eclipse.fdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.fdt.ui.wizards.NewFortranProjectWizard;
import org.eclipse.fdt.ui.wizards.NewFortranProjectWizardOptionPage;
import org.eclipse.ui.help.WorkbenchHelp;

public class NewManagedProjectOptionPage extends NewFortranProjectWizardOptionPage {
	

	public class ManagedWizardOptionBlock extends ManagedProjectOptionBlock {
		
		NewManagedProjectOptionPage parent;
		ErrorParserBlock errorParsers;
		IndexerBlock indexBlock;
		

		public ManagedWizardOptionBlock(NewManagedProjectOptionPage parentPage) {
			super(parentPage);
			parent = parentPage;
		}
		
		public void updateProjectTypeProperties() {
			//  Update the error parser list
			if (errorParsers != null) {
				errorParsers.updateValues();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.fdt.ui.dialogs.TabFolderOptionBlock#addTabs()
		 */
		protected void addTabs() {
			addTab(new ReferenceBlock());
			// NOTE: The setting of error parsers is commented out here
			//       because they need to be set per-configuration.
			//       The other tabs on this page are per-project.
			//       Error parsers can be selected per configuration in the 
			//        project properties
			//errorParsers = new ErrorParserBlock();
			//addTab(errorParsers);
			addTab(indexBlock = new IndexerBlock());
		}
		
		public void setupHelpContextIds(){
			List pages = getOptionPages();
			
			Iterator iter = pages.iterator();
			for( int i = 0; i < 3 && iter.hasNext(); i++ ) {
				ICOptionPage page = (ICOptionPage) iter.next();
				
				String id = null;
				if (page instanceof ReferenceBlock) {
					id = ManagedBuilderHelpContextIds.MAN_PROJ_WIZ_PROJECTS_TAB;
				} else if (page instanceof ErrorParserBlock) {
					id = ManagedBuilderHelpContextIds.MAN_PROJ_WIZ_ERRORPARSERS_TAB;
				} else if (page instanceof IndexerBlock) {
					id = ManagedBuilderHelpContextIds.MAN_PROJ_WIZ_INDEXER_TAB;
				}
				WorkbenchHelp.setHelp(page.getControl(), id);	
				
			}
		}
	}
	
	protected ManagedWizardOptionBlock optionBlock;
	protected NewManagedProjectWizard parentWizard;

	/**
	 * @param pageName
	 */
	public NewManagedProjectOptionPage(String pageName, NewManagedProjectWizard parentWizard) {
		super(pageName);
		this.parentWizard = parentWizard;
		optionBlock = new ManagedWizardOptionBlock(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.ui.wizards.NewFortranProjectWizardOptionPage#createOptionBlock()
	 */
	protected TabFolderOptionBlock createOptionBlock() {
		return optionBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		return ((NewFortranProjectWizard)getWizard()).getNewProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.ui.dialogs.ICOptionContainer#getPreferenceStore()
	 */
	public Preferences getPreferences() {
		return ManagedBuilderUIPlugin.getDefault().getPluginPreferences();
	}
	
	public void updateProjectTypeProperties() {
		//  Update the error parser list
		optionBlock.updateProjectTypeProperties();
	}
	
	public void setupHelpContextIds(){
		optionBlock.setupHelpContextIds();
	}
	
}
