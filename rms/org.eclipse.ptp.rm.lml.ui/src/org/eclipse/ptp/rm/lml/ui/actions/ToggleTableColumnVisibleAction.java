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
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.views.TableView;

public class ToggleTableColumnVisibleAction extends Action {

	private final String gid;
	private final String title;
	private final TableView view;
	private boolean visible;

	public ToggleTableColumnVisibleAction(String gid, String title, boolean initialVisible, TableView view) {
		super(title, Action.AS_CHECK_BOX);
		this.gid = gid;
		this.title = title;
		this.view = view;
		visible = initialVisible;
		setChecked(visible);
	}

	@Override
	public void run() {
		ILguiItem lguiItem = LMLManager.getInstance().getSelectedLguiItem();
		lguiItem.getTableHandler().changeTableColumnsWidth(view.getWidths(), gid);
		lguiItem.getTableHandler().changeTableColumnsOrder(gid, view.getRemoveColumnOrder());
		if (!visible) {
			LMLManager.getInstance().setTableColumnActive(gid, title);
		} else {
			LMLManager.getInstance().setTableColumnNonActive(gid, title);
		}
		visible = !visible;
		setChecked(visible);
	}

}
