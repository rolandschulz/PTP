/**
 * 
 */
package org.eclipse.ptp.pldt.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * @author beth
 * 
 */
public abstract class PldtBaseTestFramework extends BaseTestFramework {
	private static HashMap<String, ArrayList<Integer>> lineMaps = new HashMap<String, ArrayList<Integer>>();

	/**
	 * Put the file into the test project
	 */
	protected IFile importFile(String srcDir, String filename) throws Exception {
		// project.getProject().getFile(filename).delete(true, new
		// NullProgressMonitor());
		IFile result = super.importFile(filename, readTestFile(srcDir, filename));
		// project.refreshLocal(IResource.DEPTH_INFINITE, new
		// NullProgressMonitor());
		return result;
	}

	protected String readTestFile(String srcDir, String filename) throws IOException, URISyntaxException {
		ArrayList<Integer> lineMap = new ArrayList<Integer>(50);
		lineMaps.put(filename, lineMap);
		lineMap.add(0); // Offset of line 1
		return readStream(lineMap, getClass().getResourceAsStream("/" + srcDir + "/" + filename));
	}

	protected String readStream(ArrayList<Integer> lineMap, InputStream inputStream) throws IOException {
		StringBuffer sb = new StringBuffer(4096);
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		for (int offset = 0, ch = in.read(); ch >= 0; ch = in.read()) {
			sb.append((char) ch);
			offset++;

			if (ch == '\n' && lineMap != null) {
				// System.out.println("Line " + (lineMap.size()+1) +
				// " starts at offset " + offset);
				lineMap.add(offset);
			}
		}
		in.close();
		return sb.toString();
	}

	protected String readStream(InputStream inputStream) throws IOException {
		return readStream(null, inputStream);
	}

	protected String readWorkspaceFile(String filename) throws IOException, CoreException {
		return readStream(project.getFile(filename).getContents());
	}

	/**
	 * @param filename
	 * @param line
	 *            line number, starting at 1
	 * @param col
	 *            column number, starting at 1
	 */
	protected int getLineColOffset(String filename, int line, int col) {
		return lineMaps.get(filename).get(line - 1) + (col - 1);
	}
}
