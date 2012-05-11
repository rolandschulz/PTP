/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.scannerinfo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.IncludeTab;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.serviceproviders.IRemoteToolsIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.serviceproviders.RSECIndexServiceProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * Reuse the standard CDT IncludeTab but override the functionality of the Add
 * and Edit buttons to pop up our custom remote dialog.
 */
public class RemoteIncludeTab extends IncludeTab {

	@Override
	public ICLanguageSettingEntry doAdd() {
		RemoteIncludeDialog dlg = new RemoteIncludeDialog(usercomp.getShell(), Messages.RemoteIncludeTab_title, false, getResDesc()
				.getConfiguration());

		setRemoteConnection(dlg);
		if (dlg.open() && dlg.getDirectory().trim().length() > 0) {
			toAllCfgs = dlg.isAllConfigurations();
			toAllLang = dlg.isAllLanguages();
			int flags = 0;
			return new CIncludePathEntry(dlg.getDirectory(), flags);
		}

		return null;
	}

	@Override
	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		RemoteIncludeDialog dlg = new RemoteIncludeDialog(usercomp.getShell(), Messages.RemoteIncludeTab_title, true, getResDesc()
				.getConfiguration());

		dlg.setPathText(ent.getValue());
		setRemoteConnection(dlg);

		if (dlg.open()) {
			int flags = 0;
			return new CIncludePathEntry(dlg.getDirectory(), flags);
		}

		return null;
	}

	@Override
	public String getHelpContextId() {
		return RDTHelpContextIds.REMOTE_INCLUDE_TAB;
	}

	private void setRemoteConnection(RemoteIncludeDialog dlg) {
		IServiceModelManager manager = ServiceModelManager.getInstance();
		IServiceConfiguration config = manager.getActiveConfiguration(page.getProject());
		if (config != null) {
			IService service = manager.getService(IRDTServiceConstants.SERVICE_C_INDEX);
			if (service != null) {
				IServiceProvider provider = config.getServiceProvider(service);
				if (provider != null) {
					if (provider instanceof RSECIndexServiceProvider) {
						dlg.setHost(((RSECIndexServiceProvider) provider).getHost());
					} else if (provider instanceof IRemoteToolsIndexServiceProvider) {
						dlg.setConnection(((IRemoteToolsIndexServiceProvider) provider).getConnection());
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractLangsListTab#getLangSetting(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
		ICLanguageSetting[] langSettings_orig = super.getLangSetting(rcDes);
		List<ICLanguageSetting> langSettings = new ArrayList<ICLanguageSetting>();
		
		// bug 378579 - remove the old Remote XL C++ Compiler's C source file input type in case 
		// this is a project created before the fix for bug 378579
		for (int i = 0; i < langSettings_orig.length; i++) {
			if (langSettings_orig[i].getId().indexOf("org.eclipse.ptp.rdt.managedbuilder.xlc.ui.cpp.c.compiler.input") == -1) { //$NON-NLS-1$
				langSettings.add(langSettings_orig[i]);
			}
		}
		return langSettings.toArray(new ICLanguageSetting[langSettings.size()]);
	}
}
