/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.pldt.common.ArtifactManager;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.IArtifact;
import org.eclipse.ptp.pldt.mpi.analysis.IDs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Like SimpleTableView - easy view that shows markers - but uses a TreeTable to
 * show some hierarchy. Currently limited to one level of hierarchy <br>
 * Markers are the leaf nodes.<br>
 * An attribute in the markers indicates the (artificial) parent node, for
 * grouping.
 * 
 * TODO: This is basically a copy of class from common plugin
 * (org.eclipse.ptp.pldt.common); need to work toward replacing this with that one -
 * however some changes have been made to this one
 * 
 * 
 * 
 */
public class SimpleTreeTableMarkerView extends ViewPart {
	protected TreeViewer viewer;

	/**
	 * marker attribute indicating parent node. All markers with this parentAttr
	 * will be the children of the same parent, and a node for that parent will
	 * be created so that the poor things won't be orphans.
	 */
	// SimpleTreeTableMarkerView.parentMarkerAttrib;
	private String parentMarkerAttrib = "parent";

	private Tree tree; // keep so we can dispose of listeners in dispose()?

	protected Action infoAction;

	private Action filterAction;

	private Action doubleClickAction;

	private static final boolean traceOn = false;

	private static final boolean traceStatusLine = false;

	protected ViewerSorter nameSorter;

	protected GenericSorter lineNoSorter;

	protected FilenameSorter filenameSorter;

	protected GenericSorter orderSorter; // by "icon"

	protected GenericSorter nameArtifactSorter;

	protected GenericSorter constructSorter;

	private IMarker selectedMarker_ = null;

	/**
	 * List of artifacts that were changed due to some action upon them in the
	 * view (currently unused)
	 */
	protected StackList changedArts_ = new StackList();

	/**
	 * List of markers that were involved in a change on the the associated
	 * artifact due to some action upon them here in the view (currently unused)
	 */
	protected StackList changedMarkers_ = new StackList();

	protected UpdateVisitor visitor_ = new UpdateVisitor();

	/**
	 * Be consistent about what we call these things; generic values (can be)
	 * replaced on ctor. <br>
	 * These are read from plugin.xml if not passed on ctor
	 */
	protected String thingname_ = "Artifact";

	protected String thingnames_ = "Artifacts";

	private String columnName_ = "Value";

	private AbstractUIPlugin thePlugin_;

	private String iconName_ = "icons/sample.gif";

	private String viewName_;

	private String markerID_;

	protected ArtifactManager artifactManager_;

	private String[] columnNames_;

	private String[] markerAttrNames_;

	/**
	 * The ID used in the marker for the unique ID for each artifact. Enables
	 * mapping back to the Artifact object if necessary.
	 */
	protected String uniqueID_ = "uniqueID";

	/**
	 * The ID used in the marker for the extra column of information (last
	 * column)
	 */
	protected String columnID_ = "constructType"; // id for (variable)

	/** Marker ID for artifact name - e.g. API name, pragma name, etc. */
	protected static final String NAME = "name";

	/** Marker ID for storage of the filename in which the artifact is found */
	protected static final String FILENAME = "filename";

	/**
	 * Marker id for storage of line number on which the artifact is found.
	 * Reuse of default ID used by IMarker, repeated here for ease of use and
	 * for clarity that THIS is the marker ID for line number.
	 */
	protected static final String LINE = IMarker.LINE_NUMBER;

	/** Marker id for storage of additional information about the artifact */
	protected static final String DESCRIPTION = "description";

	public static final int NONE = 0;

	public static final int FUNCTION_CALL = 1;

	public static final int CONSTANT = 2;

	/** types of constructs, for the default case */
	public static final String[] CONSTRUCT_TYPE_NAMES = { "None",
			"Function Call", "Constant" };

	/**
	 * Simple Artifact Table View constructor
	 * <p>
	 * Everything can be null, and defaults will be taken, or read from
	 * plugin.xml for the view.
	 * <p>
	 * Note: if a null plugIn instance is provided, the default plugin (this
	 * one) will not be able to find resources (e.g. icon images) if the derived
	 * class is in its own plug-in, and its icons are, too.
	 */
	public SimpleTreeTableMarkerView(AbstractUIPlugin thePlugin,
			String thingname, String thingnames, String columnName,
			String markerID) {

		if (thePlugin == null) {
			thePlugin_ = CommonPlugin.getDefault();
		} else {
			this.thePlugin_ = thePlugin;
		}
		if (thingname != null)
			this.thingname_ = thingname;
		if (thingnames != null)
			this.thingnames_ = thingnames;
		if (columnName != null) {
			this.columnName_ = columnName; // last column named by subclass
		}
		this.markerID_ = markerID;// if null, will use view id.

		findViewInfo();

	}

	/**
	 * Simple table view with an arbitrary number of extra columns
	 * 
	 * @param thePlugin
	 * @param thingname
	 * @param thingnames
	 * @param attrNames
	 *            list of marker attributes, for which the column values will be
	 *            extractd
	 * @param colNames
	 *            list of Column names, used as headers for the values found in
	 *            the marker attributes
	 * @param markerID_
	 */
	public SimpleTreeTableMarkerView(AbstractUIPlugin thePlugin,
			String thingname, String thingnames, String[] attrNames,
			String[] colNames, String markerID) {
		this(thePlugin, thingname, thingnames, null, markerID);
		columnNames_ = colNames;
		columnName_ = null;// set this so we can tell we are using array of
		// attrs/cols
		markerAttrNames_ = attrNames;

	}

	/**
	 * Ctor that uses defaults for everything (testing? theoretically, this
	 * should work, and should be reusable since info that must be unique is
	 * read from from plugin.xml.)
	 * 
	 */
	public SimpleTreeTableMarkerView() {
		this(null, null, null, null, null, null);
	}

	/**
	 * Find info from the view info in the manifest. This includes the icon
	 * name, the view id (used as marker id if none given on ctor), and
	 * constructs an artifact manager for this view's artifact objects
	 * 
	 */
	protected void findViewInfo() {
		String classname = this.getClass().getName();
		// try to find the icon specified in the plugin.xml for this
		// extension/view
		IExtension[] ext = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.ui.views").getExtensions();
		for (int i = 0; i < ext.length; i++) {
			IExtension extension = ext[i];
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (int j = 0; j < ces.length; j++) {
				IConfigurationElement cElement = ces[j];
				String iconName = cElement.getAttribute("icon");
				String classN = cElement.getAttribute("class");
				String name = cElement.getAttribute("name");
				if (classname.equals(classN)) {
					if (iconName != null) {
						iconName_ = iconName;
					}
					this.viewName_ = name;
					if (markerID_ == null) {
						// use plugin id for marker id, if not specified
						markerID_ = cElement.getAttribute("id");
					}
					artifactManager_ = ArtifactManager.getManager(markerID_);
					if (artifactManager_ == null) {
						artifactManager_ = new ArtifactManager(markerID_);
					}
				}

			}

		}
	}

	/**
	 * It might be useful for subclasses to override this, to say which
	 * filenames should allow the action "run analysis" to create new artifacts
	 * and thus new markers. <br>
	 * This is a default implementation
	 * 
	 * @param filename
	 * @return
	 */
	public boolean validForAnalysis(String filename) {
		// return MpiUtil.validForAnalysis(filename);
		return true;

	}

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider, IResourceChangeListener {
		private IResource input;
		private boolean hasRegistered = false;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// could use this to change the list to just artifacts from one
			// resource,
			// etc...
			// could cache viewer here this.viewer=v;
			if (traceOn)
				System.out.println("STTMV inputChanged()...");
			// if this is the first time we have been given an input
			if (!hasRegistered) {
				// add me as a resource change listener so i can refresh at
				// least when markers are changed
				// POST_CHANGE: only want event notifications for after-the-fact
				ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
						IResourceChangeEvent.POST_CHANGE);
				hasRegistered = true;
				if (traceOn)
					System.out
							.println("STTMV: Registered RCL for ViewContentProvider");
			}
			if (newInput instanceof IResource) {
				this.input = (IResource) newInput;
			}

		}

		public void dispose() {
			if (traceOn)
				System.out.println("STTMV.ViewContentProvider.dispose()");
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

		}

		/**
		 * Get the list of objects to populate this view.
		 */
		public Object[] getElements(Object parent) {
			ArrayList list = new ArrayList();
			Object[] objs = null;
			try {
				String id = markerID_;
				if (input == null) {
					if (traceOn)
						System.out.println("input is null in getElements...");
				}
				// use the cached input object instead of querying from
				// workspace
				// objs =
				// ResourcesPlugin.getWorkspace().getRoot().findMarkers(id,
				// false, IResource.DEPTH_INFINITE);
				objs = input.findMarkers(id, true, IResource.DEPTH_INFINITE);
				IMarker[] markers = (IMarker[]) objs;
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					int parentID = getParentID(marker);
					if (parentID == 0) {
						list.add(marker);
					}
				}
			} catch (CoreException e) {
				System.out
						.println("STTMV, exception getting model elements (markers for Table view)");
				e.printStackTrace();
			}
			if (traceOn)
				System.out.println("STTMV.get---Elements, found " + objs.length
						+ " markers/parentNodes");
			return list.toArray();

		}

		private int getParentID(IMarker marker) {
			int parentID = 0;
			try {
				parentID = ((Integer) marker.getAttribute(IDs.parentIDAttr))
						.intValue();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return parentID;
		}

		private int getMyID(IMarker marker) {
			int parentID = 0;
			try {
				parentID = ((Integer) marker.getAttribute(IDs.myIDAttr))
						.intValue();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return parentID;
		}

		private String getMyName(IMarker marker) {
			String name = (String) null;
			try {
				name = (String) marker.getAttribute(IDs.myNameAttr);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return name;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof IMarker) {
				IMarker parentMarker = (IMarker) parent;
				int parentID = getMyID(parentMarker);
				IMarker[] markers = null;
				try {
					markers = input.findMarkers(markerID_, false,
							IResource.DEPTH_INFINITE);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				List<IMarker> children = new ArrayList<IMarker>();
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					int id = getParentID(marker);
					if (id == parentID) {
						children.add(marker);
					}
				}
				return children.toArray();
			}
			// markers are leaf nodes, they don't have children
			// if not a ParentNode, then it should be a leaf node (marker)
			return new Object[] {};
		}

		public Object getParent(Object element) {
			if (element instanceof IMarker) {
				IMarker child = (IMarker) element;
				int parentID = getParentID(child);
				IMarker[] markers = null;
				try {
					markers = input.findMarkers(markerID_, false,
							IResource.DEPTH_INFINITE);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					int id = getMyID(marker);
					if (id == parentID) {
						return marker;
					}
				}
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			Object[] kids = getChildren(element);
			boolean hasKids = kids.length > 0;
			return hasKids;
		}

		/**
		 * react to a resource change event
		 * 
		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
		 */

		public void resourceChanged(IResourceChangeEvent event) {
			if (traceOn)
				System.out.println("-----------------resourceChanged()");
			final IResourceDelta delta = event.getDelta();
			if (traceOn)
				printResourcesChanged(delta, 1);
			// remove the following when resource delta visitor does it all?
			Control ctrl = viewer.getControl();
			if (ctrl != null && !ctrl.isDisposed()) {
				ctrl.getDisplay().syncExec(new Runnable() {
					public void run() {
						try {
							if (traceOn)
								System.out
										.println("viewer.update ea mkr in delta-- from resourceChanged()...");
							if (traceOn)
								System.out
										.println("----processResourceChangeDelta()...");
							processResourceChangeDelta(delta);
							if (traceOn)
								System.out
										.println("----END processResourceChangeDelta()...");
							if (traceOn)
								System.out.println("viewer.refresh()");
							// we should have updated the indiv. rows we care
							// about,
							// but need this for Marker display after initial
							// analysis,
							// and for markers deleted, etc. Can remove when we
							// more completely
							// handle things in processResourceChangeDelta
							// (removes etc.)
							viewer.refresh();

						} catch (Exception e) {
							System.out
									.println("STTMV: Exception refreshing viewer: "
											+ e);
							e.printStackTrace();
						}

					}
				});
			}
			if (traceOn)
				System.out.println("-----------------END resourceChanged()\n");

		}

		/**
		 * Debugging statement help - prints the events, indented by nesting
		 * level
		 * 
		 * @param delta
		 * @param indent
		 */
		private void printResourcesChanged(IResourceDelta delta, int indent) {
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
		private void printOneResourceChanged(IResourceDelta delta, int indent) {
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
		private void testDelta(IResourceDelta delta) {
			// -- code from eclipse help:
			// case IResourceDelta.CHANGED:
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
		protected void processResourceChangeDelta(IResourceDelta delta) {
			try {
				delta.accept(visitor_);

			} catch (CoreException e2) {
				System.out
						.println("Error in PITV.processResourceChangeDelta()..");
				e2.printStackTrace();
			}
		}
	} // end ViewContentProvider

	class ParentNode {

		private String name;
		private int ID;
		private IMarker marker_;

		public ParentNode(String name) {
			this.name = name;
			ID = 0;
			marker_ = null;
		}

		public ParentNode(int ID) {
			name = (String) null;
			this.ID = ID;
			marker_ = null;
		}

		public ParentNode(IMarker marker) {
			name = (String) null;
			ID = 0;
			marker_ = marker;
		}

		public String getName() {
			return name;
		}

		public int getID() {
			return ID;
		}

		public IMarker getMarker() {
			return marker_;
		}

		public void setMarker(IMarker marker) {
			marker_ = marker;
		}

		public void setName(String name) {
			this.name = name;
		}

		/**
		 * for dummy testing only
		 * 
		 * @return
		 */
		public Object[] getChildren() {
			return new String[] { "one", "two", "three" };
		}

		public String toString() {
			return "ParentNode: " + name;
		}

	}

	/**
	 * get artifact from marker
	 * 
	 * @param marker
	 * @return
	 */
	protected IArtifact getSimpleArtifact(IMarker marker) {
		String id = null;
		IArtifact artifact = null;
		try {
			id = (String) marker.getAttribute(uniqueID_);
			artifact = artifactManager_.getArtifact(id);

		} catch (CoreException e) {
			// e.printStackTrace();
			System.out.println(e.getMessage()
					+ " ... STV, CoreException getting artifact from hashMap; "
					+ thingname_ + " id=" + id);
		} catch (NullPointerException ne) {
			System.out.println(ne.getMessage()
					+ " ... STV, NullPtrExcp getting artifact from hashMap;"
					+ thingname_ + " id=" + id);
		}
		return artifact;

	}

	/**
	 * Get string representing the type of construct
	 * 
	 * @param marker
	 * @return
	 * @throws CoreException
	 */
	protected String getConstructStr(IMarker marker) throws CoreException {
		Integer temp = (Integer) marker.getAttribute(columnID_);
		if (temp != null) {
			Integer constructType = (Integer) temp;
			// return CONSTRUCT_TYPE_NAMES[constructType.intValue()];
			Integer index = new Integer(constructType.intValue() - 4);
			return index.toString();
		} else
			return " ";
	}

	/**
	 * 
	 * ViewLabelProvider - provides the text and images for the artifacts in the
	 * Table View
	 * 
	 * @author Beth Tibbitts
	 * 
	 * 
	 */
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		/**
		 * Keep icons already created, and reuse the images
		 */
		private HashMap iconHash = new HashMap();

		private IArtifact artifact;

		/**
		 * provide what goes in each column; get the info from the marker
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object o) {
			String temp = super.getText(o);
			// System.out.println("Text: " + temp);
			return temp;
		}

		/**
		 * Determine the text to go in each column. Note: the parent nodes will
		 * be ParentNode objects, and the child nodes will be IMarkers.
		 * 
		 * @param obj
		 *            the Marker (we hope) that goes on the current row
		 * @param index
		 *            the column number in the table
		 * 
		 */
		public String getColumnText(Object obj, int index) {
			if (obj == null) {
				System.out.println("STTMV: LabelProv obj is null; index="
						+ index);
				return "STTMV obj null";
			}
			if (obj instanceof IMarker) {
				IMarker marker = (IMarker) obj;
				try {
					String myName = (String) marker
							.getAttribute(IDs.myNameAttr);
					switch (index) {
					case 0:
						return myName;
					case 1:
						String id = (String) marker.getAttribute(NAME);
						return id;
					case 2:
						return (String) marker.getAttribute(FILENAME);
					case 3:
						String line = (marker.getAttribute(IMarker.LINE_NUMBER))
								.toString();
						if (traceOn)
							debugMarkerInfo(marker, line);
						return line;
					case 4:
						/*
						 * if (columnName_ != null) {// we're not using array
						 * return getConstructStr(marker); } // else drop
						 * through...
						 */
						Integer indexNum = (Integer) marker
								.getAttribute(IDs.myIndexAttr);
						return indexNum.toString();
					default:
						String attrName = markerAttrNames_[index - 4];
						String val = marker.getAttribute(attrName, "");
						return val;
					}
				} catch (CoreException ce) {
					return ("STTMV error in ViewContentProvider of SimpleTreeTableMarkerView");
				}
			}
			return "STTMV, vcp???";
		}

		/**
		 * Purely for debugging marker info in ViewLabelProvider.getColumnText()
		 * 
		 * @param marker
		 * @param line
		 */
		private void debugMarkerInfo(IMarker marker, String line) {
			artifact = getSimpleArtifact(marker);
			String compLine = line + "-";
			if (artifact == null) {
				if (traceOn)
					System.out.println("STTMV getColumnText- null artifact");
			} else {
				int lineArtifact = artifact.getLine();
				compLine = compLine + lineArtifact;
			}
			System.out
					.println("STTMV.ViewLabelProvider gets marker line: mkr-artifact: "
							+ compLine);
			showMarker(marker);
		}

		/**
		 * Provide the image that goes in a column, if any (Note that a table
		 * cell can contain both, an image and text, which will be displayed
		 * side-by-side)
		 * 
		 * @param obj -
		 *            the object we're getting the image for
		 * @param index -
		 *            the column that this image is to go in
		 */
		public Image getColumnImage(Object obj, int index) {
			// we only put image icon in the first column
			switch (index) {
			case 0:
				return getCustomImage(obj);
			default:
				return null;
			}
		}

		/**
		 * Get image for artifact. Note that different images could be used for
		 * different types of artifacts. For now we have a single image.
		 * 
		 * @param obj
		 *            the marker object that this artifact is represented by
		 * @return image for marker
		 *         <p>
		 *         Note: if a null plugIn instance is provided on the view ctor,
		 *         the default plugin (this one) will not be able to find
		 *         resources (e.g. icon images) if the derived class is in its
		 *         own plug-in, and its icons are, too.
		 * 
		 */
		protected Image getCustomImage(Object obj) {
			// if we've already created one of this type of icon, reuse it.
			// Note: use ImageRegistry instead?
			Image img = (Image) iconHash.get(iconName_);
			if (img == null) {
				Path path = new Path(iconName_);
				// BRT make sure the specific plugin is being used here to find
				// its OWN icons
				URL url = thePlugin_.find(path);
				ImageDescriptor id = ImageDescriptor.createFromURL(url);
				img = id.createImage();
				if (traceOn)
					System.out.println("STTMV: ***** created image for "
							+ iconName_);
				iconHash.put(iconName_, img);// save for reuse
			}
			return img;
		}

		/**
		 * Dispose of anything that would hang around rudely otherwise (such as
		 * image objects from the icons)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */

		public void dispose() {
			if (traceOn)
				System.out
						.println("STTMV.ViewLabelProvider.dispose(); dispose of icon images");
			for (Iterator iter = iconHash.values().iterator(); iter.hasNext();) {
				Image img = (Image) iter.next();
				img.dispose();
			}
			super.dispose();
		}

	}

	/**
	 * Default sorter for items - the order they were created, which tends to
	 * group items with their source code locations
	 * 
	 * @author Beth Tibbitts
	 */
	class NameSorter extends ViewerSorter {
	}

	/**
	 * Sort items by one or more of: artifact, filename, lineNo,
	 * variableColumnName.<br>
	 * The derived classes will implement combine() to say how the attributes
	 * are combined to get the sort desired.
	 * 
	 * @author Beth Tibbitts
	 */
	abstract class GenericSorter extends ViewerSorter {
		protected boolean ascending = true;

		/**
		 * Compare two items to determine sort order. Sort items by one or more
		 * of: artifact name, then file, then line number, then construct
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int result = 0;

			int cat1 = category(e1);
			int cat2 = category(e2);

			if (cat1 != cat2)
				return cat1 - cat2;

			java.text.Collator collator = this.getCollator();

			if (e1 instanceof IMarker) {
				try {
					IMarker m1 = (IMarker) e1;
					IMarker m2 = (IMarker) e2;
					String name1 = (String) m1.getAttribute(NAME);
					String file1 = (String) m1.getAttribute(FILENAME);

					String line1 = (String) m1.getAttribute(LINE).toString();
					String construct1 = getConstructStr(m1);
					String sort1 = combine(name1, file1, line1, construct1);

					String name2 = (String) m2.getAttribute(NAME);
					String file2 = (String) m2.getAttribute(FILENAME);
					String line2 = (String) m2.getAttribute(LINE).toString();
					String construct2 = getConstructStr(m2);
					String sort2 = combine(name2, file2, line2, construct2);

					if (ascending)
						result = collator.compare(sort1, sort2);
					else
						result = collator.compare(sort2, sort1);

					return result;
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return 0;

		}

		/**
		 * Combine name, file, and/or line number to provide the string to sort
		 * by. Will be overridden by derived classes as needed
		 * 
		 * @param name
		 * @param file
		 * @param line
		 * @param construct
		 * @return always return null, subclass can choose to impl. this method.
		 */
		protected String combine(String name, String file, String line,
				String construct) {
			return null;
		}

		/**
		 * switch to this sorter. If it was already this sorter, then toggle the
		 * sort order
		 * 
		 */
		public void sort() {
			// String className = this.getClass().getName();
			// System.out.println(className+".sort() ascending="+ascending);
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
	 * Sorter to sort by line number on which the SimpleArtifact is Found
	 * 
	 * @author Beth Tibbitts created
	 * 
	 * 
	 */
	class LineNoSorter extends GenericSorter {
		/**
		 * sort items by line number
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {

			int cat1 = category(e1);
			int cat2 = category(e2);

			if (cat1 != cat2)
				return cat1 - cat2;

			if (e1 instanceof IMarker) {
				try {
					IMarker m1 = (IMarker) e1;
					Object tempObj = m1.getAttribute(LINE);
					int line1 = 0;
					int line2 = 0;
					if (tempObj instanceof Integer) {
						line1 = ((Integer) tempObj).intValue();
						IMarker m2 = (IMarker) e2;
						tempObj = m2.getAttribute(LINE);
						// we assume if the first was Integer, this one is, too.
						assert tempObj instanceof Integer;
						line2 = ((Integer) tempObj).intValue();

					}
					int result = 0;
					if (ascending)
						result = line1 - line2;
					else
						result = line2 - line1;

					return result;
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return 0;

		}
	}

	/**
	 * Sort items by name
	 * 
	 * @author Beth Tibbitts
	 * 
	 * 
	 */
	class NameArtifactSorter extends GenericSorter {

		/**
		 * @param name
		 * @param file
		 * @param line
		 * @param construct
		 * @return BRT note: Sort isn't quite right: if name,filename identical,
		 *         "10" would sort before "2" e.g.
		 */
		protected String combine(String name, String file, String line,
				String construct) {
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
	 * Sort items by filename (then line number)
	 * 
	 * @author Beth Tibbitts
	 * 
	 * 
	 */
	class FilenameSorter extends GenericSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {

			int cat1 = category(e1);
			int cat2 = category(e2);

			if (cat1 != cat2)
				return cat1 - cat2;
			int res = 0;
			try {
				IMarker m1 = (IMarker) e1;
				IMarker m2 = (IMarker) e2;
				String file1 = (String) m1.getAttribute(FILENAME);
				String file2 = (String) m2.getAttribute(FILENAME);
				System.out.println("ascending=" + ascending);
				if (ascending)
					res = collator.compare(file1, file2);
				else
					res = collator.compare(file2, file1);
				// if the filename is the same, only then do we look at line
				// number
				if (res == 0) {
					String line1 = m1.getAttribute(LINE).toString();
					String line2 = m2.getAttribute(LINE).toString();
					int l1 = Integer.parseInt(line1);
					int l2 = Integer.parseInt(line2);
					if (ascending)
						res = l1 - l2;
					else
						res = l2 - l1;
				}
				// if the filename and line no are the same, only then do we
				// look at construct
				if (res == 0) {
					if (ascending) {
						res = collator.compare(getConstructStr(m1),
								getConstructStr(m2));
					} else {
						res = collator.compare(getConstructStr(m2),
								getConstructStr(m1));
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return res;
		}
	}

	class ConstructSorter extends GenericSorter {

		/**
		 * @param name
		 * @param file
		 * @param line
		 * @param construct
		 * @return BRT note: Sort isn't quite right: if name,filename identical,
		 *         "10" would sort before "2" e.g.
		 */
		protected String combine(String name, String file, String line,
				String construct) {
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
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		tree = new Tree(parent, SWT.BORDER);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);

		createTreeColumns();
		if (false) {
			SelectionListener treeSelectionListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					Object src = event.getSource();
					if (!traceOn)
						return;
					if (src instanceof TreeItem) {
						if (traceOn)
							System.out.println("TreeItem selected");
					} else if (src instanceof TreeColumn) {
						if (traceOn)
							System.out.println("TreeColumn selected");

					} else if (src instanceof Tree) {
						if (traceOn)
							System.out.println("Tree selected");
						Tree tree = (Tree) src;
					}
				}
			};
			tree.addSelectionListener(treeSelectionListener);
		}
		viewer = new TreeViewer(tree);
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
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event
						.getSelection();
				Object obj = sel.getFirstElement();
				if (obj instanceof IMarker) {
					selectedMarker_ = (IMarker) obj;
				}
				showStatusMessage("", "selectionChanged");
			}
		});

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void createTreeColumns() {
		TreeColumn column;
		column = new TreeColumn(tree, SWT.LEFT); // col 1 barrier name
		column = new TreeColumn(tree, SWT.LEFT); // col 2 fn name
		column = new TreeColumn(tree, SWT.LEFT); // col 3 filename
		column = new TreeColumn(tree, SWT.LEFT); // col 4 lineno
		column = new TreeColumn(tree, SWT.LEFT); // col 5 construct

		TreeColumn[] columns = tree.getColumns();
		int numColumns = columns.length;
		SelectionListener[] columnListeners = new SelectionListener[numColumns];
		String[] headings = new String[] { "Barrier Matching Set", "Function",
				"Filename", "LineNo", "IndexNo" };
		int[] widths = new int[] { 120, 100, 100, 100, 100 };

		for (int i = 0; i < numColumns; i++) {
			int colNo = i; // could be re-ordered later
			// int colNo = columnOrder[i];
			columns[colNo].setText(headings[colNo]);
			columns[colNo].setWidth(widths[colNo]);
			columns[colNo].setResizable(true);
			columns[colNo].setMoveable(true); // can reorder columns by
			// dragging
			SelectionListener columnListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					Object src = event.getSource();
					if (src instanceof TreeColumn) {
						TreeColumn tc = (TreeColumn) src;
						String text = tc.getText();
						if (true)
							System.out.println("tree column selected: " + text);
						Object data = tc.getData();
						if (data instanceof ViewerSorter) {
							GenericSorter sorter = (GenericSorter) data;
							sorter.sort();

						} else
							System.out.println("** not a sorter ** " + data);

					}
				}
			};

			columns[colNo].addSelectionListener(columnListener);
			columnListeners[colNo] = columnListener;
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SimpleTreeTableMarkerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(infoAction);
		manager.add(new Separator());
		manager.add(filterAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(infoAction);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator("Additions"));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(infoAction);
	}

	/**
	 * Make the action objects for the menus and toolbar.
	 * 
	 */
	private void makeActions() {
		makeShowInfoAction();
		makeFilterAction();
		makeDoubleClickAction();
	}

	/**
	 * Make "show info" action to display artifact information
	 */
	protected void makeShowInfoAction() {
		infoAction = new Action() {
			public void run() {
				String title = thingname_ + " information";
				if (selectedMarker_ != null) {
					String idFromMarker = (String) selectedMarker_
							.getAttribute(uniqueID_, null);
					if (idFromMarker == null) {
						System.out
								.println("STTMV: exception reading marker ID");
						return;
					}
					StringBuffer info = new StringBuffer();

					IArtifact artifact = artifactManager_
							.getArtifact(idFromMarker);
					info.append("\nFile name: ").append(artifact.getFileName());
					info.append("\nLine number: ").append(artifact.getLine());
					info.append("\nName: ").append(artifact.getShortName());
					info.append("\nDescription: ").append(
							artifact.getDescription());

					MessageDialog.openInformation(null, title, info.toString());
				}// end if selectedMarker!=null
				else {
					MessageDialog.openInformation(null, title, "No "
							+ thingname_ + " selected.");
				}
				// ------------------
			}
		};
		infoAction.setText("Show Info");
		infoAction.setToolTipText("Show detailed info for selected "
				+ thingname_);
		infoAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/**
	 * make filter action (TBD)
	 */
	private void makeFilterAction() {
		filterAction = new Action() {
			public void run() {
				showMessage("Filter " + thingnames_ + "\nDetermine which "
						+ thingnames_ + " are shown in this view.");
			}
		};
		filterAction.setText("Filter " + thingnames_);
		filterAction.setToolTipText("Filter which " + thingnames_
				+ " are shown in this view");
	}

	/**
	 * Make double-click action, which moves editor to the artifact instance in
	 * the source code (editor to line in source code)
	 * 
	 */
	private void makeDoubleClickAction() {

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				// This action only makes sense on child nodes (markers)
				if (!(obj instanceof IMarker)) {
					return;
				}
				IMarker marker = (IMarker) obj;
				try {
					IFile f = (IFile) marker.getResource();
					int lineNo = getMarkerLineNo(marker);
					if (f != null && f.exists()) {
						IWorkbenchPage wbp = getSite().getPage();
						// IEditorInput ieu = new FileEditorInput(f);
						IEditorPart editor = IDE.openEditor(wbp, f);

						if (traceOn)
							System.out.println("dca: marker lineNo before "
									+ MarkerUtilities.getLineNumber(marker));
						// note: (re?) setting linenumber here is required to
						// put marker in editor!?!
						MarkerUtilities.setLineNumber(marker, lineNo);
						if (traceOn)
							System.out.println("dca: marker lineNo after "
									+ MarkerUtilities.getLineNumber(marker));
						IDE.gotoMarker(editor, marker);
						if (traceOn)
							System.out
									.println("STTMV: DoubleClickAction, clear status");
						showStatusMessage("", "double click action");
					}
				} catch (Exception e) {
					System.out
							.println("STTMV.doubleclickAction: Error positioning editor page from marker line number");
					showStatusMessage(
							"Error positioning editor from marker line number",
							"error marker goto");
					e.printStackTrace();
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				viewName_, message);
	}

	private void showStatusMessage(String message, String debugMessage) {
		if (traceStatusLine) {
			message += " - ";
			message += debugMessage;
		}
		getViewSite().getActionBars().getStatusLineManager()
				.setMessage(message);
		getViewSite().getActionBars().getStatusLineManager().update(true);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		showStatusMessage("", "setFocus"); // reset status message
		if (!viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

	public void dispose() {
		if (traceOn)
			System.out.println("SimpleTableView.dispose()");
		// BRT do we need to dispose of imageDescriptors we made? or just
		// images?

	}

	public void showMarker(IMarker marker) {
		System.out.println("Marker-------  IMarker.LINE_NUMBER="
				+ IMarker.LINE_NUMBER);
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

	public ImageDescriptor makeImageDescriptor(String iconName) {
		URL url = thePlugin_.find(new Path(iconName));
		ImageDescriptor id = ImageDescriptor.createFromURL(url);
		return id;
	}

	/**
	 * Push change info (artifact and marker) onto a stack so we can remember
	 * it, for possible undo action. Also enables/disables the Undo action
	 * button.
	 * 
	 * @param artifact
	 * @param marker
	 */
	protected void pushChangedInfo(IArtifact artifact, IMarker marker) {
		changedArts_.push(artifact);
		changedMarkers_.push(marker);
		checkUndoStatus();
	}

	/**
	 * Set status of undo action (enabled or disabled) based on if there are any
	 * artifact changes, or other changes, available to undo
	 * 
	 */
	protected void checkUndoStatus() {

	}

	/**
	 * Get marker line numbers.
	 * 
	 * @param marker
	 * @return
	 */
	protected int getMarkerLineNo(IMarker marker) {
		int lineNo = getIntAttr(marker, IMarker.LINE_NUMBER);
		return lineNo;
	}

	/**
	 * Get an int value that is assumed to be stored in a marker in a given
	 * attribute.
	 * 
	 * @param marker
	 * @param attr
	 *            the attribute name
	 * 
	 * @return the int value, or 0 if none found, or invalid value found
	 */
	protected int getIntAttr(IMarker marker, String attr) {
		String temp = null;
		try {
			temp = marker.getAttribute(attr).toString();
		} catch (Exception e) { // CoreException or ClassCastException possible
			e.printStackTrace();
			System.out.println("STTMV: Marker lineNo(" + attr
					+ ") invalid; using 0");
			return 0;
		}
		int lineNo = 0;
		try {
			lineNo = Integer.parseInt(temp);
		} catch (NumberFormatException nfe) {
			System.out.println("STTMV: Marker lineNo(" + temp + " from attr "
					+ attr + ") invalid (NumberFormatException); using 0");
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
	protected String getAttribute(IMarker marker, String attr) {
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
	public class StackList {
		private LinkedList list = new LinkedList();

		public void push(Object v) {
			list.addFirst(v);
		}

		public Object top() {
			return list.getFirst();
		}

		public Object pop() {
			return list.removeFirst();
		}

		public boolean isEmpty() {
			return list.isEmpty();
		}

	}

	/**
	 * Visit the resource delta to look for the marker changes we are interested
	 * in
	 * 
	 * @author Beth Tibbitts
	 */
	public class UpdateVisitor implements IResourceDeltaVisitor {

		/**
		 * Visit appropriate parts of the resource delta to find the markers
		 * that changed that we care about.
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			String name = resource.getName();
			if (resource.getType() == IResource.FILE) {
				if (delta.getKind() == IResourceDelta.CHANGED) {
					if (traceOn)
						System.out.println("UpdateVisitor: file changed: "
								+ name);

					// Handle file changes (saves) by reporting the changes
					// made to the file, to update backend analysis
					// representation
					IFile f = (IFile) resource;
					int flags = delta.getFlags();
					int contentChanged = flags & IResourceDelta.CONTENT;

					if (validForAnalysis(f.getName())) {
						if (traceOn)
							System.out
									.println("File "
											+ f.getName()
											+ " is valid for analysis so will process the change...");
						if (contentChanged != 0) {
							// do we need to tell back end (analysis engine)
							// that file changed?
						}

						// refresh markers for that file?
						IMarkerDelta[] mDeltas = delta.getMarkerDeltas();
						int len = mDeltas.length;
						for (int j = 0; j < len; j++) {
							IMarkerDelta delta3 = mDeltas[j];
							if (traceOn)
								showMarkerDeltaKind(delta3);
							IMarker m = delta3.getMarker();
							String ln = IMarker.LINE_NUMBER;
							if (traceOn)
								System.out
										.println("---UpdateVisitor.visit():viewer update marker: (lineNo)");
							// showMarker(m);
							String[] props = new String[1]; // awkward. why???
							props[0] = ln;
							// just update viewer item, not the whole view
							// viewer.refresh();
							viewer.update(m, props);
						} // end loop
					} else {
						if (traceOn)
							System.out
									.println("File "
											+ f.getName()
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

		private void checkMarkerDeltas(IResourceDelta delta) {
			IMarkerDelta[] md1 = delta.getMarkerDeltas();
			int len = md1.length;
			System.out.println("       ... found " + len + " markerDeltas.");
		}

		/**
		 * Show info about the marker in the marker delta. This is just tracing
		 * the info available until we do something with it. For now, we're just
		 * doing a (big) viewer.refresh() to refresh all the markers. When we
		 * get more intelligent about just updating the ones that changed, we
		 * can remove that. Shouldn't make much different for small sets of
		 * markers, but for many markers, this could be a significant
		 * performance improvement.
		 * 
		 * @param delta3
		 */
		private void showMarkerDeltaKind(IMarkerDelta delta3) {

			// int mdKind = delta3.getKind();
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

			if (traceOn)
				System.out.println("    markerDeltaKind=" + kind);
			String mid = "", ml = "", mlpi = "";
			try {
				// note: we're getting marker deltas on ALL markers,
				// not just artifact markers, which can throw us off.
				// in particular, temp markers used by actions?

				mid = m.getAttribute(uniqueID_).toString();
				ml = m.getAttribute(IMarker.LINE_NUMBER).toString();
				// mlpi = m.getAttribute(IDs.LINE).toString();
			} catch (Exception e1) {
				// ignore errors; only tracing for now.
				System.out
						.println("STTMV.UpdateVisitor error getting marker info for change type: "
								+ kind);
				// e1.printStackTrace();
			}
			if (traceOn)
				System.out.println("    markerID_=" + mid
						+ "  lineNo(mkr-mpiA)=" + ml + "-" + mlpi);
		}

	} // end class UpdateVisitor

}