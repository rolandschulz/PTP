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

import java.util.ArrayList;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Fortran Analysis/Refactoring Problems view, A.K.A. VPG Problems view.
 * <p>
 * Most of the code was copied from Eclipse JFace TableView Tutorial
 * (http://www.vogella.de/articles/EclipseJFaceTable/aritcle.html) and
 * Java Developer's Guide to Eclipse, Chapter 18,
 * (http://www.jdg2e.com/jdg2e_CD_for_eclipse321/plug-in_development/examples/com.ibm.jdg2e.view.marker/src-marker/com/ibm/jdg2e/view/marker/MarkerView.java)
 *
 * @author Timofey Yuvashev
 * 
 * @author Esfar Huq
 * @author Rui Wang
 * 
 * Replaced call to setSorter() to setComparator() in method createPartControl()
 */
public class VPGProblemView extends ViewPart implements VPGLog.ILogListener
{
    private TableViewer tableViewer             = null;
    private TableSorter tableSorter             = null;
    private Clipboard clipboard                 = null;
    private CopyMarkedFileAction copyAction     = null;
    private OpenMarkedFileAction openAction     = null;
    private ShowFullMessageAction showAction    = null;
    //private RemoveMarkerAction remAction        = null;

    //private VPGViewFilterAction infosMarkerFilterAction     = null;
    private VPGViewFilterAction warningsMarkerFilterAction  = null;
    private VPGViewFilterAction errorsMarkerFilterAction    = null;

    private static final String[] COLUMN_NAMES = {"Description",
                                                  "Resource", "Path" /*,
                                                  "Location"*/};
    private static final int[] COLUMN_WIDTHS   = {44,
                                                  10, 20/*,
                                                  6*/};

    //TODO: Depending on how we will handle updates to markers, we might need a
    // way to update this array. Currently, it is populated as Workbench's start-time
    // and remains unchaged since then
    public static int[] MARKER_COUNT = {0,0,0};  //Number of Infos, Warnings and Errors respectively


//    /* (non-Javadoc)
//     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
//     */
    @Override
    public void createPartControl(Composite parent)
    {
        GridLayout overallLayout = new GridLayout(1,false);
        parent.setLayout(overallLayout);

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.MULTI | SWT.FULL_SELECTION);

        tableSorter = new TableSorter();
        tableViewer.setComparator(this.tableSorter);

        createTableColumns(tableViewer);
        setTableGridData();

        //Share Viewer Selection with other workbench parts
        getSite().setSelectionProvider(tableViewer);

        //TODO: Change the default string
        MenuManager manager = new VPGProblemContextMenu(getViewSite(), "Problems View Menu");
        tableViewer.getTable().setMenu(manager.createContextMenu(tableViewer.getTable()));

        //Register Viewer ContextMenu with other workbench parts
        getSite().registerContextMenu(manager, tableViewer);

        tableViewer.setContentProvider(new VPGProblemContentProvider());
        tableViewer.setLabelProvider(new VPGProblemLabelProvider());

        PhotranVPG.getInstance().log.addLogListener(this);

        createToolbarButtons();
        initEvents();
    }

    private static RecreateMarkers markersTask = null;
    
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
            super("Updating Fortran Analysis/Refactoring Problems view");
        }
        
        @Override public IStatus runInWorkspace(final IProgressMonitor monitor)
        {
            final List<IMarker> markers = PhotranVPG.getInstance().recomputeErrorLogMarkers();
            
            getSite().getShell().getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    Table t = tableViewer.getTable();
                    t.removeAll();
                    t.update();
                    tableViewer.setInput(markers);
                    countMarkers(markers);
                    if (warningsMarkerFilterAction != null && errorsMarkerFilterAction != null)
                    {
                        String warnStr = String.valueOf(MARKER_COUNT[IMarker.SEVERITY_WARNING]) + " Warnings";
                        String errStr  = String.valueOf(MARKER_COUNT[IMarker.SEVERITY_ERROR]) + " Errors";
                        warningsMarkerFilterAction.setText(warnStr);
                        errorsMarkerFilterAction.setText(errStr);
                    }
                }
            });
            
            markersTask = null;
            return Status.OK_STATUS;
        }
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
        for(int i = 0; i < MARKER_COUNT.length; ++i)
        {
            MARKER_COUNT[i] = 0;
        }
    }
    
    private void countMarkers(List<IMarker> markers)
    {
        //Get all the markers in the workspace
        //IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(null, true, IResource.DEPTH_INFINITE);
        resetMarkerCount();;
        
        //HACK
        for(IMarker marker : markers)
        {
            int sev = MarkerUtilities.getSeverity(marker);

            if(sev == IMarker.SEVERITY_ERROR    ||
               sev == IMarker.SEVERITY_WARNING  ||
               sev == IMarker.SEVERITY_INFO)
                MARKER_COUNT[sev]++;
        }
    }

    private void createActions()
    {
        copyAction = new CopyMarkedFileAction(this, "Copy");
        openAction = new OpenMarkedFileAction(getSite());
        showAction = new ShowFullMessageAction(getSite());
        //remAction  = new RemoveMarkerAction(getSite());

        //int sevInfo = IMarker.SEVERITY_INFO;
        int sevWarn = IMarker.SEVERITY_WARNING;
        int sevErr  = IMarker.SEVERITY_ERROR;

        // Copy list to avoid ConcurrentModificationException
        List<IMarker> markers = new ArrayList<IMarker>(PhotranVPG.getInstance().recomputeErrorLogMarkers());
        countMarkers(markers);

        //String infoStr = String.valueOf(MARKER_COUNT[sevInfo]) + " Infos";
        String warnStr = String.valueOf(MARKER_COUNT[sevWarn]) + " Warnings";
        String errStr  = String.valueOf(MARKER_COUNT[sevErr]) + " Errors";

        /*infosMarkerFilterAction     = new VPGViewFilterAction(tableViewer,
                                                              infoStr,
                                                              sevInfo);*/

        warningsMarkerFilterAction  = new VPGViewFilterAction(tableViewer,
                                                              warnStr,
                                                              sevWarn);

        errorsMarkerFilterAction    = new VPGViewFilterAction(tableViewer,
                                                              errStr,
                                                              sevErr);
    }

    private void addActionsToToolbar()
    {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

        toolBarManager.add(errorsMarkerFilterAction);
        toolBarManager.add(warningsMarkerFilterAction);
        //toolBarManager.add(infosMarkerFilterAction);
        toolBarManager.add(openAction);
        toolBarManager.add(copyAction);
        //toolBarManager.add(remAction);
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
                //remAction.setEnabled(isEnabled);
                showAction.setEnabled(isEnabled);
            }
        });
    }

    private void createToolbarButtons()
    {
        createActions();

        openAction.setEnabled(false);
        copyAction.setEnabled(false);
        //remAction.setEnabled(false);
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
        for(int i = 0; i < COLUMN_WIDTHS.length; i++)
        {
            layout.addColumnData(new ColumnWeightData(COLUMN_WIDTHS[i],true));
        }
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

        for(int i = 0; i < COLUMN_NAMES.length; i++)
        {
            //We need these variables to be final, b/c we want to use them later on in the
            // definition of widgetSelected() method
            final int index = i;
            final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
            final TableColumn column = viewerColumn.getColumn();

            //Create column and set its parameters
            column.setText(COLUMN_NAMES[i]);
            column.setToolTipText(COLUMN_NAMES[i]);
            column.setAlignment(SWT.LEFT);
            column.setResizable(true);
            column.setMoveable(true);

            //Add an even listener to the column
            column.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        tableSorter.setColumn(index);
                        int dir = viewer.getTable().getSortDirection();
                        if(viewer.getTable().getSortColumn() == column)
                            dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                        else
                            dir = SWT.DOWN;
                        viewer.getTable().setSortDirection(dir);
                        viewer.getTable().setSortColumn(column);
                        viewer.refresh();
                    }
                });
        }

        table.setLinesVisible(true);
        table.setHeaderVisible(true);
    }

    /*
     * Initializes event Listeners and Actions for the Table
     */
    private void initEvents()
    {
        tableViewer.getTable().addMouseListener(new MouseListener()
            {
                public void mouseDoubleClick(MouseEvent dblClick)
                {
                    Table t = (Table)dblClick.getSource();
                    TableItem[] selection = t.getSelection();
                    for(int i = 0; i < selection.length; i++)
                    {
                        if (selection[i].getData() instanceof IMarker)
                        {
                            IMarker marker = (IMarker)(selection[i].getData());
                            if (marker.getResource() != null)
                            {
                                try
                                {
                                    OpenMarkedFileAction openAction = new OpenMarkedFileAction(getViewSite());
                                    openAction.run(marker);
                                }
                                catch (Throwable x)
                                {
                                    ;
                                }
                            }
                        }

                    }
                }

                public void mouseDown(MouseEvent arg0)
                {}

                public void mouseUp(MouseEvent arg0)
                {}

            });
    }

    public Clipboard getClipboard()
    {
        if(clipboard == null)
            clipboard = new Clipboard(getSite().getShell().getDisplay());
        return clipboard;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        tableViewer.getControl().setFocus();
    }

    //TODO: Should we remove more stuff?
    @Override
    public void dispose()
    {
        if(clipboard != null)
            clipboard.dispose();
        super.dispose();
    }
}
