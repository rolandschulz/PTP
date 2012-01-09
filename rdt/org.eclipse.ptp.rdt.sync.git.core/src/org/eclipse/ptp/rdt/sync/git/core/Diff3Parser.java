package org.eclipse.ptp.rdt.sync.git.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EnumSet;

// A utility class for parsing a merged file in diff3 format
// This class is generally useful and contains no code specific to synchronized projects or git
public final class Diff3Parser {
	private enum ParseTarget {
		LEFT, RIGHT, ANC
	}

	// Make non-instantiable
	private Diff3Parser() {
	}

	/**
	 * Parse the given file and return the three individual parts of the merge (left, right, and ancestor).
	 * Note that for a file without ancestor data, the parsing still works, but the ancestor string will be empty.
	 *
	 * @param file
	 * 			file to parse
	 * @return A string array containing the contents of the left, right, and ancestor of the merge, respectively.
	 * @throws IOException on problems reading the file
	 */
	public static String[] parseFile(File file) throws IOException {
		String left = ""; //$NON-NLS-1$
		String right = ""; //$NON-NLS-1$
		String ancestor = ""; //$NON-NLS-1$
		FileInputStream fis = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(dis));
		
		EnumSet<ParseTarget> filesToRead = EnumSet.allOf(ParseTarget.class);
		String fileLine;
		try {
			while ((fileLine = fileReader.readLine()) != null) {
				fileLine = fileLine.concat("\n"); //$NON-NLS-1$
				if (fileLine.startsWith("<<<<<<<")) { //$NON-NLS-1$
					filesToRead.clear();
					filesToRead.add(ParseTarget.LEFT);
				} else if (fileLine.startsWith("|||||||")) { //$NON-NLS-1$
					filesToRead.clear();
					filesToRead.add(ParseTarget.ANC);
				} else if (fileLine.startsWith("=======")) { //$NON-NLS-1$
					filesToRead.clear();
					filesToRead.add(ParseTarget.RIGHT);
				} else if (fileLine.startsWith(">>>>>>>")) { //$NON-NLS-1$
					filesToRead.addAll(Arrays.asList(ParseTarget.values()));
				} else {
					for (ParseTarget fileToRead : filesToRead) {
						switch (fileToRead) {
						case LEFT:
							left = left.concat(fileLine);
							break;
						case RIGHT:
							right = right.concat(fileLine);
							break;
						case ANC:
							ancestor = ancestor.concat(fileLine);
							break;
						}
					}
				}
			}
		} finally {
			fileReader.close();
		}
		
		return new String[]{left, right, ancestor};
	}
}
