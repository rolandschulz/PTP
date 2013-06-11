/**********************************************************************
 * Copyright (c) 2007,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.common.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
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
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactManager;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.IArtifact;
import org.eclipse.ptp.pldt.common.messages.Messages;
import org.eclipse.ptp.pldt.internal.common.IDs;
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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.osgi.framework.Bundle;

/**
 * This view shows generic artifacts, represented by a marker, that are found in
 * a (source) file. <br>
 * It provides information and actions. The object model is the marker.
 * 
 * It provides standard columns of name, filename, lineNo, and one more. <br>
 * Names and last column name provided on ctor. Intended for quick reuse.
 * 
 * All information comes from the marker. Artifact objects containing arbitrary
 * information can be used (not extensively tested here yet).195
 * 
 * 
 * 
 */
public class SimpleTableMarkerView extends ViewPart {
	protected TableViewer viewer;

	protected Action infoAction;
	protected Action removeMarkerAction;

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
	protected String thingname_ = Messages.SimpleTableMarkerView_0;

	protected String thingnames_ = Messages.SimpleTableMarkerView_1;

	private String columnName_ = Messages.SimpleTableMarkerView_2;

	private AbstractUIPlugin thePlugin_;

	private String iconName_ = "icons/sample.gif"; //$NON-NLS-1$

	private String viewName_;

	private String[] markerIDs_;

	private String[] columnNames_;

	private String[] markerAttrNames_;

	/**
	 * The ID used in the marker for the unique ID for each artifact. Enables
	 * mapping back to the Artifact object if necessary.
	 */
	protected String uniqueID_ = IDs.UNIQUE_ID;

	/**
	 * The ID used in the marker for the extra column of information (last
	 * column)
	 */
	protected String columnID_ = "constructType"; // id for (variable) //$NON-NLS-1$

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

	/**
	 * Simple Artifact Table View constructor
	 * <p>
	 * Everything can be null, and defaults will be taken, or read from plugin.xml for the view.
	 * <p>
	 * Note: if a null plugIn instance is provided, the default plugin (this one) will not be able to find resources (e.g. icon
	 * images) if the derived class is in its own plug-in, and its icons are, too.
	 */
	public SimpleTableMarkerView(AbstractUIPlugin thePlugin, String thingname,
			String thingnames, String columnName, String[] markerIDs) {

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
		this.markerIDs_ = markerIDs;// if null, will use view id.

		findViewInfo();

	}

	/**
	 * Constructor with a single marker ID that is converted to a single element list
	 * 
	 * @param thePlugin
	 * @param thingname
	 * @param thingnames
	 * @param columnName
	 * @param markerID
	 */
	public SimpleTableMarkerView(AbstractUIPlugin thePlugin, String thingname,
			String thingnames, String columnName, String markerID) {
		this(thePlugin, thingname, thingnames, columnName, new String[] { markerID });

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
	public SimpleTableMarkerView(AbstractUIPlugin thePlugin, String thingname,
			String thingnames, String[] attrNames, String[] colNames,
			String[] markerIDs) {
		this(thePlugin, thingname, thingnames, null, markerIDs);
		columnNames_ = colNames;
		columnName_ = null;// set this so we can tell we are using array of
							// attrs/cols
		markerAttrNames_ = attrNames;

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
					if (markerIDs_ == null) {
						// use plug-in id for marker id, if not specified
						markerIDs_ = new String[1];
						String pluginID = cElement.getAttribute("id"); //$NON-NLS-1$
						markerIDs_[1] = pluginID;
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
			IResourceChangeListener {
		private IResource input;

		private boolean hasRegistered = false;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// could use this to change the list to just artifacts from one
			// resource,
			// etc...
			// could cache viewer here this.viewer=v;
			if (traceOn)
				System.out.println("ATV inputChanged()..."); //$NON-NLS-1$
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
							.println("ATV: Registered RCL for ViewContentProvider"); //$NON-NLS-1$
			}
			if (newInput instanceof IResource) {
				this.input = (IResource) newInput;
			}

		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			if (traceOn)
				System.out.println("ATV.ViewContentProvider.dispose()"); //$NON-NLS-1$
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

		}

		/**
		 * Get the list of objects to populate this view.
		 */
		public Object[] getElements(Object parent) {
			Object[] objs = null;
			List<IMarker> allObjs = new ArrayList<IMarker>();
			try {
				// String id = markerIDs_;
				if (input == null) {
					if (traceOn)
						System.out.println("input is null in getElements..."); //$NON-NLS-1$
				}
				// use the cached input object instead of querying from
				// workspace objs = ResourcesPlugin.getWorkspace().getRoot().findMarkers(id,
				// false, IResource.DEPTH_INFINITE);
				for (int i = 0; i < markerIDs_.length; i++) {
					String id = markerIDs_[i];
					objs = input.findMarkers(id, false, IResource.DEPTH_INFINITE);
					List markList = Arrays.asList(objs);
					// allObjs.addAll((List<IMarker>) Arrays.asList(objs));
					if (traceOn)
						System.out.println("found " + (objs.length) + " markers of type " //$NON-NLS-1$ //$NON-NLS-2$
								+ id);
					allObjs.addAll(markList);

					// String[] sl = (String[]) list.toArray(new String[0]);
				}
				if (traceOn)
					System.out.println("Found " + allObjs.size() + " total markers"); //$NON-NLS-1$ //$NON-NLS-2$

			} catch (CoreException e) {
				System.out
						.println("ATV, exception getting model elements (markers for Table view)"); //$NON-NLS-1$
				e.printStackTrace();
			}
			if (traceOn)
				System.out.println("ATV.get---Elements, found " + objs.length //$NON-NLS-1$
						+ " markers"); //$NON-NLS-1$
			return allObjs.toArray();

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
								System.out
										.println("viewer.update ea mkr in delta-- from resourceChanged()..."); //$NON-NLS-1$
							if (traceOn)
								System.out
										.println("----processResourceChangeDelta()..."); //$NON-NLS-1$
							processResourceChangeDelta(delta);
							if (traceOn)
								System.out
										.println("----END processResourceChangeDelta()..."); //$NON-NLS-1$
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
							System.out
									.println("ATV: Exception refreshing viewer: " //$NON-NLS-1$
											+ e);
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
		 *            number of levels of indent we should consider ourselves as being in
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
				System.out
						.println("Error in PITV.processResourceChangeDelta().."); //$NON-NLS-1$
				e2.printStackTrace();
			}
		}

	} // end ViewContentProvider

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
			artifact = ArtifactManager.getArtifact(marker);
		} catch (CoreException e) {
			// e.printStackTrace();
			System.out.println(e.getMessage()
					+ " ... STV, CoreException getting artifact from hashMap; " //$NON-NLS-1$
					+ thingname_ + " id=" + id); //$NON-NLS-1$
		} catch (NullPointerException ne) {
			System.out.println(ne.getMessage()
					+ " ... STV, NullPtrExcp getting artifact from hashMap;" //$NON-NLS-1$
					+ thingname_ + " id=" + id); //$NON-NLS-1$
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
			return Artifact.CONSTRUCT_TYPE_NAMES[constructType.intValue()];
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
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		/**
		 * Keep icons already created, and reuse the images
		 */
		private HashMap<String, Image> iconHash = new HashMap<String, Image>();

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
		 * Determine the text to go in each column
		 * 
		 * @param obj
		 *            the Marker (we hope) that goes on the current row
		 * @param index
		 *            the column number in the table
		 * 
		 */
		public String getColumnText(Object obj, int index) {
			if (obj == null) {
				System.out
						.println("ATV: LabelProv obj is null; index=" + index); //$NON-NLS-1$
				return "ATV obj null"; //$NON-NLS-1$
			}
			IMarker marker = (IMarker) obj;
			try {
				switch (index) {
				case 0:
					return ""; //$NON-NLS-1$
				case 1:
					String id = (String) marker.getAttribute(NAME);
					return id;
				case 2:

					return (String) marker.getAttribute(FILENAME);
				case 3:
					String line = (marker.getAttribute(IMarker.LINE_NUMBER))
							.toString();

					if (traceOn) { // all this is for debugging purposes so
						artifact = getSimpleArtifact(marker);
						String compLine = line + "-"; //$NON-NLS-1$
						if (artifact == null) {
							if (traceOn)
								System.out
										.println("ATV getColumnText- null artifact"); //$NON-NLS-1$
						} else {
							int lineArtifact = artifact.getLine();
							compLine = compLine + lineArtifact;
						}
						System.out
								.println("ATV.ViewLabelProvider gets marker line: mkr-artifact: " //$NON-NLS-1$
										+ compLine);
						showMarker(marker);
					}
					return line;
				case 4:
					if (columnName_ != null) {// we're not using array
						return getConstructStr(marker);
					}
					// else drop through...

				default:
					String attrName = markerAttrNames_[index - 4];
					String val = marker.getAttribute(attrName, ""); //$NON-NLS-1$
					return val;
				}
			} catch (CoreException ce) {
				// get this error 3x "Marker id: 999 not found." while deleting
				// markers. why?
				// Why is this even getting called, and why does it matter?
				// String tmp = ce.getMessage();
				// ce.printStackTrace();
				return ("ATV error"); //$NON-NLS-1$
			}
		}

		/**
		 * Provide the image that goes in a column, if any (Note that a table
		 * cell can contain both, an image and text, which will be displayed
		 * side-by-side)
		 * 
		 * @param obj
		 *            -
		 *            the object we're getting the image for
		 * @param index
		 *            -
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
		 *         Note: if a null plugIn instance is provided on the view ctor, the default plugin (this one) will not be able to
		 *         find resources (e.g. icon images) if the derived class is in its own plug-in, and its icons are, too.
		 * 
		 */
		protected Image getCustomImage(Object obj) {
			// if we've already created one of this type of icon, reuse it.
			// Note: use ImageRegistry instead?
			Image img = iconHash.get(iconName_);
			if (img == null) {
				Path path = new Path(iconName_);
				// BRT make sure the specific plugin is being used here to find
				// its OWN icons
				URL url = FileLocator.find(thePlugin_.getBundle(), path, null);
				ImageDescriptor id = ImageDescriptor.createFromURL(url);
				img = id.createImage();
				if (traceOn)
					System.out.println("ATV: ***** created image for " //$NON-NLS-1$
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
						.println("ATV.ViewLabelProvider.dispose(); dispose of icon images"); //$NON-NLS-1$
			for (Iterator<Image> iter = iconHash.values().iterator(); iter.hasNext();) {
				Image img = iter.next();
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
	public void createPartControl(Composite parent) {
		// Widget created and customized and then passed to viewer during
		// creation :
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		String[] STD_HEADINGS = { " ", thingname_, Messages.SimpleTableMarkerView_filename, Messages.SimpleTableMarkerView_lineno, //$NON-NLS-1$
				this.columnName_ };

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

		TableColumn tc4 = null;
		if (this.columnName_ != null) {
			layout.addColumnData(new ColumnWeightData(5, true));
			tc4 = new TableColumn(table, SWT.NONE);
			tc4.setText(STD_HEADINGS[4]);
			tc4.setAlignment(SWT.LEFT);
			tc4.setResizable(true);
		} else {
			int numCols = columnNames_.length;
			TableColumn[] tableCols = new TableColumn[numCols];
			for (int i = 0; i < numCols; i++) {
				layout.addColumnData(new ColumnWeightData(5, true));
				TableColumn tc = new TableColumn(table, SWT.NONE);
				tc.setText(columnNames_[i]);
				tc.setAlignment(SWT.LEFT);
				tc.setResizable(true);
				tableCols[i] = tc;
			}
		}

		// add listeners for table sorting
		// Sort by "icon" (the original sort order, actually)
		tc0.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				viewer.setSorter(null);
				viewer.setSorter(nameSorter);
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		// Sort by artifact name
		tc1.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				nameArtifactSorter.sort();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		// Sort by file name (then by lineNo)
		tc2.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				filenameSorter.sort();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		// Sort by Line number
		tc3.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				lineNoSorter.sort();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		// Sort by Construct (if we're not doing an array of extra columns)
		if (tc4 != null) {
			tc4.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent event) {
					constructSorter.sort();
				}

				public void widgetDefaultSelected(SelectionEvent event) {
				}
			});
		}

		// Selection listener to know when a table row is selected.

		table.addSelectionListener(new SelectionAdapter() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// System.out.println("widgetDefaultSelected");
			}

			public void widgetSelected(SelectionEvent e) {
				Object obj = e.getSource();
				if (obj instanceof Table) {
					Table t = (Table) obj;
					int row = t.getSelectionIndex();
					// rowSelected_ = row;
					// print marker info when selected in table
					if (traceOn) {
						TableItem ti = t.getItem(row);
						IMarker marker = (IMarker) ti.getData();
						IArtifact artifact = getSimpleArtifact(marker);
						String id = marker.getAttribute(uniqueID_, Messages.SimpleTableMarkerView_error);
						int mLine = MarkerUtilities.getLineNumber(marker);
						int lineNo = 0;
						if (artifact != null)
							lineNo = artifact.getLine();

						if (traceOn)
							System.out.println("MARKER id=" + id //$NON-NLS-1$
									+ " mkrLineNo=" + mLine //$NON-NLS-1$
									+ " artifactLineNo=" + lineNo); //$NON-NLS-1$
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
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event
						.getSelection();
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

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SimpleTableMarkerView.this.fillContextMenu(manager);
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
		manager.add(removeMarkerAction);
		manager.add(filterAction);
	}

	/**
	 * Make the action objects for the menus and toolbar.
	 * 
	 */
	private void makeActions() {
		makeShowInfoAction();
		makeRemoveMarkerAction();
		makeFilterAction();
		makeDoubleClickAction();
	}

	/**
	 * Make "show info" action to display artifact information
	 */
	protected void makeShowInfoAction() {
		infoAction = new Action() {
			public void run() {
				String title = thingname_ + Messages.SimpleTableMarkerView_information;
				if (selectedMarker_ != null) {
					String idFromMarker = (String) selectedMarker_
							.getAttribute(uniqueID_, null);
					if (idFromMarker == null) {
						System.out.println("ATV: exception reading marker ID"); //$NON-NLS-1$
						return;
					}
					StringBuffer info = new StringBuffer();

					IArtifact artifact = null;
					;
					try {
						artifact = ArtifactManager.getArtifact(selectedMarker_);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (artifact != null) {
						info.append(Messages.SimpleTableMarkerView_filename2).append(artifact.getFileName());
						info.append(Messages.SimpleTableMarkerView_lineno2).append(artifact.getLine());
						info.append(Messages.SimpleTableMarkerView_name2).append(artifact.getShortName());
						String desc = artifact.getDescription();
						if (desc != null) {
							info.append(Messages.SimpleTableMarkerView_description2).append(desc);
						}
					}
					else {
						info.append(Messages.SimpleTableMarkerView_no_info_avail);
					}
					MessageDialog.openInformation(null, title, info.toString());
				}// end if selectedMarker!=null
				else {
					MessageDialog.openInformation(null, title, Messages.SimpleTableMarkerView_no
							+ thingname_ + Messages.SimpleTableMarkerView_selected);
				}
			}
		};
		infoAction.setText(Messages.SimpleTableMarkerView_showInfo);
		infoAction.setToolTipText(Messages.SimpleTableMarkerView_showInfoTooltip
				+ thingname_);
		infoAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/**
	 * Make "remove marker" action to display artifact information
	 */
	protected void makeRemoveMarkerAction() {
		removeMarkerAction = new Action() {
			public void run() {
				// batch changes so we get only one resource change event
				final IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();

				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						try {
							int depth = IResource.DEPTH_INFINITE;

							for (int i = 0; i < markerIDs_.length; i++) {
								String markerID = markerIDs_[i];
								wsResource.deleteMarkers(markerID, false, depth);
							}

							if (traceOn)
								System.out.println("markers removed."); //$NON-NLS-1$
						} catch (CoreException e) {
							System.out.println("RM: exception deleting markers."); //$NON-NLS-1$
							// e.printStackTrace();
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
		removeMarkerAction.setText(Messages.SimpleTableMarkerView_removeMarkers);
		removeMarkerAction.setToolTipText(Messages.SimpleTableMarkerView_removeMarkersTooltip);
		removeMarkerAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));// nice "red X" image

	}

	/**
	 * make filter action, allowing user to customize the information
	 * shown in the view.
	 */
	private void makeFilterAction() {
		filterAction = new Action() {
			public void run() {
				showMessage(Messages.SimpleTableMarkerView_91 + thingnames_ + Messages.SimpleTableMarkerView_92
						+ thingnames_ + Messages.SimpleTableMarkerView_93);
			}
		};
		filterAction.setText(Messages.SimpleTableMarkerView_94 + thingnames_);
		filterAction.setToolTipText(Messages.SimpleTableMarkerView_95 + thingnames_
				+ Messages.SimpleTableMarkerView_96);
		// from org.eclipse.ui.views plugin
		Path path = new Path("icons/filter_ps.gif"); //$NON-NLS-1$
		Bundle b = CommonPlugin.getDefault().getBundle();
		String temp = b.getSymbolicName();
		URL url = FileLocator.find(CommonPlugin.getDefault().getBundle(), path, null);
		ImageDescriptor id = ImageDescriptor.createFromURL(url);

		Image img = id.createImage();

		filterAction.setImageDescriptor(id);
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
				IMarker marker = (IMarker) obj;
				try {
					IFile f = (IFile) marker.getResource();
					int lineNo = getMarkerLineNo(marker);
					if (f != null && f.exists()) {
						IWorkbenchPage wbp = getSite().getPage();
						// IEditorInput ieu = new FileEditorInput(f);
						IEditorPart editor = IDE.openEditor(wbp, f);

						if (traceOn)
							System.out.println("dca: marker lineNo before " //$NON-NLS-1$
									+ MarkerUtilities.getLineNumber(marker));
						// note: (re?) setting linenumber here is required to
						// put marker in editor!?!
						MarkerUtilities.setLineNumber(marker, lineNo);
						if (traceOn)
							System.out.println("dca: marker lineNo after " //$NON-NLS-1$
									+ MarkerUtilities.getLineNumber(marker));
						IDE.gotoMarker(editor, marker);
						if (traceOn)
							System.out
									.println("ATV: DoubleClickAction, clear status"); //$NON-NLS-1$
						showStatusMessage("", "double click action"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (Exception e) {
					System.out
							.println("ATV.doubleclickAction: Error positioning editor page from marker line number"); //$NON-NLS-1$
					showStatusMessage("Error positioning editor from marker line number", //$NON-NLS-1$
							"error marker goto"); //$NON-NLS-1$
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
			message += " - "; //$NON-NLS-1$
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
		showStatusMessage("", "setFocus"); // reset status message //$NON-NLS-1$ //$NON-NLS-2$
		if (!viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		if (traceOn)
			System.out.println("SimpleTableView.dispose()"); //$NON-NLS-1$
	}

	/**
	 * Used for debugging to expose marker information conveniently
	 * @param marker
	 */
	public void showMarker(IMarker marker) {
		System.out.println("Marker-------  IMarker.LINE_NUMBER=" //$NON-NLS-1$
				+ IMarker.LINE_NUMBER);
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

	/**
	 * Create an ImageDescriptor for an image
	 * @param iconName
	 * @return
	 */
	public ImageDescriptor makeImageDescriptor(String iconName) {
		URL url = FileLocator.find(thePlugin_.getBundle(), new Path(iconName), null);
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
			System.out.println("ATV: Marker lineNo(" + attr //$NON-NLS-1$
					+ ") invalid; using 0"); //$NON-NLS-1$
			return 0;
		}
		int lineNo = 0;
		try {
			lineNo = Integer.parseInt(temp);
		} catch (NumberFormatException nfe) {
			System.out.println("ATV: Marker lineNo(" + temp + " from attr " //$NON-NLS-1$ //$NON-NLS-2$
					+ attr + ") invalid (NumberFormatException); using 0"); //$NON-NLS-1$
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
	 * a Stack that isn't based on Vector - Generic LIFO stack
	 * 
	 * @author Beth Tibbitts
	 * 
	 * 
	 */
	public class StackList {
		private LinkedList<Object> list = new LinkedList<Object>();

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
						System.out.println("UpdateVisitor: file changed: " //$NON-NLS-1$
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
									.println("File " //$NON-NLS-1$
											+ f.getName()
											+ " is valid for analysis so will process the change..."); //$NON-NLS-1$
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
										.println("---UpdateVisitor.visit():viewer update marker: (lineNo)"); //$NON-NLS-1$
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
									.println("File " //$NON-NLS-1$
											+ f.getName()
											+ " is NOT valid for analysis so will ignore change..."); //$NON-NLS-1$

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
				System.out
						.println("ATV.UpdateVisitor error getting marker info "); //$NON-NLS-1$
				e1.printStackTrace();
			}
			if (traceOn)
				System.out.println("    markerID_=" + mid //$NON-NLS-1$
						+ "  lineNo(mkr-mpiA)=" + ml + "-" + mlpi); //$NON-NLS-1$ //$NON-NLS-2$
		}

	} // end class UpdateVisitor

	class ArtifactFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IMarker) {
				IMarker marker = (IMarker) element;
				try {
					Object obj = marker.getAttribute("foo"); //$NON-NLS-1$
					System.out.println("obj=" + obj); //$NON-NLS-1$
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;
		}

	}

}