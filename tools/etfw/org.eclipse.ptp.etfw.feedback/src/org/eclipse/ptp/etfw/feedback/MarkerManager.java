/**********************************************************************
 * Copyright (c) 2009,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.etfw.feedback;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.etfw.feedback.messages.Messages;
import org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem;
import org.eclipse.ptp.etfw.feedback.preferences.PreferenceConstants;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Creates markers representing IFeedbackItem objects, to be shown in the
 * Feedback view
 */
public class MarkerManager {
	private static final boolean traceOn = false;

	static String path;
	static String filename;
	private static MarkerManager instance;

	private static final String SLASH = System.getProperty("file.separator"); //$NON-NLS-1$

	/**
	 * Remove the markers from the files we're about to add new markers to
	 * 
	 * @param sfList
	 *            List of source files
	 */
	public void removeMarkers(IResource res, String markerID) {
		try {
			res.deleteMarkers(markerID, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			System.out.println("Error deleting markers on " + res.getName()); //$NON-NLS-1$
			e.printStackTrace();
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
		IFile file = root.getFileForLocation(new Path(filename));
		return file;
	}

	private static int counter = 0;

	/**
	 * Create marker attributes common to all marker items.
	 * 
	 * @param item the IFeedback item that this marker represents.  
	 * @param itemID
	 * @param name
	 * @param parentID
	 * @param filename
	 * @param pathname possibly not used?
	 * @param lineNo
	 * @param desc
	 * @return
	 * @since 2.0
	 */
	public Map<String, Object> createCommonMarkers(IFeedbackItem item, String itemID, String name, String parentID, String filename, String pathname,
			int lineNo,  String desc) {
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
		// attrs.put(IMarker.CHAR_END, new Integer(ila.getColumn()+5));// hack, what is end?
		attrs.put(FeedbackIDs.FEEDBACK_ATTR_DESC, desc);
		attrs.put(FeedbackIDs.FEEDBACK_ATTR_LOOP_ID, ""); // filled in by (only) transform attempts //$NON-NLS-1$
		return attrs;
	}

	public void createMarker(IResource resource, Map<String, Object> attrs, String markerID) {
		try {
			MarkerUtilities.createMarker(resource, attrs, markerID);
			if (traceOn)
				System.out.println("  MarkerManager: Created marker for " + resource.getName() + " " + attrs.get(FeedbackIDs.FEEDBACK_ATTR_NAME) + " lineNo:" + attrs.get(IMarker.LINE_NUMBER) + " parentID=" + attrs.get(FeedbackIDs.FEEDBACK_ATTR_PARENT)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} catch (CoreException e) {
			System.out.println("Error creating Xform marker: " + e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	/**
	 * Note: some Items may be parent groups and not have file, etc. info
	 * 
	 * Do we create markers for children that don't appear yet? think so Or not
	 * until they are expanded? think not
	 * 
	 * Note: need to batch this in a single resource change event, getElements()
	 * is being called on every marker creation.
	 * 
	 * @param itemlist
	 */
	public void createMarkers(List<IFeedbackItem> itemlist, String markerID) {
		boolean dbgTags = false;
		if(itemlist.size()==0) {
			IPreferenceStore pf = Activator.getDefault().getPreferenceStore();
			boolean showDialog = pf.getBoolean(PreferenceConstants.P_SHOW_NO_ITEMS_FOUND_DIALOG);
			System.out.println("showDialog="+showDialog);
			if (showDialog) {
				String title = Messages.MarkerManager_noFeedbackItemsFoundTitle;
				String msg = Messages.MarkerManager_noFeedbackItemsFoundMessage;
				// MessageDialog.openInformation(null, title, msg);
				String togMsg = Messages.MarkerManager_dontShowMeThisAgain;
				MessageDialogWithToggle.openInformation(null, title, msg.toString(), togMsg, false, pf,
						PreferenceConstants.P_SHOW_NO_ITEMS_FOUND_DIALOG);
			}
			return;
		}
		// Will we need this list of the files elsewhere? should we keep it elsewhere?
		Set<String> files=new HashSet<String>();
		for (Iterator iterator = itemlist.iterator(); iterator.hasNext();) {
			IFeedbackItem item = (IFeedbackItem) iterator.next();
			String f1 = item.getFile();
			if( (f1!=null) && (!files.contains(f1))  ) {
				files.add(f1);
				if(traceOn)System.out.println("Source file: "+f1);// print each unique one we find
			}
		} 
		 
		// remove "our" markers on all source files referenced in this file
		IResource res = null;
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			String filename = (String) iterator.next();
			res=getResource(filename);
			try {
				removeMarkers(res, markerID);
			} catch (Exception e) {
				System.out.println("Error deleting markers on file: " + res); //$NON-NLS-1$
				//e.printStackTrace();
			}
		}

		// for root nodes, may have no parent ID
		String parentID = ""; //$NON-NLS-1$
		//IFeedbackItem temp = itemlist.get(0);
		int count = 0;
		Map<String, Object> attrs;

		int size = itemlist.size();
		for (Iterator<IFeedbackItem> iterator = itemlist.iterator(); iterator.hasNext();) {
			IFeedbackItem item = iterator.next();
			String filename = item.getFile();
			String name = item.getName();// +" "+item.getID();
			int lineNo = item.getLineNoStart();
			String desc = item.getDescription();
			String itemID = item.getID();
			parentID = item.getParentID();
			String pathname = ""; // we assume it's fully qualified filename now //$NON-NLS-1$
			if (filename!=null && filename.contains(Path.SEPARATOR + "")) { //$NON-NLS-1$
				IPath path = new Path(filename);
				pathname = path.removeLastSegments(1).toString();
				filename = path.segment(path.segmentCount() - 1);
			}
			attrs = createCommonMarkers(item, itemID, name, parentID, filename, pathname, lineNo, desc);

			IResource resource = getResource(pathname, filename);
			if (resource != null) {
				createMarker(resource, attrs, markerID);
			} else {
				System.out.println("Feedback MarkerManager: Null resource for pathname=" + pathname + "; filename=" + filename
						+ "; no markers created.  item.getFile()=" + item.getFile());
			}
			if (item.hasChildren()) {
				List<IFeedbackItem> kids = item.getChildren();
				for (Iterator iterator2 = kids.iterator(); iterator2.hasNext();) {
					IFeedbackItem kid = (IFeedbackItem) iterator2.next();
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
					// fixme make this recursive so level of hierarchy doesn't  matter
					if (gkids) {
						if (traceOn)
							System.out.println("grandkids"); //$NON-NLS-1$
						List<IFeedbackItem> gkidItems = kid.getChildren();
						for (Object gkid : gkidItems) {
							IFeedbackItem gki = (IFeedbackItem) gkid;
							String gkNamePrefix = Messages.MarkerManager_solution; // HACK
							attrs = createCommonMarkers(gki, gki.getID(), gkNamePrefix + gki.getName(), uidStr, filename,
									pathname, lineNo,  gki.getDescription());
							createMarker(resource, attrs, markerID);
						}
					}
				}
			}
		}
	}
}
