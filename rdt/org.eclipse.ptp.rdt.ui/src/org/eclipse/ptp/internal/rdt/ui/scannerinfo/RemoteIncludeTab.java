package org.eclipse.ptp.internal.rdt.ui.scannerinfo;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.ui.newui.IncludeTab;


/**
 * Reuse the standard CDT IncludeTab but override the functionality
 * of the Add and Edit buttons to pop up our custom remote dialog.
 * 
 * @author Mike Kucera
 */
public class RemoteIncludeTab extends IncludeTab {
	
	private static final String DIALOG_TITLE = "Remote Include Directory"; //$NON-NLS-1$

	@Override
	public ICLanguageSettingEntry doAdd() {
		
		RemoteIncludeDialog dlg = new RemoteIncludeDialog(usercomp.getShell(), DIALOG_TITLE);
		
		if(dlg.open() && dlg.getDirectory().trim().length() > 0 ) {
			toAllCfgs = dlg.isAllConfigurations();
			toAllLang = dlg.isAllLanguages();
			int flags = 0;
			return new CIncludePathEntry(dlg.getDirectory(), flags);
		}
		
		return null;
	}
	
	@Override
	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		
		RemoteIncludeDialog dlg = new RemoteIncludeDialog(usercomp.getShell(), DIALOG_TITLE);
		dlg.setPathText(ent.getValue());
		
		if(dlg.open()) {
			int flags = 0;
			return new CIncludePathEntry(dlg.getDirectory(), flags);
		}
		
		return null;
	}
	
}
