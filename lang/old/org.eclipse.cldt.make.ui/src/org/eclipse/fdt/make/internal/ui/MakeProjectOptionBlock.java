/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.make.internal.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.fdt.make.core.MakeBuilder;
import org.eclipse.fdt.make.core.MakeCorePlugin;
import org.eclipse.fdt.make.internal.ui.properties.MakePropertyPage;
import org.eclipse.fdt.make.ui.IMakeHelpContextIds;
import org.eclipse.fdt.make.ui.dialogs.DiscoveryOptionsBlock;
import org.eclipse.fdt.make.ui.dialogs.SettingsBlock;
import org.eclipse.fdt.ui.dialogs.BinaryParserBlock;
import org.eclipse.fdt.ui.dialogs.ICOptionContainer;
import org.eclipse.fdt.ui.dialogs.ICOptionPage;
import org.eclipse.fdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.help.WorkbenchHelp;

public class MakeProjectOptionBlock extends TabFolderOptionBlock {
	private ICOptionContainer optionContainer;
	public MakeProjectOptionBlock() {
		super(true);
	}
	
	public MakeProjectOptionBlock(ICOptionContainer parent) {
		super(parent);
		optionContainer = parent;
	}

	protected void addTabs() {
		addTab(new SettingsBlock(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID));
		addTab(new ErrorParserBlock(MakeCorePlugin.getDefault().getPluginPreferences()));
		addTab(new BinaryParserBlock());
		addTab(new DiscoveryOptionsBlock());
	}

	public void setOptionContainer(ICOptionContainer parent) {
		super.setOptionContainer( parent );
		optionContainer = parent;
	}
	public Control createContents(Composite parent) {
		Control control = super.createContents( parent );
		
		List optionPages = getOptionPages();
		Iterator iter = optionPages.iterator();
		for( int i = 0; i < 4 && iter.hasNext(); i++ ){
			ICOptionPage page = (ICOptionPage) iter.next();
			if( optionContainer != null && optionContainer instanceof MakePropertyPage )
				switch( i ){
					case 0 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_BUILDER_SETTINGS); break;
					case 1 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_ERROR_PARSER );    break;
					case 2 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_BINARY_PARSER );   break;
					case 3 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_DISCOVERY );       break;
				}
			else 
				switch( i ){
					case 0 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_BUILDER_SETTINGS);             break;
					case 1 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PREF_ERROR_PARSER );           break;
					case 2 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PREF_BINARY_PARSER );          break;
					case 3 : WorkbenchHelp.setHelp(page.getControl(), IMakeHelpContextIds.SCANNER_CONFIG_DISCOVERY_OPTIONS ); break;
				}
		}

		return control;
	}
}
