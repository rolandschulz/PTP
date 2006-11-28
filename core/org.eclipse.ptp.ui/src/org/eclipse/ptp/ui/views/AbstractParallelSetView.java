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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.internal.ui.actions.ChangeSetAction;
import org.eclipse.ptp.internal.ui.actions.CreateSetAction;
import org.eclipse.ptp.internal.ui.actions.DeleteSetAction;
import org.eclipse.ptp.internal.ui.actions.DisplayRulerAction;
import org.eclipse.ptp.internal.ui.actions.RemoveElementAction;
import org.eclipse.ptp.internal.ui.actions.ZoomInAction;
import org.eclipse.ptp.internal.ui.actions.ZoomOutAction;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractParallelSetView extends AbstractParallelElementView {
	protected List clipboard = new ArrayList();
	
	// selected element
	protected String cur_selected_element_id = IManager.EMPTY_ID;
	// default actions
	protected ParallelAction createSetAction = null;
	protected ParallelAction deleteSetAction = null;
	protected ParallelAction deleteProcessAction = null;
	protected ParallelAction changeSetAction = null;
	protected ParallelAction zoomInAction = null;
	protected ParallelAction zoomOutAction = null;

	//zoom
	protected int zoom_depth = 0;
	protected Zoom zoom = new Zoom();
		
	public AbstractParallelSetView(IManager manager) {
		super(manager);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		clipboard.clear();
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		createToolBarActionGroup(toolBarMgr);
		createToolBarActions(toolBarMgr);
		IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		createMenuActions(menuMgr);
		createContextMenu();
		initialView();
	}
	protected void createToolBarActionGroup(IToolBarManager toolBarMgr) {
		toolBarMgr.add(new Separator(IPTPUIConstants.IUIZOOMGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUINAVIGATORGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUIACTIONGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUIEMPTYGROUP));

		toolBarMgr.add(new Separator(IPTPUIConstants.IUISETGROUP));
		toolBarMgr.add(new Separator(IPTPUIConstants.IUICHANGESETGROUP));
	}
	/** Build-in Toolbar actions
	 * @param toolBarMgr
	 */
	protected void buildInToolBarActions(IToolBarManager toolBarMgr) {
		zoomOutAction = new ZoomOutAction(this);
		zoomInAction = new ZoomInAction(this);
		createSetAction = new CreateSetAction(this);
		deleteSetAction = new DeleteSetAction(this);
		deleteProcessAction = new RemoveElementAction(this);
		changeSetAction = new ChangeSetAction(this);
		
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIZOOMGROUP, zoomOutAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIZOOMGROUP, zoomInAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUISETGROUP, createSetAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUISETGROUP, deleteSetAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUISETGROUP, deleteProcessAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUICHANGESETGROUP, changeSetAction);
	}
	/** Create toolbar actions
	 * @param toolBarMgr
	 */
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		buildInToolBarActions(toolBarMgr);
	}
	/** Create menu actions
	 * @param menuMgr
	 */
	protected void createMenuActions(IMenuManager menuMgr) {}
	/** Create context menu
	 * 
	 */
	protected void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#popupmenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(canvas);
		canvas.setMenu(menu);
		// Be sure to register it so that other plug-ins can add actions.
		getSite().registerContextMenu(menuMgr, canvas);
	}
	/** Create context menu
	 * @param manager
	 */
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IPTPUIConstants.IUIACTIONGROUP));
		manager.add(new Separator(IPTPUIConstants.IUIEMPTYGROUP));
		manager.add(new Separator(IPTPUIConstants.IUIZOOMGROUP));
		manager.add(zoomOutAction);
		manager.add(zoomInAction);
		manager.add(new Separator(IPTPUIConstants.IUICHANGESETGROUP));
		manager.add(changeSetAction);
		manager.add(new Separator(IPTPUIConstants.IUIVIEWGROUP));
		manager.add(new DisplayRulerAction(this));
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#update()
	 */
	public void update() {
		updateAction();
		updateTitle();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#updateTitle()
	 */
	public void updateTitle() {
		if (cur_element_set != null)
			changeTitle(manager.getName(getCurrentID()), cur_element_set.getID(), cur_set_size);
		else
			changeTitle(manager.getName(getCurrentID()));
	}
	/** Update action
	 * 
	 */
	protected void updateAction() {
		boolean deleteActionEnable = (manager.getCurrentSetId().length() == 0 || manager.getCurrentSetId().equals(IElementHandler.SET_ROOT_ID));
		deleteSetAction.setEnabled(!deleteActionEnable);
		deleteProcessAction.setEnabled(!deleteActionEnable);
		createSetAction.setEnabled(cur_set_size > 0);
		IElementHandler elementHandler = getCurrentElementHandler();
		changeSetAction.setEnabled(!(elementHandler == null || elementHandler.size() == 0));
		
		zoomInAction.setEnabled(cur_set_size > 0 && zoom_depth > 0);
		zoomOutAction.setEnabled(cur_set_size > 0 && zoom_depth < Zoom.max_depth);
	}
	/** Open process viewer
	 * @param element Target IPProcess
	 */
	protected void openProcessViewer(final IPProcess element) {
		if (element == null)
			return;
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				try {
					PTPUIPlugin.getActivePage().openEditor(new ProcessEditorInput(element), IPTPUIConstants.VIEW_PARALLELProcess);
				} catch (PartInitException e) {
					PTPUIPlugin.log(e);
				}
			}
		});
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * IIconCanvasActionListener
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IIconCanvasActionListener#handleAction(int, int[])
	 */
	public void handleAction(int type, int[] indexes) {
		if (cur_element_set == null)
			return;
		
		IElement[] elements = canvas.getElements(indexes);
		switch (type) {
		case IIconCanvasActionListener.COPY_ACTION:
			if (elements.length > 0) {
				clipboard.clear();
				clipboard.add(elements);
			}
			break;
		case IIconCanvasActionListener.CUT_ACTION:
			if (elements.length > 0) {
				if (!cur_element_set.isRootSet()) {
					manager.removeFromSet(elements, cur_element_set.getID(), cur_element_set.getElementHandler());
					clipboard.clear();
					clipboard.add(elements);
					selectSet(cur_element_set.getElementHandler().getSet(cur_element_set.getID()));
					updateTitle();
					refresh(false);
				}
			}
			break;
		case IIconCanvasActionListener.PASTE_ACTION:
			if (clipboard.size() > 0) {
				IElement[] clipElements = (IElement[])clipboard.get(0);
				if (cur_element_set.isRootSet())
					createSetAction.run(clipElements);
				else {
					manager.addToSet(clipElements, cur_element_set.getID(), cur_element_set.getElementHandler());
					selectSet(cur_element_set.getElementHandler().getSet(cur_element_set.getID()));					
					update();
					refresh(false);
				}
			}
			break;
		case IIconCanvasActionListener.DELETE_ACTION:
			if (elements.length > 0) {
				if (!cur_element_set.isRootSet()) {
					deleteProcessAction.run(elements);
				}
			}
			break;
		}
	}
	/*****************************************************************
	 * Zoom
	 *****************************************************************/
	public void zoomOut() {
		if (zoom_depth < Zoom.max_depth) {
			zoom_depth++;
			update();
			if (!canvas.isDisposed()) {
				canvas.setIconSize(zoom.scaled(IPTPUIConstants.DEFAULT_VIEW_ICON_WIDTH, zoom_depth), zoom.scaled(IPTPUIConstants.DEFAULT_VIEW_ICON_HEIGHT, zoom_depth));
				canvas.setIconSpace(IPTPUIConstants.DEFAULT_VIEW_ICON_SPACING_X - zoom_depth, IPTPUIConstants.DEFAULT_VIEW_ICON_SPACING_Y);
				canvas.setFontSizeSmaller();
				canvas.resetCanvas();
			}
		}
	}
	public void zoomIn() {
		if (zoom_depth > 0) {
			zoom_depth--;
			update();
			if (!canvas.isDisposed()) {
				canvas.setIconSize(zoom.scaled(IPTPUIConstants.DEFAULT_VIEW_ICON_WIDTH, zoom_depth), zoom.scaled(IPTPUIConstants.DEFAULT_VIEW_ICON_HEIGHT, zoom_depth));
				canvas.setIconSpace(IPTPUIConstants.DEFAULT_VIEW_ICON_SPACING_X - zoom_depth, IPTPUIConstants.DEFAULT_VIEW_ICON_SPACING_Y);
				canvas.setFontSizeBigger();
				canvas.resetCanvas();
			}
		}
	}
	public Image getStatusIcon(Object obj, boolean isSelected) {
		Image img = super.getStatusIcon(obj, isSelected);
		if (img != null) {
			return zoom.getImage(img, zoom_depth);
		}
		return null;
	}
    
	class Zoom {
		protected static final int max_depth = 3;
		protected Map zoomImageMap = new HashMap();
		
		public void cleanup() {
			zoomImageMap.clear();
		}
		public Image getImage(Image image, int depth) {
			if (depth == 0)
				return image;
			
			Image[] images = (Image[])zoomImageMap.get(image);
			if (images == null) {
				images = new Image[max_depth];
				zoomImageMap.put(image, images);
			}
			return getZoomImage(images, image, depth);
		}
		public Image getZoomImage(Image[] images, Image orgImg, int depth) {
			if (images[depth-1] == null) {
				images[depth-1] = scaledImage(orgImg, depth);
			}
			return images[depth-1];
		}
		public double getScale(int depth) {
			double fix_factor = 0.2;
			return (1 - (fix_factor * depth));
		}
		public int scaled(int measure, int depth) {
			return (int)(measure * getScale(depth));
		}
		public Image scaledImage(Image image, int depth) {
			return new Image(image.getDevice(), image.getImageData().scaledTo(scaled(IPTPUIConstants.DEFAULT_VIEW_ICON_WIDTH, depth), scaled(IPTPUIConstants.DEFAULT_VIEW_ICON_HEIGHT, depth)));
		}
	}    
}
