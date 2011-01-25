package org.eclipse.ptp.core.util;

import java.io.File;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.utils.core.BitSetIterable;

/**
 * @since 4.0
 */
public class ProcessOutput {
	private boolean lastOutputEndedInLineFeed = true;
	private final BitSet previousOutputProcesses = new BitSet();
	private String outputDirPath = null;
	private final OutputTextFile outputFile;
	private int storeLines = 0;

	public ProcessOutput(IPJob job) {

		setOutputStore();

		/*
		 * Derive a unique name for the output file
		 */
		String name = job.getResourceManager().getName() + "_" + job.getName(); //$NON-NLS-1$

		outputFile = new OutputTextFile(name, ProcessAttributes.getStdoutAttributeDefinition().getId(), outputDirPath, storeLines);

	}

	/**
	 * @param output
	 * @param processJobRanks
	 */
	public void addOutput(String output, BitSet processJobRanks) {
		if (processJobRanks.isEmpty()) {
			return;
		}
		if (output.length() == 0) {
			return;
		}

		final StringBuilder prefix = new StringBuilder("["); //$NON-NLS-1$
		for (Integer rank : new BitSetIterable(processJobRanks)) {
			prefix.append(rank + ","); //$NON-NLS-1$
		}
		prefix.deleteCharAt(prefix.length() - 1);
		prefix.append("] "); //$NON-NLS-1$

		final List<String> lines = Arrays.asList(output.split("\n")); //$NON-NLS-1$
		final StringBuilder prefixedOutput = new StringBuilder();

		Iterator<String> lineIter = lines.iterator();

		// if the last output did not end in a linefeed
		// then don't start a new line if the same processes
		// are writing again, otherwise terminate the last
		// output by starting with a linefeed

		if (!lastOutputEndedInLineFeed) {
			if (previousOutputProcesses.equals(processJobRanks)) {
				String firstLine = lineIter.next();
				prefixedOutput.append(firstLine + "\n"); //$NON-NLS-1$
			} else {
				prefixedOutput.append("\n"); //$NON-NLS-1$
			}
		}

		while (lineIter.hasNext()) {
			String line = lineIter.next();
			prefixedOutput.append(prefix.toString() + line + "\n"); //$NON-NLS-1$
		}

		// if the output doesn't end with a linefeed
		// delete the last linefeed of the prefixed output
		if (!output.endsWith("\n")) { //$NON-NLS-1$
			prefixedOutput.deleteCharAt(prefixedOutput.length() - 1);
		}

		// write the prefixed output to the file
		outputFile.write(prefixedOutput.toString());

		previousOutputProcesses.clear();
		previousOutputProcesses.or(processJobRanks);
		lastOutputEndedInLineFeed = output.endsWith("\n"); //$NON-NLS-1$
	}

	public void delete() {
		outputFile.delete();
	}

	public String getSavedOutput(int jobRank) {
		final String savedOutput = outputFile.getContents();
		String[] jobLines = savedOutput.split("\n"); //$NON-NLS-1$

		Pattern prefixPattern = Pattern.compile("^\\[([0-9]+,)*" + jobRank + "(,[0-9]+)*\\] (.*)"); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuilder sb = new StringBuilder();
		for (String jobLine : jobLines) {
			Matcher matcher = prefixPattern.matcher(jobLine);
			boolean matched = matcher.matches();
			if (matched) {
				sb.append(matcher.group(3) + "\n"); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}

	/**
	 * Create a local cache for process output.
	 */
	private void setOutputStore() {
		outputDirPath = Preferences.getString(PTPCorePlugin.getUniqueIdentifier(), PreferenceConstants.PREFS_OUTPUT_DIR);
		storeLines = Preferences.getInt(PTPCorePlugin.getUniqueIdentifier(), PreferenceConstants.PREFS_STORE_LINES);
		if (outputDirPath == null || outputDirPath.length() == 0) {
			outputDirPath = ResourcesPlugin.getWorkspace().getRoot().getLocation()
					.append(PreferenceConstants.DEFAULT_OUTPUT_DIR_NAME).toOSString();
		}
		if (storeLines == 0) {
			storeLines = PreferenceConstants.DEFAULT_STORE_LINES;
		}
		File outputDirectory = new File(outputDirPath);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
	}
}
