package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;

public class RemoteUIServicesUtils implements IJAXBUINonNLSConstants {

	public static URI browse(Shell shell, URI current, RemoteServicesDelegate delegate, boolean remote) throws URISyntaxException {
		IRemoteUIServices uIServices = null;
		IRemoteUIFileManager uiFileManager = null;
		IRemoteConnection conn = null;
		URI home = null;
		String path = null;

		if (!remote) {
			uIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(delegate.getLocalServices());
			uiFileManager = uIServices.getUIFileManager();
			conn = delegate.getLocalConnection();
			home = delegate.getLocalHome();
		} else {
			uIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(delegate.getRemoteServices());
			uiFileManager = uIServices.getUIFileManager();
			conn = delegate.getRemoteConnection();
			home = delegate.getRemoteHome();
		}

		path = current == null ? home.getPath() : current.getPath();

		try {
			uiFileManager.setConnection(conn);
			uiFileManager.showConnections(remote);
			path = uiFileManager
					.browseFile(shell, Messages.JAXBRMConfigurationSelectionWizardPage_0, path, IRemoteUIConstants.OPEN);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		if (path == null) {
			return current;
		}

		return new URI(home.getScheme(), home.getUserInfo(), home.getHost(), home.getPort(), path, home.getQuery(),
				home.getFragment());
	}

	public static URI exportResource(URI resource, Shell shell) throws Throwable {
		if (resource == null || ZEROSTR.equals(resource)) {
			return null;
		}
		// File source = new File(uri);
		// FileDialog fileDialog = new FileDialog(shell, SWT.SINGLE | SWT.SAVE);
		// fileDialog.setText(Messages.ConfigUtils_exportResourceTitle);
		// String path = fileDialog.open();
		// if (path == null) {
		// return null;
		// }
		// File target = new File(path);
		// if (target.equals(source)) {
		// throw new
		// IllegalArgumentException(Messages.ConfigUtils_exportResourceError_0);
		// }
		//
		// FileInputStream fis = new FileInputStream(source);
		// FileOutputStream fos = new FileOutputStream(target);
		//
		// long total = 0;
		// long size = source.length();
		// int recvd = 0;
		// byte[] buffer = new byte[COPY_BUFFER_SIZE];
		// try {
		// while (size == UNDEFINED || total < size) {
		// recvd = fis.read(buffer, 0, COPY_BUFFER_SIZE);
		// if (recvd == UNDEFINED) {
		// break;
		// }
		// if (recvd > 0) {
		// fos.write(buffer, 0, recvd);
		// total += recvd;
		// }
		// }
		// } catch (IOException ioe) {
		// throw new Throwable(Messages.ConfigUtils_exportResourceError_1, ioe);
		// } finally {
		// try {
		// fos.flush();
		// fos.getFD().sync();
		// fos.close();
		// fis.close();
		// } catch (IOException ignore) {
		// }
		// }

		// return target;
		return null;
	}

	public static IRemoteServices[] getAvailableServices() {
		IRemoteServices[] remoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(null);
		Arrays.sort(remoteServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		return remoteServices;
	}

	public static String getFileContents(URI file) throws Throwable {
		StringBuffer buffer = new StringBuffer();
		// if (file.exists() && file.isFile()) {
		// BufferedReader reader = new BufferedReader(new FileReader(file));
		// try {
		// while (true) {
		// try {
		// String line = reader.readLine();
		// if (line == null) {
		// break;
		// }
		// buffer.append(line).append(LINE_SEP);
		// } catch (EOFException eof) {
		// break;
		// }
		// }
		// } finally {
		// reader.close();
		// }
		// }
		return buffer.toString();
	}

	public static URI getUserHome() {
		return new File(System.getProperty(JAVA_USER_HOME)).toURI();
	}

	public static URI writeContentsToFile(Shell shell, String contents, URI file) throws Throwable {
		// FileDialog fileDialog = new FileDialog(shell, SWT.SINGLE | SWT.SAVE);
		// fileDialog.setText(Messages.ConfigUtils_exportResourceTitle);
		// fileDialog.setOverwrite(true);
		// fileDialog.setFileName(file.getName());
		// String path = fileDialog.open();
		// if (path == null) {
		// return null;
		// }
		//
		// FileWriter fw = new FileWriter(path, false);
		// fw.write(contents);
		// fw.flush();
		// fw.close();
		// return path;
		return null;
	}
}
