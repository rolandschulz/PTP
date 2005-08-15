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

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.internal.ui.actions.ChangeSetAction;
import org.eclipse.ptp.internal.ui.actions.CreateSetAction;
import org.eclipse.ptp.internal.ui.actions.DeleteProcessAction;
import org.eclipse.ptp.internal.ui.actions.DeleteSetAction;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.old.PTPUIPlugin;
import org.eclipse.ptp.ui.views.old.ProcessEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;

/**
 * @author Clement chu
 *
 */
public abstract class AbstractParallelSetView extends AbstractParallelElementView {
	// default actions
	protected ParallelAction createSetAction = null;
	protected ParallelAction deleteSetAction = null;
	protected ParallelAction deleteProcessAction = null;
	protected ParallelAction changeSetAction = null;
	
	protected int DEFAULT_DEL_KEY = '\u007f';
	protected int DEFAULT_BACK_KEY = '\u0008';
		
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		createToolBarGroups(toolBarMgr);
		createToolBarActions(toolBarMgr);
		IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		createMenuGroups(menuMgr);
		createMenuActions(menuMgr);
		createContextMenu();
		initialView();
		if (menuMgr.getItems().length == 1)
			menuMgr.removeAll();
	}
	
	protected void createToolBarGroups(IToolBarManager toolBarMgr) {	
		toolBarMgr.add(new Separator(IPTPUIConstants.IUINAVIGATORGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUIACTIONGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUISETGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUISETGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUICHANGESETGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUIEMPTYGROUP));
	}
	protected void buildInToolBarActions(IToolBarManager toolBarMgr) {
		createSetAction = new CreateSetAction(this);
		deleteSetAction = new DeleteSetAction(this);
		deleteProcessAction = new DeleteProcessAction(this);
		changeSetAction = new ChangeSetAction(this);
		
		toolBarMgr.appendToGroup(IPTPUIConstants.IUISETGROUP, createSetAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUISETGROUP, deleteSetAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUISETGROUP, deleteProcessAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUICHANGESETGROUP, changeSetAction);
	}
	
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		buildInToolBarActions(toolBarMgr);
	}
	
	protected void createMenuGroups(IMenuManager menuMgr) {
		menuMgr.add(new Separator(IPTPUIConstants.IUIEMPTYGROUP));
	}
	protected void createMenuActions(IMenuManager menuMgr) {}

	protected void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#popupmenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				//if right click occur, eclipse will ignore the key up event, so clear keyCode when popup occur.
				keyCode = SWT.NONE;
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(drawComp);
		drawComp.setMenu(menu);
		// Be sure to register it so that other plug-ins can add actions.
		getSite().registerContextMenu(menuMgr, this);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IPTPUIConstants.IUIACTIONGROUP));
		manager.add(new Separator(IPTPUIConstants.IUIEMPTYGROUP));
		manager.add(new Separator(IPTPUIConstants.IUICHANGESETGROUP));
		manager.add(new ChangeSetAction(this));
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}	
	
	public void update() {
		updateAction();
		updateTitle();		
	}
	
	protected void updateAction() {
		boolean deleteActionEnable = manager.getCurrentSetId().equals(IElementHandler.SET_ROOT_ID);
		deleteSetAction.setEnabled(!deleteActionEnable);
		deleteProcessAction.setEnabled(!deleteActionEnable);
		createSetAction.setEnabled(cur_set_size > 0);
	}
	
	protected void keyDownEvent(int mx, int my, int keyCode) {
		if (keyCode == DEFAULT_DEL_KEY || keyCode == DEFAULT_BACK_KEY) // delete key
			removeProcess();
		else
			super.keyDownEvent(mx, my, keyCode);
	}

	public void removeProcess() {
		if (!manager.getCurrentSetId().equals(IElementHandler.SET_ROOT_ID)) {
			deleteProcessAction.run(cur_element_set.getSelectedElements());
		}
	}
	
    protected void openProcessViewer(final IPProcess element) {
    	if (element == null)
    		return;
    	
    	BusyIndicator.showWhile(getDisplay(), new Runnable() {
            public void run() {
                try {
                    PTPUIPlugin.getActivePage().openEditor(new ProcessEditorInput(element), IPTPUIConstants.VIEW_PARALLELProcess);
                } catch (PartInitException e) {
                    System.out.println("PartInitException err: " + e.getMessage());
                }
            }
        });
    }	
}
