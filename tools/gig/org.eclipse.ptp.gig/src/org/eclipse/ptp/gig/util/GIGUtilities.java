/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.log.GkleeLog;
import org.eclipse.ptp.gig.log.LogException;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ptp.gig.preferences.GIGPreferencePage;
import org.eclipse.ptp.gig.views.GIGView;
import org.eclipse.ptp.gig.views.ServerTreeItem;
import org.eclipse.ptp.gig.views.ServerView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

/*
 * Contains most of the logical and generic utility code of the plug-in
 */
public class GIGUtilities {

	/*
	 * Indicates what state the job is in to prevent multiple jobs from being accidentally started.
	 */
	public enum JobState {
		None,
		Running,
		Canceled
	}

	// The current non-GUI job being run
	private static Job job;
	// lock is needed to protect critical sections
	private static Lock jobsLock = new ReentrantLock();
	// This keeps track of what part of the life of a job we are in
	private static volatile JobState jobState = JobState.None;

	// the port to communicate with the server
	static final int port = 8883;
	/*
	 * The socket, outputStream and inputStream are all together. Used for communication to server
	 */
	private static Socket socket;
	private static OutputStream socketOutputStream;
	private static InputStream socketInputStream;
	// Used for cancelling jobs
	private static IProgressMonitor progressMonitor;

	/*
	 * Builds the paths needed to run klee and gklee based on values from the Preferences
	 */
	private static void buildEnvPath(Map<String, String> env) {
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		env.put(GIGPreferencePage.GKLEE_HOME, preferenceStore.getString(GIGPreferencePage.GKLEE_HOME));
		env.put(GIGPreferencePage.FLA_KLEE_HOME_DIR, preferenceStore.getString(GIGPreferencePage.FLA_KLEE_HOME_DIR));
		final StringBuilder sBuilder = new StringBuilder(env.get("PATH") + ':'); //$NON-NLS-1$
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.GKLEE_DEBUG_PLUS_ASSERTS_BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.LLVM_DEBUG_PLUS_ASSERTS_BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.LLVM_GCC_LINUX_BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.ADDITIONAL_PATH));
		final String path = sBuilder.toString();
		env.put("PATH", path); //$NON-NLS-1$
	}

	/*
	 * Gklee leaves a few files and folders when it runs, and we don't need to keep those around. This detects and deletes them.
	 */
	private static void cleanUpGklee(IContainer gigFolder) throws CoreException {
		final IResource[] resources = gigFolder.members();
		for (final IResource res : resources) {
			if (res.getName().startsWith("klee-")) { //$NON-NLS-1$
				res.delete(true, progressMonitor);
			}
		}
	}

	/*
	 * Closes the connection.
	 */
	private static void closeConnection() throws IOException {
		/*
		 * the int received should always be 0. This is actually to prevent a race. We ensure that the server is done with the
		 * current operation before requesting an additional operation. For example, we might send over a file and ask for updated
		 * server file structure information; we want the first command completed before a second command is sent.
		 */
		recvInt();
		socketOutputStream.close();
		socketInputStream.close();
		socket.close();
		socketOutputStream = null;
		socketInputStream = null;
		socket = null;
	}

	/*
	 * Send the command to delete these files on the server
	 */
	public static void deleteRemoteFiles(Object[] objects) throws IOException, IncorrectPasswordException, IllegalCommandException {
		initializeConnection(4);

		sendInt(objects.length);
		for (final Object o : objects) {
			final ServerTreeItem item = (ServerTreeItem) o;
			sendString(item.getFullName());
		}

		closeConnection();
	}

	/*
	 * Cancels the job
	 */
	public static void doCancel() {
		if (progressMonitor != null) {
			progressMonitor.setCanceled(true);
		}
	}

	/*
	 * Code from eclipse.org to get a console to print to
	 */
	private static MessageConsole findConsole(String name) {
		final ConsolePlugin plugin = ConsolePlugin.getDefault();
		final IConsoleManager conMan = plugin.getConsoleManager();
		final IConsole[] existing = conMan.getConsoles();
		for (final IConsole element : existing) {
			if (name.equals(element.getName())) {
				return (MessageConsole) element;
			}
		}
		// no console found, so create a new one
		final MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public static JobState getJobState() {
		return jobState;
	}

	/*
	 * Requests the file structure from the server
	 */
	public static ServerTreeItem getServerFoldersAndFilesRoot() throws IOException, IncorrectPasswordException,
			IllegalCommandException {
		final ServerTreeItem root = new ServerTreeItem("root"); //$NON-NLS-1$

		initializeConnection(1);
		final int numFolders = recvInt();
		for (int i = 0; i < numFolders; i++) {
			recvFolderInfo(root);
		}
		final int numFiles = recvInt();
		for (int i = 0; i < numFiles; i++) {
			recvFileInfo(root);
		}
		closeConnection();

		return root;
	}

	/*
	 * This is a way to try and get the IProject that import and log files should go to.
	 */
	public static IProject getTargetProject() throws ProjectNotFoundException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot root = workspace.getRoot();
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		final String projectName = preferenceStore.getString(Messages.TARGET_PROJECT);
		final IProject project = root.getProject(projectName);
		if (!project.exists()) {
			throw new ProjectNotFoundException(projectName);
		}
		return project;
	}

	/*
	 * This organizes the objects into a ProjectToRecv object, then sends the request and receives the data into the given project.
	 */
	public static void importFoldersAndFiles(IProject project, Object[] objects) throws IOException, IncorrectPasswordException,
			CoreException, IllegalCommandException {
		final ProjectToRecv projectToRecv = new ProjectToRecv();
		for (final Object o : objects) {
			final ServerTreeItem item = (ServerTreeItem) o;
			projectToRecv.add(item, true);
		}
		projectToRecv.sendNamesRecvData(project);
	}

	/*
	 * This initializes the connection, opening the socket, output streams, sending the instruction, and handling error codes that
	 * may be sent.
	 */
	private static void initializeConnection(int instructionType) throws IOException, IncorrectPasswordException,
			IllegalCommandException {
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		socket = new Socket(preferenceStore.getString(Messages.SERVER_NAME), port);
		socketOutputStream = socket.getOutputStream();
		socketInputStream = socket.getInputStream();

		// login
		String username, password;
		username = preferenceStore.getString(GIGPreferencePage.USERNAME);
		password = preferenceStore.getString(GIGPreferencePage.PASSWORD);
		sendString(username);
		sendString(password);
		int i = recvInt();
		if (i == -1) {
			throw new IncorrectPasswordException();
		}
		sendInt(instructionType);
		if (instructionType == 0 || instructionType == 4) {
			i = recvInt();
			if (i != 0) {
				throw new IllegalCommandException();
			}
		}
	}

	/*
	 * Jumps to the specified line in the specified file in the main editor
	 */
	public static void jumpToLine(IFile file, int line) throws CoreException {
		final IMarker marker = file.createMarker(IMarker.MARKER);
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		final IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true);
		IDE.gotoMarker(editor, marker);
	}

	/*
	 * Makes the specified folder and all of its parent folders similar to java's Folder.mkdirs()
	 */
	private static void makeFolder(IFolder iFolder) throws CoreException {
		final IContainer parent = iFolder.getParent();
		if (!parent.exists()) {
			makeFolder((IFolder) parent);
		}
		iFolder.create(true, true, progressMonitor);
	}

	private static void printToConsole(InputStream inputStream) {
		final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		final Scanner scan = new Scanner(br);
		final MessageConsole myConsole = findConsole(GIGPlugin.PLUGIN_ID);
		final MessageConsoleStream out = myConsole.newMessageStream();
		while (scan.hasNextLine()) {
			final String line = scan.nextLine();
			out.println(line);
			if (progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	/*
	 * Processes the binary created by klee
	 */
	private static IStatus processBinary(IPath binaryPath) throws IOException, CoreException, InterruptedException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final IFile binaryFile = workspaceRoot.getFile(binaryPath);

		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		final boolean local = preferenceStore.getBoolean(GIGPreferencePage.LOCAL);
		if (!local) {
			try {
				// First send the file over
				final List<IFile> fileList = new ArrayList<IFile>();
				fileList.add(binaryFile);
				sendFoldersAndFiles(new ArrayList<IFolder>(), fileList);

				requestVerification(binaryFile.getProject(), binaryFile.getProjectRelativePath());

				final UIJob job = new UIJob(Messages.RESET_SERVER_VIEW) {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						ServerView.getDefault().reset();
						return Status.OK_STATUS;
					}

				};
				startJob(job);
				return Status.OK_STATUS;
			} catch (final IncorrectPasswordException ipe) {
				showErrorDialog(Messages.INCORRECT_PASSWORD, Messages.INCORRECT_PASSWORD_MESSAGE);
				StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INCORRECT_PASSWORD, ipe));
			} catch (final IllegalCommandException e) {
				showErrorDialog(Messages.ILLEGAL_COMMAND, Messages.ILLEGAL_COMMAND_MESSAGE);
				StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.ILLEGAL_COMMAND, e));
			}
			return Status.CANCEL_STATUS;
		}

		// setup the log file path
		final IPath logPath = binaryPath.removeFileExtension().addFileExtension("log"); //$NON-NLS-1$
		final String binaryOSPath = binaryFile.getLocation().toOSString();
		final String gkleeOSPath = preferenceStore.getString(GIGPreferencePage.BIN) + "/gklee"; //$NON-NLS-1$
		final ProcessBuilder processBuilder = new ProcessBuilder(gkleeOSPath, "-emacs", binaryOSPath); //$NON-NLS-1$
		buildEnvPath(processBuilder.environment());

		final Process process = processBuilder.start();
		final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		final Scanner scan = new Scanner(bufferedReader);
		final StringBuilder stringBuilder = new StringBuilder();
		while (scan.hasNextLine()) {
			final String line = scan.nextLine();
			stringBuilder.append(line);
			stringBuilder.append('\n');
			if (progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		process.waitFor();
		if (process.exitValue() != 0) {
			showErrorDialog(Messages.RUNTIME_ERROR, Messages.SEE_CONSOLE);
			printToConsole(process.getErrorStream());
			return Status.CANCEL_STATUS;
		}
		final InputStream logStringInputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
		final IFile logFile = workspaceRoot.getFile(logPath);
		if (logFile.exists()) {
			logFile.setContents(logStringInputStream, true, false, progressMonitor);
		}
		else {
			logFile.create(logStringInputStream, true, progressMonitor);
		}
		logStringInputStream.close();

		// refresh eclipse environment to make it aware of the new log file
		final IContainer gigFolder = logFile.getParent();
		gigFolder.refreshLocal(1, progressMonitor);
		// also be sure to get rid of temporary files that gklee created
		cleanUpGklee(gigFolder);
		gigFolder.refreshLocal(1, progressMonitor);

		return processLog(logPath);
	}

	/*
	 * Processes a log file generated by gklee
	 */
	private static IStatus processLog(IPath logPath) throws IOException, CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final IFile logFile = workspaceRoot.getFile(logPath);
		final IProject project = logFile.getProject();
		final InputStream logInputStream = logFile.getContents();
		try {
			final GkleeLog gkleeLog = new GkleeLog(logInputStream, logFile);
			logInputStream.close();
			final UIJob job = new UIJob(Messages.UPDATE_GIG) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					GIGView.getDefault().update(gkleeLog, project);
					return Status.OK_STATUS;
				}
			};
			startJob(job);
			return Status.OK_STATUS;
		} catch (final IllegalStateException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.PARSE_EXCEPTION, e));
		} catch (final LogException e) {
			showErrorDialog(Messages.EXCEPTION_WHILE_PARSING, Messages.BAD_LOG_FILE);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.BAD_LOG_FILE, e));
		} finally {
			logInputStream.close();
		}
		return Status.CANCEL_STATUS;
	}

	/*
	 * This will process the source, binary or log file passed to it
	 */
	public static IStatus processSource(IPath filePath) throws IOException, CoreException, InterruptedException {
		// enforce that the file is of the right type, and switch to something else if needed
		final String fileExtension = filePath.getFileExtension();
		if (fileExtension.equals("gig")) { //$NON-NLS-1$
			return processBinary(filePath);
		}
		else if (fileExtension.equals("log")) { //$NON-NLS-1$
			return processLog(filePath);
		}
		else if (!fileExtension.equals("cu") && !fileExtension.equals("C") && !fileExtension.equals("c")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			showErrorDialog(Messages.INCORRECT_FILE_EXTENSION, Messages.CHANGE_FILE_EXTENSION);
			return Status.CANCEL_STATUS;
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();

		// If we are doing a remote execution, now is the time to go over to it
		final IFile origFile = workspaceRoot.getFile(filePath);
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		final boolean local = preferenceStore.getBoolean(GIGPreferencePage.LOCAL);
		if (!local) {
			try {
				// First send the file over
				final List<IFile> fileList = new ArrayList<IFile>();
				fileList.add(origFile);

				if (!preferenceStore.getString(Messages.USERNAME).equals(preferenceStore.getDefaultString(Messages.USERNAME))) {
					sendFoldersAndFiles(new ArrayList<IFolder>(), fileList);
				}

				requestVerification(origFile.getProject(), origFile.getProjectRelativePath());

				final UIJob job = new UIJob(Messages.RESET_SERVER_VIEW) {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						ServerView.getDefault().reset();
						return Status.OK_STATUS;
					}

				};
				startJob(job);
				return Status.OK_STATUS;
			} catch (final IncorrectPasswordException ipe) {
				showErrorDialog(Messages.INCORRECT_PASSWORD, Messages.INCORRECT_PASSWORD_MESSAGE);
				StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INCORRECT_PASSWORD, ipe));
			} catch (final IllegalCommandException e) {
				showErrorDialog(Messages.ILLEGAL_COMMAND, Messages.ILLEGAL_COMMAND_MESSAGE);
				StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.ILLEGAL_COMMAND, e));
			}
			return Status.CANCEL_STATUS;
		}

		// klee-l++ can't handle the .cu extension, so make a link to it
		IFile currFile;
		final IContainer gigContainer = origFile.getParent();
		if (fileExtension.equals("cu")) { //$NON-NLS-1$
			final IPath newPath = origFile.getProjectRelativePath().removeFileExtension().addFileExtension("C"); //$NON-NLS-1$
			currFile = origFile.getProject().getFile(newPath);
			origFile.getParent().refreshLocal(1, progressMonitor);
			if (currFile.exists()) {
				currFile.delete(true, progressMonitor);
			}
			origFile.copy(currFile.getFullPath(), 0, progressMonitor);
			gigContainer.refreshLocal(1, progressMonitor);
		}
		else {
			currFile = origFile;
		}
		final IPath binaryPath = filePath.removeFileExtension().addFileExtension("gig"); //$NON-NLS-1$

		// begin building the command line with absolute paths, especially from the preferenceStore
		String sourceOSPath, binaryOSPath;
		final IPath sourceAbsoluteIPath = currFile.getLocation();
		sourceOSPath = sourceAbsoluteIPath.toOSString();
		binaryOSPath = sourceAbsoluteIPath.removeFileExtension().addFileExtension("gig").toOSString(); //$NON-NLS-1$
		final String kleeOSPath = preferenceStore.getString(GIGPreferencePage.BIN) + "/klee-l++"; //$NON-NLS-1$
		final ProcessBuilder processBuilder = new ProcessBuilder(kleeOSPath, sourceOSPath, "-o", binaryOSPath); //$NON-NLS-1$

		// klee depends on a lot of path variables, so set these in the environment
		final Map<String, String> environment = processBuilder.environment();
		buildEnvPath(environment);

		final Process process = processBuilder.start();
		printToConsole(process.getInputStream());
		process.waitFor();

		if (process.exitValue() != 0) {
			showErrorDialog(Messages.COMPILATION_ERROR, Messages.SEE_CONSOLE);
			printToConsole(process.getErrorStream());
			return Status.CANCEL_STATUS;
		}

		// refresh environment so that eclipse is aware of the new binary
		gigContainer.refreshLocal(1, progressMonitor);
		return processBinary(binaryPath);
	}

	/*
	 * Receives the filename and adds it to the tree. Assumes connection is already established.
	 */
	private static void recvFileInfo(ServerTreeItem root) throws IOException {
		final String name = recvString();
		new ServerTreeItem(name, root, false);
	}

	/*
	 * Receives the folder information and recursively its contents. Assumes connection is already established.
	 */
	private static void recvFolderInfo(ServerTreeItem root) throws IOException {
		final String name = recvString();
		final ServerTreeItem folder = new ServerTreeItem(name, root, true);
		final int numFolders = recvInt();
		for (int i = 0; i < numFolders; i++) {
			recvFolderInfo(folder);
		}
		final int numFiles = recvInt();
		for (int i = 0; i < numFiles; i++) {
			recvFileInfo(folder);
		}
	}

	/*
	 * Receives an integer. Assumes connection is already established.
	 */
	protected static int recvInt() throws IOException {
		final int len = 4;
		int off = 0;
		final byte[] buffer = new byte[4];
		while (off < len) {
			off += socketInputStream.read(buffer, off, len - off);
		}
		int ret = 0;
		ret |= (buffer[0] << 24) & 0xff000000;
		ret |= (buffer[1] << 16) & 0x00ff0000;
		ret |= (buffer[2] << 8) & 0x0000ff00;
		ret |= (buffer[3] << 0) & 0x000000ff;
		return ret;
	}

	/*
	 * Receives a String. Assumes connection is already established.
	 */
	protected static String recvString() throws IOException {
		final int len = recvInt();
		int off = 0;
		final byte[] buffer = new byte[len];
		while (off < len) {
			off += socketInputStream.read(buffer, off, len - off);
		}
		return new String(buffer);
	}

	/*
	 * Requests a remote verification of the file from the given project specified in the item.
	 * This also receives the log file, processes it, and displays it.
	 */
	public static void remoteVerifyFile(IProject project, ServerTreeItem item) throws IOException, IncorrectPasswordException,
			CoreException, IllegalCommandException {
		final String filePathString = item.getFullName();
		final IFile file = project.getFile(filePathString);
		final IPath filePath = file.getProjectRelativePath();

		requestVerification(project, filePath);
	}

	/*
	 * Requests remote verification of the file in the project. The filePath needs to be relative to the project.
	 * This also receives the log file, processes it, and displays it.
	 */
	private static void requestVerification(IProject project, IPath filePath) throws IOException, IncorrectPasswordException,
			CoreException, IllegalCommandException {
		initializeConnection(2);

		sendString(filePath.toString());
		final String logString = recvString();

		final IPath logPath = filePath.removeFileExtension().addFileExtension("log"); //$NON-NLS-1$
		final IFile logFile = project.getFile(logPath);
		final InputStream logInputStream = new ByteArrayInputStream(logString.getBytes());
		if (logFile.exists()) {
			logFile.setContents(logInputStream, true, false, progressMonitor);
		}
		else {
			final IContainer parentToLog = logFile.getParent();
			if (!parentToLog.exists()) {
				makeFolder((IFolder) (parentToLog));
			}
			logFile.create(logInputStream, true, progressMonitor);
		}

		closeConnection();

		logFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);

		processLog(logFile.getFullPath());
	}

	/*
	 * Sends the file. Assumes connection is already established.
	 */
	public static void sendFile(IFile file) throws IOException, CoreException {
		final String filename = file.getName();
		sendString(filename);
		final InputStream is = file.getContents();
		final int len = is.available();
		sendInt(len);
		int off = 0;
		final byte[] buffer = new byte[len];
		while (off < len) {
			final int rec = is.read(buffer, off, len - off);
			socketOutputStream.write(buffer, off, rec);
			off += rec;
		}
		is.close();
	}

	/*
	 * This sends the folders and files to the server including their containing parent directories up to but not including the
	 * project.
	 */
	public static void sendFoldersAndFiles(List<IFolder> folders, List<IFile> files) throws CoreException, IOException,
			IncorrectPasswordException, IllegalCommandException {
		initializeConnection(0);
		final ProjectToSend projectToSend = new ProjectToSend();
		for (final IFolder folder : folders) {
			final FolderToSend folderToSend = new FolderToSend(folder);
			projectToSend.add(folderToSend);
		}
		for (final IFile file : files) {
			projectToSend.add(file);
		}
		projectToSend.send();
		closeConnection();
	}

	/*
	 * Sends the int. Assumes connection is already established.
	 */
	protected static void sendInt(int i) throws IOException {
		final byte[] buffer = new byte[4];
		buffer[0] = (byte) (i >> 24 & 0xff);
		buffer[1] = (byte) (i >> 16 & 0xff);
		buffer[2] = (byte) (i >> 8 & 0xff);
		buffer[3] = (byte) (i >> 0 & 0xff);
		socketOutputStream.write(buffer);
	}

	/*
	 * Sends the names of folders and files and receives the data to create the files. Assumes connection is already established.
	 */
	public static void sendNamesRecvData(IContainer container, String name, List<FolderToRecv> folders, List<String> files)
			throws IOException, CoreException {
		sendString(name);
		IFolder folder;
		if (container instanceof IFolder) {
			folder = ((IFolder) container).getFolder(name);
		}
		else {
			folder = ((IProject) container).getFolder(name);
		}
		if (!folder.exists()) {
			folder.create(true, true, progressMonitor);
		}
		final int numFolders = folders.size();
		sendInt(numFolders);
		for (int i = 0; i < numFolders; i++) {
			folders.get(i).sendNamesRecvData(folder);
		}
		final int numFiles = files.size();
		sendInt(numFiles);
		for (int i = 0; i < numFiles; i++) {
			final String filename = files.get(i);
			sendString(filename);
			final IFile file = folder.getFile(filename);
			final int len = recvInt();
			final byte[] buffer = new byte[len];
			int off = 0;
			while (off < len) {
				off += socketInputStream.read(buffer, off, len - off);
			}
			final InputStream inputStream = new ByteArrayInputStream(buffer);
			if (file.exists()) {
				file.setContents(inputStream, true, true, progressMonitor);
				inputStream.close();
			}
			else {
				file.create(inputStream, true, progressMonitor);
				inputStream.close();
			}
		}
	}

	/*
	 * Sends the names of the folders and files and receives their data in the specified project.
	 */
	public static void sendNamesRecvData(IProject project, List<FolderToRecv> folders, List<String> files) throws IOException,
			IncorrectPasswordException, CoreException, IllegalCommandException {
		initializeConnection(3);

		sendInt(folders.size());
		for (final FolderToRecv folder : folders) {
			folder.sendNamesRecvData(project);
		}
		sendInt(files.size());
		for (final String filename : files) {
			sendString(filename);
			final IFile file = project.getFile(filename);
			final int len = recvInt();
			final byte[] buffer = new byte[len];
			int off = 0;
			while (off < len) {
				off += socketInputStream.read(buffer, off, len - off);
			}
			final InputStream inputStream = new ByteArrayInputStream(buffer);
			if (file.exists()) {
				file.setContents(inputStream, true, true, progressMonitor);
				inputStream.close();
			}
			else {
				file.create(inputStream, true, progressMonitor);
				inputStream.close();
			}
		}

		closeConnection();
		project.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
	}

	/*
	 * Sends the String. Assumes connection is already established.
	 */
	protected static void sendString(String string) throws IOException {
		sendInt(string.length());
		socketOutputStream.write(string.getBytes());
	}

	/*
	 * Thread safe way to change the job state.
	 */
	public static void setJobState(JobState newState) {
		jobsLock.lock();
		jobState = newState;
		jobsLock.unlock();
	}

	/*
	 * This creates a popup that displays an error message. The Eclipse ErrorDialog didn't work in Juno after several attempts.
	 */
	public static void showErrorDialog(final String title, final String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}

	/*
	 * Thread safe way to start a job. No job will be started if an other job is running. Be sure to call setJobState(JobState.None)
	 * after the job is done, or no more jobs can execute via this method.
	 */
	public static void startJob(Job job) {
		jobsLock.lock();
		try {
			if (jobState == JobState.None) {
				GIGUtilities.job = job;
				job.setPriority(Job.LONG);
				progressMonitor = new NullProgressMonitor();
				progressMonitor.setCanceled(false);
				job.schedule();
				jobState = JobState.Running;
			}
		} finally {
			jobsLock.unlock();
		}
	}

	/*
	 * Starts a UIJob. No need to attach a progress monitor or anything as canceling occurs in the UI thread anyways. As such, these
	 * jobs should be relatively short.
	 */
	public static void startJob(UIJob job) {
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	/*
	 * Used to correctly stop the job midway through for both canceling and exiting purposes.
	 */
	public static void stopJob() {
		jobsLock.lock();
		if (jobState == JobState.Running) {
			progressMonitor.setCanceled(true);
			jobState = JobState.Canceled;
			jobsLock.unlock();
			int i = 1000;
			/*
			 * We are waiting and giving the job time to cleanly exit itself, it will signal us by changing the jobState to
			 * None.
			 * Or we wait for timeout and kill it with a cancel
			 */
			while (jobState == JobState.Canceled && i > 0) {
				try {
					Thread.sleep(1);
					i--;
				} catch (final InterruptedException e) {
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INTERRUPTED_EXCEPTION, e));
				}
			}
			if (i <= 0) {
				job.cancel();
				job = null;
				jobsLock.lock();
				jobState = JobState.None;
				jobsLock.unlock();
			}
		}
		else {
			jobsLock.unlock();
		}
	}
}