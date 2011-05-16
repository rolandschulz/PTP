package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.views.TableView;

public class HideTableColumnAction extends Action {

	private String gid;
	private String title;
	private TableView view;
	
	public HideTableColumnAction(String gid, String title, TableView view) {
		super(title);
		this.gid = gid;
		this.title = title;
		this.view = view;
	}
	
	public void run() {
		ILguiItem lguiItem = LMLManager.getInstance().getSelectedLguiItem();
		lguiItem.getTableHandler().changeTableColumnsWidth(view.getWidths(), gid);
		lguiItem.getTableHandler().changeTableColumnsOrder(gid, view.getRemoveColumnOrder());
		LMLManager.getInstance().setTableColumnNonActive(gid, title);
	}

}