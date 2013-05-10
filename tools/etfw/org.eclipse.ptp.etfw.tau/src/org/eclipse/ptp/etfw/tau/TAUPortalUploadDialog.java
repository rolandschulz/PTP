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
package org.eclipse.ptp.etfw.tau;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Manages uploading TAU profile data to the TAUPortal online performance data storage system
 * 
 * @author wspear
 * 
 */
public class TAUPortalUploadDialog extends Dialog {

	private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;

	/**
	 * Converts the login and profile data for remote trasfer
	 * 
	 * @param data
	 * @return
	 */
	private static String convertToHex(byte[] data) {
		final StringBuffer buf = new StringBuffer();
		for (final byte element : data) {
			int halfbyte = (element >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = element & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Obtain the names of the users available workspaces
	 * 
	 * @param uname
	 *            Username
	 * @param pwd
	 *            Password
	 * @return
	 */
	private static String getWorkspaces(String url, String uname, String pwd)
	{
		String pwd2;
		try {
			pwd2 = SHA1(pwd);

			final String portalURL = url + "/trial/list_workspaces"; //$NON-NLS-1$
			String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(uname, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pwd2, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

			final String workspaces = portalAccess(portalURL, data);

			return workspaces;

		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return "NO_VALID_WORKSPACES"; //$NON-NLS-1$
	}

	/**
	 * Uploads data to the given URL
	 * 
	 * @param portalURL
	 *            The web address of the portal
	 * @param data
	 *            The string-representation of the data being sent
	 * @return The portal's response
	 * @throws Exception
	 */
	private static String portalAccess(String portalURL, String data) throws Exception {
		final URL url = new URL(portalURL);

		trustHttpsCertificates();

		final URLConnection conn = url.openConnection();

		((javax.net.ssl.HttpsURLConnection) conn).setHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
			public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
				return true;
			}
		});

		conn.setDoOutput(true);
		final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

		wr.write(data);
		wr.flush();
		String line = ""; //$NON-NLS-1$
		final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String result = ""; //$NON-NLS-1$
		while ((line = rd.readLine()) != null) {
			result += line;
		}
		wr.close();
		return result;
	}

	/**
	 * Handhes hash conversion for security
	 * 
	 * @param text
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private static String SHA1(String text)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes("iso-8859-1"), 0, text.length()); //$NON-NLS-1$
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	/**
	 * Bypass certificate checking
	 * 
	 * @throws Exception
	 */
	static private void trustHttpsCertificates() throws Exception {

		Class<?> sslProvider = null;
		boolean goodProvider = true;
		try {
			sslProvider = Class.forName("com.sun.net.ssl.internal.ssl.Provider"); //$NON-NLS-1$
		} catch (final ClassNotFoundException e) {
			goodProvider = false;
		}

		if (!goodProvider)
		{
			sslProvider = Class.forName("com.ibm.jsse.IBMJSSEProvider"); //$NON-NLS-1$
		}

		Security.addProvider((Provider) sslProvider.newInstance());// new
																	// com.ibm.jsse.IBMJSSEProvider());//com.sun.net.ssl.internal.ssl.Provider());
		// Create a trust manager that does not validate certificate chains:
		final TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
							throws java.security.cert.CertificateException {
					}

					public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
							throws java.security.cert.CertificateException {
					}

					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				}
		};

		// Install the all-trusting trust manager:
		final SSLContext sc = SSLContext.getInstance("SSL"); //$NON-NLS-1$
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// avoid "HTTPS hostname wrong: should be <myhostname>" exception:
		final HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
					System.out.println(Messages.TAUPortalUploadDialog_WarningURLHost + urlHostName
							+ Messages.TAUPortalUploadDialog_IsDifferent + session.getPeerHost() + "'.");
				}
				return true; // also accept different hostname (e.g. domain name instead of IP address)
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	/**
	 * Uploads ppkFile to the specified workspace of the asccount associated with the given username and password
	 * 
	 * @param usr
	 *            Username
	 * @param pwd
	 *            Password
	 * @param workspace
	 *            Selected workspace
	 * @param ppkFile
	 *            Packed profile file
	 * @return
	 * @throws Exception
	 */
	private static String uploadPPK(String url, String usr, String pwd, String workspace, IFileStore ppkFile) throws Exception {

		final String pwd2 = SHA1(pwd);

		final String portalURL = url + "/trial/batch_upload"; //$NON-NLS-1$
		String ppkName = ppkFile.getName();
		ppkName = ppkName.substring(0, ppkName.lastIndexOf('.'));

		//RandomAccessFile rfile = new RandomAccessFile(ppkFile.openOutputStream(EFS.NONE, null), "r"); //$NON-NLS-1$
		final InputStream rfile = (new BufferedInputStream(ppkFile.openInputStream(EFS.NONE, null)));
		ppkFile.fetchInfo().getLength();
		final byte barr[] = new byte[(int) ppkFile.fetchInfo().getLength()];// TODO: This needs testing.
		rfile.read(barr);
		String ppkString = new String(barr, "ISO-8859-1"); //$NON-NLS-1$
		ppkString = URLEncoder.encode(ppkString, "ISO-8859-1"); //$NON-NLS-1$

		String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(usr, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pwd2, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		data += "&" + URLEncoder.encode(ppkName, "UTF-8") + "=" + ppkString; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		data += "&" + URLEncoder.encode("workspace", "UTF-8") + "=" + URLEncoder.encode(workspace, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		return portalAccess(portalURL, data);

	}

	private Text urlField;
	private Text usernameField;
	private Text passwordField;

	private Label portalStatus;

	private Combo workspaceCombo;

	private String url = ""; //$NON-NLS-1$

	private String uname = ""; //$NON-NLS-1$

	private String pwd = ""; //$NON-NLS-1$

	private IFileStore ppk = null;

	public TAUPortalUploadDialog(Shell parentShell, IFileStore ppk) {
		super(parentShell);
		this.ppk = ppk;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == RESET_ID) {
			usernameField.setText(""); //$NON-NLS-1$
			passwordField.setText(""); //$NON-NLS-1$
		} else {
			if (buttonId == Window.OK) {
				if (!workspaceCombo.getEnabled()) {
					portalStatus.setText(Messages.TAUPortalUploadDialog_ErrorNoWorkspace);
					return;
				}
				if (workspaceCombo.getSelectionIndex() == -1)
				{
					portalStatus.setText(Messages.TAUPortalUploadDialog_ErrorNoWorkspace);
					return;
				}

				try {
					final String success = uploadPPK(url, uname, pwd, workspaceCombo.getItem(workspaceCombo.getSelectionIndex()),
							ppk);
					if (success.indexOf(Messages.TAUPortalUploadDialog_Error) >= 0)
					{
						portalStatus.setText(success);
						return;
					}
				} catch (final Exception e) {
					e.printStackTrace();
					portalStatus.setText(Messages.TAUPortalUploadDialog_UploadError);
					return;
				}
			}
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, RESET_ID, Messages.TAUPortalUploadDialog_ResetAll, false);
	}

	/**
	 * Creates the primary interface for the upload dialog
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite comp = (Composite) super.createDialogArea(parent);

		final GridLayout layout = (GridLayout) comp.getLayout();
		layout.numColumns = 2;

		final Label pwdLabel = new Label(comp, SWT.RIGHT);
		pwdLabel.setText(Messages.TAUPortalUploadDialog_TauPortURL);

		urlField = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		urlField.setLayoutData(data);
		urlField.setText("https://tau.nic.uoregon.edu"); //$NON-NLS-1$

		final Label usernameLabel = new Label(comp, SWT.RIGHT);
		usernameLabel.setText(Messages.TAUPortalUploadDialog_TauPortUname);

		usernameField = new Text(comp, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		usernameField.setLayoutData(data);

		final Label passwordLabel = new Label(comp, SWT.RIGHT);
		passwordLabel.setText(Messages.TAUPortalUploadDialog_TauPortPwd);

		passwordField = new Text(comp, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		passwordField.setLayoutData(data);

		final SelectionListener subListen = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				url = urlField.getText();
				uname = usernameField.getText();
				pwd = passwordField.getText();
				if (uname.length() == 0 || pwd.length() == 0)
				{
					portalStatus.setText(Messages.TAUPortalUploadDialog_SubmitValidUnamePass);
					workspaceCombo.clearSelection();
					workspaceCombo.setEnabled(false);
					return;
				}

				String workspaces = getWorkspaces(url, uname, pwd);

				if (workspaces.equals("NO_VALID_WORKSPACES")) //$NON-NLS-1$
				{
					portalStatus.setText(Messages.TAUPortalUploadDialog_LoginError);
					workspaceCombo.clearSelection();
					workspaceCombo.setEnabled(false);
					return;
				}

				if (workspaces.indexOf(',') == -1)//
				{
					portalStatus.setText(workspaces);
					workspaceCombo.clearSelection();
					workspaceCombo.setEnabled(false);
					return;
				}

				workspaces = workspaces.trim();

				final StringTokenizer tz = new StringTokenizer(workspaces, ",", false); //$NON-NLS-1$

				final String wSArray[] = new String[tz.countTokens()];
				int i = 0;
				while (tz.hasMoreTokens()) {
					wSArray[i++] = tz.nextToken().trim();
				}
				workspaceCombo.setItems(wSArray);
				workspaceCombo.setEnabled(true);
				workspaceCombo.select(0);
				portalStatus.setText(Messages.TAUPortalUploadDialog_SelectWorkspace);
			}
		};

		final Button login = new Button(comp, SWT.PUSH | SWT.CENTER);
		login.setText(Messages.TAUPortalUploadDialog_SubmitLogin);
		login.addSelectionListener(subListen);

		portalStatus = new Label(comp, SWT.LEFT);
		portalStatus.setText(Messages.TAUPortalUploadDialog_SubmitValidEtcSpace);

		final Label workspaceLabel = new Label(comp, SWT.RIGHT);
		workspaceLabel.setText(Messages.TAUPortalUploadDialog_SelectWorkspaceTab);

		workspaceCombo = new Combo(comp, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		workspaceCombo.setLayoutData(data);
		workspaceCombo.setEnabled(false);

		return comp;
	}

}
