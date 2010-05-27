/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/

package org.eclipse.photran.internal.ui.views.vpgproblems;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.eclipse.VPGSchedulingRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Fortran Analysis/Refactoring Problems view, A.K.A. VPG Problems view.
 * <p>
 * Based on Eclipse JFace TableView Tutorial; thanks to Lars Vogel
 * for posting the tutorial
 * (http://www.vogella.de/articles/EclipseJFaceTable/aritcle.html)
 * Based on samples provided in Java Developer’s Guide to Eclipse,
 * Chapter 18 (http://www.jdg2e.com/ch18.views/doc/index.htm and
 * http://www.jdg2e.com/jdg2e_CD_for_eclipse321/plug-in_development/examples/com.ibm.jdg2e.view.marker/src-marker/com/ibm/jdg2e/view/marker/MarkerView.java)
 * © Copyright International Business Machines Corporation, 2003, 2004, 2006.
 * All Rights Reserved.
 * Code or samples provided therein are provided without warranty of any kind.
 *
 * @author Timofey Yuvashev
 * @author Esfar Huq, Rui Wang - Replaced setSorter() with setComparator(); added add'l filtering
 * @author Jeff Overbey - Refactoring/cleanup
 */

public class VPGProblemView extends ViewPart implements VPGLog.ILogListener
{
    static enum VPGViewColumn
    {
        // Column    Label         Width (in pixels)
        DESCRIPTION(Messages.VPGProblemView_DescriptionColumnHeader, 44),
        RESOURCE   (Messages.VPGProblemView_ResourceColumnHeader,    10),
        PATH       (Messages.VPGProblemView_PathColumnHeader,        20);
        
        public final String name;
        public final int width;
        
        private VPGViewColumn(String name, int widthInPixels)
        {
            this.name = name;
            this.width = widthInPixels;
        }
    }
    
    private static RecreateMarkers markersTask = null;
    
    private TableViewer tableViewer          = null;
    private TableSorter tableSorter          = null;
    private Clipboard clipboard              = null;
    private CopyMarkedFileAction copyAction  = null;
    private OpenMarkedFileAction openAction  = null;
    private ShowFullMessageAction showAction = null;

    private ErrorWarningFilterAction warningsFilterAction = null;
    private ErrorWarningFilterAction errorsFilterAction   = null;
    private SelectedResourceFilterAction selectionFilterAction      = null;

    public int[] markerCount = {0,0,0};  //Number of Warnings and Errors respectively
    
    @Override
    public void createPartControl(Composite parent)
    {
        GridLayout overallLayout = new GridLayout(1,false);
        parent.setLayout(overallLayout);

        createTableViewer(parent);
        createTableColumns(tableViewer);
        setTableGridData();

        getSite().setSelectionProvider(tableViewer);

        //TODO: Change the default string
        MenuManager manager = createMenuManager();

        getSite().registerContextMenu(manager, tableViewer);

        tableViewer.setContentProvider(new VPGProblemContentProvider());
        tableViewer.setLabelProvider(new VPGProblemLabelProvider());

        PhotranVPG.getInstance().log.addLogListener(this);

        createToolbarButtons();
        initEvents();
    }
    
    private void createTableViewer(Composite parent)
    {
        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.MULTI | SWT.FULL_SELECTION);

        tableSorter = new TableSorter();
        tableViewer.setComparator(this.tableSorter);
    }

    private MenuManager createMenuManager()
    {
        MenuManager manager = new VPGProblemContextMenu(getViewSite());
        tableViewer.getTable().setMenu(manager.createContextMenu(tableViewer.getTable()));
        return manager;
    }
    
    public void onLogChange()
    {
        if (markersTask == null)
        {
            // If non-null, this task is already running; don't start a 2nd instance
            
            markersTask = new RecreateMarkers();
            
            WorkspaceJob job = new RecreateMarkers();
            job.setRule(MultiRule.combine(VPGSchedulingRule.getInstance(),
                                          ResourcesPlugin.getWorkspace().getRoot()));
            job.schedule();
        }
    }
    
    private class RecreateMarkers extends WorkspaceJob
    {
        private RecreateMarkers()
        {
            super(Messages.VPGProblemView_UpdatingProblemsViewMessage);
        }
        
        @Override public IStatus runInWorkspace(final IProgressMonitor monitor)
        {   
            getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    final List<IMarker> markers = PhotranVPG.getInstance().recomputeErrorLogMarkers();
                    
                    Table t = tableViewer.getTable();
                    
                    t.removeAll();
                    t.update();
                
                    tableViewer.setInput(markers);
                    countMarkers(markers);
                   
                    setErrorWarningFilterButtonText();
                }
            });
            
            markersTask = null;
            return Status.OK_STATUS;
        }
    }

    void setErrorWarningFilterButtonText()
    {
        if (warningsFilterAction != null && errorsFilterAction != null)
        {
            //only print out counts when looking at an unfiltered view
            if(!selectionFilterAction.isChecked())
            {
                warningsFilterAction.setText(Messages.bind(Messages.VPGProblemView_nWarnings, markerCount[IMarker.SEVERITY_WARNING]));
                errorsFilterAction.setText(Messages.bind(Messages.VPGProblemView_nErrors, markerCount[IMarker.SEVERITY_ERROR]));
            }
            else //FILTERED
            {
                warningsFilterAction.setText(Messages.VPGProblemView_Warnings);
                errorsFilterAction.setText(Messages.VPGProblemView_Errors);
            }
        }
    }
    
    TableViewer getTableViewer()
    {
        return tableViewer;
    }
    
    private void setTableGridData()
    {
        GridData tableData = new GridData();
        tableData.grabExcessHorizontalSpace = true;
        tableData.grabExcessVerticalSpace = true;
        tableData.horizontalAlignment = GridData.FILL;
        tableData.verticalAlignment = GridData.FILL;
        tableViewer.getTable().setLayoutData(tableData);
    }

    private void resetMarkerCount()
    {
        for(int i = 0; i < markerCount.length; ++i)
        {
            markerCount[i] = 0;
        }
    }
    
    private void countMarkers(List<IMarker> markers)
    {
        //Get all the markers in the workspace
        //IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(null, true, IResource.DEPTH_INFINITE);
        resetMarkerCount();      
     
        for(IMarker marker : markers)
        {
            int sev = MarkerUtilities.getSeverity(marker);

            if(sev == IMarker.SEVERITY_ERROR || sev == IMarker.SEVERITY_WARNING)
                markerCount[sev]++;
        }
    }

    public void createActions()
    {
        copyAction = new CopyMarkedFileAction(this);
        openAction = new OpenMarkedFileAction(getSite());
        showAction = new ShowFullMessageAction(getSite());

        warningsFilterAction = new ErrorWarningFilterAction(tableViewer, IMarker.SEVERITY_WARNING);

        errorsFilterAction = new ErrorWarningFilterAction(tableViewer, IMarker.SEVERITY_ERROR);

        selectionFilterAction = new SelectedResourceFilterAction(this);
    }

    private void addActionsToToolbar()
    {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

        toolBarManager.add(errorsFilterAction);
        toolBarManager.add(warningsFilterAction);
        toolBarManager.add(selectionFilterAction);
        toolBarManager.add(openAction);
        toolBarManager.add(copyAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(showAction);
    }

    private void addTableViewerSelectionChangeListener()
    {
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent e)
            {
                boolean isEnabled = !e.getSelection().isEmpty();
                openAction.setEnabled(isEnabled);
                copyAction.setEnabled(isEnabled);
                showAction.setEnabled(isEnabled);
            }
        });
    }

    private void createToolbarButtons()
    {
        createActions();

        openAction.setEnabled(false);
        copyAction.setEnabled(false);
        showAction.setEnabled(false);
        
        addActionsToToolbar();
        addTableViewerSelectionChangeListener();
    }


    /*
     * Creates layout for a table. Simply adds ColumnData to each column in the table,
     * setting that column's width to pre-defined value
     */
    private TableLayout createTableLayout()
    {
        TableLayout layout = new TableLayout();
        for (VPGViewColumn col : VPGViewColumn.values())
            layout.addColumnData(new ColumnWeightData(col.width, true));
        return layout;
    }

    /*
     * Creates columns for the table contained in TableViewer.
     * Assigns them name, size, alignment and adds a selectionListener
     * that is responsible for sorting that column, if it is clicked on
     */
    private void createTableColumns(final TableViewer viewer)
    {
        Table table = viewer.getTable();
        TableLayout layout = createTableLayout();
        table.setLayout(layout);

        for (final VPGViewColumn vpgCol : VPGViewColumn.values())
        {
            final TableColumn viewerCol = new TableViewerColumn(viewer, SWT.NONE).getColumn();

            viewerCol.setText(vpgCol.name);
            viewerCol.setToolTipText(vpgCol.name);
            viewerCol.setAlignment(SWT.LEFT);
            viewerCol.setResizable(true);
            viewerCol.setMoveable(true);

            viewerCol.addSelectionListener(new ColumnSelectionListener(viewerCol, vpgCol, viewer));
        }

        table.setLinesVisible(true);
        table.setHeaderVisible(true);
    }

    private final class ColumnSelectionListener extends SelectionAdapter
    {
        private final TableColumn column;
        private final VPGViewColumn col;
        private final TableViewer viewer;

        private ColumnSelectionListener(TableColumn column, VPGViewColumn col, TableViewer viewer)
        {
            this.column = column;
            this.col = col;
            this.viewer = viewer;
        }

        @Override public void widgetSelected(SelectionEvent e)
        {
            tableSorter.setColumn(col.ordinal());
            int dir = viewer.getTable().getSortDirection();
            if(viewer.getTable().getSortColumn() == column)
                dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
            else
                dir = SWT.DOWN;
            viewer.getTable().setSortDirection(dir);
            viewer.getTable().setSortColumn(column);
            viewer.refresh();
        }
    }

    /*
     * Initializes event Listeners and Actions for the Table
     */
    private void initEvents()
    {
        tableViewer.getTable().addMouseListener(new DoubleClickListener());
    }

    private final class DoubleClickListener extends MouseAdapter
    {
        @Override public void mouseDoubleClick(MouseEvent dblClick)
        {
            Table t = (Table)dblClick.getSource();
            for (TableItem item : t.getSelection())
                if (item.getData() instanceof IMarker)
                    openMarker((IMarker)item.getData());
        }

        private void openMarker(IMarker marker)
        {
            if (marker.getResource() != null)
                new OpenMarkedFileAction(getViewSite()).run(marker);
        }
    }

    public Clipboard getClipboard()
    {
        if (clipboard == null)
            clipboard = new Clipboard(getDisplay());
        return clipboard;
    }
    
    private Display getDisplay()
    {
        return getSite().getWorkbenchWindow().getWorkbench().getDisplay();
    }

    @Override
    public void setFocus()
    {
        tableViewer.getControl().setFocus();
    }

    //TODO: Should we remove more stuff?
    @Override
    public void dispose()
    {
        if (clipboard != null)
            clipboard.dispose();
        super.dispose();
    }
}
