/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.tau.selinst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IFunctionDeclaration;

/**
 * Manages reading, writing and adding/removing commands to selective instrumentation files
 * 
 * @author wspear
 * 
 */
public class Selector {

	/**
	 * Spaces out star (*) characters so function signatures can be read by TAU's selective instrumentation file parser
	 * 
	 * @param signature
	 *            function signature
	 * @return corrected function signature
	 */
	public static String fixStars(String signature) {
		int star = signature.indexOf('*');

		while (star >= 1) {
			if (signature.charAt(star - 1) != '*')
			{
				signature = signature.substring(0, star) + " " + signature.substring(star);
				star++;
			}
			star = signature.indexOf('*', star + 1);
		}
		return signature;
	}

	public static String getFullSigniture(IFunctionDeclaration fun)
	{
		final String returntype = Selector.fixStars(fun.getReturnType());
		String signature;
		try {
			signature = Selector.fixStars(fun.getSignature());
			return returntype + " " + signature + "#";
		} catch (final CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}

	public static String getRoutine(IFunctionDeclaration fun)
	{
		return "routine=\"" + getFullSigniture(fun) + "\"";
	}

	private final LinkedHashSet<String> routInc;
	private final LinkedHashSet<String> routEx;
	private final LinkedHashSet<String> fileInc;

	private final LinkedHashSet<String> fileEx;

	private final LinkedHashSet<String> instSec;

	private final String selString;

	/**
	 * Initializes a new selector object
	 * 
	 * @param path
	 *            path to the selective instrumentation file to be used or created
	 */
	public Selector(String path) {
		routInc = new LinkedHashSet<String>();
		routEx = new LinkedHashSet<String>();
		fileInc = new LinkedHashSet<String>();
		fileEx = new LinkedHashSet<String>();
		instSec = new LinkedHashSet<String>();
		selString = path + File.separator + "tau.selective";
		readSelFile();

	}

	/**
	 * Adds the given list of instrumentation commands to this object's selective instrumentation file
	 * 
	 * @param instlines
	 *            The list of instrument commands to add
	 */
	public void addInst(HashSet<String> instlines) {
		instSec.addAll(instlines);
		writeSelFile();
	}

	/**
	 * Removes the given set of 'instrument' and/or 'exclude' file commands from this Selector's selective instrumentation file
	 * 
	 * @param remfile
	 *            The set of 'instrument' and/or 'eclude' file commands to be removed
	 */
	public void clearFile(HashSet<String> remfile) {
		fileInc.removeAll(remfile);
		fileEx.removeAll(remfile);

		writeSelFile();
	}

	/**
	 * Removes the indicated selective instrumentation commands from the
	 * 'selective instrumentation' section from this Selector's selective instrumentation file
	 * 
	 * @param remlines
	 *            The set of selective instrumentation commands to be removed
	 */
	public void clearGenInst(HashSet<String> remlines) {
		try {
			final Iterator<String> remit = remlines.iterator();
			Iterator<String> removal;
			final HashSet<String> removethese = new HashSet<String>();
			String remtem = "";
			String remcan = "";
			while (remit.hasNext())
			{
				remtem = (remit.next());
				removal = instSec.iterator();
				while (removal.hasNext())
				{
					remcan = removal.next();
					if (remcan.indexOf(remtem) == 0)
					{
						removethese.add(remcan);
					}
				}
			}
			instSec.removeAll(removethese);
			writeSelFile();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void clearInstrumentSection(HashSet<String> elementNames)
	{
		final Iterator<String> elementIt = elementNames.iterator();
		Iterator<String> selectiveIt;
		final HashSet<String> toRemove = new HashSet<String>();
		String curElement = "";
		String curSelLine = "";
		while (elementIt.hasNext())
		{
			curElement = elementIt.next();
			selectiveIt = instSec.iterator();
			while (selectiveIt.hasNext())
			{
				curSelLine = selectiveIt.next();
				if (curSelLine.indexOf(curElement) >= 0)
				{
					toRemove.add(curSelLine);
				}
			}
		}
		instSec.removeAll(toRemove);
		writeSelFile();
	}

	/**
	 * Removes the given set of 'instrument' and/or 'exclude' routine commands from this Selector's selective instrumentation file
	 * 
	 * @param remrouts
	 *            The set of 'instrument' and/or 'eclude' routine commands to be removed
	 */
	public void clearRout(HashSet<String> remrouts) {
		routInc.removeAll(remrouts);
		routEx.removeAll(remrouts);

		writeSelFile();
	}

	/**
	 * Adds the given set of files to the exclude list of this object's selective instrumentation
	 * file and removes them from its include list if they are present
	 * 
	 * @param exfiles
	 *            The set of routines to be excluded
	 */
	public void excludeFile(HashSet<String> exfiles) {
		fileEx.addAll(exfiles);
		fileInc.removeAll(exfiles);
		writeSelFile();
	}

	/**
	 * Adds the given set of routines to the exclude list of this object's selective instrumentation
	 * file and removes them from its include list if they are present
	 * 
	 * @param exrouts
	 *            The set of routines to be excluded
	 */
	public void excludeRout(HashSet<String> exrouts) {
		routEx.addAll(exrouts);
		routInc.removeAll(exrouts);
		writeSelFile();
	}

	/**
	 * Adds the given set of files to the include list of this object's selective instrumentation
	 * file and removes them from its exclude list if they are present
	 * 
	 * @param incfiles
	 *            The set of routines to be included
	 */
	public void includeFile(HashSet<String> incfiles) {
		fileInc.addAll(incfiles);
		fileEx.removeAll(incfiles);
		writeSelFile();
	}

	/**
	 * Adds the given set of routines to the include list of this object's selective instrumentation
	 * file and removes them from its exclude list if they are present
	 * 
	 * @param incrouts
	 *            The set of routines to be included
	 */
	public void includeRout(HashSet<String> incrouts) {
		routInc.addAll(incrouts);
		routEx.removeAll(incrouts);
		writeSelFile();
	}

	/**
	 * Reads a complete selective instrumentation file, with individual selection types being placed in their respective sets
	 * 
	 */
	private void readSelFile()
	{
		try
		{
			// WorkspaceDescription.
			final File selfile = new File(selString);

			if (!selfile.exists())
			{
				return;
			}
			final BufferedReader in = new BufferedReader(new FileReader(selfile));
			String ourline = in.readLine();
			while (ourline != null)
			{
				if (ourline.equals("BEGIN_EXCLUDE_LIST"))
				{
					ourline = in.readLine();
					while (!ourline.equals("END_EXCLUDE_LIST") && !ourline.equals(null))
					{
						routEx.add(ourline);
						ourline = in.readLine();
					}
				}

				if (ourline.equals("BEGIN_INCLUDE_LIST"))
				{
					ourline = in.readLine();
					while (!ourline.equals("END_INCLUDE_LIST") && !ourline.equals(null))
					{
						routInc.add(ourline);
						ourline = in.readLine();
					}
				}

				if (ourline.equals("BEGIN_FILE_INCLUDE_LIST"))
				{
					ourline = in.readLine();
					while (!ourline.equals("END_FILE_INCLUDE_LIST") && !ourline.equals(null))
					{
						fileInc.add(ourline);
						ourline = in.readLine();
					}
				}

				if (ourline.equals("BEGIN_FILE_EXCLUDE_LIST"))
				{
					ourline = in.readLine();
					while (!ourline.equals("END_FILE_EXCLUDE_LIST") && !ourline.equals(null))
					{
						fileEx.add(ourline);
						ourline = in.readLine();
					}
				}

				if (ourline.equals("BEGIN_INSTRUMENT_SECTION"))
				{
					ourline = in.readLine();
					while (!ourline.equals("END_INSTRUMENT_SECTION") && !ourline.equals(null))
					{
						instSec.add(ourline);
						ourline = in.readLine();
					}
				}
				ourline = in.readLine();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the given set of 'instrument' commands from this Selector's selective instrumentation file
	 * 
	 * @param remlines
	 *            The set of 'instrument' commands to be removed
	 */
	public void remInst(HashSet<String> remlines) {

		instSec.removeAll(remlines);
		writeSelFile();

	}

	/**
	 * Writes a complete selective instrumentation file, with section content provided by the individual sets provided
	 * 
	 */
	private void writeSelFile()
	{
		try
		{
			final BufferedWriter out = new BufferedWriter(new FileWriter(selString));

			out.write("#Generated by the TAU PTP plugin\n");
			if (routEx.size() > 0)
			{
				out.write("BEGIN_EXCLUDE_LIST\n");
				final Iterator<String> routExIt = routEx.iterator();
				while (routExIt.hasNext()) {
					out.write(routExIt.next() + "\n");
				}
				out.write("END_EXCLUDE_LIST\n");
			}

			if (routInc.size() > 0)
			{
				out.write("BEGIN_INCLUDE_LIST\n");
				final Iterator<String> routIncIt = routInc.iterator();

				while (routIncIt.hasNext()) {
					out.write(routIncIt.next() + "\n");
				}
				out.write("END_INCLUDE_LIST\n");
			}

			if (fileEx.size() > 0)
			{
				out.write("BEGIN_FILE_EXCLUDE_LIST\n");
				final Iterator<String> fileExIt = fileEx.iterator();
				while (fileExIt.hasNext()) {
					out.write(fileExIt.next() + "\n");
				}
				out.write("END_FILE_EXCLUDE_LIST\n");
			}

			if (fileInc.size() > 0)
			{
				out.write("BEGIN_FILE_INCLUDE_LIST\n");
				final Iterator<String> fileIncIt = fileInc.iterator();
				while (fileIncIt.hasNext()) {
					out.write(fileIncIt.next() + "\n");
				}
				out.write("END_FILE_INCLUDE_LIST\n");
			}

			if (instSec.size() > 0)
			{
				out.write("BEGIN_INSTRUMENT_SECTION\n");
				final Iterator<String> instSecIt = instSec.iterator();
				while (instSecIt.hasNext()) {
					out.write(instSecIt.next() + "\n");
				}
				out.write("END_INSTRUMENT_SECTION\n");
			}
			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
