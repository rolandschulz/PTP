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

package org.eclipse.ptp.internal.etfw.feedback;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;
import org.eclipse.ptp.internal.etfw.feedback.messages.Messages;
import org.eclipse.ptp.internal.etfw.feedback.preferences.PreferenceConstants;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Creates markers representing IFeedbackItem objects, to be shown in the
 * Feedback view
 * 
 * assumed to be a singleton; fileMap is created once
 */
public class MarkerManager {
	private static final boolean traceOn = false;

	static String path;
	static String filename;

	/**
	 * Hash table of filenames and resources. Since usually a single file is
	 * used often, we don't recreate the IResource.<br>
	 * This is the list of unique files found in the IFeedbackItems
	 */
	Map<String, IResource> fileMap = new HashMap<String, IResource>();

	/**
	 * FIXME this will be valid on local file system only
	 */
	private static final String SLASH = System.getProperty("file.separator"); //$NON-NLS-1$

	/**
	 * Remove the markers and remove the then-empty parent nodes too
	 * 
	 */
	public void removeMarkers(IResource res, String markerID) {
		try {
			res.deleteMarkers(markerID, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			System.out.println("Error deleting markers on " + res.getName() + " - probably can ignore this."); //$NON-NLS-1$
			// e.printStackTrace();
		}
	}

	/**
	 * Translate /path/to/xml/file.xml to /path/to/src to obtain a path to the
	 * presumed location of the source file
	 * 
	 * @param file
	 * @return
	 */
	public String getSrcPath(File file) {
		// hack until we can get more complete pathname from the xml
		String docpath = null;
		try {
			docpath = file.getAbsolutePath();
			int indx = docpath.lastIndexOf(SLASH);
			String pathname = docpath.substring(0, indx);
			indx = pathname.lastIndexOf(SLASH);
			pathname = pathname.substring(0, indx);
			pathname = pathname + SLASH + "src"; //$NON-NLS-1$
			return pathname;
		} catch (Exception e) {
			System.out.println("Exception parsing src path from " + docpath); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * temporary: because the supplied xml is inconsistent about whether or not
	 * it lists the filename fully qualified or not.
	 * 
	 * @param filename
	 * @return
	 */
	public String stripFileNameOnly(String filename) {
		int indx = filename.lastIndexOf(SLASH);
		if (indx >= 0) {
			filename = filename.substring(indx + 1);
		}
		// the original filename in the xml could have been built on *nix but is
		// being processed on windows.
		// if so, translate
		else {
			// note there is a reported bug in string.replaceAll regarding
			// double slashes
			String otherSlash = "/"; //$NON-NLS-1$
			if (SLASH.equals(otherSlash))
				otherSlash = "\\"; //$NON-NLS-1$
			StringBuffer newf = new StringBuffer();
			int len = filename.length();
			try {
				for (int i = 0; i < len; i++) {
					String next = filename.substring(i, i + 1);
					if (next.equals(otherSlash)) {
						newf.append(SLASH);
					} else {
						newf.append(next);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			indx = newf.lastIndexOf(SLASH);
			if (indx >= 0) {
				filename = newf.substring(indx + 1);
			}
		}
		return filename;
	}

	String getStrAttr(NamedNodeMap attribs, String attrName) {
		Node node = attribs.getNamedItem(attrName);
		if (node == null)
			return ""; //$NON-NLS-1$
		String val = node.getNodeValue();
		return val;
	}

	int getIntAttr(NamedNodeMap attribs, String attrName) {
		Node node = attribs.getNamedItem(attrName);
		String val = node.getNodeValue();
		int intVal = toInt(val);
		return intVal;
	}

	/**
	 * create an int from a string
	 * 
	 * @param str
	 * @return
	 */
	public int toInt(String str) {
		int value = 0;
		try {
			value = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			System.out.println("NumberFormatException(" + str + "): " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			value = 0;

		}
		return value;
	}

	public IResource getResource(String pathname, String filename) {

		ResourcesPlugin.getWorkspace();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFileForLocation(new Path(pathname + SLASH + filename));
		return file;
	}

	/**
	 * get IResource from a fully qualified file name
	 * 
	 * @param filename
	 * @return
	 */
	public IResource getResource(String filename) {
		ResourcesPlugin.getWorkspace();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFileForLocation(new Path(filename));// local file
																	// system
																	// only
																	// (null for
																	// remote)
		file = root.getFile(new Path(filename));
		return file;
	}

	/**
	 * @since 5.0
	 */
	public IResource getResource(URI location) {
		ResourcesPlugin.getWorkspace();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile[] files = root.findFilesForLocationURI(location);
		IFile file = files[0];// hack
		return file;
	}

	/**
	 * from SampleFeedbackParser.getResource <br>
	 * Works for remote projects/files too
	 * 
	 * @param projName
	 * @param filename
	 * @return
	 * @since 5.0
	 */
	public static IResource getResourceInProject(String projName, String filename) {
		ResourcesPlugin.getWorkspace();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject proj = root.getProject(projName);
		IResource res = proj.findMember(filename);
		// boolean exists=res.exists();

		// IFile file=root.getFile(new Path(filename)); // works when filename
		// contains project name
		return res;
	}

	private static int counter = 0;

	/**
	 * Create marker attributes common to all marker items.
	 * 
	 * @param item
	 *            the IFeedback item that this marker represents.
	 * @param itemID
	 * @param name
	 * @param parentID
	 * @param filename
	 * @param pathname
	 *            possibly not used?
	 * @param lineNo
	 * @param desc
	 * @return
	 * @since 5.0
	 */
	public Map<String, Object> createCommonMarkers(IFeedbackItem item, String itemID, String name, String parentID,
			String filename, String pathname,
			int lineNo, String desc) {
		Map<String, Object> attrs = new HashMap<String, Object>();

		attrs.put(FeedbackIDs.FEEDBACK_ATTR_ID, itemID);
		attrs.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_NORMAL));
		attrs.put(FeedbackIDs.FEEDBACK_ATTR_NAME, name);
		attrs.put(FeedbackIDs.FEEDBACK_ATTR_FILENAME, filename);
		attrs.put(FeedbackIDs.FEEDBACK_ATTR_PARENT, parentID);

		attrs.put(FeedbackIDs.FEEDBACK_ATTR_PATHNAME, pathname);
		attrs.put(IMarker.LINE_NUMBER, new Integer(lineNo));

		attrs.put(FeedbackIDs.FEEDBACK_ATTR_ITEM, item);

		// later, set the marker to more precise location - but omit for now, or
		// else lineNumber won't be used
		// attrs.put(IMarker.CHAR_START, new Integer(ila.getColumn()));
		// attrs.put(IMarker.CHAR_END, new Integer(ila.getColumn()+5));// hack,
		// what is end?
		attrs.put(FeedbackIDs.FEEDBACK_ATTR_DESC, desc);
		attrs.put(FeedbackIDs.FEEDBACK_ATTR_LOOP_ID, ""); // filled in by (only) transform attempts //$NON-NLS-1$
		return attrs;
	}

	public void createMarker(IResource resource, Map<String, Object> attrs, String markerID) {
		try {
			MarkerUtilities.createMarker(resource, attrs, markerID);
			if (traceOn)
				System.out
						.println("  MarkerManager: Created marker for " + resource.getName() + " " + attrs.get(FeedbackIDs.FEEDBACK_ATTR_NAME) + " lineNo:" + attrs.get(IMarker.LINE_NUMBER) + " parentID=" + attrs.get(FeedbackIDs.FEEDBACK_ATTR_PARENT)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} catch (CoreException e) {
			System.out.println("Error creating Xform marker: " + e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	/**
	 * Create the markers for the IFeedbackItems. Note: some Items may be parent
	 * groups and not have file, etc. info
	 * 
	 * 
	 * @param itemlist
	 */
	public void createMarkers(final List<IFeedbackItem> itemlist, final String markerID) {
		long firstStart = System.currentTimeMillis();
		if (traceOn)
			System.out.println("MarkerMgr.createMarkers()...");
		if (itemlist.size() == 0) {
			showNoItemsFound();
			return;
		}
		// create the list of unique files found in the IFeedbackItems
		fileMap = new HashMap<String, IResource>();
		for (Iterator<IFeedbackItem> iterator = itemlist.iterator(); iterator.hasNext();) {
			IFeedbackItem item = iterator.next();

			String filename = item.getFile();

			// Populate fileMap
			if ((filename != null) && (!fileMap.containsKey(filename))) {
				// IResource res = getResource(f1);
				IFile ifile = item.getIFile();// works for a remote file :)
				if (traceOn)
					System.out.println("MM: found: " + ifile.getLocationURI());
				fileMap.put(filename, ifile);
				if (traceOn)
					System.out.println("Source file: " + filename);// print each
																	// unique
																	// one we
																	// find
			}
		}
		if (traceOn)
			System.out.println("MarkerMgr.createMarkers()...after file gathering, found # files: " + fileMap.size());

		// remove "our" markers on all source files referenced in this file
		IResource res = null;
		for (Iterator<String> iterator = fileMap.keySet().iterator(); iterator.hasNext();) {
			String filename = iterator.next();
			// res=getResource(filename);
			res = fileMap.get(filename);
			try {
				removeMarkers(res, markerID);
			} catch (Exception e) {
				System.out.println("Error deleting markers on file: " + res); //$NON-NLS-1$
				// e.printStackTrace();
			}
		}

		// BATCH all the resource changes from the marker creation in a runnable
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				loopItemsCreateMarkers(itemlist, markerID);
			}
		};
		// if this takes a non-trivial amt of time we can put up a real progress
		// monitor
		IProgressMonitor monitor = null;
		try {
			workspace.run(operation, monitor);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (traceOn)
			System.out.println("Total elapsed time in createMarkers: " + (System.currentTimeMillis() - firstStart));
	}// end createMarkers

	/**
	 * Loop thru the IFeedbackItems and create the markers. This can be batched
	 * within a runnable to avoid sending resource change events until all the
	 * markers are created.
	 * 
	 * @param itemlist
	 * @param markerID
	 */
	private void loopItemsCreateMarkers(List<IFeedbackItem> itemlist, String markerID) {
		final boolean dbgTags = false;
		String parentID;
		Map<String, Object> attrs;
		for (Iterator<IFeedbackItem> iterator = itemlist.iterator(); iterator.hasNext();) {
			IFeedbackItem item = iterator.next();
			String filename = item.getFile();
			String name = item.getName();// +" "+item.getID();
			int lineNo = item.getLineNoStart();
			String desc = item.getDescription();
			String itemID = item.getID();
			parentID = item.getParentID();
			String pathname = ""; // we assume it's fully qualified filename now //$NON-NLS-1$
			IResource resource = fileMap.get(filename);
			if (filename != null && filename.contains(Path.SEPARATOR + "")) { //$NON-NLS-1$
				// note: we could do this ONCE instead. probably re-creating it
				// here.
				IPath path = new Path(filename);
				pathname = path.removeLastSegments(1).toString();
				filename = path.segment(path.segmentCount() - 1);
			}
			attrs = createCommonMarkers(item, itemID, name, parentID, filename, pathname, lineNo, desc);

			if (resource != null) {
				createMarker(resource, attrs, markerID);
			} else {
				System.out.println("Feedback MarkerManager: Null resource for pathname=" + pathname + "; filename=" + filename
						+ "; no markers created.  item.getFile()=" + item.getFile());
			}
			if (item.hasChildren()) {
				List<IFeedbackItem> kids = item.getChildren();
				for (Iterator<IFeedbackItem> iterator2 = kids.iterator(); iterator2.hasNext();) {
					IFeedbackItem kid = iterator2.next();
					String parentid = item.getID();
					String namePrefix = "";//"Bottleneck: ";   //$NON-NLS-1$
					String kname = kid.getName();
					if (dbgTags)
						kname = namePrefix + kname;
					if (dbgTags)
						kname += " parent=" + parentid; //$NON-NLS-1$
					int uid = counter++; // need something unique
					String uidStr = Integer.toString(uid);
					// make file/location the same as parent
					attrs = createCommonMarkers(kid, uidStr, kname, parentid, filename, pathname, lineNo, kid.getDescription());
					createMarker(resource, attrs, markerID);
					boolean gkids = kid.hasChildren();
					// fixme make this recursive so level of hierarchy doesn't
					// matter
					if (gkids) {
						if (dbgTags)
							System.out.println("grandkids"); //$NON-NLS-1$
						List<IFeedbackItem> gkidItems = kid.getChildren();
						for (Object gkid : gkidItems) {
							IFeedbackItem gki = (IFeedbackItem) gkid;
							String gkNamePrefix = Messages.MarkerManager_solution; // HACK
							attrs = createCommonMarkers(gki, gki.getID(), gkNamePrefix + gki.getName(), uidStr, filename,
									pathname, lineNo, gki.getDescription());
							createMarker(resource, attrs, markerID);
						}
					}
				}
			}
		}
	}// end loopItemsCreateMarkers

	/**
	 * Put up a dialog informing the user that no items were found to be shown
	 * in the feedback view. Preference value can prevent this from being shown.
	 */
	private void showNoItemsFound() {
		IPreferenceStore pf = Activator.getDefault().getPreferenceStore();
		boolean showDialog = pf.getBoolean(PreferenceConstants.P_SHOW_NO_ITEMS_FOUND_DIALOG);
		if (showDialog) {
			String title = Messages.MarkerManager_noFeedbackItemsFoundTitle;
			String msg = Messages.MarkerManager_noFeedbackItemsFoundMessage;
			// MessageDialog.openInformation(null, title, msg);
			String togMsg = Messages.MarkerManager_dontShowMeThisAgain;
			MessageDialogWithToggle.openInformation(null, title, msg.toString(), togMsg, false, pf,
					PreferenceConstants.P_SHOW_NO_ITEMS_FOUND_DIALOG);
		}
	}
	/*
	 * long start; void time(String label){ long
	 * time=System.currentTimeMillis();
	 * System.out.println(label+"  elapsed: "+(time-start));
	 * start=System.currentTimeMillis(); } void timeStart(){
	 * start=System.currentTimeMillis(); }
	 */
}
