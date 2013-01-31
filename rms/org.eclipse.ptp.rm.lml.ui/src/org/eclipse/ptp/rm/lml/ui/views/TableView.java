/**
 * Copyright (c) 2011-2012 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, Carsten Karbach, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ptp.rm.lml.core.ILMLCoreConstants;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableFilterEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.IPattern;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.internal.core.model.Cell;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.Pattern;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.ui.UIUtils;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.ui.providers.FilterDialog;
import org.eclipse.ptp.rm.lml.ui.providers.support.ColorConversion;
import org.eclipse.ptp.rm.lml.ui.providers.support.EventForwarder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class TableView extends ViewPart {

	/**
	 * Provides images for the first column of the table.
	 * Ensures correct disposal.
	 * 
	 * @author Carsten Karbach
	 * 
	 */
	private final class FirstColumnLabelProvider extends ColumnLabelProvider {
		/**
		 * Saves a generated image for each color.
		 */
		private final Map<Color, Image> generatedImages = new HashMap<Color, Image>();

		/**
		 * Disposes and removes all images created so far.
		 */
		public void clearImages() {
			for (final Image img : generatedImages.values()) {
				img.dispose();
			}
			generatedImages.clear();
		}

		@Override
		public void dispose() {
			super.dispose();
			clearImages();
		}

		@Override
		public Image getImage(Object element) {
			final Display display = treeColumns[0].getDisplay();

			Color backgroundColor = null;
			if (((Row) element).oid != null) {
				LMLColor color = ((Row) element).color;
				if (color == null) {
					color = LMLColor.LIGHT_GRAY;
				}
				backgroundColor = ColorConversion.getColor(color);
			} else {
				backgroundColor = display.getSystemColor(SWT.COLOR_WHITE);
			}

			Image image = null;
			// Was image generated before?
			if (generatedImages.containsKey(backgroundColor)) {
				image = generatedImages.get(backgroundColor);
			}
			else {
				// Generate new image for new color
				image = new Image(display, 12, 12);
				final GC gc = new GC(image);
				gc.setBackground(backgroundColor);
				gc.fillRectangle(image.getBounds().x, image.getBounds().y, image.getBounds().width, image.getBounds().height);
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				gc.drawRectangle(image.getBounds().x, image.getBounds().y, image.getBounds().width - 1,
						image.getBounds().height - 1);
				gc.dispose();
				generatedImages.put(backgroundColor, image);
			}

			return image;
		}

		@Override
		public String getText(Object element) {
			return null;
		}
	};

	private final class LMLTableListListener implements ILMLListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.ILguiAddedEvent)
		 */
		@Override
		public void handleEvent(ILguiAddedEvent event) {

			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (composite != null && !viewCreated) {
						fLguiItem = lmlManager.getSelectedLguiItem();
						if (fLguiItem != null) {
							viewCreated = createTable();
							if (fLguiItem.getObjectStatus() != null) {
								fLguiItem.getObjectStatus().addComponent(eventForwarder);
								componentAdded = true;
							}
						}
					}
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.ILguiRemovedEvent)
		 */
		@Override
		public void handleEvent(ILguiRemovedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (composite != null && fLguiItem != null && viewCreated) {
						if (componentAdded) {
							fLguiItem.getObjectStatus().removeComponent(eventForwarder);
							componentAdded = false;
						}
						saveColumnLayout();
						fLguiItem = null;
						setViewerInput();
						disposeTable();
						viewCreated = false;
					}
				}

			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.IMarkObjectEvent)
		 */
		@Override
		public void handleEvent(IMarkObjectEvent event) {
			selectedOid = event.getOid();
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					// if (composite != null && !composite.isDisposed()) {
					// viewer.refresh();
					// }
					if (composite != null && !composite.isDisposed() && viewer.getInput() != null) {
						tree.deselectAll();

						Row[] rows = null;
						if (viewer.getInput() instanceof Row[]) {
							rows = (Row[]) viewer.getInput();
						}
						int index = -1;
						if (rows != null) {
							for (int i = 0; i < rows.length; i++) {
								if (rows[i].oid != null && rows[i].oid.equals(selectedOid)) {
									index = i;
									break;
								}
							}
						}
						if (index > -1) {
							tree.setSelection(tree.getItem(index));
						}
					}
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.ISelectObjectEvent)
		 */
		@Override
		public void handleEvent(ISelectObjectEvent event) {
			final String oid = event.getOid();
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (composite != null && !composite.isDisposed() && viewer.getInput() != null) {
						tree.deselectAll();

						Row[] rows = null;
						if (viewer.getInput() instanceof Row[]) {
							rows = (Row[]) viewer.getInput();
						}
						int index = -1;
						if (rows != null) {
							for (int i = 0; i < rows.length; i++) {
								if (rows[i].oid != null && rows[i].oid.equals(oid)) {
									index = i;
									break;
								}
							}
						}
						if (index > -1) {
							tree.select(tree.getItem(index));
						}
					}

				}
			});

		}

		@Override
		public void handleEvent(final ITableFilterEvent event) {
			if (event.getGid().equals(gid)) {
				UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						if (event.getGid().equals(gid)) {
							if (event.getPattern().size() > 0) {
								setViewerInput(event.getPattern());
								updateAfterLastFilter = false;
							} else {
								setViewerInput();
							}

							filterOwnJobsActionItem.getAction().setChecked(fLguiItem.isFilterOwnJobActive(gid));
						}
					}
				});
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.ITableSortedEvent)
		 */
		@Override
		public void handleEvent(ITableSortedEvent e) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (fLguiItem != null && fLguiItem.getTableHandler() != null && tree.getSortDirection() != 0
							&& tree.getSortColumn() != null) {
						fLguiItem.getTableHandler().sort(gid, SWT.UP, getSortIndex(), tree.getSortDirection());
					}
					if (!updateAfterLastFilter && fLguiItem.getTableHandler().getPattern(gid).size() > 0) {
						setViewerInput(fLguiItem.getTableHandler().getPattern(gid));
					} else {
						setViewerInput();
					}
				}
			});

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.IUnmarkObjectEvent)
		 */
		@Override
		public void handleEvent(IUnmarkObjectEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					selectedOid = null;
					if (composite != null && !composite.isDisposed()) {
						tree.deselectAll();
					}
				}
			});

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.IUnselectedObjectEvent)
		 */
		@Override
		public void handleEvent(IUnselectedObjectEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (composite != null && !composite.isDisposed()) {
						tree.deselectAll();
					}
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILMLListener# handleEvent (org.eclipse.ptp.core.events.IViewUpdateEvent)
		 */
		@Override
		public void handleEvent(IViewUpdateEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (composite != null && viewCreated) {
						if (firstColumnlabelProvider != null) {
							firstColumnlabelProvider.clearImages();
						}
						if (selectedItem != null && !selectedItem.isDisposed()) {
							lmlManager.unmarkObject(selectedItem.getData().toString());
							selectedItem = null;
						}
						if (componentAdded) {
							if (fLguiItem != null && fLguiItem.getObjectStatus() != null) {
								fLguiItem.getObjectStatus().removeComponent(eventForwarder);
							}
							componentAdded = false;
						}
						fLguiItem = lmlManager.getSelectedLguiItem();

						if (fLguiItem != null && fLguiItem.getTableHandler() != null && tree.getSortColumn() != null) {
							fLguiItem.getTableHandler().getSortProperties(gid);
							fLguiItem.getTableHandler().sort(gid, SWT.UP, getSortIndex(), tree.getSortDirection());
						}
						setViewerInput();
						updateAfterLastFilter = true;
						if (fLguiItem != null && fLguiItem.getTableHandler() != null) {
							fLguiItem.getObjectStatus().addComponent(eventForwarder);
							componentAdded = true;
						}

					}
				}
			});

		}
	}

	private Composite composite;
	private Menu headerMenu;
	private Tree tree;
	private TreeColumn[] treeColumns;
	private int[] savedColumnWidths;
	private TreeColumnLayout treeColumnLayout;
	private TreeViewer viewer;
	private ILguiItem fLguiItem = null;
	private String gid = null;
	private final ILMLListener lmlListener = new LMLTableListListener();
	private final LMLManager lmlManager = LMLManager.getInstance();
	private IMenuManager viewMenuManager;
	private ActionContributionItem filterActionItem;
	private ActionContributionItem filterOwnJobsActionItem;
	private TreeItem selectedItem = null;
	private String selectedOid = null;
	private boolean componentAdded = false;
	private boolean viewCreated = false;
	private IViewSite viewSite = null;

	private boolean isMouseDown = false;
	private final EventForwarder eventForwarder = new EventForwarder();

	private boolean updateAfterLastFilter = true;

	/**
	 * Stores the label provider, which generates images for the first column.
	 * Has to be stored in order to dispose the generated images.
	 */
	private FirstColumnLabelProvider firstColumnlabelProvider;

	@Override
	public void createPartControl(Composite parent) {
		gid = getViewSite().getId();
		composite = new Composite(parent, SWT.None);
		treeColumnLayout = new TreeColumnLayout();
		composite.setLayout(treeColumnLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		viewer = new TreeViewer(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.VIRTUAL);
		viewer.getTree().setLayout(new TableLayout());
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ILazyTreeContentProvider() {
			private Row[] rows;

			@Override
			public void dispose() {
				// Nothing
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof Cell) {
					return ((Cell) element).row;
				}
				return rows;
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				this.rows = (Row[]) newInput;
			}

			@Override
			public void updateChildCount(Object element, int currentChildCount) {
				// Nothing
			}

			@Override
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
		});
		viewer.setUseHashlookup(true);
		/*
		 * Must expand first level of table, otherwise it may not be redrawn.
		 */
		viewer.setAutoExpandLevel(2);

		tree = viewer.getTree();

		headerMenu = new Menu(composite);
		// Part for the controlling Monitor - context menu
		final MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		final Menu menu = contextMenu.createContextMenu(composite);
		getSite().registerContextMenu(contextMenu, viewer);

		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tree.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final Point pt = tree.getDisplay().map(null, tree, new Point(event.x, event.y));
				final Rectangle clientArea = tree.getClientArea();
				final boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + tree.getHeaderHeight());
				tree.setMenu(header ? headerMenu : menu);
			}
		});

		/*
		 * Get the selected LguiItem (if there is one) so that the table will be populated when the it is first created
		 */
		fLguiItem = lmlManager.getSelectedLguiItem();

		// ViewMenuManager
		viewMenuManager = getViewSite().getActionBars().getMenuManager();
		final IAction filterOwnJobsAction = new Action(Messages.TableView_Show_only_my_jobs, IAction.AS_CHECK_BOX) {

			@Override
			public void run() {

				final List<String> columnsTitle = new LinkedList<String>();
				columnsTitle.add(ILMLCoreConstants.OWNER);
				fLguiItem.getTableHandler().deleteOldPattern(gid, columnsTitle);

				if (isChecked()) {
					final List<IPattern> filterValuesNew = new LinkedList<IPattern>();
					filterValuesNew.add((new Pattern(ILMLCoreConstants.OWNER, ILMLCoreConstants.TABLECOLUMN_ALPHA)).setRelation(
							ILMLCoreConstants.EQ, fLguiItem.getUsername()));

					fLguiItem.getTableHandler().generateNewPattern(gid, filterValuesNew);
					setViewerInput(fLguiItem.getTableHandler().getPattern(gid));
				} else {
					setViewerInput();
				}
				updateAfterLastFilter = false;
			}

		};
		final IAction filterAction = new Action(Messages.TableView_Filters) {

			@Override
			public void run() {
				final FilterDialog dialog = new FilterDialog(new Shell(viewSite.getShell()), gid);
				dialog.open();
			}
		};

		filterOwnJobsActionItem = new ActionContributionItem(filterOwnJobsAction);
		filterActionItem = new ActionContributionItem(filterAction);
		viewMenuManager.add(filterOwnJobsActionItem);
		viewMenuManager.add(new Separator());
		viewMenuManager.add(filterActionItem);
		filterOwnJobsActionItem.getAction().setEnabled(false);
		filterActionItem.getAction().setEnabled(false);

		viewCreated = createTable();

		tree.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (fLguiItem != null) {
					saveColumnLayout();
				}
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		lmlManager.removeListener(lmlListener);
		ColorConversion.disposeColors();
	}

	@Override
	public void init(IViewSite site) {
		try {
			super.init(site);
		} catch (final PartInitException e) {
			e.printStackTrace();
		}
		lmlManager.addListener(lmlListener, this.getClass().getName());
		viewSite = site;
	}

	/**
	 * Refresh the viewer.
	 */
	public void refresh() {
		new UIJob(Messages.TableUpdate) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (viewer != null) {
					if (fLguiItem.getTableHandler().getPattern(gid).size() > 0) {
						setViewerInput(fLguiItem.getTableHandler().getPattern(gid));
					} else {
						setViewerInput();
					}
					viewer.refresh();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void setFocus() {
		if (!viewCreated) {
			viewCreated = createTable();
		}
		viewer.getControl().setFocus();
	}

	/**
	 * 
	 * @param tableViewer
	 * @param fSelected
	 */
	private void createColumns() {
		if (fLguiItem.isLayout() || fLguiItem.getTableHandler() == null) {
			return;
		}

		final ITableColumnLayout[] tableColumnLayouts = fLguiItem.getTableHandler().getTableColumnLayout(gid);
		if (tableColumnLayouts.length == 0) {
			return;
		}

		treeColumns = new TreeColumn[tableColumnLayouts.length];
		savedColumnWidths = new int[tableColumnLayouts.length + 1];

		final String[] columnTitlesPattern = fLguiItem.getColumnTitlePattern(gid);

		// first column with color rectangle
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		if (firstColumnlabelProvider != null) {
			firstColumnlabelProvider.dispose();
		}
		firstColumnlabelProvider = new FirstColumnLabelProvider();

		treeViewerColumn.setLabelProvider(firstColumnlabelProvider);
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setMoveable(false);
		treeColumn.setResizable(false);
		treeColumn.setAlignment(SWT.LEFT);
		treeColumn.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				final List<Integer> newOrder = new ArrayList<Integer>();
				newOrder.add(0);
				for (final int column : tree.getColumnOrder()) {
					if (column != 0) {
						newOrder.add(column);
					}
				}
				final int[] newOrderArray = new int[newOrder.size()];
				for (int i = 0; i < newOrder.size(); i++) {
					newOrderArray[i] = newOrder.get(i);
				}

				tree.setColumnOrder(newOrderArray);
			}
		});
		createMenuItem(headerMenu, treeColumn, 0);
		treeColumnLayout.setColumnData(treeColumn, new ColumnPixelData(40, true));

		// the remaining columns
		for (int i = 0; i < tableColumnLayouts.length; i++) {
			treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
			final int cellNumber = i;
			treeViewerColumn.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(ViewerCell cell) {
					String value = "-"; //$NON-NLS-1$
					final Row row = (Row) cell.getElement();
					if (row != null && row.cells.length > cellNumber) {
						value = row.cells[cellNumber].value;
					}
					cell.setText(value);
				}
			});
			final TreeColumn treeColumnI = treeViewerColumn.getColumn();
			treeColumnI.setMoveable(true);
			boolean isFiltered = false;
			for (final String title : columnTitlesPattern) {
				if (title.equals(tableColumnLayouts[i].getTitle())) {
					isFiltered = true;
				}
			}
			if (isFiltered) {
				treeColumnI.setText(tableColumnLayouts[i].getTitle() + " #"); //$NON-NLS-1$
			} else {
				treeColumnI.setText(tableColumnLayouts[i].getTitle());
			}

			treeColumnI.setAlignment(getColumnAlignment(tableColumnLayouts[i].getStyle()));

			if (tableColumnLayouts[i].isActive()) {
				final boolean resizable = true;
				treeColumnI.setResizable(resizable);

				/*
				 * Create the header menu for this column
				 */
				createMenuItem(headerMenu, treeColumnI, i + 1);

				/*
				 * Set the column width
				 */
				treeColumnLayout.setColumnData(treeColumnI,
						new ColumnPixelData((int) (tableColumnLayouts[i].getWidth() * composite.getClientArea().width), resizable));
			} else {
				final boolean resizable = false;
				treeColumnI.setResizable(resizable);

				/*
				 * Create the header menu for this column
				 */
				createMenuItem(headerMenu, treeColumnI, i + 1);

				/*
				 * Set the column width
				 */
				savedColumnWidths[i + 1] = 0;
				treeColumnLayout.setColumnData(treeColumnI, new ColumnPixelData(0, resizable));
			}
			if (Util.isLinux()) {
				treeColumnI.addControlListener(new ControlAdapter() {
					@Override
					public void controlResized(ControlEvent e) {
						if (treeColumnI.getWidth() < 10) {
							treeColumnI.setWidth(10);
						}
					}
				});
			}
			treeColumns[i] = treeColumnI;
		}

		// just a default column
		treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		treeViewerColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setMoveable(true);
		treeColumn.setText(""); //$NON-NLS-1$
		treeColumn.setAlignment(SWT.TRAIL);
		treeColumn.setResizable(false);
		treeColumnLayout.setColumnData(treeColumn, new ColumnPixelData(0, false));

		/*
		 * Sorting is done in the model as the table is virtual and has a lazy content provider.
		 */
		final Listener sortListener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				final TreeColumn currentColumn = (TreeColumn) e.widget;

				if (tree.getSortColumn() == currentColumn) {
					tree.setSortDirection(tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
				} else {
					tree.setSortColumn(currentColumn);
					tree.setSortDirection(SWT.UP);
				}
				if (fLguiItem != null && fLguiItem.getTableHandler() != null) {
					fLguiItem.getTableHandler().setSortProperties(gid, getSortIndex(), getSortDirectionString());
				}
				lmlManager.sortLgui();
			}
		};
		for (final TreeColumn col : treeColumns) {
			col.addListener(SWT.Selection, sortListener);
		}
		// Mouse action (in combination with nodedisplay)
		if (gid.equals(ILMLCoreConstants.ID_ACTIVE_JOBS_VIEW)) {
			tree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (e.button == 1) {
						isMouseDown = true;
						final TreeItem item = tree.getItem(new Point(e.x, e.y));
						if (item != null && !composite.isDisposed()) {
							lmlManager.selectObject(item.getData().toString());
							lmlManager.markObject(item.getData().toString());
						}
					}
				}

				@Override
				public void mouseUp(MouseEvent e) {
					if (e.button == 1) {
						final TreeItem item = tree.getItem(new Point(e.x, e.y));
						if (item != null && !composite.isDisposed()) {
							lmlManager.unmarkObject(item.getData().toString());
							lmlManager.unselectObject(item.getData().toString());
						}
						isMouseDown = false;
					}
				}

			});
		}
		/*
		 * tree.addMouseMoveListener(new MouseMoveListener() {
		 * 
		 * public void mouseMove(MouseEvent e) { final TreeItem item = tree.getItem(new Point(e.x, e.y)); if (item == null) {
		 * return; } if (selectedItem != null && !selectedItem.equals(item)) { if (!selectedItem.isDisposed()) {
		 * lmlManager.unselectObject(selectedItem.getData().toString()); } } selectedItem = item; if (!selectedItem.isDisposed()) {
		 * lmlManager.selectObject(selectedItem.getData().toString()); } }
		 * 
		 * }); tree.addMouseTrackListener(new MouseTrackListener() {
		 * 
		 * public void mouseEnter(MouseEvent e) { // nothing }
		 * 
		 * public void mouseExit(MouseEvent e) { if (selectedItem != null && !selectedItem.isDisposed()) {
		 * lmlManager.unselectObject(selectedItem.getData().toString()); selectedItem = null; } }
		 * 
		 * public void mouseHover(MouseEvent e) { // nothing } });
		 */
	}

	private void createMenuItem(Menu parent, final TreeColumn column, final int index) {

		final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
		itemName.setText(column.getText());
		itemName.setSelection(column.getResizable());
		itemName.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				boolean active = true;
				if (itemName.getSelection()) {
					if (savedColumnWidths[index] <= 50) {
						savedColumnWidths[index] = 50;
					}
					column.setWidth(savedColumnWidths[index]);
					column.setResizable(true);
				} else {
					savedColumnWidths[index] = column.getWidth();
					column.setWidth(0);
					column.setResizable(false);
					active = false;
				}
				if (fLguiItem != null) {
					fLguiItem.getTableHandler().setTableColumnActive(gid, column.getText(), active);
				}
			}
		});

	}

	private boolean createTable() {
		boolean viewChanged = false;
		if (fLguiItem != null && composite.getClientArea().width > 0) {
			createColumns();
			if (fLguiItem.getTableHandler() != null) {
				final Object[] sortProperties = fLguiItem.getTableHandler().getSortProperties(gid);

				if (!((String) sortProperties[1]).equals(ITableColumnLayout.SORT_DIRECTION_NONE)
						&& ((Integer) sortProperties[0]) != -1) {
					tree.setSortDirection(getSortDirectionInt((String) sortProperties[1]));
					tree.setSortColumn(getSortColumn((Integer) sortProperties[0]));
					fLguiItem.getTableHandler().sort(gid, SWT.UP, getSortIndex(), tree.getSortDirection());
				}

				filterOwnJobsActionItem.getAction().setEnabled(true);
				filterOwnJobsActionItem.getAction().setChecked(fLguiItem.isFilterOwnJobActive(gid));
				filterActionItem.getAction().setEnabled(true);
			}

			// Insert the input
			setViewerInput();
			viewChanged = true;
		}
		composite.layout();
		return viewChanged;
	}

	/**
	 * Disposing the table.
	 * 
	 * Setting defaults values to sort column and sort direction. Deleting for every TreeColumn corresponding listeners and the
	 * TreeColumn itself. All MenuItems from the headerMenu and the MenuManager of the View are also erased.
	 */
	private void disposeTable() {
		tree.setSortColumn(null);
		tree.setSortDirection(0);

		filterOwnJobsActionItem.getAction().setEnabled(false);
		filterActionItem.getAction().setEnabled(false);

		/*
		 * Remove columns
		 */
		final TreeColumn[] oldColumns = tree.getColumns();
		for (final TreeColumn oldColumn : oldColumns) {
			final Listener[] oldListeners = oldColumn.getListeners(SWT.Selection);
			for (final Listener oldListener : oldListeners) {
				oldColumn.removeListener(SWT.Selection, oldListener);
			}
			oldColumn.dispose();
		}
		treeColumns = new TreeColumn[0];
		if (firstColumnlabelProvider != null) {
			firstColumnlabelProvider.dispose();
		}

		/*
		 * Remove menu items
		 */
		for (final MenuItem item : headerMenu.getItems()) {
			item.dispose();
		}
	}

	/**
	 * Changes the width of a tree column in order to send an
	 * SWT resize event. Thus, the table columns obtain the correct heights.
	 * Tries to decrease and increase the width of the last column by one.
	 * As a result, the width does not change overall.
	 */
	private void fixRowHeight() {
		// Fixes squished rows issue documented in bug 349994, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=349994
		if (!org.eclipse.jface.util.Util.isGtk()) {
			return;
		}

		if (tree == null || tree.getColumns() == null) {
			return;
		}
		final int lastItem = tree.getColumns().length - 1;
		if (lastItem >= 0) {
			final TreeColumn lastColumn = tree.getColumn(lastItem);
			if (lastColumn == null) {
				return;
			}
			final int width = lastColumn.getWidth();
			// Try to decrease the width by one
			int newWidth = width - 1;
			if (newWidth < 0) {
				newWidth = width + 1;
			}
			lastColumn.setWidth(newWidth);
			lastColumn.setWidth(width);
		}
	}

	private int getColumnAlignment(String alignment) {
		if (alignment.equals(ITableColumnLayout.COLUMN_STYLE_LEFT)) {
			return SWT.LEAD;
		}
		if (alignment.equals(ITableColumnLayout.COLUMN_STYLE_RIGHT)) {
			return SWT.TRAIL;
		}
		if (alignment.equals(ITableColumnLayout.COLUMN_STYLE_CENTER)) {
			return SWT.CENTER;
		}
		return SWT.LEAD;
	}

	private TreeColumn getSortColumn(Integer index) {
		TreeColumn column = null;
		if (index >= 0 && index < treeColumns.length) {
			column = treeColumns[index];
		}
		return column;
	}

	private int getSortDirectionInt(String sortDirection) {
		if (sortDirection.equals(ITableColumnLayout.SORT_DIRECTION_UP)) {
			return SWT.UP;
		} else if (sortDirection.equals(ITableColumnLayout.SORT_DIRECTION_DOWN)) {
			return SWT.DOWN;
		}
		return -1;
	}

	/**
	 * Determines the sort direction and gives back the int value of the determined sort direction. Default value is "NONE".
	 * 
	 * @return int value of the determined sort direction
	 */
	private String getSortDirectionString() {
		if (tree.getSortDirection() == SWT.UP) {
			return ITableColumnLayout.SORT_DIRECTION_UP;
		} else if (tree.getSortDirection() == SWT.DOWN) {
			return ITableColumnLayout.SORT_DIRECTION_DOWN;
		}
		return ITableColumnLayout.SORT_DIRECTION_NONE;
	}

	/**
	 * Determines the sort column and gives back the index of the determined sort column. Default value is -1.
	 * 
	 * @return index value of the determined sort column
	 */
	private int getSortIndex() {
		for (int i = 0; i < treeColumns.length; i++) {
			if (treeColumns[i].equals(tree.getSortColumn())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private Double[] getWidths() {
		final TreeColumn[] columns = tree.getColumns();
		int width = 0;
		for (final TreeColumn column : columns) {
			width += column.getWidth();
		}
		final Double[] widths = new Double[columns.length - 2];
		for (int i = 0; i < columns.length - 2; i++) {
			final TreeColumn column = columns[i + 1];
			Double columnWidth = (double) 0;
			if (column.getWidth() > 0) {
				columnWidth = Integer.valueOf(column.getWidth()).doubleValue() / width;
			}
			widths[i] = columnWidth;
		}
		return widths;
	}

	/**
	 * Rearranging of the columns order.
	 * 
	 * The table of the TableView has two additional columns (one visible, the other is hidden) than there are saved in the inner
	 * model. This methods eliminates these two columns and rewrite the numbers of the column order. The visible one should always
	 * be the first, but during moving other columns this fact can change. The hidden column is always the last one.
	 * 
	 * @param order
	 *            order at the beginning
	 * @return new column order array with two columns removed
	 */
	private int[] removeFirstColumn(int[] order) {
		final int[] orderNew = new int[order.length - 2];
		int dif = 0;
		for (int i = 0; i < order.length - 1; i++) {
			if (order[i] != 0) {
				orderNew[i - dif] = order[i] - 1;
			} else {
				dif = 1;
			}
		}
		return orderNew;
	}

	/**
	 * Determines the current Layout of the view and transmit it to the inner model.
	 * 
	 * Sends first changes of the column order to the model side, then changes of the width. After that the sorting parameters are
	 * determined. There can be changes of the column order, so the position of the column is checked also.
	 * 
	 * The default values of sortIndex and sortDirection are -1 and "NON".
	 */
	private void saveColumnLayout() {
		if (fLguiItem != null && fLguiItem.getTableHandler() != null && tree.getColumnOrder().length != 0) {
			final int[] indexe = removeFirstColumn(tree.getColumnOrder());
			fLguiItem.getTableHandler().changeTableColumnsOrder(gid, indexe);
			fLguiItem.getTableHandler().changeTableColumnsWidth(gid, getWidths());

			final int sortIndex = getSortIndex();
			final String sortDirection = getSortDirectionString();

			int index = 0;
			for (int i = 0; i < indexe.length; i++) {
				if (indexe[i] == sortIndex) {
					index = i;
				}
			}
			fLguiItem.getTableHandler().setSortProperties(gid, index, sortDirection);

		}
	}

	/**
	 * 
	 */
	private void setViewerInput() {
		/*
		 * Don't change input if mouse is down as this causes a SIGSEGV in SWT!
		 */
		if (!isMouseDown) {
			Row[] input = new Row[0];
			if (fLguiItem != null && fLguiItem.getTableHandler() != null) {
				input = fLguiItem.getTableHandler().getTableDataWithColor(gid, gid.equals(ILMLCoreConstants.ID_ACTIVE_JOBS_VIEW));
			}
			if (!composite.isDisposed()) {
				viewer.setInput(input);
				viewer.setChildCount(input, input.length);
				fixRowHeight();
			}
		}
	}

	/**
	 * 
	 */
	private void setViewerInput(List<IPattern> filterValues) {
		/*
		 * Don't change input if mouse is down as this causes a SIGSEGV in SWT!
		 */
		if (!isMouseDown) {
			Row[] input = new Row[0];
			if (fLguiItem != null && fLguiItem.getTableHandler() != null) {
				input = fLguiItem.getTableHandler().getTableDataWithColor(gid, gid.equals(ILMLCoreConstants.ID_ACTIVE_JOBS_VIEW),
						filterValues);
			}
			if (!composite.isDisposed()) {
				viewer.setInput(input);
				viewer.setChildCount(input, input.length);
				fixRowHeight();
			}
		}
	}
}
