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
package org.eclipse.ptp.etfw.tau.papiselect.papic;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

/**
 * Logic for providing lists of available PAPI counters
 * 
 * @author wspear
 * 
 */
public class PapiSelect {

	public static final int PRESET = 0;
	public static final int NATIVE = 1;

	private LinkedHashSet<String> avCounters = null;
	private final Vector<String> counterNames = new Vector<String>(256);
	private final Vector<String> counterDefs = new Vector<String>(256);
	private String location = "";
	private int countType = 0;

	/**
	 * Creates a PapiSelect object that will use the utilities at the given directory to determing
	 * PAPI counter availability
	 * 
	 * @param papiLocation
	 *            Directory containing PAPI utilities
	 * @param papiCountType
	 *            Determines if counters requested are preset or native type
	 */
	public PapiSelect(String papiLocation, int papiCountType) {
		location = papiLocation;
		if (papiCountType == PRESET) {
			findPresetAvail();
		} else
		{
			findNativeAvail();
			countType = NATIVE;
		}
	}

	/**
	 * Returns all (native) counters available on the system
	 * */
	private void findNativeAvail() {
		final String papi_avail = location + File.separator + "papi_native_avail";
		String s = null;

		final LinkedHashSet<String> avail = new LinkedHashSet<String>();
		String holdcounter = null;
		try {
			final Process p = Runtime.getRuntime().exec(papi_avail, null, null);

			final BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((s = stdInput.readLine()) != null)
			{
				if (s.indexOf("   0x") > 0)
				{
					holdcounter = s.substring(0, s.indexOf(" "));
					avail.add(holdcounter);
					counterNames.add(holdcounter);
					String defCounter = s.substring(s.lastIndexOf("   ") + 3);
					int lendex = 55;
					int freespace = 0;
					while (lendex < defCounter.length()) {
						freespace = defCounter.lastIndexOf(' ', lendex - 1);
						defCounter = defCounter.substring(0, freespace + 1) + '\n' + defCounter.substring(freespace + 1);
						lendex += 55;
					}
					counterDefs.add(defCounter);
				}
			}
			boolean fault = false;
			while ((s = stdErr.readLine()) != null)
			{
				fault = true;
			}
			if (fault) {
				p.destroy();
				avCounters = null;
			}

			p.destroy();
		} catch (final Exception e) {
			System.out.println(e);
		}
		avCounters = avail;
	}

	/**
	 * Returns all (preset) counters available on the system
	 * */
	private void findPresetAvail() {
		final String papi_avail = location + File.separator + "papi_avail";
		String s = null;

		final LinkedHashSet<String> avail = new LinkedHashSet<String>();
		String holdcounter = null;

		try {
			final Process p = Runtime.getRuntime().exec(papi_avail, null, null);

			final BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// read the output from the command
			while ((s = stdInput.readLine()) != null)
			{
				if (s.indexOf("PAPI_") == 0 && s.indexOf("\tYes\t") > 0)
				{
					holdcounter = s.substring(0, s.indexOf("\t"));
					avail.add(holdcounter);
					counterNames.add(holdcounter);
					String defCounter = s.substring(s.lastIndexOf("\t") + 1);
					int lendex = 55;
					int freespace = 0;
					while (lendex < defCounter.length()) {
						freespace = defCounter.lastIndexOf(' ', lendex - 1);
						defCounter = defCounter.substring(0, freespace + 1) + '\n' + defCounter.substring(freespace + 1);
						lendex += 55;
					}
					counterDefs.add(defCounter);
				}
			}
			boolean fault = false;
			while ((s = stdErr.readLine()) != null)
			{
				fault = true;
			}
			if (fault) {
				p.destroy();
				avCounters = null;
			}

			p.destroy();
		} catch (final Exception e) {
			System.out.println(e);
		}
		avCounters = avail;
	}

	/**
	 * Gets the list of available counters
	 * 
	 * @return available counters
	 */
	public LinkedHashSet<String> getAvail() {
		return avCounters;
	}

	/**
	 * Gets the list of counter definitions in the same order as returned by getCounterNames
	 * 
	 * @return counter definitions
	 */
	public Vector<String> getCounterDefs() {
		return counterDefs;
	}

	/**
	 * Gets the list of counter names in the same order as returned by getCounterDefs
	 * 
	 * @return counter names
	 */
	public Vector<String> getCounterNames() {
		return counterNames;
	}

	/**
	 * Given a list of already selected and already rejected counters, returns all available remaining counters
	 * */
	public LinkedHashSet<Object> getGrey(Object[] checked, Object[] greyed) {
		final LinkedHashSet<Object> active = new LinkedHashSet<Object>();
		final LinkedHashSet<Object> greyset = new LinkedHashSet<Object>();
		final LinkedHashSet<String> notgrey = new LinkedHashSet<String>(avCounters);

		if (checked.length > 0) {
			active.addAll(Arrays.asList(checked));
		}
		if (greyed != null && greyed.length > 0) {
			active.removeAll(Arrays.asList(greyed));
		}
		notgrey.removeAll(active);
		if (greyed != null && greyed.length > 0) {
			notgrey.removeAll(Arrays.asList(greyed));
		}

		greyset.addAll(getRejects(active));

		if (greyed != null && greyed.length > 0)
		{
			greyset.add(Arrays.asList(greyed));
		}
		return greyset;
	}

	/**
	 * Returns the set of counters rejected given the selected set
	 * */
	private LinkedHashSet<String> getRejects(LinkedHashSet<Object> selected)
	{
		int entryIndex = 14;
		int entryLines = 1;

		if (countType == 1)
		{
			entryIndex = 13;
			entryLines = 5;
		}

		String counterString = "PRESET";
		if (countType != 0) {
			counterString = "NATIVE";
		}

		String papi_event_chooser = location + File.separator + "papi_event_chooser " + counterString;
		if (selected != null && selected.size() > 0)
		{
			final Iterator<Object> itsel = selected.iterator();
			while (itsel.hasNext())
			{
				papi_event_chooser += " " + (String) itsel.next();
			}
		}
		else
		{
			return new LinkedHashSet<String>(1);
		}

		String s = null;
		final LinkedHashSet<String> result = new LinkedHashSet<String>(avCounters);
		result.removeAll(selected);
		try {
			final Process p = Runtime.getRuntime().exec(papi_event_chooser, null, null);
			final BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			int countLines = 0;
			int tabDex = 0;
			while ((s = stdInput.readLine()) != null)
			{
				countLines++;
				if (countLines >= entryIndex && (countLines - entryIndex) % entryLines == 0)
				{
					tabDex = s.indexOf("\t");
					if (tabDex == -1)
					{
						if (countLines == entryIndex)
						{

						}
						countLines = 0;
					}
					else
					{
						result.remove(s.substring(0, tabDex));
					}
				}
			}

			while ((s = stdErr.readLine()) != null)
			{
			}
			p.destroy();
		} catch (final Exception e) {
			System.out.println(e);
		}

		return result;
	}
}