/**********************************************************************
 * Copyright (c) 2013 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.perfsuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackParser;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;
import org.eclipse.ptp.internal.etfw.feedback.perfsuite.util.ValueSortedMap;
import org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml.PS_HwpcProfileReport;
import org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml.PS_MultiHwpcProfileReport;
import org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml.PS_Report;
import org.xml.sax.SAXException;

/**
 * PerfSuite parser to return items for the ETFw Feedback view
 * 
 * @author Rui Liu
 * 
 */

public class PerfSuiteFeedbackParser extends AbstractFeedbackParser {
	private final boolean traceOn = false;
	private List<IFeedbackItem> items = new ArrayList<IFeedbackItem>();

	public List<IFeedbackItem> getFeedbackItems(IFile ifile) {
		if (traceOn) {
			System.out.println("Reading xml file: " + ifile.getLocation());
		}

		items = new ArrayList<IFeedbackItem>();
		try {
			items = parse(ifile);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return items;
	}

	/**
	 * @deprecated use getFeedbackItems(IFile) instead
	 */
	@Deprecated
	public List<IFeedbackItem> getFeedbackItems(File file) {
		// this is probably twisted around, too much converting back and forth
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(file.getAbsolutePath());
		IFile ifile = workspace.getRoot().getFileForLocation(location);
		List<IFeedbackItem> items = getFeedbackItems(ifile);
		return items;
	}

	/**
	 * Marker ID for markers added by this feedback parser.
	 * For now they are all the same, using plugin id.
	 */
	public String getMarkerID() {
		return Activator.MARKER_ID;
	}

	public String getViewID() {
		return Activator.VIEW_ID;
	}

	/**
	 * Populate objects from the xml file given
	 * 
	 * @param xmlfile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public List<IFeedbackItem> parse(IFile ifile) throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {

		List<IFeedbackItem> items = new ArrayList<IFeedbackItem>();

		/*
		 * Tried ifile.getFullPath().toPortableString(), Path.ROOT,
		 * ifile.getName(), getFullPath, getLocation, getLocationURI, getRawLocation, getRawLocationURI,
		 * project.getName(), getFullPath, .. , getFolder(ifile.getName()).
		 */

		IProject project = ifile.getProject();
		String remoteAbsPath = project.getLocationURI().getPath();
		String projectFullPath = project.getFullPath().toString();

		if (traceOn) {
			System.out.println("remoteAbsPath = '" + remoteAbsPath + "'");
			System.out.println("projectFullPath = '" + projectFullPath + "'");
		}

		InputStream ais = null;
		try {
			ais = ifile.getContents();
		} catch (CoreException ce) {
			System.out.println("Caught CoreException: " + ce.getMessage());
		}

		/*
		 * Used to use the file name. Changed to use InputStream to support remote projects as well.
		 * 
		 * String filename = ifile.getLocation().toString();
		 * PS_Report report0 = PS_Report.newInstance (filename, false);
		 */
		PS_Report report0 = PS_Report.newInstance(ais, false);
		if (!(report0 instanceof PS_MultiHwpcProfileReport)) {
			System.err.println("Error: The XML is not a PerfSuite multi-HWPC profile report.");
			System.exit(1);
		}

		// Contains the files that are not in the project work space.
		// The Default c-tor values of capacity: 16, load factor: 0.75 seem OK.
		HashSet<String> skipFileSet = new HashSet<String>();

		PS_MultiHwpcProfileReport report = (PS_MultiHwpcProfileReport) report0;
		for (PS_HwpcProfileReport rep : report.getReports()) {
			// Note: Using only the first profile if there are multiple -- this is rare: getProfiles().get(0)
			int numProfiles = rep.getProfiles().size();
			if (numProfiles > 1) {
				System.out
						.println("Warning: The report contains multiple profiles, using only the first one.");
			}
			Map<String, Map<String, Map<String, Map<Long, Long>>>> nestedMap = rep.getProfiles().get(0).getNestedModuleMap();
			// The nested parts for module, file, func, line.
			for (Map.Entry<String, Map<String, Map<String, Map<Long, Long>>>> mentry : ValueSortedMap.getNestedMapDesc(nestedMap)
					.entrySet()) {
				String mName = mentry.getKey();
				Map<String, Map<String, Map<Long, Long>>> fileMap = mentry.getValue();
				for (Map.Entry<String, Map<String, Map<Long, Long>>> fientry : ValueSortedMap.getNestedMapDesc(fileMap).entrySet()) {
					String fiName = fientry.getKey();
					Map<String, Map<Long, Long>> funcMap = fientry.getValue();
					for (Map.Entry<String, Map<Long, Long>> fuentry : ValueSortedMap.getNestedMapDesc(funcMap).entrySet()) {
						String fuName = fuentry.getKey();
						Map<Long, Long> lineMap = fuentry.getValue();
						for (Map.Entry<Long, Long> lentry : ValueSortedMap.getMapDesc(lineMap).entrySet()) {
							long lineno = lentry.getKey();
							Long numSamples = lentry.getValue();
							String parentID = rep.getHostName() + "-" + "PID_"
									+ rep.getExecutionInfo().getPid() + "-"
									+ "thread_" + rep.getExecutionInfo().getThreadId();

							// Check whether the file with the given name (fiName) is in the project's workspace.
							// A bit brute force, but it does not need the use of the IResource's.
							// Tested to be working for both local and remote projects.
							if (!fiName.startsWith(remoteAbsPath)) {
								// Use a hash set to print out the msg only once for each file.
								if (!skipFileSet.contains(fiName)) {
									System.out.println("Plugin: org.eclipse.ptp.etfw.feedback.perfsuite: Project '"
											+ project + "', file '" + fiName
											+ "' does not exist in the work space, skipping...");
									skipFileSet.add(fiName);
								}
								continue;
							}

							String fiNameInWS = fiName.replace(remoteAbsPath, projectFullPath);
							if (traceOn) {
								System.out.println("fiName = '" + fiName + "'");
								System.out.println("mName = '" + mName + "'");
								System.out.println("fiNameInWS = '" + fiNameInWS + "'");
							}

							if (!fiName.equals("??")) {
								try {
									PerfSuiteFeedbackItem item = new PerfSuiteFeedbackItem(mName, parentID, fuName, fiNameInWS,
											lineno, Long.toString(numSamples));
									items.add(item);
								} catch (Exception e) {
									System.out.println("PerfSuiteFeedbackParser: Exception creating item.  Likely the file '"
											+ fiName +
											"' is not in the workspace.  Exception message: " + e.getMessage());
								}
							}
						}
					}
				}
			}
		}

		if (traceOn) {
			System.out.println("SFP found items: " + items.size() + " elements");
		}
		return items;
	}
	/**
	 * For testing only:
	 * try to create an IFile/IResource from the info we have
	 * 
	 * @param fname
	 */
	/*
	 * private void tryCreateFile(String fname, IFile xmlFile) {
	 * System.out.println("xmlFile: "+xmlFile);
	 * IProject proj=xmlFile.getProject();
	 * IResource foundRes=proj.findMember(fname);
	 * boolean exists=foundRes.exists();
	 * IPath path = foundRes.getFullPath();
	 * String s = path.toString();
	 * String s2=path.toPortableString();
	 * String s3=path.toOSString();
	 * IResource recreatedRes=getResourceInProject(proj,fname);
	 * exists=recreatedRes.exists();
	 * 
	 * }
	 */

}
