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

package org.eclipse.ptp.rm.lml.ui.views;

//import java.net.URL;
//
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.IMenuManager;
//import org.eclipse.jface.action.IToolBarManager;
//import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.internal.core.model.Cell;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.ui.actions.HideTableColumnAction;
import org.eclipse.ptp.rm.lml.ui.actions.ShowTableColumnAction;
import org.eclipse.ptp.rm.lml.ui.providers.LMLViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TableView extends LMLViewPart {
	private class ContentProvider implements ILazyTreeContentProvider {
		private final TreeViewer viewer;
		private Row[] rows;

		public ContentProvider(TreeViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.rows = (Row[]) newInput;
		}

		public void updateElement(Object parent, int index) {
			Object element;
			if (parent instanceof Row) {
				element = ((Row) parent).cells[index];
			} else {
				element = rows[index];
			}
			viewer.replace(parent, index, element);
			updateChildCount(element, -1);

		}

		public void updateChildCount(Object element, int currentChildCount) {

		}

		public Object getParent(Object element) {
			if (element instanceof Cell) {
				return ((Cell) element).row;
			}
			return rows;
		}

	}

	private final class LMLTableListListener implements ILMLListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILguiListener# handleEvent
		 * (org.eclipse.ptp.core.events.IJobListSortEvent)
		 */
		public void handleEvent(IJobListSortedEvent e) {
			input = fSelectedLguiItem.getTableHandler().getTableDataWithColor(gid);
			viewer.setInput(input);
			viewer.getTree().setItemCount(input.length);
		}

		public void handleEvent(ITableColumnChangeEvent e) {
			disposeTable();
			createTable();
		}

		public void handleEvent(ISelectedObjectChangeEvent event) {
			if (!composite.isDisposed()) {
				tree.deselectAll();

				Row[] rows = null;
				if (viewer.getInput() instanceof Row[]) {
					rows = (Row[]) viewer.getInput();
				}
				int index = -1;
				for (int i = 0; i < rows.length; i++) {
					if (rows[i].oid.equals(event.getOid())) {
						index = i;
						break;
					}
				}
				if (index > -1) {
					tree.select(tree.getItem(index));
				}
			}

		}

		public void handleEvent(IMarkObjectEvent event) {
			selectedOid = event.getOid();
			if (!composite.isDisposed()) {
				viewer.refresh();
			}

		}

		public void handleEvent(IUnmarkObjectEvent event) {
			selectedOid = null;
			if (!composite.isDisposed()) {
				viewer.refresh();
			}
		}

		public void handleEvent(IUnselectedObjectEvent event) {
			if (!composite.isDisposed()) {
				tree.deselectAll();
			}

		}
	}

	Row[] input = null;
	private Composite composite;
	private Tree tree;
	private TreeColumn[] treeColumns;
	public TreeViewer viewer;
	public int sizeViewer;
	public ILguiItem fSelectedLguiItem = null;
	public String gid = null;
	private final ILMLListener lmlListener = new LMLTableListListener();
	private final ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
	private TreeItem selectedItem = null;
	private String selectedOid = null;

	@Override
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.None);
		composite.setLayout(new FillLayout());

		viewer = new TreeViewer(composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.VIRTUAL);
		viewer.setLabelProvider(new ITableLabelProvider() {
			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {

			}

			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0) {
					Display display = treeColumns[columnIndex].getDisplay();
					Image image = new Image(display, 12, 12);
					GC gc = new GC(image);
					if (selectedOid == null || ((Row) element).oid.equals(selectedOid)) {
						LMLColor color = ((Row) element).color;
						gc.setBackground(new Color(display, color.getRed(), color.getGreen(), color.getBlue()));
					} else {
						gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
					}
					gc.fillRectangle(image.getBounds().x, image.getBounds().y, image.getBounds().width, image.getBounds().height);
					gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					gc.drawRectangle(image.getBounds().x, image.getBounds().y, image.getBounds().width - 1,
							image.getBounds().height - 1);
					gc.dispose();
					return image;
				}
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 0 || ((Row) element).cells[columnIndex - 1] == null) {
					return null;
				}
				return ((Row) element).cells[columnIndex - 1].toString();
			}

		});

		viewer.setContentProvider(new ContentProvider(viewer));
		lmlManager.addListener(lmlListener, this.getClass().getName());
		tree = viewer.getTree();
		tree.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (fSelectedLguiItem == null) {
					return;
				}
				fSelectedLguiItem.getTableHandler().changeTableColumnsWidth(getWidths(), gid);
				fSelectedLguiItem.getTableHandler().changeTableColumnsOrder(gid, removingColumn(tree.getColumnOrder()));
				redrawColumns();
			}
		});
		viewer.setUseHashlookup(true);

	}

	public void generateTable(String acitveTableLayoutGid) {
		this.gid = acitveTableLayoutGid;
		fSelectedLguiItem = lmlManager.getSelectedLguiItem();
		createTable();
		composite.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				lmlManager.removeView(gid);

			}

		});
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void prepareDispose() {
		fSelectedLguiItem.getTableHandler().changeTableColumnsWidth(getWidths(), gid);
		fSelectedLguiItem.getTableHandler().changeTableColumnsOrder(gid, removingColumn(tree.getColumnOrder()));
		lmlManager.removeListener(lmlListener);
	}

	private void disposeTable() {
		if (fSelectedLguiItem != null) {
			TreeColumn[] oldColumns = tree.getColumns();
			for (int i = 0; i < oldColumns.length; i++) {
				Listener[] oldListeners = oldColumns[i].getListeners(SWT.Selection);
				for (int j = 0; j < oldListeners.length; j++) {
					oldColumns[i].removeListener(SWT.Selection, oldListeners[j]);
				}
				oldColumns[i].dispose();
			}
			treeColumns = null;
		}
		viewer.setInput(null);
		viewer.getTree().setItemCount(0);
		this.getViewSite().getActionBars().getMenuManager().removeAll();
	}

	private void createTable() {
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		createColumns();
		createMenu();
		input = fSelectedLguiItem.getTableHandler().getTableDataWithColor(gid);
		viewer.setInput(input);
		viewer.getTree().setItemCount(input.length);
	}

	private void createMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		
		IMenuManager subMenuShow = new MenuManager("Show column...");
		String[] columnNonActive = fSelectedLguiItem.getTableHandler().getTableColumnNonActive(gid);
		for (String column : columnNonActive) {
			IAction action = new ShowTableColumnAction(gid, column, this);
			subMenuShow.add(action);
		}
		menuManager.add(subMenuShow);
		
		IMenuManager subMenuHide = new MenuManager("Hide column...");
		String[] columnActive = fSelectedLguiItem.getTableHandler().getTableColumnActive(gid);
		for (String column : columnActive) {
			IAction action = new HideTableColumnAction(gid, column, this);
			subMenuHide.add(action);
		}
		menuManager.add(subMenuHide);
		
		getViewSite().getActionBars().updateActionBars();
	}
	
	private int getColumnAlignment(String alignment) {
		if (alignment.equals("LEFT")) {
			return SWT.LEAD;
		}
		if (alignment.equals("RIGHT")) {
			return SWT.TRAIL;
		}
		return SWT.LEAD;
	}

	/**
	 * 
	 * @param tableViewer
	 * @param fSelected
	 */
	private void createColumns() {
		if (fSelectedLguiItem.isLayout()) {
			return;
		}
		this.setPartName(fSelectedLguiItem.getTableHandler().getTableTitle(gid));
		TreeColumn treeColumnImage = new TreeColumn(tree, SWT.LEAD, 0);
		treeColumnImage.setWidth(40);
		treeColumnImage.setMoveable(true);
		treeColumnImage.setResizable(false);

		treeColumns = new TreeColumn[fSelectedLguiItem.getTableHandler().getNumberOfTableColumns(gid)];
		sizeViewer = composite.getSize().x - 70;
		ITableColumnLayout[] tableColumnLayouts = fSelectedLguiItem.getTableHandler().getTableColumnLayout(gid, sizeViewer);

		for (int i = 0; i < tableColumnLayouts.length; i++) {
			TreeColumn treeColumn = new TreeColumn(tree, getColumnAlignment(tableColumnLayouts[i].getStyle()));

			treeColumn.setText(tableColumnLayouts[i].getTitle());
			treeColumn.setWidth(tableColumnLayouts[i].getWidth());
			treeColumn.setMoveable(true);
			treeColumns[i] = treeColumn;
		}

		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				TreeColumn currentColumn = (TreeColumn) e.widget;

				if (tree.getSortColumn() == currentColumn) {
					tree.setSortDirection(tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
				} else {
					tree.setSortColumn(currentColumn);
					tree.setSortDirection(SWT.UP);
				}
				int sortIndex = 0;
				for (int i = 0; i < treeColumns.length; i++) {
					if (treeColumns[i] == tree.getSortColumn()) {
						sortIndex = i;
					}
				}
				fSelectedLguiItem.getTableHandler().sort(gid, SWT.UP, sortIndex, tree.getSortDirection());
				tree.setSortDirection(tree.getSortDirection());
				LMLCorePlugin.getDefault().getLMLManager().sortLgui();
			}
		};

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					TreeItem item = tree.getItem(new Point(e.x, e.y));
					lmlManager.markObject(item.getData().toString());
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					TreeItem item = tree.getItem(new Point(e.x, e.y));
					lmlManager.unmarkObject(item.getData().toString());
				}
			}

		});
		tree.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {

				TreeItem item = tree.getItem(new Point(e.x, e.y));
				if (item == null) {
					return;
				}

				if (selectedItem != null && !selectedItem.equals(item)) {
					lmlManager.unselectObject(selectedItem.getData().toString());
				}
				selectedItem = item;
				lmlManager.selectObject(selectedItem.getData().toString());
			}

		});
		tree.addMouseTrackListener(new MouseTrackListener() {

			public void mouseHover(MouseEvent e) {

			}

			public void mouseExit(MouseEvent e) {
				if (selectedItem != null) {
					lmlManager.unselectObject(selectedItem.getData().toString());
					selectedItem = null;
				}
			}

			public void mouseEnter(MouseEvent e) {

			}
		});

		for (int i = 0; i < treeColumns.length; i++) {
			treeColumns[i].addListener(SWT.Selection, sortListener);
		}

	}

	public Double[] getWidths() {
		Double[] widths = new Double[treeColumns.length];
		Double widthColumn = Integer.valueOf(sizeViewer).doubleValue() / treeColumns.length;
		for (int i = 0; i < treeColumns.length; i++) {
			widths[i] = widthColumn / treeColumns[i].getWidth();
		}
		return widths;
	}

	private int[] removingColumn(int[] order) {
		int[] orderNew = new int[order.length - 1];
		int dif = 0;
		for (int i = 0; i < order.length; i++) {
			if (order[i] != 0) {
				orderNew[i - dif] = order[i] - 1;
			} else {
				dif = 1;
			}
		}
		return orderNew;
	}
	
	public int[] getRemoveColumnOrder() {
		return removingColumn(tree.getColumnOrder());
	}

	private void redrawColumns() {
		sizeViewer = composite.getSize().x - 70;
		int[] columnsWidth = fSelectedLguiItem.getTableHandler().getTableColumnsWidth(gid, sizeViewer);
		for (int i = 0; i < columnsWidth.length; i++) {
			treeColumns[i].setWidth(columnsWidth[i]);
		}
	}
}
