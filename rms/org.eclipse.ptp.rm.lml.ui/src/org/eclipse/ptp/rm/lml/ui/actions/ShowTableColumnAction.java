/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */
package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.views.TableView;

public class ShowTableColumnAction extends Action {

	private final String gid;
	private final String title;
	private TableView view;

	public ShowTableColumnAction(String gid, String title, TableView view) {
		super(title);
		this.gid = gid;
		this.title = title;
		this.view = view;
	}

	public void run() {
		ILguiItem lguiItem = LMLCorePlugin.getDefault().getLMLManager().getSelectedLguiItem();
		lguiItem.getTableHandler().changeTableColumnsWidth(view.getWidths(), gid);
		lguiItem.getTableHandler().changeTableColumnsOrder(gid, view.getRemoveColumnOrder());
		LMLCorePlugin.getDefault().getLMLManager().setTableColumnActive(gid, title);
	}


}
