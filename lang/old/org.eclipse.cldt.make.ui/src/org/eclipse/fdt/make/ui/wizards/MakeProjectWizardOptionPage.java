package org.eclipse.fdt.make.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.fdt.make.core.MakeCorePlugin;
import org.eclipse.fdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.fdt.make.ui.IMakeHelpContextIds;
import org.eclipse.fdt.ui.dialogs.ICOptionContainer;
import org.eclipse.fdt.ui.dialogs.ICOptionPage;
import org.eclipse.fdt.ui.dialogs.IndexerBlock;
import org.eclipse.fdt.ui.dialogs.ReferenceBlock;
import org.eclipse.fdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.fdt.ui.wizards.NewCProjectWizard;
import org.eclipse.fdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new CProjectWizardPage("basicCProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class MakeProjectWizardOptionPage extends NewCProjectWizardOptionPage {
	MakeWizardOptionBlock makeWizardBlock; 
	
	public class MakeWizardOptionBlock extends MakeProjectOptionBlock {
		IndexerBlock indexBlock;
		
		public MakeWizardOptionBlock(ICOptionContainer parent) {
			super(parent);
		}

		protected void addTabs() {
			addTab(new ReferenceBlock());
			super.addTabs();
			addTab(indexBlock = new IndexerBlock()); 
		}
		
		public void setupHelpContextIds(){
			List pages = getOptionPages();
			
			Iterator iter = pages.iterator();
			for( int i = 0; i < 6 && iter.hasNext(); i++ ) {
				ICOptionPage page = (ICOptionPage) iter.next();
				
				String id = null;
				switch( i ){
					case 0 : id = IMakeHelpContextIds.MAKE_PROJ_WIZ_PROJECTS_TAB;     break;
					case 1 : id = IMakeHelpContextIds.MAKE_PROJ_WIZ_MAKEBUILDER_TAB;  break;
					case 2 : id = IMakeHelpContextIds.MAKE_PROJ_WIZ_ERRORPARSER_TAB;  break;
					case 3 : id = IMakeHelpContextIds.MAKE_PROJ_WIZ_BINARYPARSER_TAB; break;
					case 4 : id = IMakeHelpContextIds.MAKE_PROJ_WIZ_DISCOVERY_TAB;    break;
					case 5 : id = IMakeHelpContextIds.MAKE_PROJ_WIZ_INDEXER_TAB;      break;
				}
				WorkbenchHelp.setHelp(page.getControl(), id);	
			}
		}
	}

	public MakeProjectWizardOptionPage(String title, String description) {
		super("MakeProjectSettingsPage"); //$NON-NLS-1$
		setTitle(title);
		setDescription(description);
	}

	protected TabFolderOptionBlock createOptionBlock() {
		return (makeWizardBlock  = new MakeWizardOptionBlock(this));
	}

	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.ui.dialogs.ICOptionContainer#getPreference()
	 */
	public Preferences getPreferences() {
		return MakeCorePlugin.getDefault().getPluginPreferences();
	}
	
	public boolean isIndexerEnabled(){
	  return	makeWizardBlock.indexBlock.isIndexEnabled();
	}

	public void setupHelpContextIds(){
		makeWizardBlock.setupHelpContextIds();
	}
}
