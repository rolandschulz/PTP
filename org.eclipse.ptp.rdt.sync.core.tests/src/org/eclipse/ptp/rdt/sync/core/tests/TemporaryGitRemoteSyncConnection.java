package org.eclipse.ptp.rdt.sync.core.tests;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.eclipse.ptp.rdt.sync.git.core.GitRemoteSyncConnection;
import org.eclipse.ptp.rdt.sync.git.core.SyncFileFilter;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import org.eclipse.core.filesystem.EFS;

public class TemporaryGitRemoteSyncConnection extends ExternalResource {
	private GitRemoteSyncConnection fGITConn;
	private IRemoteConnection fRemoteConnection;
	private TemporaryFolder localFolder;
	private String remoteFolder;
	private static Random random = new Random();
	private IRemoteFileManager fileManager;
	
	public TemporaryGitRemoteSyncConnection(IRemoteConnection remoteConnection, String remoteBaseDir) {
		fRemoteConnection = remoteConnection;
		fileManager = fRemoteConnection.getRemoteServices().getFileManager(fRemoteConnection);
		long n = random.nextInt(1000000);
		remoteFolder = remoteBaseDir + "/junit" + n;

	}

	@Override
	protected void before() throws Throwable {
		create();
	}

	@Override
	protected void after() {
		delete();
	}
	
	public GitRemoteSyncConnection getGITConn() {
		return fGITConn;
	}
	
	private void create() throws Exception {
		localFolder = new TemporaryFolder();
		localFolder.create();
		
		
		if (!fRemoteConnection.isOpen()) {
			fRemoteConnection.open(null);
		}
		
		fileManager.getResource(remoteFolder).delete(EFS.NONE, null);
		
		fGITConn = new GitRemoteSyncConnection(fRemoteConnection,
				localFolder.getRoot().getPath(), remoteFolder,
				new SyncFileFilter() {
					public boolean shouldIgnore(String fileName) {
						if (fileName.startsWith(GitRemoteSyncConnection.gitDir)) {
							return true;
						}
						return false;
					}
				}, null);
	}

	private void delete()  {
		fGITConn.close();
		
		/*Delete Remote folder*/
		try {
			fileManager.getResource(remoteFolder).delete(EFS.NONE, null);
		} catch (Exception e){
			//TODO: What should we do here? Method "after" is not allowed to throw.    
			e.printStackTrace();
			assertTrue(false);
		}
		
		localFolder.delete();
	}


	
	
}
