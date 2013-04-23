/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.BuildConfigUtils;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.ui.AbstractSynchronizeProperties;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizePropertiesDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class SynchronizeProperties extends AbstractSynchronizeProperties {
	private Group fUserDefinedContent;
	private Combo fConfigCombo;

	public SynchronizeProperties(ISynchronizePropertiesDescriptor descriptor) {
		super(descriptor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#createConfigurationArea(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.operation.IRunnableContext)
	 */
	@Override
	public void createConfigurationArea(Composite parent, IProject project, IRunnableContext context) {
		fUserDefinedContent = new Group(parent, SWT.NONE);
		fUserDefinedContent.setText("CDT Build Configurations"); //$NON-NLS-1$
		fUserDefinedContent.setLayout(new GridLayout(2, false));
		fUserDefinedContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		Label label = new Label(fUserDefinedContent, SWT.NONE);
		label.setText("Link Configuration:"); //$NON-NLS-1$
		fConfigCombo = new Combo(fUserDefinedContent, SWT.READ_ONLY);
		fConfigCombo.setItems(getConfigurationNames(project));
		fConfigCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
	}

	private String[] getConfigurationNames(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo != null) {
			return buildInfo.getConfigurationNames();
		}
		return new String[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#selectConfiguration(org.eclipse.ptp.rdt.sync.core.SyncConfig)
	 */
	@Override
	public void selectConfiguration(SyncConfig syncConfig) {
		IConfiguration config = BuildConfigUtils.getBuildConfigurationForSyncConfig(syncConfig.getProject(), syncConfig.getName());
		if (config != null) {
			for (int i = 0; i < fConfigCombo.getItemCount(); i++) {
				if (fConfigCombo.getItem(i).equals(config.getName())) {
					fConfigCombo.select(i);
					break;
				}
			}
		}
	}

}