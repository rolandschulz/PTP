package org.eclipse.ptp.core.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class SFTPTests extends TestCase {
	private static final String USERNAME = "user"; //$NON-NLS-1$
	private static final String PASSWORD = "password"; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	private static final String PATH1 = "/home/user/sftp_test"; //$NON-NLS-1$
	private static final String PATH2 = PATH1 + "/.file1"; //$NON-NLS-1$
	private static final String TEST_STRING = "a string containing fairly *()(*&^$%## random text"; //$NON-NLS-1$
	
	
	private class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
		private SSHUserInfo() { }

		public String getPassword() {
			return PASSWORD;
		}
		
		public void setPassword(String password) {
		}

		public boolean promptYesNo(String str) {
			return true;
		}

		public String getPassphrase() {
			return "";
		}
		
		public void setPassphrase(String passphrase) {
		}

		public boolean promptPassphrase(String message) {
			return false;
		}

		public boolean promptPassword(String message) {
			return true;
		}
		
		public void setUsePassword(boolean usePassword) {
		}

		public void showMessage(String message) {
		}

		public String[] promptKeyboardInteractive(final String destination,
				final String name, final String instruction,
				final String[] prompt, final boolean[] echo) {
			if (prompt.length != 1
					|| echo[0] != false) {
				return null;
			}
			String[] response = new String[1];
			response[0] = PASSWORD;
			return response;
		}
	}
	
	private JSch jsch;
	private Session session;
	private ChannelSftp sftp;
	
	public void testSftp() {
		for (int i = 0; i < 5; i++) {
			System.out.print("starting test... ");
			
			// stat
			SftpATTRS attrs = null;
			try {
				attrs = sftp.stat(PATH1);
			} catch (SftpException e) {
				assertTrue(e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE);
			} catch (Exception e) {
				fail(e.getLocalizedMessage());
			}
			assertNull(attrs);
			
			// mkdir
			
			try {
				sftp.mkdir(PATH1);
			} catch (Exception e) {
				fail(e.getLocalizedMessage());
			}
			attrs = null;
			try {
				attrs = sftp.stat(PATH1);
			} catch (Exception e) {
				fail(e.getLocalizedMessage());
			}
			assertNotNull(attrs);
			assertTrue(attrs.isDir());
			
			// test write
			attrs = null;
			try {
				attrs = sftp.stat(PATH2);
			} catch (SftpException e) {
				assertTrue(e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE);
			} catch (Exception e) {
				fail(e.getLocalizedMessage());
			}
			assertNull(attrs);
			
			try {
				OutputStream stream = sftp.put(PATH2);
				assertNotNull(stream);
				BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(stream));
				buf.write(TEST_STRING);
				buf.close();
			} catch (Exception e) {
				fail(e.getLocalizedMessage());
			}
			
			attrs = null;
			try {
				attrs = sftp.stat(PATH2);
			} catch (Exception e) {
				fail(e.getLocalizedMessage());
			}
			assertNotNull(attrs);
	
			// read
			try {
				InputStream stream = sftp.get(PATH2);
				assertNotNull(stream);
				BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
				String line = buf.readLine().trim();
				assertTrue(line.equals(TEST_STRING));
				buf.close();
			} catch (Exception e) {
				fail(e.getLocalizedMessage());
			}
			
			try {
				sftp.rm(PATH2);
			} catch (SftpException e) {
				fail(e.getLocalizedMessage());
			}
			
			try {
				sftp.rmdir(PATH1);
			} catch (SftpException e) {
				fail(e.getLocalizedMessage());
			}
			
			System.out.println("completed");
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		jsch = new JSch();
		session = jsch.getSession(USERNAME, HOST);
		session.setUserInfo(new SSHUserInfo());
		session.connect();
		sftp = (ChannelSftp) session.openChannel("sftp");
		sftp.connect();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		sftp.disconnect();
		session.disconnect();
	}
	
}
