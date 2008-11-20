package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.ptp.internal.rdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ptp.internal.rdt.ui.search.actions.SelectionSearchGroup;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Remote version of the outline page.
 * 
 * @author Mike Kucera
 */
public class RemoteCContentOutlinePage extends CContentOutlinePage {

	public RemoteCContentOutlinePage(CEditor editor) {
		super(editor);
	}

	@Override
	protected ActionGroup createSearchActionGroup() {
		return new SelectionSearchGroup(this);
	}

	@Override
	protected ActionGroup createOpenViewActionGroup() {
		OpenViewActionGroup ovag= new OpenViewActionGroup(this);
		ovag.setEnableIncludeBrowser(true);
		return ovag;
	}
}
