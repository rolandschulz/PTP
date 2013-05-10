package org.eclipse.ptp.etfw;

//import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;

//import org.eclipse.core.filesystem.IFileStore;

/**
 * @since 5.0
 */
public interface IBuildLaunchUtils {

	/**
	 * Given a tool's name, ask the user for the location of the tool
	 * */
	public String askToolPath(String archpath, String toolName);

	/**
	 * Given a string as a starting point, this asks the user for the location of a tool's directory
	 * */
	public String askToolPath(String archpath, String toolText, String toolMessage);

	public String checkToolEnvPath(String toolname);

	/**
	 * Returns the directory containing the tool's executable file. Prompts the user for the location if it is not found. Returns
	 * the empty string if no selection is made
	 * 
	 * @param toolfind
	 *            The name of the executable being sought
	 * @param suggPath
	 *            The suggested path upon which to focus the directory locator window
	 * @param toolName
	 *            The name of the tool used when prompting the user for its location
	 * @param selshell
	 *            The shell in which to launch the directory locator window
	 * @return
	 */
	public String findToolBinPath(String toolfind, String suggPath, String toolName);

	/**
	 * Returns the directory containing the tool's executable file. Prompts the user for the location if it is not found. Returns
	 * the empty string if no selection is made
	 * 
	 * @param toolfind
	 *            The name of the executable being sought
	 * @param suggPath
	 *            The suggested path upon which to focus the directory locator window
	 * @param queryText
	 *            The text asking the user to search for the binary
	 * @param queryMessage
	 *            The text providing more detail on the search task
	 * @param selshell
	 *            The shell in which to launch the directory locator window
	 * @return
	 */
	public String findToolBinPath(String toolfind, String suggPath, String queryText, String queryMessage);

	/**
	 * Iterates through an array of tools, populating the preference store with their binary directory locations
	 * 
	 * @param tools
	 *            The array of tools to be checked
	 * @param force
	 *            If true existing values will be overridden.
	 */
	public void getAllToolPaths(ExternalToolProcess[] tools, boolean force);

	public IFileStore getFile(String path);

	/**
	 * Given a tool's ID, returns the path to that tool's bin directory if already known and stored locally, otherwise returns the
	 * empty string
	 * 
	 * @param toolID
	 * @return
	 */
	public String getToolPath(String toolID);

	public String getWorkingDirectory();

	/**
	 * @since 7.0
	 */
	public boolean isRemote();

	/**
	 * Launches a command on the local system.
	 * 
	 * @param tool
	 *            The command to be run
	 * @param env
	 *            A list of environment variables to associate with the tool
	 * @param directory
	 *            The directory where the tool is invoked
	 */
	public boolean runTool(List<String> tool, Map<String, String> env, String directory);

	/**
	 * 
	 * @param tool
	 *            The command to be run
	 * @param env
	 *            A list of environment variables to associate with the tool
	 * @param directory
	 *            The directory where the tool is invoked
	 * @param output
	 *            The path to the file where the output should be written
	 * @return
	 */
	public boolean runTool(List<String> tool, Map<String, String> env, String directory, String output);

	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory);

	/**
	 * @since 7.0
	 */
	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory, boolean showErr);

	public void runVis(List<String> tool, Map<String, String> env, String directory);

	public void verifyRequestToolPath(ExternalToolProcess tool, boolean force);

}
