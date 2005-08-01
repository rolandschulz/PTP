/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.ui.actions.CreateGroupAction;
import org.eclipse.ptp.ui.actions.DeleteGroupAction;
import org.eclipse.ptp.ui.actions.DeleteProcessAction;
import org.eclipse.ptp.ui.actions.GroupAction;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElementGroup;
import org.eclipse.ptp.ui.model.IGroupManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * @author Clement chu
 *
 */
public abstract class AbstractParallelGroupView extends AbstractParallelElementView {
	// default actions
	protected ParallelAction createGroupAction = null;
	protected ParallelAction deleteGroupAction = null;
	protected ParallelAction deleteProcessAction = null;
	
	protected int DEFAULT_DEL_KEY = '\u007f';
	
	//Set element to display 
	protected abstract void initialElement();
	
	protected void initialKey(String os) {
		super.initialKey(os);
		if (os.equals(Platform.OS_MACOSX)) {
			DEFAULT_DEL_KEY = 8;
		}
	}	
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createBuildInToolBarActions();
		createBuildInMenuActions();
		initialView();
		createContextMenu();
	}	
	
	protected void initialView() {
		if (groupManager.size() == 1)
			initialElement();

		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		IElementGroup[] groups = groupManager.getSortedGroups();
		for (int i = 1; i < groups.length; i++) {
			IAction action = new GroupAction(groups[i].getID(), this);
			groups[i].setSelected(false);
			manager.add(action);
		}
		selectGroup(groupManager.getGroupRoot().getID());
		updateMenu(manager);
	}	
	
	protected void createBuildInToolBarActions() {		
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		if (createToolBarActions(toolBarMgr))
			toolBarMgr.add(new Separator());

		//default actions
		createGroupAction = new CreateGroupAction(this);
		deleteGroupAction = new DeleteGroupAction(this);
		deleteProcessAction = new DeleteProcessAction(this);

		toolBarMgr.add(createGroupAction);
		toolBarMgr.add(deleteGroupAction);
		toolBarMgr.add(deleteProcessAction);
	}
	/**
	 * @return true - need seperator, false - no seperator
	 */
	protected abstract boolean createToolBarActions(IToolBarManager toolBarMgr);
	
	protected void createBuildInMenuActions() {
		IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		if (createMenuActions(menuMgr))
			menuMgr.add(new Separator());
		
		//default Root menu
		IAction action = new GroupAction(IGroupManager.GROUP_ROOT_ID, this);
		action.setText(GroupAction.GROUP_ROOT);
		menuMgr.add(action);
	}
	protected abstract boolean createMenuActions(IMenuManager menuMgr);

	protected void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				//if right click occur, eclipse will ignore the key up event, so clear keyCode when popup occur.
				keyCode = SWT.NONE;
				fillBuildInContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(drawComp);
		drawComp.setMenu(menu);
		// Be sure to register it so that other plug-ins can add actions.
		getSite().registerContextMenu(menuMgr, this);
	}

	protected void fillBuildInContextMenu(IMenuManager manager) {
		if (fillContextMenu(manager))
			manager.add(new Separator());
		
		IElementGroup[] groups = groupManager.getSortedGroups();
		for (int i = 0; i < groups.length; i++) {
			IAction action = new GroupAction(groups[i].getID(), this);
			if (i == 0)
				action.setText(GroupAction.GROUP_ROOT);

			action.setChecked(groups[i].getID().equals(cur_group_id));
			manager.add(action);
		}

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}	
	protected abstract boolean fillContextMenu(IMenuManager manager);
	
	 //Before this please make sure the group is set
	public void updateMenu(IMenuManager manager) {
		IElementGroup[] groups = groupManager.getGroups();
		for (int i = 0; i < groups.length; i++) {
			IContributionItem item = manager.find(groups[i].getID());
			if (item != null && item instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem) item).getAction();
				action.setChecked(groups[i].isSelected());
				if (action.isChecked()) {
					changeTitle(action.getText(), groups[i].size());
				}
			}
		}
		boolean deleteActionEnable = groups.length > 1 && !cur_group_id.equals(IGroupManager.GROUP_ROOT_ID);
		deleteGroupAction.setEnabled(deleteActionEnable);
		deleteProcessAction.setEnabled(deleteActionEnable);
		createGroupAction.setEnabled(cur_group_size > 0);
		setActionEnable();
	}
	//set which action need to be enable or disenale
	protected abstract void setActionEnable();	

	protected void keyDownEvent(int mx, int my, int keyCode) {
		super.keyDownEvent(mx, my, keyCode);
		if (keyCode == DEFAULT_DEL_KEY) // delete key
			removeProcess();
	}

	public void removeProcess() {
		if (!cur_group_id.equals(IGroupManager.GROUP_ROOT_ID)) {
			deleteProcessAction.run(cur_element_group.getSelectedElements());
		}
	}	
}
