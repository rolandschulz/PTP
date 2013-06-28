/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.SyncWizardDataCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DefaultBuildConfigWidget extends Composite {
	private static final String ConfigMapKey = "config-map"; //$NON-NLS-1$

	private final String[] syncConfigs;
	private final String[] buildConfigs;
	private final Map<String, String> syncConfigToBuildConfigMap;

	private final Composite composite;

	public DefaultBuildConfigWidget(Composite parent, int style, String[] sConfigs, String[] bConfigs,
			Map<String, String> configMap) {
		super(parent, style);

		syncConfigs = new String[sConfigs.length];
		System.arraycopy(sConfigs, 0, syncConfigs, 0, sConfigs.length);

		buildConfigs = new String[bConfigs.length];
		System.arraycopy(bConfigs, 0, buildConfigs, 0, bConfigs.length);

		syncConfigToBuildConfigMap = Collections.synchronizedMap(new HashMap<String, String>(configMap));
		SyncWizardDataCache.setMap(ConfigMapKey, syncConfigToBuildConfigMap);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create page headings
		Label syncConfigLabel = new Label(composite, SWT.CENTER);
		syncConfigLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		syncConfigLabel.setText(Messages.SyncConfigToBuildConfigWizardPage_10);
		Label buildConfigLabel = new Label(composite, SWT.CENTER);
		buildConfigLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		buildConfigLabel.setText(Messages.SyncConfigToBuildConfigWizardPage_11);

		// Create row for each sync config
		for (final String sname : syncConfigs) {
			// Label for sync config
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
			label.setText(sname + ":"); //$NON-NLS-1$
			final Combo combo = new Combo(composite, SWT.READ_ONLY);

			// Combo for sync config. Contains all build configs with default build config selected.
			int initialSelection = -1;
			String defaultBuildConfig = syncConfigToBuildConfigMap.get(sname);
			for (String bname : buildConfigs) {
				if (bname.equals(defaultBuildConfig)) {
					initialSelection = combo.getItemCount();
				}
				combo.add(bname);
			}
			if (initialSelection > -1) {
				combo.select(initialSelection);
			}

			combo.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = combo.getSelectionIndex();
					if (index >= 0) {
						syncConfigToBuildConfigMap.put(sname, combo.getText());
					} else {
						syncConfigToBuildConfigMap.remove(sname);
					}
					SyncWizardDataCache.setMap(ConfigMapKey, syncConfigToBuildConfigMap);
				}
			});
		}
	}
}
