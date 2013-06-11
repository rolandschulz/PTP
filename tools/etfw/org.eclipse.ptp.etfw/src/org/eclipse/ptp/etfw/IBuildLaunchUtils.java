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

	/**
	 * This locates the directory containing the given tool.
	 * 
	 * @param The
	 *            name of the tool whose directory is being located
	 * @return The uri of the location of the tool's containing directory, or null if it is not found or if the architecture is windows
	 * 
	 */
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

	/**
	 * Convenience method to return the the given file as located by the IBuildLaunch object's file store
	 * @param path
	 * @return
	 */
	public IFileStore getFile(String path);

	/**
	 * Given a tool's ID, returns the path to that tool's bin directory if already known and stored locally, otherwise returns the
	 * empty string
	 * 
	 * @param toolID
	 * @return
	 */
	public String getToolPath(String toolID);

	/**
	 * Returns the working directory associated with this object's remote connection, if any.
	 * @return
	 */
	public String getWorkingDirectory();

	/**
	 * Returns true if this object is configured with a remote connection, false if it is using a local connection.
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

	/**
	 * 
	 * @param tool The command to be run, including arguments
	 * @param env A list of environment variables to associate with the tool
	 * @param directory The directory where the tool is invoked
	 * @return byte array representing the output of the program executed
	 */
	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory);

	/**
	 * @since 7.0

	 * @param showErr set to true if the stderr from the program should be included in the output, otherwise false
	 * @return byte array representing the output of the program executed
	 */
	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory, boolean showErr);

	/**
	 * Attempt to launch a UI based application.
	 * @param tool The command to be run, including arguments
	 * @param env A list of environment variables to associate with the tool
	 * @param directory The directory where the tool is invoked
	 */
	public void runVis(List<String> tool, Map<String, String> env, String directory);

	/**
	 * Associate the filesystem location of this tool's executable with its group
	 * @param tool The object containing the tool to be located
	 * @param force if true proceed with the check and association even if there is already a path associated with the group
	 */
	public void verifyRequestToolPath(ExternalToolProcess tool, boolean force);

}
