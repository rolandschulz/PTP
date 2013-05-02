/**********************************************************************
 * Copyright (c) 2009,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.etfw.feedback.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackAction;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;
import org.eclipse.ptp.internal.etfw.feedback.Activator;
import org.eclipse.ptp.internal.etfw.feedback.FeedbackIDs;
import org.eclipse.ptp.internal.etfw.feedback.messages.Messages;
import org.eclipse.ptp.internal.etfw.feedback.views.FeedbackActionCreator;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.osgi.framework.Bundle;

/**
 * Like SimpleTableMarkerView - easy view that shows markers - but uses a
 * TreeTable to show some hierarchy.
 * 
 * Intent: Markers are the leaf nodes.<br>
 * An attribute in the markers indicates the (artificial) parent node, for
 * grouping. Some actual parent markers are used in some cases.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is no guarantee that
 * this API will work or that it will remain the same. We do not recommending using this API without consulting with the
 * etfw.feedback team.
 * 
 * 
 */
public class SimpleTreeTableMarkerView extends ViewPart {
	protected TreeViewer viewer;

	/**
	 * marker attribute indicating parent node. All markers with this parentAttr
	 * will be the children of the same parent, and a node for that parent will
	 * be created (optionally) so that the poor things won't be orphans. Value
	 * can be changed in some constructors.
	 */
	protected String parentMarkerAttrib = "parent"; //$NON-NLS-1$

	private Tree tree; // keep so we can dispose of listeners in dispose()?

	protected Action infoAction;
	/**
	 * @since 2.0
	 */
	protected Action extensionAction; // possible addition by an plug-in
										// extension
	/**
	 * @since 2.0
	 */
	protected AbstractFeedbackAction feedbackAction;

	protected Action filterAction;

	protected Action doubleClickAction;
	protected Action removeMarkerAction;

	protected Action expandAllAction;
	protected Action collapseAllAction;
	/**
	 * Cache whether the user has done expand/contract all, so we can maintain
	 * the state if possible
	 */
	protected int expandCollapseStatus;
	protected static final int EXPAND_COLLAPSE_NONE = 0;
	protected static final int EXPAND_COLLAPSE_EXPANDALL = 1;
	protected static final int EXPAND_COLLAPSE_COLLAPSEALL = 2;

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
	protected String thingname_ = Messages.SimpleTreeTableMarkerView_artifact;

	protected String thingnames_ = Messages.SimpleTreeTableMarkerView_artifacts;

	private String columnName_ = Messages.SimpleTreeTableMarkerView_value;

	private AbstractUIPlugin thePlugin_;

	private String iconName_ = "icons/feedback.png"; //$NON-NLS-1$

	private String viewName_;
	private String viewID_;

	private String markerID_;

	// protected ArtifactManager artifactManager_;

	/** for arbitrary number of columns, the column titles for the view */
	private String[] columnNames_;
	/**
	 * for arbitrary number of columns, the default column widths, specified on
	 * ctor
	 */
	private int[] widths_ = null;
	/** default view column width if nothing is specified. */
	private static final int DEFAULT_WIDTH = 100;
	/**
	 * for arbitrary number of columns, the attribute names in the markers that
	 * correspond to the columns.
	 */
	private String[] markerAttrNames_;

	/**
	 * The ID used in the marker for the unique ID for each artifact. Enables
	 * mapping back to the Artifact object if necessary.
	 */
	protected String uniqueID_ = "uniqueID"; //$NON-NLS-1$

	/**
	 * The ID used in the marker for the extra column of information (last
	 * column)
	 */
	protected String columnID_ = "constructType"; // id for (variable) //$NON-NLS-1$

	/**
	 * whether or not to create parent nodes if they don't exist. If true, for
	 * each unique "parent=" attribute in the XML items, a parent node of that
	 * name will be created, and the children will be grouped under it.
	 * org.eclipse.ptp.etfw.sample project uses this. If false, then the
	 * IFeedbackItems should themselves indicate their parents/children.
	 * 
	 */
	private boolean createParentsIfNeeded = false;

	/** Marker ID for artifact name - e.g. API name, pragma name, etc. */
	protected static final String NAME = "name"; //$NON-NLS-1$

	/** Marker ID for storage of the filename in which the artifact is found */
	protected static final String FILENAME = "filename"; //$NON-NLS-1$

	/**
	 * Marker id for storage of line number on which the artifact is found.
	 * Reuse of default ID used by IMarker, repeated here for ease of use and
	 * for clarity that THIS is the marker ID for line number.
	 */
	protected static final String LINE = IMarker.LINE_NUMBER;

	/** Marker id for storage of additional information about the artifact */
	protected static final String DESCRIPTION = "description"; //$NON-NLS-1$

	public static final int NONE = 0;

	public static final int FUNCTION_CALL = 1;

	public static final int CONSTANT = 2;

	/** types of constructs, for the default case */
	public static final String[] CONSTRUCT_TYPE_NAMES = { Messages.SimpleTreeTableMarkerView_none,
			Messages.SimpleTreeTableMarkerView_functionCall, Messages.SimpleTreeTableMarkerView_constant };

	/**
	 * Simple Artifact Table View constructor
	 * <p>
	 * Everything can be null, and defaults will be taken, or read from plugin.xml for the view.
	 * <p>
	 * Note: if a null plugIn instance is provided, the default plugin (this one) will not be able to find resources (e.g. icon
	 * images) if the derived class is in its own plug-in, and its icons are, too. BRT 11/2/09: this ctor is called
	 */
	public SimpleTreeTableMarkerView(AbstractUIPlugin thePlugin, String thingname, String thingnames, String columnName,
			String markerID, String parentMarkerAttrName) {

		if (thePlugin == null) {
			thePlugin_ = Activator.getDefault();
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
		this.parentMarkerAttrib = parentMarkerAttrName;

		findViewInfo();

	}

	// FIXME should probably have the other ctor call this one with default
	// value of createParentsIfNeeded
	public SimpleTreeTableMarkerView(AbstractUIPlugin thePlugin, String thingname, String thingnames, String columnName,
			String markerID, String parentMarkerAttrName, boolean createParentsIfNeeded) {
		this(thePlugin, thingname, thingnames, columnName, markerID, parentMarkerAttrName);
		this.createParentsIfNeeded = createParentsIfNeeded;

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
	public SimpleTreeTableMarkerView(AbstractUIPlugin thePlugin, String thingname, String thingnames, String[] attrNames,
			String[] colNames, String markerID, String parentMarkerAttribName) {
		this(thePlugin, thingname, thingnames, attrNames, colNames, null, markerID, parentMarkerAttribName, false);
	}

	/**
	 * Note: this ctor is the only one that is being actively used/tested.
	 * 
	 * @param thePlugin
	 * @param thingname
	 * @param thingnames
	 * @param attrNames
	 * @param colNames
	 * @param widths
	 * @param markerID
	 * @param parentMarkerAttribName
	 * @param createParentsIfNeeded
	 *            * this ctor is called(2)
	 */
	public SimpleTreeTableMarkerView(AbstractUIPlugin thePlugin, String thingname, String thingnames, String[] attrNames,
			String[] colNames, int[] widths, String markerID, String parentMarkerAttribName, boolean createParentsIfNeeded) {
		this(thePlugin, thingname, thingnames, null, markerID, parentMarkerAttribName);
		columnNames_ = colNames;
		columnName_ = null;// set this so we can tell we are using array of
							// attrs/cols
		markerAttrNames_ = attrNames;
		widths_ = widths;

		this.createParentsIfNeeded = createParentsIfNeeded;
		int len1 = attrNames.length;
		int len2 = colNames.length;
		int len3 = widths.length;
		if ((len1 != len2) || (len2 != len3)) {
			System.out
					.println("WARNING: SimpleTreeTableMarkerView expects attrNames, colNames, and widths to all be the same length."); //$NON-NLS-1$
		}
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
	 * name and the view id (used as marker id if none given on ctor)
	 * 
	 */
	protected void findViewInfo() {
		String classname = this.getClass().getName();
		// try to find the icon specified in the plugin.xml for this
		// extension/view
		IExtension[] ext = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.views").getExtensions(); //$NON-NLS-1$
		for (int i = 0; i < ext.length; i++) {
			IExtension extension = ext[i];
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (int j = 0; j < ces.length; j++) {
				IConfigurationElement cElement = ces[j];
				String iconName = cElement.getAttribute("icon"); //$NON-NLS-1$
				String classN = cElement.getAttribute("class"); //$NON-NLS-1$
				String name = cElement.getAttribute("name"); //$NON-NLS-1$
				if (classname.equals(classN)) {
					if (iconName != null) {
						iconName_ = iconName;
					}
					this.viewName_ = name;
					this.viewID_ = cElement.getAttribute("id"); //$NON-NLS-1$
					if (markerID_ == null) {
						// use plugin id for marker id, if not specified
						markerID_ = this.viewID_;
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
	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider, IResourceChangeListener {
		private IResource input;

		private List<ParentNode> parentList = new ArrayList<ParentNode>();

		private boolean hasRegistered = false;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// could use this to change the list to just artifacts from one
			// resource,
			// etc...
			// could cache viewer here this.viewer=v;
			if (traceOn)
				System.out.println("STTMV inputChanged()..."); //$NON-NLS-1$
			// if this is the first time we have been given an input
			if (!hasRegistered) {
				// add me as a resource change listener so i can refresh at
				// least when markers are changed
				// POST_CHANGE: only want event notifications for after-the-fact
				ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
				hasRegistered = true;
				if (traceOn)
					System.out.println("STTMV: Registered RCL for ViewContentProvider"); //$NON-NLS-1$
			}
			if (newInput instanceof IResource) {
				this.input = (IResource) newInput;
			}

		}

		public void dispose() {
			if (traceOn)
				System.out.println("STTMV.ViewContentProvider.dispose()"); //$NON-NLS-1$
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

		}

		/**
		 * Get the list of objects to populate this view.
		 */
		public Object[] getElements(Object parent) {
			IMarker[] markers = null;
			ArrayList rootNodeList = new ArrayList();
			try {
				String id = markerID_;
				if (input == null) {
					if (traceOn)
						System.out.println("input is null in getElements..."); //$NON-NLS-1$
				}
				markers = input.findMarkers(id, false, IResource.DEPTH_INFINITE);
				if (createParentsIfNeeded) {
					parentList = createParents(markers);
				}

				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					String itemID = (String) marker.getAttribute(FeedbackIDs.FEEDBACK_ATTR_ID);
					// FIXME parent marker attr is used here and value on ctor
					// is ignored?
					String parentID = (String) marker.getAttribute(FeedbackIDs.FEEDBACK_ATTR_PARENT);
					// If this node doesn't have a parent id, then it IS a
					// parent (root node)
					if (parentID == null || parentID.length() == 0) {
						rootNodeList.add(marker);
					}
				}
			} catch (CoreException e) {
				System.out.println("STTMV, exception getting model elements (markers for Table view)"); //$NON-NLS-1$
				e.printStackTrace();
			}
			if (traceOn)
				System.out.println("STTMV.get---Elements, found " + markers.length + " markers"); //$NON-NLS-1$ //$NON-NLS-2$
			// the "parents" are the root nodes of the view. they may have
			// children.
			if (createParentsIfNeeded) {
				int len = parentList.size();
				return parentList.toArray();
			} else {
				return rootNodeList.toArray();
			}

		}

		/**
		 * create the parent objects needed for these markers
		 * 
		 * @param objs
		 * @return
		 */
		private List<ParentNode> createParents(Object[] objs) {
			// remove (old) parent objects
			ArrayList list = new ArrayList();

			IMarker[] markers = (IMarker[]) objs;
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];

				// if this IS a parent, put *it* in the parent list
				String parentName = getParentAttr(marker);
				// make one single parent, if attrs don't have parent info (yet)

				if (parentName == null) {
					// then this is a parent!
					list.add(marker); // ???
				} else {
					ParentNode pn = getParentNode(parentName);
					// System.out.println("parent");
					// list.add(pn);

				}
			}
			if (createParentsIfNeeded) {
				int len = parentList.size();
				return parentList;
			} else {
				return list;// parentList;
			}
		}

		private String getParentAttr(IMarker marker) {
			return getStrAttr(marker, parentMarkerAttrib);
		}

		private String getStrAttr(IMarker marker, String attrName) {
			String attrValue = null;
			try {
				attrValue = (String) marker.getAttribute(attrName);
			} catch (CoreException e) {
				System.out.println("Exception getting  attr from marker " + marker + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return attrValue;

		}

		/**
		 * get the parent node with the given name. If one doesn't exist, can
		 * create it.
		 * 
		 * @param parentName
		 *            the marker attribute with the name of the parent required
		 * @param createIfNeeded
		 *            whether or not to create the parent node if we don't find
		 *            one already exists
		 * @return
		 */
		private ParentNode getParentNode(String parentName, boolean createIfNeeded) {
			ParentNode parentNode;
			for (Iterator iter = parentList.iterator(); iter.hasNext();) {
				parentNode = (ParentNode) iter.next();
				if (parentNode.parentAttrName.equals(parentName)) {
					return parentNode;
				}
			}
			// not found; make a new one
			parentNode = null;
			if (createIfNeeded) {
				parentNode = new ParentNode(parentName);
				parentList.add(parentNode);
			}
			return parentNode;
		}

		/**
		 * Get the parent node for a given parentName. Optionally this will
		 * create if it it doesn't exist, based on createParentsIfNeeded boolean
		 * setting
		 * 
		 * @param parentName
		 * @return
		 */
		protected ParentNode getParentNode(String parentName) {
			return getParentNode(parentName, createParentsIfNeeded);
		}

		/**
		 * react to a resource change event
		 * 
		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
		 */

		public void resourceChanged(IResourceChangeEvent event) {
			if (traceOn)
				System.out.println("-----------------resourceChanged()"); //$NON-NLS-1$
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
								System.out.println("viewer.update ea mkr in delta-- from resourceChanged()..."); //$NON-NLS-1$
							if (traceOn)
								System.out.println("----processResourceChangeDelta()..."); //$NON-NLS-1$
							processResourceChangeDelta(delta);
							if (traceOn)
								System.out.println("----END processResourceChangeDelta()..."); //$NON-NLS-1$
							if (traceOn)
								System.out.println("viewer.refresh()"); //$NON-NLS-1$
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
							System.out.println("STTMV: Exception refreshing viewer: " + e); //$NON-NLS-1$
							e.printStackTrace();
						}

					}
				});
			}
			if (traceOn)
				System.out.println("-----------------END resourceChanged()\n"); //$NON-NLS-1$

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
				buf.append("  "); //$NON-NLS-1$
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				buf.append("ADDED"); //$NON-NLS-1$
				break;
			case IResourceDelta.REMOVED:
				buf.append("REMOVED"); //$NON-NLS-1$
				break;
			case IResourceDelta.CHANGED:
				buf.append("CHANGED"); //$NON-NLS-1$
				testDelta(delta);
				break;
			default:
				buf.append("["); //$NON-NLS-1$
				buf.append(delta.getKind());
				buf.append("]"); //$NON-NLS-1$
				break;
			}
			buf.append(" "); //$NON-NLS-1$
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
			System.out.print("Resource "); //$NON-NLS-1$
			System.out.print(delta.getFullPath());
			System.out.println(" has changed."); //$NON-NLS-1$
			int flags = delta.getFlags();
			if ((flags & IResourceDelta.CONTENT) != 0) {
				System.out.println("--> Content Change"); //$NON-NLS-1$
			}
			if ((flags & IResourceDelta.REPLACED) != 0) {
				System.out.println("--> Content Replaced"); //$NON-NLS-1$
			}
			if ((flags & IResourceDelta.MARKERS) != 0) {
				System.out.println("--> Marker Change"); //$NON-NLS-1$
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
				System.out.println("Error in PITV.processResourceChangeDelta().."); //$NON-NLS-1$
				e2.printStackTrace();
			}
		}

		/**
		 * get the children (markers) of a parent node
		 */
		public Object[] getChildren(Object parentElement) {
			String parentName = null;
			if (parentElement instanceof IMarker) {

				IMarker parentMarker = (IMarker) parentElement;
				// String
				// parentName=parentMarker.getAttribute(parentMarkerAttrib);

				try {
					parentName = (String) parentMarker.getAttribute(FeedbackIDs.FEEDBACK_ATTR_ID);
				} catch (CoreException e1) {
					System.out.println("unable to get id attr of marker in order to find children with that parent id."); //$NON-NLS-1$
					e1.printStackTrace();
					return null;
				}
			} else if (parentElement instanceof ParentNode) {
				ParentNode parentNode = (ParentNode) parentElement;// messy
				parentName = parentNode.getParentAttrName();
				// System.out.println("STTMV: parentNode parentName: "+parentName);

			}

			IMarker[] markers = null;
			try {
				markers = input.findMarkers(markerID_, false, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				System.out.println("Exception getting children of " + parentElement + " " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			List<IMarker> children = new ArrayList<IMarker>();
			// fixme inefficient
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];
				String parentAttr = getParentAttr(marker);
				String itemID = getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_ID);

				if (parentAttr != null && parentAttr.equals(parentName)) {
					children.add(marker);
				}
			}
			return children.toArray();
		}

		/**
		 * get the parent. If element is a marker, return the parent node, since
		 * leaf nodes are markers. if it's a ParentNode, return null - it's a
		 * top level node. ?? is there an invisible root node?
		 */
		public Object getParent(Object element) {
			if (element instanceof IMarker) {
				IMarker marker = (IMarker) element;
				String parentName = getParentAttr(marker);
				ParentNode parent = getParentNode(parentName, false);
				return parent;
			} else {
				return null; // only markers have parents, they are the leaf
								// nodes.
			}
		}

		private ParentNode getParent(IMarker marker, boolean b) {
			// TODO do we need to handle boolean? 'createifneeded' ??
			return (ParentNode) getParent(marker);
		}

		public boolean hasChildren(Object element) {
			Object[] kids = getChildren(element);
			if (kids != null) {
				return kids.length > 0;
			} else {
				return false;
			}
		}

	} // end ViewContentProvider

	/**
	 * (Fake?) parentNode
	 * 
	 * @author beth
	 * 
	 */
	public class ParentNode {
		private final String parentAttrName;

		public ParentNode(String parentName) {
			this.parentAttrName = parentName;
		}

		public String getParentAttrName() {
			return parentAttrName;
		}

		@Override
		public String toString() {
			return "ParentNode  parentName=" + parentAttrName; //$NON-NLS-1$
		}

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
			Integer constructType = temp;
			return CONSTRUCT_TYPE_NAMES[constructType.intValue()];
		} else
			return " "; //$NON-NLS-1$
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
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		 * Keep icons already created, and reuse the images
		 */
		private final HashMap iconHash = new HashMap();

		/**
		 * provide what goes in each column; get the info from the marker
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object o) {
			String temp = super.getText(o);
			// System.out.println("Text: " + temp);
			return temp;
		}

		/**
		 * Determine the text to go in each column
		 * 
		 * @param obj
		 *            the Marker (we hope) that goes on the current row
		 * @param index
		 *            the column number in the table
		 * 
		 */
		public String getColumnText(Object obj, int index) {
			final boolean traceText = false;
			if (obj == null) {
				System.out.println("STTMV: LabelProv obj is null; index=" + index); //$NON-NLS-1$
				return "STTMV obj null"; //$NON-NLS-1$
			}
			if (!(obj instanceof IMarker)) {
				// something other than a marker was encountered. Perhaps a
				// parent node not represented as a Marker? Return blank
				// eventually, the parent nodes don't need to be fancy.
				// But we do probably at least need the parent name
				if (obj instanceof ParentNode) {
					ParentNode parentNode = (ParentNode) obj;
					if (index == 0) {
						String name = parentNode.getParentAttrName();
						return name;
					} else
						return "";// "" vs null??? //$NON-NLS-1$

				}
				return "!marker";// HACK //$NON-NLS-1$
			}
			IMarker marker = (IMarker) obj;

			if (!marker.exists()) {
				// probably marker is being deleted. Ignore, and need
				// to investigate why we aren't ignoring *that* resource change.
				return "";
			}
			IFeedbackItem item = getFeedbackItem(marker);

			try {
				// Get column text value from the feedback item now, via attr lookup
				// (not from the marker attr)
				String attrname = markerAttrNames_[index];

				String itemVal = item.getAttr(attrname);
				if (traceText) {
					if (index == 0)
						System.out.println(" ");

					if (traceText)
						System.out.println("col " + index + " attrname=" + attrname + " IFeedbackItem val=" + itemVal);
				}
				if (itemVal != null) {
					if (attrname.equals("filename")) {
						// temporary until we find a better way.
						// Preference value to show full path or not???
						itemVal = lastSegmentOnly(itemVal);
					}
					return itemVal;
				}
				String value = itemVal;
				if ((itemVal == null) || (itemVal.length() == 0)) {
					itemVal = "[" + attrname + "]";
				}
				return value;

			} catch (Exception ce) {
				return ("STTMV error"); //$NON-NLS-1$
			}

		}

		/**
		 */
		String lastSegmentOnly(String filename) {
			if (filename.contains(Path.SEPARATOR + "")) { //$NON-NLS-1$
				IPath path = new Path(filename);
				// String pathname = path.removeLastSegments(1).toString();
				filename = path.segment(path.segmentCount() - 1);
			}
			return filename;
		}

		/**
		 * Get the feedback item from the marker object
		 * 
		 * @param marker
		 */
		private IFeedbackItem getFeedbackItem(IMarker marker) {
			IFeedbackItem item = null;
			Object obji = null;
			try {
				obji = marker.getAttribute(FeedbackIDs.FEEDBACK_ATTR_ITEM);
				if (obji instanceof IFeedbackItem) {
					item = (IFeedbackItem) obji;
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				boolean exists = marker.exists();
				e.printStackTrace();// happens when deleting markers. ??
			}
			return item;
		}

		/**
		 * Provide the image that goes in a column, if any (Note that a table
		 * cell can contain both, an image and text, which will be displayed
		 * side-by-side)
		 * 
		 * @param obj
		 *            - the object we're getting the image for
		 * @param index
		 *            - the column that this image is to go in
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
		 *         Note: if a null plugIn instance is provided on the view ctor, the default plugin (this one) will not be able to
		 *         find resources (e.g. icon images) if the derived class is in its own plug-in, and its icons are, too.
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
					System.out.println("STTMV: ***** created image for " + iconName_); //$NON-NLS-1$
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

		@Override
		public void dispose() {
			if (traceOn)
				System.out.println("STTMV.ViewLabelProvider.dispose(); dispose of icon images"); //$NON-NLS-1$
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
		@Override
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

					String line1 = m1.getAttribute(LINE).toString();
					String construct1 = getConstructStr(m1);
					String sort1 = combine(name1, file1, line1, construct1);

					String name2 = (String) m2.getAttribute(NAME);
					String file2 = (String) m2.getAttribute(FILENAME);
					String line2 = m2.getAttribute(LINE).toString();
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
		protected String combine(String name, String file, String line, String construct) {
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
		@Override
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
		@Override
		protected String combine(String name, String file, String line, String construct) {
			final String delim = " - "; //$NON-NLS-1$
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
		@Override
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
				if (traceOn)
					System.out.println("ascending=" + ascending); //$NON-NLS-1$
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

	class ConstructSorter extends GenericSorter {

		/**
		 * @param name
		 * @param file
		 * @param line
		 * @param construct
		 * @return BRT note: Sort isn't quite right: if name,filename identical,
		 *         "10" would sort before "2" e.g.
		 */
		@Override
		protected String combine(String name, String file, String line, String construct) {
			final String delim = " - "; //$NON-NLS-1$
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
	@Override
	public void createPartControl(Composite parent) {
		tree = new Tree(parent, SWT.BORDER);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		createTreeColumns();
		/*
		 * TreeColumn column; column = new TreeColumn(tree, SWT.LEFT); // col 1
		 * column = new TreeColumn(tree, SWT.LEFT); // col 2 column = new
		 * TreeColumn(tree, SWT.LEFT); // col 3 column = new TreeColumn(tree,
		 * SWT.LEFT); // col 4
		 * 
		 * // Selection listener to know when a table row is selected.
		 * 
		 * tree.addSelectionListener(new SelectionAdapter() {
		 * 
		 * // public void widgetDefaultSelected(SelectionEvent e) { // //
		 * System.out.println("widgetDefaultSelected"); // }
		 * 
		 * public void widgetSelected(SelectionEvent e) { Object obj =
		 * e.getSource(); if (obj instanceof Table) { Table t = (Table) obj; int
		 * row = t.getSelectionIndex(); // rowSelected_ = row; // print marker
		 * info when selected in table if (traceOn) { TableItem ti =
		 * t.getItem(row); IMarker marker = (IMarker) ti.getData(); IArtifact
		 * artifact = getSimpleArtifact(marker); String id =
		 * marker.getAttribute(uniqueID_, "(error)"); int mLine =
		 * MarkerUtilities.getLineNumber(marker); int lineNo = 0; if (artifact
		 * != null) lineNo = artifact.getLine();
		 * 
		 * if (traceOn) System.out.println("MARKER id=" + id + " mkrLineNo=" +
		 * mLine + " artifactLineNo=" + lineNo); }
		 * 
		 * } } });
		 */

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
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Object obj = sel.getFirstElement();
				if (obj instanceof IMarker) {
					selectedMarker_ = (IMarker) obj;
				}
				showStatusMessage("", "selectionChanged"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void createTreeColumns() {

		TreeColumn column;

		int numCols = markerAttrNames_.length;
		for (int i = 0; i < numCols; i++) {
			column = new TreeColumn(tree, SWT.LEFT);
		}

		TreeColumn[] columns = tree.getColumns();
		int numColumns = columns.length;
		assert (numCols == numColumns);

		SelectionListener[] columnListeners = new SelectionListener[numColumns];

		// if the view column default widths weren't specified on the
		// constructor, use a reasonable default width.
		int[] widths = null;
		;
		if (widths_ != null) {
			widths = widths_;
		} else {
			widths = new int[columnNames_.length];
			for (int i = 0; i < widths.length; i++) {
				widths[i] = DEFAULT_WIDTH;
			}
		}

		for (int i = 0; i < numColumns; i++) {
			int colNo = i; // could be re-ordered later
			// int colNo = columnOrder[i];
			columns[colNo].setText(columnNames_[colNo]);
			columns[colNo].setWidth(widths_[colNo]);
			columns[colNo].setResizable(true);
			columns[colNo].setMoveable(true); // can reorder columns by dragging
			SelectionListener columnListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					Object src = event.getSource();
					if (src instanceof TreeColumn) {
						TreeColumn tc = (TreeColumn) src;
						String text = tc.getText();
						if (true)
							System.out.println("tree column selected: " + text); //$NON-NLS-1$
						Object data = tc.getData();
						if (data instanceof ViewerSorter) {
							GenericSorter sorter = (GenericSorter) data;
							sorter.sort();

						} else
							System.out.println("** not a sorter ** " + data); //$NON-NLS-1$

					}
				}
			};

			columns[colNo].addSelectionListener(columnListener);
			columnListeners[colNo] = columnListener;
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
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
		manager.add(new Separator("Additions")); //$NON-NLS-1$
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(infoAction);
		if (extensionAction != null) {
			manager.add(extensionAction);
		}
		manager.add(removeMarkerAction);
		manager.add(expandAllAction);
		manager.add(collapseAllAction);
	}

	/**
	 * Make the action objects for the menus and toolbar.
	 * 
	 */
	private void makeActions() {
		makeShowInfoAction();
		makeExtensionAction();
		makeFilterAction();
		makeDoubleClickAction();
		makeRemoveMarkerAction();
		makeExpandCollapseActions();
	}

	/**
	 * Make "show info" action to display artifact information
	 */
	protected void makeShowInfoAction() {
		infoAction = new Action() {
			@Override
			public void run() {
				String title = thingname_ + Messages.SimpleTreeTableMarkerView_information;
				if (selectedMarker_ != null) {
					String info = null;
					String idFromMarker = selectedMarker_.getAttribute(uniqueID_, null);
					if (idFromMarker == null) {
						// See if implementation (subclass) has any information
						// to show
						info = extractMarkerInfo(selectedMarker_);
						if (info == null) {
							System.out
									.println("STTMV: Info action: exception reading marker ID; no info in extraceMarkerInfo either."); //$NON-NLS-1$
							return;
						}

					} else {
						info = "STTMV: no info, no ArtifactManager"; //$NON-NLS-1$
					}
					MessageDialog.openInformation(null, title, info);
				}// end if selectedMarker!=null
				else {
					MessageDialog.openInformation(null, title, Messages.SimpleTreeTableMarkerView_no + thingname_
							+ Messages.SimpleTreeTableMarkerView_selected);
				}
				// ------------------
			}
		};
		infoAction.setText(Messages.SimpleTreeTableMarkerView_showInfo);
		infoAction.setToolTipText(Messages.SimpleTreeTableMarkerView_showInfoForSelected + thingname_);
		infoAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/**
	 * make filter action
	 */
	private void makeFilterAction() {
		filterAction = new Action() {
			@Override
			public void run() {
				showMessage(Messages.SimpleTreeTableMarkerView_filter + thingnames_
						+ Messages.SimpleTreeTableMarkerView_determineWhich + thingnames_
						+ Messages.SimpleTreeTableMarkerView_areShownInThisViewPeriod);
			}
		};
		filterAction.setText(Messages.SimpleTreeTableMarkerView_filter + thingnames_);
		filterAction.setToolTipText(Messages.SimpleTreeTableMarkerView_filterWhich + thingnames_
				+ Messages.SimpleTreeTableMarkerView_areShownInThisView);
	}

	/**
	 * Make double-click action, which moves editor to the artifact instance in
	 * the source code (editor to line in source code)
	 * 
	 */
	private void makeDoubleClickAction() {

		doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
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
							System.out.println("dca: marker lineNo before " + MarkerUtilities.getLineNumber(marker)); //$NON-NLS-1$
						// note: (re?) setting linenumber here is required to
						// put marker in editor!?!
						MarkerUtilities.setLineNumber(marker, lineNo);
						if (traceOn)
							System.out.println("dca: marker lineNo after " + MarkerUtilities.getLineNumber(marker)); //$NON-NLS-1$
						IDE.gotoMarker(editor, marker);
						if (traceOn)
							System.out.println("STTMV: DoubleClickAction, clear status"); //$NON-NLS-1$
						showStatusMessage("", "double click action"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (Exception e) {
					System.out.println("STTMV.doubleclickAction: Error positioning editor page from marker line number"); //$NON-NLS-1$
					showStatusMessage(Messages.SimpleTreeTableMarkerView_errorPositioningEditorFromMarkerLineNumber,
							"error marker goto"); //$NON-NLS-2$
					e.printStackTrace();
				}
				maintainExpandCollapseStatus();
			}
		};
	}

	/**
	 * Make "remove marker" action
	 */
	protected void makeRemoveMarkerAction() {
		removeMarkerAction = new Action() {
			@Override
			public void run() {
				// batch changes so we get only one resource change event
				final IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();

				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						try {
							int depth = IResource.DEPTH_INFINITE;

							IMarker[] m = wsResource.findMarkers(markerID_, true, depth);
							int size = m.length;

							wsResource.deleteMarkers(markerID_, false, depth);
							wsResource.findMarkers(markerID_, true, depth);
							if (traceOn)
								System.out.println("markers removed."); //$NON-NLS-1$

						} catch (CoreException e) {
							System.out.println("STTMV: exception deleting markers."); //$NON-NLS-1$
						}
					}
				};
				try {
					runnable.run(null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}// end run()
		};// end new action
		removeMarkerAction.setText(Messages.SimpleTreeTableMarkerView_removeMarkers);
		removeMarkerAction.setToolTipText(Messages.SimpleTreeTableMarkerView_removeMarkers);
		removeMarkerAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));// nice
																	// "red X"
																	// image

	}

	private void makeExpandCollapseActions() {
		expandAllAction = new Action() {
			@Override
			public void run() {
				viewer.expandAll();
				expandCollapseStatus = EXPAND_COLLAPSE_EXPANDALL;
			}
		};
		expandAllAction.setText(Messages.SimpleTreeTableMarkerView_expandAll);
		expandAllAction.setToolTipText(Messages.SimpleTreeTableMarkerView_expandAllNodesInTheTree);
		ImageDescriptor descExpand = createImageDescriptor("icons/expandall.gif", "expandall"); //$NON-NLS-1$ //$NON-NLS-2$
		expandAllAction.setImageDescriptor(descExpand);

		collapseAllAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
				expandCollapseStatus = EXPAND_COLLAPSE_COLLAPSEALL;
			}
		};
		collapseAllAction.setText(Messages.SimpleTreeTableMarkerView_collapseAll);
		collapseAllAction.setToolTipText(Messages.SimpleTreeTableMarkerView_collapseAllNodesInTheTree);
		ImageDescriptor descCollapse = createImageDescriptor("icons/collapseall.gif", "collapseall"); //$NON-NLS-1$ //$NON-NLS-2$
		collapseAllAction.setImageDescriptor(descCollapse);

	}

	/**
	 * Allow derived classes to...after executing an action, the expand/contract
	 * status seems to revert (to.. collapseall?). Try to maintain what the user
	 * did. However, individual expansion of nodes won't be known. Only the last
	 * expandAll/contractAll command issued, if any.
	 */
	protected void maintainExpandCollapseStatus() {

	}

	/**
	 * Make "show info" action to display artifact information
	 * 
	 * @since 2.0
	 */
	protected void makeExtensionAction() {
		FeedbackActionCreator creator = new FeedbackActionCreator();
		String ps = thePlugin_.toString();// ??
		// do we also want ability to have a view id such that the action ONLY
		// gets attached to that view?
		feedbackAction = creator.findFeedbackAction(this.viewID_);
		if (feedbackAction != null) {
			extensionAction = new Action() {
				@Override
				public void run() {

					if (selectedMarker_ != null) {
						feedbackAction.run(selectedMarker_);
					}// end if selectedMarker!=null
					else {
						MessageDialog.openInformation(null, Messages.SimpleTreeTableMarkerView_selectAnItemInTheView,
								Messages.SimpleTreeTableMarkerView_NoFeedbackItemSelected);
					}
				}
			};
			extensionAction.setText(feedbackAction.getText());
			extensionAction.setToolTipText(feedbackAction.getToolTip());
			extensionAction.setImageDescriptor(feedbackAction.getIconImageDescriptor());
			// TODO destroy image description on shutdown/dispose?
		}
	}

	/**
	 * Create image descriptor and register it with the image registry. This
	 * means that the plugin's image registry should take care of disposing of
	 * images
	 * 
	 * @param iconLoc
	 *            Location of image file relative to plugin project: something
	 *            like "icons/foo.gif"
	 * @param key
	 *            image descriptor string key (probably only relevant if this
	 *            image is likely to be reused by others)
	 */
	ImageDescriptor createImageDescriptor(String iconLoc, String key) {
		String pluginID = Activator.PLUGIN_ID;
		Bundle bundle = Platform.getBundle(pluginID);
		IPath path = new Path(iconLoc);
		URL url = FileLocator.find(bundle, path, null);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		Activator.getDefault().getImageRegistry().put(pluginID + "." + key, desc); // it //$NON-NLS-1$
																					// should
																					// dispose
																					// when
																					// done
		return desc;
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), viewName_, message);
	}

	private void showStatusMessage(String message, String debugMessage) {
		if (traceStatusLine) {
			message += " - "; //$NON-NLS-1$
			message += debugMessage;
		}
		getViewSite().getActionBars().getStatusLineManager().setMessage(message);
		getViewSite().getActionBars().getStatusLineManager().update(true);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		showStatusMessage("", "setFocus"); // reset status message //$NON-NLS-1$ //$NON-NLS-2$
		if (!viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		if (traceOn)
			System.out.println("SimpleTableView.dispose()"); //$NON-NLS-1$
		// BRT do we need to dispose of imageDescriptors we made? or just
		// images?

	}

	public void showMarker(IMarker marker) {
		System.out.println("Marker-------  IMarker.LINE_NUMBER=" + IMarker.LINE_NUMBER); //$NON-NLS-1$
		try {
			Map attrs = marker.getAttributes();
			Iterator iter = attrs.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry e = (Map.Entry) iter.next();
				System.out.println("   " + e.getKey() + " " + e.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
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
	/*
	 * protected void pushChangedInfo(IArtifact artifact, IMarker marker) {
	 * changedArts_.push(artifact); changedMarkers_.push(marker);
	 * checkUndoStatus(); }
	 */
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
			System.out.println("STTMV: Marker lineNo(" + attr + ") invalid; using 0"); //$NON-NLS-1$ //$NON-NLS-2$
			return 0;
		}
		int lineNo = 0;
		try {
			lineNo = Integer.parseInt(temp);
		} catch (NumberFormatException nfe) {
			System.out
					.println("STTMV: Marker lineNo(" + temp + " from attr " + attr + ") invalid (NumberFormatException); using 0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			System.out.println("** Exception getting marker attribute " + e); //$NON-NLS-1$
			e.printStackTrace();
		}
		return result;

	}

	/**
	 * Subclasses may implement this to provide string info to show in the info
	 * popup, based on the marker selected.
	 * 
	 * @param marker
	 * @return
	 */
	public String extractMarkerInfo(IMarker marker) {
		return null;
	}

	/**
	 * remove spaces from a string
	 * 
	 * @param s
	 * @return
	 */
	public String removeSpaces(String s) {
		StringTokenizer st = new StringTokenizer(s, " ", false); //$NON-NLS-1$
		String t = ""; //$NON-NLS-1$
		while (st.hasMoreElements())
			t += st.nextElement();
		return t;
	}

	/**
	 * Keep icons already created, and reuse the images
	 */
	private final HashMap iconHash = new HashMap();

	/**
	 * a Stack that isn't based on Vector - Generic LIFO stack
	 * 
	 * @author Beth Tibbitts
	 * 
	 * 
	 */
	public class StackList {
		private final LinkedList list = new LinkedList();

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
						System.out.println("UpdateVisitor: file changed: " + name); //$NON-NLS-1$

					// Handle file changes (saves) by reporting the changes
					// made to the file, to update backend analysis
					// representation
					IFile f = (IFile) resource;
					int flags = delta.getFlags();
					int contentChanged = flags & IResourceDelta.CONTENT;

					if (validForAnalysis(f.getName())) {
						if (traceOn)
							System.out.println("File " + f.getName() + " is valid for analysis so will process the change..."); //$NON-NLS-1$ //$NON-NLS-2$
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
								System.out.println("---UpdateVisitor.visit():viewer update marker: (lineNo)"); //$NON-NLS-1$
							// showMarker(m);
							String[] props = new String[1]; // awkward. why???
							props[0] = ln;
							// just update viewer item, not the whole view
							// viewer.refresh();
							viewer.update(m, props);
						} // end loop
					} else {
						if (traceOn)
							System.out.println("File " + f.getName() + " is NOT valid for analysis so will ignore change..."); //$NON-NLS-1$ //$NON-NLS-2$

					}
				} // end if CHANGED
				else if (delta.getKind() == IResourceDelta.ADDED) {
					// System.out.println("Resource added.");
					checkMarkerDeltas(delta);
				} else if (delta.getKind() == IResourceDelta.REPLACED) {
					// System.out.println("Resource replaced.");
					checkMarkerDeltas(delta);
				} else if (delta.getKind() == IResourceDelta.REMOVED) {
					// System.out.println("Resource removed.");
					checkMarkerDeltas(delta);
				}
			} // end if FILE
			return true; // keep going
		}

		private void checkMarkerDeltas(IResourceDelta delta) {
			IMarkerDelta[] md1 = delta.getMarkerDeltas();
			int len = md1.length;
			// System.out.println("       ... found " + len + " markerDeltas.");
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

			String kind = "UNKNOWN"; //$NON-NLS-1$
			switch (delta3.getKind()) {
			case IResourceDelta.ADDED:
				kind = "ADDED"; //$NON-NLS-1$
				break;
			case IResourceDelta.CHANGED:
				kind = "CHANGED"; //$NON-NLS-1$
				break;
			case IResourceDelta.REMOVED:
				kind = "REMOVED"; //$NON-NLS-1$
				break;
			default:
				kind = "UNKNOWN"; //$NON-NLS-1$
				break;
			}

			if (traceOn)
				System.out.println("    markerDeltaKind=" + kind); //$NON-NLS-1$
			String mid = "", ml = "", mlpi = ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				// note: we're getting marker deltas on ALL markers,
				// not just artifact markers, which can throw us off.
				// in particular, temp markers used by actions?

				mid = m.getAttribute(uniqueID_).toString();
				ml = m.getAttribute(IMarker.LINE_NUMBER).toString();
				// mlpi = m.getAttribute(IDs.LINE).toString();
			} catch (Exception e1) {
				// ignore errors; only tracing for now.
				System.out.println("STTMV.UpdateVisitor error getting marker info "); //$NON-NLS-1$
				e1.printStackTrace();
			}
			if (traceOn)
				System.out.println("    markerID_=" + mid + "  lineNo(mkr-mpiA)=" + ml + "-" + mlpi); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

	} // end class UpdateVisitor
	/**
	 * Manage icon images ... Adapted from CDT DOMASTPluginImages ..
	 * ImageRegistry example... imageregistry only really needed when images are
	 * used a LOT and shared???
	 * 
	 * @author beth
	 * 
	 */

}