/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.ui.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.actions.ShowViewAction;
import org.eclipse.ui.actions.CompoundContributionItem;

public class LLGuiOpenContributionItem extends CompoundContributionItem {

	private final LMLManager lmlManager = LMLManager.getInstance();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> list = new ArrayList<IContributionItem>();
		ILguiItem selected = lmlManager.getSelectedLguiItem();
		Map<String, String> gids = selected.getLayoutAccess().getInactiveComponents();
		for (Map.Entry<String, String> gid : gids.entrySet()) {
			list.add(new ActionContributionItem(new ShowViewAction(gid)));
		}
		return list.toArray(new IContributionItem[0]);
	}
}
