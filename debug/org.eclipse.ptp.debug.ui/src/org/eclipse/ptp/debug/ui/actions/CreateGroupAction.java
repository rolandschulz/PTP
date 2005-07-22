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
package org.eclipse.ptp.debug.ui.actions;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.views.AbstractDebugParallelView;
import org.eclipse.ptp.debug.ui.views.DebugParallelProcessView;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
/**
 * @author clement chu
 *
 */
public class CreateGroupAction extends ParallelDebugAction {
	public static final String name = "Create Group";

    private IMenuCreator menuCreator = new IMenuCreator() {
        private MenuManager dropDownMenuMgr = null;

        private void createDropDownMenuMgr() {
        	if (dropDownMenuMgr != null)
        		dispose();
        	
        	dropDownMenuMgr = new MenuManager();                
        	AbstractDebugParallelView debugView = CreateGroupAction.this.debugView;
        	if (debugView instanceof DebugParallelProcessView) {
        		DebugParallelProcessView view = (DebugParallelProcessView)debugView;
        		IContributionItem[] items = view.getViewSite().getActionBars().getMenuManager().getItems();
        		for (int i=1; i<items.length; i++) {
	            	dropDownMenuMgr.add(new InternalGroupAction(items[i].getId(), view));
        		}
        	}
        }
        public Menu getMenu(Control parent) {
            createDropDownMenuMgr();
            return dropDownMenuMgr.createContextMenu(parent);
        }
        public Menu getMenu(Menu parent) {
            createDropDownMenuMgr();
            Menu menu = new Menu(parent);
            IContributionItem[] items = dropDownMenuMgr.getItems();
            for (int i = 0; i < items.length; i++) {
                IContributionItem item = items[i];
                IContributionItem newItem = item;
                if (item instanceof ActionContributionItem) {
                    newItem = new ActionContributionItem(((ActionContributionItem) item).getAction());
                }
                newItem.fill(menu, -1);
            }
            return menu;
        }
        public void dispose() {
            if (dropDownMenuMgr != null) {
                dropDownMenuMgr.dispose();
                dropDownMenuMgr = null;
            }
        }
    };
    
	public CreateGroupAction(AbstractDebugParallelView debugView) {
		super(name, IAction.AS_DROP_DOWN_MENU, debugView);
	    setImageDescriptor(ImageUtil.ID_ICON_CREATEGROUP_NORMAL);
	    setDisabledImageDescriptor(ImageUtil.ID_ICON_CREATEGROUP_NORMAL);
	    setMenuCreator(menuCreator);
	    setId(name);
	}
	
	public void run(IElement[] elements) {
		if (validation(elements)) {
			if (debugView instanceof DebugParallelProcessView) {
				groupAction(elements, (DebugParallelProcessView)debugView, null);
			}
		}
	}
	
	private void groupAction(IElement[] elements, DebugParallelProcessView view, String groupID) {
		IMenuManager manager = view.getViewSite().getActionBars().getMenuManager();
		if (groupID == null) {
			groupID = view.getUIDebugManger().createGroup(elements);
			manager.add(new GroupAction(groupID, view));
		}
		else
			view.getUIDebugManger().addToGroup(elements, groupID);
		
		view.selectGroup(groupID);
		view.getCurrentGroup().setAllSelect(false);
		view.updateMenu(manager);
		view.redraw();
	}
	
	private class InternalGroupAction extends ParallelDebugAction {
		private String group_id = "";
		private InternalGroupAction(String group_id, DebugParallelProcessView view) {
			super(GroupAction.name + " " + group_id, view);
			this.group_id = group_id;
		    setImageDescriptor(ImageUtil.ID_ICON_CREATEGROUP_NORMAL);
		    setDisabledImageDescriptor(ImageUtil.ID_ICON_CREATEGROUP_NORMAL);
		    setEnabled(!view.getCurrentGroupID().equals(group_id));
		}
		
		public void run(IElement[] elements) {
			if (validation(elements)) {
				if (debugView instanceof DebugParallelProcessView) {
					groupAction(elements, (DebugParallelProcessView)debugView, group_id);
				}
			}
		}
	}
}
