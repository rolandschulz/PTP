/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.mpi.core.views;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.mpi.core.Artifact;
import org.eclipse.ptp.mpi.core.MpiArtifactManager;
import org.eclipse.ptp.mpi.core.MpiIDs;
import org.eclipse.ptp.mpi.core.MpiPlugin;
import org.eclipse.ptp.mpi.core.util.AnalysisUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * This view shows each MPI artifact, represented by a marker. It provides MPI information and actions. The object model
 * is the marker.
 * 
 */
public class MPITableView extends ViewPart
{
    private TableViewer          viewer;
    private Action               mpiInfoAction;
    private Action               filterAction;
    private Action               doubleClickAction;
    private static final boolean traceOn            = false;
    private static final boolean traceStatusLine    = false;
    protected ViewerSorter       nameSorter;
    protected GenericSorter      lineNoSorter;
    protected FilenameSorter     filenameSorter;
    protected GenericSorter      orderSorter;                              // sortby "icon"
    protected GenericSorter      nameArtifactSorter;
    protected GenericSorter      constructSorter;
    private IMarker              selectedMarker_    = null;
    protected StackList          remediatedPIs_     = new StackList();
    protected StackList          remediatedMarkers_ = new StackList();
    protected UpdateVisitor      visitor_           = new UpdateVisitor();

    /** be consistent about what we call these things */
    private static final String  THINGNAME          = "MPI Artifact";
    private static final String  THINGNAMES         = "MPI Artifacts";

    /*
     * The content provider class is responsible for providing objects to the view. It can wrap existing objects in
     * adapters or simply return objects as-is. These objects may be sensitive to the current input of the view, or
     * ignore it and always show the same content (like Task List, for example).
     */
    class ViewContentProvider implements IStructuredContentProvider, IResourceChangeListener
    {
        private IResource input;
        private boolean   hasRegistered = false;

        public void inputChanged(Viewer v, Object oldInput, Object newInput)
        {
            if (traceOn) System.out.println("MPITV inputChanged()...");
            // if this is the first time we have been given an input
            if (!hasRegistered) {
                // add me as a resource change listener so i can refresh at
                // least when markers are changed
                // POST_CHANGE: only want event notifications for after-the-fact
                ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
                hasRegistered = true;
                if (traceOn) System.out.println("Registered RCL for ViewContentProvider in MPITableView.");
            }
            if (newInput instanceof IResource) {
                this.input = (IResource) newInput;
            }

        }

        public void dispose()
        {
            if (traceOn) System.out.println("MPITV.ViewContentProvider.dispose()");
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

        }

        /**
         * Get the list of objects to populate this view.
         */
        public Object[] getElements(Object parent)
        {
            Object[] objs = null;
            try {
                String id = MpiIDs.MARKER_ID;
                if (input == null) {
                    if (traceOn) System.out.println("input is null in getElements...");
                }
                objs = input.findMarkers(id, false, IResource.DEPTH_INFINITE);
            } catch (CoreException e) {
                System.out.println("MPITV, exception getting model elements (markers for Table view)");
                e.printStackTrace();
            }
            if (traceOn) System.out.println("MPITV.get---Elements, found " + objs.length + " markers");
            return objs;

        }

        /**
         * react to a resource change event
         * 
         * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
         */

        public void resourceChanged(IResourceChangeEvent event)
        {
            if (traceOn) System.out.println("-----------------resourceChanged()");
            final IResourceDelta delta = event.getDelta();
            if (traceOn) printResourcesChanged(delta, 1);
            // remove the following when resource delta visitor does it all?
            Control ctrl = viewer.getControl();
            if (ctrl != null && !ctrl.isDisposed()) {
                ctrl.getDisplay().syncExec(new Runnable() {
                    public void run()
                    {
                        try {
                            if (traceOn)
                                System.out.println("viewer.update ea mkr in delta-- from resourceChanged()...");
                            if (traceOn) System.out.println("----processResourceChangeDelta()...");
                            processResourceChangeDelta(delta);
                            if (traceOn) System.out.println("----END processResourceChangeDelta()...");
                            if (traceOn) System.out.println("viewer.refresh()");
                            // we should have updated the indiv. rows we care about,
                            // but need this for Marker display after initial analysis,
                            // and for markers deleted, etc.
                            viewer.refresh();
                        } catch (Exception e) {
                            System.out.println("MPITV: Exception refreshing viewer: " + e);
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (traceOn) System.out.println("-----------------END resourceChanged()\n");

        }

        /**
         * Debugging statement help - prints the events, indented by nesting level
         * 
         * @param delta
         * @param indent
         */
        private void printResourcesChanged(IResourceDelta delta, int indent)
        {
            printOneResourceChanged(delta, indent);
            IResourceDelta[] children = delta.getAffectedChildren();
            for (int i = 0; i < children.length; i++)
                printResourcesChanged(children[i], indent + 1);
        }

        /**
         * Some debugging statement help
         * 
         * @param delta
         * @param indent
         */
        private void printOneResourceChanged(IResourceDelta delta, int indent)
        {
            StringBuffer buf = new StringBuffer(80);
            for (int i = 0; i < indent; i++)
                buf.append("  ");
            switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                    buf.append("ADDED");
                    break;
                case IResourceDelta.REMOVED:
                    buf.append("REMOVED");
                    break;
                case IResourceDelta.CHANGED:
                    buf.append("CHANGED");
                    testDelta(delta);
                    break;
                default:
                    buf.append("[");
                    buf.append(delta.getKind());
                    buf.append("]");
                    break;
            }
            buf.append(" ");
            buf.append(delta.getResource());
            System.out.println(buf);
        }

        /**
         * Show debugging info for a resource delta change
         * 
         * @param delta
         */
        private void testDelta(IResourceDelta delta)
        {
            System.out.print("Resource ");
            System.out.print(delta.getFullPath());
            System.out.println(" has changed.");
            int flags = delta.getFlags();
            if ((flags & IResourceDelta.CONTENT) != 0) {
                System.out.println("--> Content Change");
            }
            if ((flags & IResourceDelta.REPLACED) != 0) {
                System.out.println("--> Content Replaced");
            }
            if ((flags & IResourceDelta.MARKERS) != 0) {
                System.out.println("--> Marker Change");
                // IMarkerDelta[] markers = delta.getMarkerDeltas();
                // if interested in markers, check these deltas
            }
        }

        /**
         * Process the resource change - just the delta
         * 
         * @param delta
         */
        protected void processResourceChangeDelta(IResourceDelta delta)
        {
            try {
                delta.accept(visitor_);

            } catch (CoreException e2) {
                System.out.println("Error in PITV.processResourceChangeDelta()..");
                e2.printStackTrace();
            }
        }

    } // end ViewContentProvider

    /**
     * get MPI artifact from marker
     * 
     * @param marker
     * @return
     */
    protected Artifact getMpiArtifact(IMarker marker)
    {
        String id = null;
        Artifact mpiA = null;
        try {
            id = (String) marker.getAttribute(MpiIDs.ID);
            mpiA = MpiArtifactManager.getMpiArtifact(id);

        } catch (CoreException e) {
            // e.printStackTrace();
            System.out.println(e.getMessage() + " ... MPITV, CoreException getting artifact from hashMap; " + THINGNAME
                    + " id=" + id);
        } catch (NullPointerException ne) {
            System.out.println(ne.getMessage() + " ... MPITV, NullPtrExcp getting artifact from hashMap;" + THINGNAME
                    + " id=" + id);
        }
        return mpiA;

    }

    private final String getConstructStr(IMarker marker) throws CoreException
    {
        Integer constructType = (Integer) marker.getAttribute(MpiIDs.CONSTRUCT_TYPE);
        return Artifact.CONSTRUCT_TYPE_NAMES[constructType.intValue()];
    }

    /**
     * 
     * ViewLabelProvider - provides the text and images for the MPI artifacts in the MPI Table View
     * 
     * @author Beth Tibbitts
     * 
     * 
     */
    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        /**
         * Keep icons already created, and reuse the images
         */
        private HashMap iconHash = new HashMap();

        /**
         * provide what goes in each column; get the info from the marker
         */
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object o)
        {
            String temp = super.getText(o);
            return temp;
        }

        /**
         * Determine the text to go in each column
         * 
         * @param obj the Marker (we hope) that goes on the current row
         * @param index the column number in the table
         * 
         */
        public String getColumnText(Object obj, int index)
        {
            if (obj == null) {
                System.out.println("MPITV: LabelProv obj is null; index=" + index);
                return "MPITV obj null";
            }
            IMarker marker = (IMarker) obj;
            try {
                switch (index) {
                    case 1:
                        String id = (String) marker.getAttribute(MpiIDs.NAME);
                        return id;
                    case 2:
                        return (String) marker.getAttribute(MpiIDs.FILENAME);
                    case 3:
                        String x = IMarker.LINE_NUMBER;
                        String line = (marker.getAttribute(x)).toString();
                        if (traceOn) { // all this is for debugging purposes so
                            // don't even calculate if not debugging
                            Artifact mpiA = getMpiArtifact(marker);
                            String compLine = line + "-";
                            if (mpiA == null) {
                                if (traceOn) System.out.println("MPITV getColumnText- null mpiA");
                            } else {
                                int lineMPIa = mpiA.getLine();
                                compLine = compLine + lineMPIa;
                            }
                            System.out.println("MPITV.ViewLabelProvider gets marker line: mkr-mpiA: " + compLine);
                            showMarker(marker);
                        }
                        return line;
                    case 4:
                        return getConstructStr(marker);
                    default:
                        return "";
                }
            } catch (CoreException ce) {
                return ("MPITV error");
            }
        }

        /**
         * Provide the image that goes in a column, if any (Note that a table cell can contain both, an image and text,
         * which will be displayed side-by-side)
         * 
         * @param obj - the object we're getting the image for
         * @param index - the column that this image is to go in
         */
        public Image getColumnImage(Object obj, int index)
        {
            // we only put image icon in the first column
            switch (index) {
                case 0:
                    return getCustomImage(obj);
                default:
                    return null;
            }
        }

        /**
         * Get image for artifact. Note that different images could be used for different types of artifacts. For now we
         * have a single image.
         * 
         * @param obj the marker object that this artifact is represented by
         * @return image for marker
         * 
         */
        private Image getCustomImage(Object obj)
        {
            String iconName = "icons/mpi.gif";
            // if we've already created one of this type of icon, reuse it.
            // Note: use ImageRegistry instead?
            Image img = (Image) iconHash.get(iconName);
            if (img == null) {
                URL url = MpiPlugin.getDefault().find(new Path(iconName));
                ImageDescriptor id = ImageDescriptor.createFromURL(url);
                img = id.createImage();
                if (traceOn) System.out.println("MPITV: ***** created image for " + iconName);
                iconHash.put(iconName, img);// save for resuse
            }
            return img;
        }

        /**
         * Dispose of anything that would hang around rudely otherwise (such as image objects from the icons)
         * 
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */

        public void dispose()
        {
            if (traceOn) System.out.println("MPITV.ViewLabelProvider.dispose(); dispose of icon images");
            for (Iterator iter = iconHash.values().iterator(); iter.hasNext();) {
                Image img = (Image) iter.next();
                img.dispose();
            }
            super.dispose();
        }

    }

    /**
     * Default sorter for MPI items - the order they were created, which tends to group MPI items with their source code
     * locations
     * 
     * @author Beth Tibbitts
     */
    class NameSorter extends ViewerSorter
    {
    }

    /**
     * Sort MPI items by one or more of: MPIa, filename, lineNo, construct The derived classes will implement combine()
     * to say how the attributes are combined to get the sort desired.
     * 
     * @author Beth Tibbitts
     */
    abstract class GenericSorter extends ViewerSorter
    {
        protected boolean ascending = true;

        /**
         * Compare two items to determine sort order. Sort MPI items by one or more of: MPIa name, then file, then line
         * number, then construct
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            int result = 0;

            int cat1 = category(e1);
            int cat2 = category(e2);

            if (cat1 != cat2) return cat1 - cat2;

            java.text.Collator collator = this.getCollator();

            if (e1 instanceof IMarker) {
                try {
                    IMarker m1 = (IMarker) e1;
                    IMarker m2 = (IMarker) e2;
                    String name1 = (String) m1.getAttribute(MpiIDs.NAME);
                    String file1 = (String) m1.getAttribute(MpiIDs.FILENAME);
                    String line1 = (String) m1.getAttribute(MpiIDs.LINE).toString();
                    String construct1 = getConstructStr(m1);
                    String sort1 = combine(name1, file1, line1, construct1);

                    String name2 = (String) m2.getAttribute(MpiIDs.NAME);
                    String file2 = (String) m2.getAttribute(MpiIDs.FILENAME);
                    String line2 = (String) m2.getAttribute(MpiIDs.LINE).toString();
                    String construct2 = getConstructStr(m2);
                    String sort2 = combine(name2, file2, line2, construct2);

                    if (ascending)
                        result = collator.compare(sort1, sort2);
                    else result = collator.compare(sort2, sort1);

                    return result;
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
            return 0;

        }

        /**
         * Combine name, file, and/or line number to provide the string to sort by. Will be overridden by derived
         * classes as needed
         * 
         * @param name
         * @param file
         * @param line
         * @param construct
         * @return always return null, subclass can choose to impl. this method.
         */
        protected String combine(String name, String file, String line, String construct)
        {
            return null;
        }

        /**
         * switch to this sorter. If it was already this sorter, then toggle the sort order
         * 
         */
        public void sort()
        {
            if (this == viewer.getSorter()) {
                ascending = !ascending;
                viewer.setSorter(null); // turn off to force re-sort
            } else {
                ascending = true;
            }
            viewer.setSorter(this);

        }

    }

    /**
     * Sorter to sort by line number on which the MpiArtifact is Found
     * 
     * @author Beth Tibbitts created
     * 
     * 
     */
    class LineNoSorter extends GenericSorter
    {
        /**
         * sort MPI items by line number
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {

            int cat1 = category(e1);
            int cat2 = category(e2);

            if (cat1 != cat2) return cat1 - cat2;

            if (e1 instanceof IMarker) {
                try {
                    IMarker m1 = (IMarker) e1;
                    String temp;
                    temp = (String) m1.getAttribute(MpiIDs.LINE);
                    int line1 = Integer.parseInt(temp);
                    IMarker m2 = (IMarker) e2;
                    temp = (String) m2.getAttribute(MpiIDs.LINE);
                    int line2 = Integer.parseInt(temp);
                    int result = 0;
                    if (ascending)
                        result = line1 - line2;
                    else result = line2 - line1;

                    return result;
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
            return 0;

        }
    }

    /**
     * Sort MPI items by name
     * 
     * @author Beth Tibbitts
     * 
     * 
     */
    class NameArtifactSorter extends GenericSorter
    {

        /**
         * @param name
         * @param file
         * @param line
         * @param construct
         * @return BRT note: Sort isn't quite right: if name,filename identical, "10" would sort before "2" e.g.
         */
        protected String combine(String name, String file, String line, String construct)
        {
            final String delim = " - ";
            StringBuffer result = new StringBuffer(name);
            result.append(delim);
            result.append(file);
            result.append(delim);
            result.append(line);
            result.append(delim);
            result.append(construct);
            return result.toString();
        }
    }

    /**
     * Sort MPI items by filename (then line number)
     * 
     * @author Beth Tibbitts
     * 
     * 
     */
    class FilenameSorter extends GenericSorter
    {
        public int compare(Viewer viewer, Object e1, Object e2)
        {

            int cat1 = category(e1);
            int cat2 = category(e2);

            if (cat1 != cat2) return cat1 - cat2;
            int res = 0;
            try {
                IMarker m1 = (IMarker) e1;
                IMarker m2 = (IMarker) e2;
                String file1 = (String) m1.getAttribute(MpiIDs.FILENAME);
                String file2 = (String) m2.getAttribute(MpiIDs.FILENAME);
                System.out.println("ascending=" + ascending);
                if (ascending)
                    res = collator.compare(file1, file2);
                else res = collator.compare(file2, file1);
                // if the filename is the same, only then do we look at line
                // number
                if (res == 0) {
                    String line1 = m1.getAttribute(MpiIDs.LINE).toString();
                    String line2 = m2.getAttribute(MpiIDs.LINE).toString();
                    int l1 = Integer.parseInt(line1);
                    int l2 = Integer.parseInt(line2);
                    if (ascending)
                        res = l1 - l2;
                    else res = l2 - l1;
                }
                // if the filename and line no are the same, only then do we look at construct
                if (res == 0) {
                    if (ascending) {
                        res = collator.compare(getConstructStr(m1), getConstructStr(m2));
                    } else {
                        res = collator.compare(getConstructStr(m2), getConstructStr(m1));
                    }
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
            return res;
        }
    }

    class ConstructSorter extends GenericSorter
    {

        /**
         * @param name
         * @param file
         * @param line
         * @param construct
         * @return BRT note: Sort isn't quite right: if name,filename identical, "10" would sort before "2" e.g.
         */
        protected String combine(String name, String file, String line, String construct)
        {
            final String delim = " - ";
            StringBuffer result = new StringBuffer(construct);
            result.append(delim);
            result.append(name);
            result.append(delim);
            result.append(file);
            result.append(delim);
            result.append(line);
            return result.toString();
        }
    }

    /**
     * MPI Artifact Table View constructor
     */
    public MPITableView()
    {
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent)
    {
        // Widget created and customized and then passed to viewer during
        // creation :
        Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        String[] STD_HEADINGS = { " ", THINGNAME, "Filename", "LineNo", "Construct" };

        layout.addColumnData(new ColumnWeightData(1, 1, true));
        TableColumn tc0 = new TableColumn(table, SWT.NONE);
        tc0.setText(STD_HEADINGS[0]);
        tc0.setAlignment(SWT.LEFT);
        tc0.setResizable(true);

        layout.addColumnData(new ColumnWeightData(10, true));
        TableColumn tc1 = new TableColumn(table, SWT.NONE);
        tc1.setText(STD_HEADINGS[1]);
        tc1.setAlignment(SWT.LEFT);
        tc1.setResizable(true);

        layout.addColumnData(new ColumnWeightData(10, true));
        TableColumn tc2 = new TableColumn(table, SWT.NONE);
        tc2.setText(STD_HEADINGS[2]);
        tc2.setAlignment(SWT.LEFT);
        tc2.setResizable(true);

        layout.addColumnData(new ColumnWeightData(5, true));
        TableColumn tc3 = new TableColumn(table, SWT.NONE);
        tc3.setText(STD_HEADINGS[3]);
        tc3.setAlignment(SWT.LEFT);
        tc3.setResizable(true);

        layout.addColumnData(new ColumnWeightData(5, true));
        TableColumn tc4 = new TableColumn(table, SWT.NONE);
        tc4.setText(STD_HEADINGS[4]);
        tc4.setAlignment(SWT.LEFT);
        tc4.setResizable(true);

        // add listeners for table sorting
        // Sort by "icon" (the original sort order, actually)
        tc0.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event)
            {
                viewer.setSorter(null);
                viewer.setSorter(nameSorter);
            }

            public void widgetDefaultSelected(SelectionEvent event)
            {
            }
        });
        // Sort by artifact name
        tc1.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event)
            {
                nameArtifactSorter.sort();
            }

            public void widgetDefaultSelected(SelectionEvent event)
            {
            }
        });
        // Sort by file name (then by lineNo)
        tc2.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event)
            {
                filenameSorter.sort();
            }

            public void widgetDefaultSelected(SelectionEvent event)
            {
            }
        });
        // Sort by Line number
        tc3.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event)
            {
                lineNoSorter.sort();
            }

            public void widgetDefaultSelected(SelectionEvent event)
            {
            }
        });
        // Sort by Construct
        tc4.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event)
            {
                constructSorter.sort();
            }

            public void widgetDefaultSelected(SelectionEvent event)
            {
            }
        });

        // Selection listener to know when a table row is selected.

        table.addSelectionListener(new SelectionAdapter() {

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // System.out.println("widgetDefaultSelected");
            }

            public void widgetSelected(SelectionEvent e)
            {
                Object obj = e.getSource();
                if (obj instanceof Table) {
                    Table t = (Table) obj;
                    int row = t.getSelectionIndex();
                    // print marker info when selected in table
                    if (traceOn) {
                        TableItem ti = t.getItem(row);
                        IMarker marker = (IMarker) ti.getData();
                        Artifact mpiA = getMpiArtifact(marker);
                        String id = marker.getAttribute(MpiIDs.ID, "(error)");
                        int mLine = MarkerUtilities.getLineNumber(marker);
                        int mpiLine = 0;
                        if (mpiA != null) mpiLine = mpiA.getLine();
                        String pid = "";

                        if (traceOn)
                            System.out.println("MARKER id=" + id + " mkrLineNo=" + mLine + " theLineNo=" + mpiLine
                                    + pid);
                    }

                }
            }
        });

        viewer = new TableViewer(table);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        // Set up the sorters.
        nameSorter = new NameSorter();
        viewer.setSorter(nameSorter);
        lineNoSorter = new LineNoSorter();
        nameArtifactSorter = new NameArtifactSorter();
        filenameSorter = new FilenameSorter();
        constructSorter = new ConstructSorter();

        viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
        // markers from workspace

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                Object obj = sel.getFirstElement();
                if (obj instanceof IMarker) {
                    selectedMarker_ = (IMarker) obj;
                    // BRT set coordinating selection in the tree viewer to
                    // match.
                    // when tree viewer's object model is also set on markers,
                    // something like this should work:
                    /*
                     * TreeViewer tv = null;//MPITreeView.getViewer(); //Make a selection of just the first one
                     * StructuredSelection ss = new StructuredSelection(selectedMarker_); tv.setSelection(ss,true);
                     */
                }
                showStatusMessage("", "selectionChanged");
            }
        });

        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager)
            {
                MPITableView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager)
    {
        manager.add(mpiInfoAction);
        manager.add(new Separator());
        manager.add(filterAction);
    }

    private void fillContextMenu(IMenuManager manager)
    {
        manager.add(mpiInfoAction);
        // Other plug-ins can contribute their actions here
        manager.add(new Separator("Additions"));
    }

    private void fillLocalToolBar(IToolBarManager manager)
    {
        manager.add(mpiInfoAction);
    }

    /**
     * Make the action objects for the menus and toolbar.
     * 
     */
    private void makeActions()
    {
        makeShowInfoAction();
        makeFilterAction();
        makeDoubleClickAction();
    }

    /**
     * Make "show info" action to display artifact information
     */
    private void makeShowInfoAction()
    {
        mpiInfoAction = new Action() {
            public void run()
            {
                String title = THINGNAME + " information";
                if (selectedMarker_ != null) {
                    String idFromMarker = (String) selectedMarker_.getAttribute(MpiIDs.ID, null);
                    if (idFromMarker == null) {
                        System.out.println("MPITV: exception reading marker ID");
                        return;
                    }
                    StringBuffer info = new StringBuffer();

                    Artifact mpiA = MpiArtifactManager.getMpiArtifact(idFromMarker);
                    info.append("\nFile name: ").append(mpiA.getFileName());
                    info.append("\nLine number: ").append(mpiA.getLine());
                    info.append("\nName: ").append(mpiA.getShortName());
                    info.append("\nDescription: ").append(mpiA.getDescription());

                    MessageDialog.openInformation(null, title, info.toString());
                }// end if selectedMarker!=null
                else {
                    MessageDialog.openInformation(null, title, "No " + THINGNAME + " selected.");
                }
                // ------------------
            }
        };
        mpiInfoAction.setText("Show Info");
        mpiInfoAction.setToolTipText("Show detailed info for selected " + THINGNAME);
        mpiInfoAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_INFO_TSK));
    }

    /**
     * make filter action (TBD)
     */
    private void makeFilterAction()
    {
        filterAction = new Action() {
            public void run()
            {
                showMessage("Filter " + THINGNAMES + "\nDetermine which " + THINGNAMES + " are shown in this view.");
            }
        };
        filterAction.setText("Filter " + THINGNAMES);
        filterAction.setToolTipText("Filter which " + THINGNAMES + " are shown in this view");
    }

    /**
     * Make double-click action, which moves editor to the artifact instance in the source code (editor to line in
     * source code)
     * 
     */
    private void makeDoubleClickAction()
    {

        doubleClickAction = new Action() {
            public void run()
            {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                IMarker marker = (IMarker) obj;
                try {
                    // String locn = (String) marker.getAttribute(IMarker.LOCATION);
                    IFile f = (IFile) marker.getResource();

                    Artifact mpiA = getMpiArtifact(marker);
                    int mpiLineNo = 0;
                    if (mpiA != null) mpiLineNo = mpiA.getLine();
                    int lineNo = getMarkerLineNo(marker);
                    if (traceOn) System.out.println("MPIa lineNo= " + mpiLineNo + "    marker lineNo= " + lineNo);

                    if (f != null && f.exists()) {
                        IWorkbenchPage wbp = getSite().getPage();
                        // IEditorInput ieu = new FileEditorInput(f);
                        IEditorPart editor = IDE.openEditor(wbp, f);

                        if (traceOn)
                            System.out.println("dca: marker lineNo before " + MarkerUtilities.getLineNumber(marker));
                        // note: (re?) setting linenumber here is required to
                        // put marker in editor!?!
                        MarkerUtilities.setLineNumber(marker, lineNo);
                        if (traceOn)
                            System.out.println("dca: marker lineNo after " + MarkerUtilities.getLineNumber(marker));
                        IDE.gotoMarker(editor, marker);
                        if (traceOn) System.out.println("MPITV: DoubleClickAction, clear status");
                        showStatusMessage("", "double click action");
                    }
                } catch (Exception e) {
                    System.out
                            .println("MPITV.doubleclickAction: Error positioning editor page from marker line number");
                    showStatusMessage("Error positioning editor from marker line number", "error marker goto");
                    e.printStackTrace();
                }
            }
        };
    }

    private void hookDoubleClickAction()
    {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                doubleClickAction.run();
            }
        });
    }

    private void showMessage(String message)
    {
        MessageDialog.openInformation(viewer.getControl().getShell(), "MPI Artifact View", message);
    }

    private void showStatusMessage(String message, String debugMessage)
    {
        if (traceStatusLine) {
            message += " - ";
            message += debugMessage;
        }
        getViewSite().getActionBars().getStatusLineManager().setMessage(message);
        getViewSite().getActionBars().getStatusLineManager().update(true);

    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        showStatusMessage("", "setFocus"); // reset status message
        if (!viewer.getControl().isDisposed()) viewer.getControl().setFocus();
    }

    public void dispose()
    {
        if (traceOn) System.out.println("MPITableView.dispose()");
    }

    public void showMarker(IMarker marker)
    {
        System.out.println("Marker-------  IMarker.LINE_NUMBER=" + IMarker.LINE_NUMBER);
        try {
            Map attrs = marker.getAttributes();
            Iterator iter = attrs.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry e = (Map.Entry) iter.next();
                System.out.println("   " + e.getKey() + " " + e.getValue());
            }

        } catch (CoreException e) {
            e.printStackTrace();
        }

    }

    public ImageDescriptor makeImageDescriptor(String iconName)
    {
        URL url = MpiPlugin.getDefault().find(new Path(iconName));
        ImageDescriptor id = ImageDescriptor.createFromURL(url);
        return id;
    }

    /**
     * Push remediation info (artifact and marker) onto a stack so we can remember it, for possible undo action. Also
     * enables/disables the Undo action button.
     * 
     * @param mpiA
     * @param marker
     */
    protected void pushRemedInfo(Artifact mpiA, IMarker marker)
    {
        remediatedPIs_.push(mpiA);
        remediatedMarkers_.push(marker);
        checkUndoStatus();
    }

    /**
     * Set status of undo action (enabled or disabled) based on if there are any artifact remediations, or other
     * changes, available to undo
     * 
     */
    protected void checkUndoStatus()
    {

    }

    /**
     * Get marker line numbers. NOTE: IMarker.LINE_NUMBER and MpiIDs.LINE are presumed to be the same (that is, return
     * the same line number value for different marker key values)
     * 
     * @param marker
     * @return
     */
    protected int getMarkerLineNo(IMarker marker)
    {
        int lineNo = getIntAttr(marker, IMarker.LINE_NUMBER);
        return lineNo;
    }

    protected int getMarkerNewLineNo(IMarker marker)
    {
        int lineNo = getIntAttr(marker, MpiIDs.NEWLINE);
        return lineNo;
    }

    /**
     * Get an int value that is assumed to be stored in a marker
     * 
     * @param marker
     * @param attr the attribute name
     * 
     * @return the int value, or 0 if none found, or invalid value found
     */
    protected int getIntAttr(IMarker marker, String attr)
    {
        String temp = null;
        try {
            temp = marker.getAttribute(attr).toString();
        } catch (Exception e) { // CoreException or ClassCastException possible
            e.printStackTrace();
            System.out.println("MPITV: Marker lineNo(" + attr + ") invalid; using 0");
            return 0;
        }
        int lineNo = 0;
        try {
            lineNo = Integer.parseInt(temp);
        } catch (NumberFormatException nfe) {
            System.out.println("MPITV: Marker lineNo(" + temp + " from attr " + attr
                    + ") invalid (NumberFormatException); using 0");
        }
        return lineNo;
    }

    /**
     * convenience method for getting attribute String value.
     * 
     * @param marker
     * @param attr
     * @return
     */
    protected String getAttribute(IMarker marker, String attr)
    {
        String result = null;
        try {
            result = (String) marker.getAttribute(attr);
        } catch (Exception e) {
            System.out.println("** Exception getting marker attribute " + e);
            e.printStackTrace();
        }
        return result;

    }

    /**
     * a Stack that isn't based on Vector - Generic LIFO stack
     * 
     * @author Beth Tibbitts
     * 
     * 
     */
    public class StackList
    {
        private LinkedList list = new LinkedList();

        public void push(Object v)
        {
            list.addFirst(v);
        }

        public Object top()
        {
            return list.getFirst();
        }

        public Object pop()
        {
            return list.removeFirst();
        }

        public boolean isEmpty()
        {
            return list.isEmpty();
        }

    }

    /**
     * Visit the resource delta to look for the marker changes we are interested in
     * 
     * @author Beth Tibbitts
     */
    public class UpdateVisitor implements IResourceDeltaVisitor
    {

        /**
         * Visit appropriate parts of the resource delta to find the markers that changed that we care about.
         * 
         * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
         */
        public boolean visit(IResourceDelta delta) throws CoreException
        {
            IResource resource = delta.getResource();
            String name = resource.getName();
            if (resource.getType() == IResource.FILE) {
                if (delta.getKind() == IResourceDelta.CHANGED) {
                    if (traceOn) System.out.println("UpdateVisitor: file changed: " + name);

                    // Handle file changes (saves) by reporting the changes
                    // made to the file, to update backend analysis
                    // representation
                    IFile f = (IFile) resource;
                    int flags = delta.getFlags();
                    int contentChanged = flags & IResourceDelta.CONTENT;
                    if (AnalysisUtil.validForAnalysis(f.getName())) {
                        if (traceOn)
                            System.out.println("File " + f.getName()
                                    + " is valid for analysis so will process the change...");
                        if (contentChanged != 0) {
                            // do we need to tell back end (analysis engine) that file changed?
                        }

                        // refresh markers for that file?
                        IMarkerDelta[] mDeltas = delta.getMarkerDeltas();
                        int len = mDeltas.length;
                        for (int j = 0; j < len; j++) {
                            IMarkerDelta delta3 = mDeltas[j];
                            if (traceOn) showMarkerDeltaKind(delta3);
                            IMarker m = delta3.getMarker();
                            String ln = IMarker.LINE_NUMBER;
                            if (traceOn) System.out.println("---UpdateVisitor.visit():viewer update marker: (lineNo)");
                            String[] props = new String[1]; // awkward. why???
                            props[0] = ln;
                            // just update viewer item, not the whole view
                            // viewer.refresh();
                            viewer.update(m, props);
                        } // end loop
                    } else {
                        if (traceOn)
                            System.out.println("File " + f.getName()
                                    + " is NOT valid for analysis so will ignore change...");

                    }
                } // end if CHANGED
                else if (delta.getKind() == IResourceDelta.ADDED) {
                    System.out.println("Resource added.");
                    checkMarkerDeltas(delta);
                } else if (delta.getKind() == IResourceDelta.REPLACED) {
                    System.out.println("Resource replaced.");
                    checkMarkerDeltas(delta);
                } else if (delta.getKind() == IResourceDelta.REMOVED) {
                    System.out.println("Resource removed.");
                    checkMarkerDeltas(delta);
                }
            } // end if FILE
            return true; // keep going
        }

        private void checkMarkerDeltas(IResourceDelta delta)
        {
            IMarkerDelta[] md1 = delta.getMarkerDeltas();
            int len = md1.length;
            System.out.println("       ... found " + len + " markerDeltas.");
        }

        /**
         * Show info about the marker in the marker delta. This is just tracing the info available until we do something
         * with it. For now, we're just doing a (big) viewer.refresh() to refresh all the markers. When we get more
         * intelligent about just updating the ones that changed, we can remove that. Shouldn't make much different for
         * small sets of markers, but for many markers, this could be a significant performance improvement.
         * 
         * @param delta3
         */
        private void showMarkerDeltaKind(IMarkerDelta delta3)
        {
            IMarker m = delta3.getMarker();

            String kind = "UNKNOWN";
            switch (delta3.getKind()) {
                case IResourceDelta.ADDED:
                    kind = "ADDED";
                    break;
                case IResourceDelta.CHANGED:
                    kind = "CHANGED";
                    break;
                case IResourceDelta.REMOVED:
                    kind = "REMOVED";
                    break;
                default:
                    kind = "UNKNOWN";
                    break;
            }

            if (traceOn) System.out.println("    markerDeltaKind=" + kind);
            String mid = "", ml = "", mlpi = "";
            try {
                // note: we're getting marker deltas on ALL markers,
                // not just artifact markers, which can throw us off.
                // in particular, temp markers used by actions?

                mid = m.getAttribute(MpiIDs.ID).toString();
                ml = m.getAttribute(IMarker.LINE_NUMBER).toString();
                mlpi = m.getAttribute(MpiIDs.LINE).toString();
            } catch (Exception e1) {
                // ignore errors; only tracing for now.
                System.out.println("MPITV.UpdateVisitor error getting marker info ");
                e1.printStackTrace();
            }
            if (traceOn) System.out.println("    markerID=" + mid + "  lineNo(mkr-mpiA)=" + ml + "-" + mlpi);
        }

    } // end class UpdateVisitor

}