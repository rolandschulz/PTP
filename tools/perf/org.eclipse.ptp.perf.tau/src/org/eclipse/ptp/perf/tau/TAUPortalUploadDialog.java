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
package org.eclipse.ptp.perf.tau;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
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
 * @author wspear
 *
 */
public class TAUPortalUploadDialog extends Dialog {

	private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;

	  private Text usernameField;

	  private Text passwordField;
	  
	  private Label portalStatus;
	  
	  private Combo workspaceCombo;
	  
	  private String uname="";
	  private String pwd="";
	  private File ppk=null;
	  
	  public TAUPortalUploadDialog(Shell parentShell, File ppk) {
	    super(parentShell);
	    this.ppk=ppk;
	  }

	  /**
	   * Creates the primary interface for the upload dialog
	   */
	  protected Control createDialogArea(Composite parent) {
		  
	    Composite comp = (Composite) super.createDialogArea(parent);

	    GridLayout layout = (GridLayout) comp.getLayout();
	    layout.numColumns = 2;

	    Label usernameLabel = new Label(comp, SWT.RIGHT);
	    usernameLabel.setText("TAU Portal Username: ");

	    usernameField = new Text(comp, SWT.SINGLE | SWT.BORDER);
	    GridData data = new GridData(GridData.FILL_HORIZONTAL);
	    usernameField.setLayoutData(data);

	    Label passwordLabel = new Label(comp, SWT.RIGHT);
	    passwordLabel.setText("TAU Portal Password: ");

	    passwordField = new Text(comp, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    passwordField.setLayoutData(data);
	    
	    
	    SelectionListener subListen = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				uname=usernameField.getText();
				pwd=passwordField.getText();
				if(uname.length()==0||pwd.length()==0)
				{
					portalStatus.setText("Submit a valid user name and password");
					workspaceCombo.clearSelection();
				    workspaceCombo.setEnabled(false);
					return;
				}
				
				String workspaces = getWorkspaces(uname,pwd);

				if(workspaces.equals("NO_VALID_WORKSPACES"))
				{
					portalStatus.setText("Unknown login error.  Try again.");
					workspaceCombo.clearSelection();
				    workspaceCombo.setEnabled(false);
					return;
				}
				
				if(workspaces.indexOf(',')==-1)//
				{
					portalStatus.setText(workspaces);
					workspaceCombo.clearSelection();
				    workspaceCombo.setEnabled(false);
					return;
				}
				
				workspaces=workspaces.trim();
				
				StringTokenizer tz = new StringTokenizer(workspaces, ",", false);
				
				String wSArray[] = new String[tz.countTokens()];
				int i=0;
				while(tz.hasMoreTokens()){
					wSArray[i++]=tz.nextToken().trim();
				}
				workspaceCombo.setItems(wSArray);
				workspaceCombo.setEnabled(true);
				workspaceCombo.select(0);
				portalStatus.setText("Select a workspace");
			}
		};
	    
	    
	    Button login = new Button(comp, SWT.PUSH|SWT.CENTER);
	    login.setText("Submit Login");
	    login.addSelectionListener(subListen);
	    
	    portalStatus=new Label(comp,SWT.LEFT);
	    portalStatus.setText("Submit a valid user name and password             ");

	    Label workspaceLabel = new Label(comp, SWT.RIGHT);
	    workspaceLabel.setText("Select Workspace: ");
	    
	    workspaceCombo = new Combo(comp, SWT.READ_ONLY);
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    workspaceCombo.setLayoutData(data);
	    workspaceCombo.setEnabled(false);

	    return comp;
	  }

	  protected void createButtonsForButtonBar(Composite parent) {
	    super.createButtonsForButtonBar(parent);
	    createButton(parent, RESET_ID, "Reset All", false);
	  }

	  protected void buttonPressed(int buttonId) {
	    if (buttonId == RESET_ID) {
	      usernameField.setText("");
	      passwordField.setText("");
	    } else {
	    	if(buttonId==Window.OK){
	    		if(!workspaceCombo.getEnabled()){
	    			portalStatus.setText("Error: No workspace selected");
	    			return;
	    		}
	    		if(workspaceCombo.getSelectionIndex()==-1)
	    		{
	    			portalStatus.setText("Error: No workspace selected");
	    			return;
	    		}
	    		
	    		try {
					String success = uploadPPK(uname, pwd, workspaceCombo.getItem(workspaceCombo.getSelectionIndex()), ppk);
					if(success.indexOf("ERROR")>=0)
					{
						portalStatus.setText(success);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					portalStatus.setText("Unknown upload error.  Try again.");
					return;
				}
	    	}
	    	super.buttonPressed(buttonId);
	    }
	  }
	

	/**
	 * Converts the login and profile data for remote trasfer
	 * @param data
	 * @return
	 */
    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                buf.append((char) ('0' + halfbyte));
                else
                buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Handhes hash conversion for security
     * @param text
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private static String SHA1(String text)
    throws NoSuchAlgorithmException, UnsupportedEncodingException  {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
	
    /**
     * Bypass certificate checking
     * @throws Exception
     */
    static private void trustHttpsCertificates() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        //Create a trust manager that does not validate certificate chains:
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}
            }
        };
 
        //Install the all-trusting trust manager:
        SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new SecureRandom());
       HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
       
       //avoid "HTTPS hostname wrong: should be <myhostname>" exception:
       HostnameVerifier hv = new HostnameVerifier() {
           public boolean verify(String urlHostName, SSLSession session) {
               if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                   System.out.println("Warning: URL host '"+urlHostName+"' is different to SSLSession host '"+session.getPeerHost()+"'.");
               }
               return true; //also accept different hostname (e.g. domain name instead of IP address)
           }
       };
       HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
    
    /**
     * Obtain the names of the users available workspaces
     * @param uname Username
     * @param pwd Password
     * @return
     */
    private static String getWorkspaces(String uname, String pwd)
    {
    	String pwd2;
		try {
			pwd2 = SHA1(pwd);
		
		
		String portalURL="https://tau.nic.uoregon.edu/trial/list_workspaces";
		String data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(uname,"UTF-8");
		data += "&"+URLEncoder.encode("password", "UTF-8")+"="+URLEncoder.encode(pwd2, "UTF-8");
		
		String workspaces = portalAccess(portalURL,data);
		
		return workspaces;
		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "NO_VALID_WORKSPACES";
	}
    
    /**
     * Uploads ppkFile to the specified workspace of the asccount associated with the given username and password
     * @param usr Username
     * @param pwd Password
     * @param workspace Selected workspace
     * @param ppkFile Packed profile file
     * @return
     * @throws Exception
     */
	private static String uploadPPK(String usr, String pwd, String workspace, File ppkFile) throws Exception {
		
		String pwd2 = SHA1(pwd);
		
		String portalURL="https://tau.nic.uoregon.edu/trial/batch_upload";
		String ppkName = ppkFile.getName();
		ppkName=ppkName.substring(0, ppkName.lastIndexOf('.'));
		
		RandomAccessFile rfile = new RandomAccessFile(ppkFile, "r");
		
		byte barr[]=new byte[(int)rfile.length()];
		rfile.read(barr);
		String ppkString=new String(barr,"ISO-8859-1");
		ppkString=URLEncoder.encode(ppkString,"ISO-8859-1");
		

		String data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(usr,"UTF-8");
		data += "&"+URLEncoder.encode("password", "UTF-8")+"="+URLEncoder.encode(pwd2, "UTF-8");
		data += "&"+URLEncoder.encode(ppkName, "UTF-8")+"="+ppkString;
		data += "&"+URLEncoder.encode("workspace", "UTF-8")+"="+URLEncoder.encode(workspace, "UTF-8");
		
		return portalAccess(portalURL,data);
		
	}
	
	/**
	 * Uploads data to the given URL
	 * @param portalURL The web address of the portal
	 * @param data The string-representation of the data being sent
	 * @return The portal's response
	 * @throws Exception
	 */
	private static String portalAccess(String portalURL, String data) throws Exception{
		URL url = new URL(portalURL);

		trustHttpsCertificates();
		
		URLConnection conn = url.openConnection();
		
	    ((javax.net.ssl.HttpsURLConnection) conn).setHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
	    	public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
	     	return true;
	     	}
	     });
		
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		
		wr.write(data);
		wr.flush();
		String line="";
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String result = "";
		while ((line = rd.readLine()) != null) {
			result+=line;
		}
		wr.close();
		return result;
	}
	
}
