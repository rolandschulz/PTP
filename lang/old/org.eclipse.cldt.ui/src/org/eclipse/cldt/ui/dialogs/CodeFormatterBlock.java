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

package org.eclipse.cldt.ui.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.core.FortranCorePreferenceConstants;
import org.eclipse.cldt.internal.ui.ICHelpContextIds;
import org.eclipse.cldt.internal.ui.util.PixelConverter;
import org.eclipse.cldt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 */
public class CodeFormatterBlock {

	private HashMap idMap = new HashMap();
	Preferences fPrefs;
	protected Combo fFormatterCombo;
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_ID="id"; //$NON-NLS-1$
	// This is a hack until we have a default Formatter.
	// For now it is comment out in the plugin.xml
	private static final String NONE="(NONE)"; //$NON-NLS-1$


	public CodeFormatterBlock(Preferences prefs) {
		fPrefs = prefs;
		initializeFormatters();
	}

	public void performOk() {
		String text = fFormatterCombo.getText();
		String selection = (String)idMap.get(text);
		if (selection != null && selection.length() > 0) {
			HashMap options = FortranCorePlugin.getOptions();
			String formatterID = (String)options.get(FortranCorePreferenceConstants.CODE_FORMATTER);
			if (formatterID == null || !formatterID.equals(selection)) {
				options.put(FortranCorePreferenceConstants.CODE_FORMATTER, selection);
				FortranCorePlugin.setOptions(options);
			}
		} else {
			// simply reset to the default one.
			performDefaults();
		}
	}

	public void performDefaults() {
		HashMap optionsDefault = FortranCorePlugin.getDefaultOptions();
		HashMap options = FortranCorePlugin.getOptions();
		String formatterID = (String)optionsDefault.get(FortranCorePreferenceConstants.CODE_FORMATTER);
		options.put(FortranCorePreferenceConstants.CODE_FORMATTER, formatterID);
		FortranCorePlugin.setOptions(options);

		fFormatterCombo.clearSelection();
		fFormatterCombo.setText(NONE);
		Iterator iterator = idMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String val = (String)entry.getValue();
			if (val != null && val.equals(formatterID)) {
				fFormatterCombo.setText((String)entry.getKey());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite control = ControlFactory.createComposite(parent, 2);
		((GridLayout) control.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout) control.getLayout()).marginWidth = 5;

		WorkbenchHelp.setHelp(control, ICHelpContextIds.CODEFORMATTER_PREFERENCE_PAGE);

		ControlFactory.createEmptySpace(control, 2);

		Label label = ControlFactory.createLabel(control, "Formatters:");
		label.setLayoutData(new GridData());
		fFormatterCombo = new Combo(control, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		fFormatterCombo.setLayoutData(gd);
		fFormatterCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleFormatterChanged();
			}
		});
		Iterator items = idMap.keySet().iterator();
		while (items.hasNext()) {
			fFormatterCombo.add((String) items.next());
		}

		initDefault();
		handleFormatterChanged();
		return control;
	}

	public void handleFormatterChanged() {	
		// TODO: UI part.
	}

	public void initDefault() {
		boolean init = false;
		String selection = FortranCorePlugin.getOption(FortranCorePreferenceConstants.CODE_FORMATTER);
		if (selection != null) {
			Iterator iterator = idMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry)iterator.next();
				String val = (String)entry.getValue();
				if (val != null && val.equals(selection)) {
					fFormatterCombo.setText((String)entry.getKey());
					init = true;
				}
			}
		}
		if (!init) {
			fFormatterCombo.setText(NONE);
		}
	}

	private void initializeFormatters() {
		idMap = new HashMap();
		idMap.put(NONE, null);
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(FortranCorePlugin.PLUGIN_ID, FortranCorePlugin.FORMATTER_EXTPOINT_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			for (int i = 0; i < exts.length; i++) {
		 		IConfigurationElement[] elements = exts[i].getConfigurationElements();
		 		for (int j = 0; j < elements.length; ++j) {
		 			String name = elements[j].getAttribute(ATTR_NAME);
		 			idMap.put(name, elements[j].getAttribute(ATTR_ID));
		 		}
			}
		}
	}

}
