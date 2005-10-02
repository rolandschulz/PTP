package org.eclipse.ptp.core.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;
import org.eclipse.ptp.core.proxy.event.ProxyEvent;

public abstract class AbstractProxyClient {
	private String				sessHost;
	private int					sessPort;
	private ServerSocket			sessSvrSock;
	private Socket				sessSock;
	private OutputStreamWriter	sessOut;
	private InputStreamReader		sessIn;
	private boolean				exitThread;
	private Thread				eventThread;
	protected List				listeners = new ArrayList(2);

	public AbstractProxyClient(String host, int port) {
		this.sessHost = host;
		this.sessPort = port;
	}
	
	private String encodeLength(int val) {
		char[] res = new char[8];
		String str = Integer.toHexString(val);
		int rem = 8 - str.length();
		
		for (int i = 0 ; i < 8 ; i++) {
			if (i < rem)
				res[i] = '0';
			else
				res[i] = str.charAt(i - rem);
		}
		return String.valueOf(res);
	}
	
	protected void SendCommand(String cmd) throws IOException {
		if (sessSock != null && sessSock.isConnected()) {
			String buf = encodeLength(cmd.length()) + " " + cmd;
			System.out.println("<" + buf + ">");
			sessOut.write(buf);
			sessOut.flush();
		}
	}
	
	public void addEventListener(IProxyEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeEventListener(IProxyEventListener listener) {
		listeners.remove(listener);
	}
	
	public int sessionConnect() {
		return 0;
	}
	
	public void sessionCreate() throws IOException {
		sessSvrSock = new ServerSocket(sessPort);
	}

	public void sessionAccept() throws IOException {
		sessSock = sessSvrSock.accept();
		sessOut = new OutputStreamWriter(sessSock.getOutputStream());
		sessIn = new InputStreamReader(sessSock.getInputStream());
		eventThread = new Thread("Proxy Client Event Thread") {
			public void run() {
				try {
					exitThread = false;
					while (!exitThread) {
						sessionProgress();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		eventThread.start();
	}
	
	protected synchronized void fireEvent(IProxyEvent event) {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IProxyEventListener listener = (IProxyEventListener) i.next();
			listener.fireEvent(event);
		}
	}
	
	public void sessionProgress() throws IOException {
		char[] len_bytes = new char[9];
		
		sessIn.read(len_bytes, 0, 9);
		String len_str = new String(len_bytes, 0, 8);
		int len = Integer.parseInt(len_str, 16);
		
		char[] event_bytes = new char[len];
		sessIn.read(event_bytes, 0, len);
		String event_str = new String(event_bytes);
		
		IProxyEvent e = convertEvent(event_str);
		if (e == null)
			e = ProxyEvent.toEvent(event_str);
		
		fireEvent(e);
	}

	public void sessionFinish() throws IOException {
		SendCommand("QUI");
	}
	
	public abstract IProxyEvent convertEvent(String str);
}
