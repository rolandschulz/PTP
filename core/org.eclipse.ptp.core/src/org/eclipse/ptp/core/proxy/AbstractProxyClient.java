/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.core.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;
import org.eclipse.ptp.core.proxy.event.ProxyConnectedEvent;
import org.eclipse.ptp.core.proxy.event.ProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public abstract class AbstractProxyClient {
	private String				sessHost = null;
	private int					sessPort = 0;
	private ServerSocketChannel	sessSvrSock = null;
	private SocketChannel			sessSock = null;
	private boolean				sessConnected = false;
	private boolean				exitThread;
	private Thread				eventThread;
	private Thread				acceptThread;
	protected List 				listeners = Collections.synchronizedList(new ArrayList());
	private static Charset		charset = Charset.forName("US-ASCII");
	private static CharsetEncoder	encoder = charset.newEncoder();
	private static CharsetDecoder	decoder = charset.newDecoder();
	
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
	
	public static String encodeString(String str) {
		if (str == null || str.length() == 0)
			return "1:00";
		
		byte[] b = str.getBytes();
		String res = Integer.toHexString(str.length()+1) + ":";
		for (int i = 0; i < b.length; i++) {
			if (b[i] < 10)
				res += "0";
			res += Integer.toHexString((int)b[i]);
		}
		
		return res + "00";
	}
	
	protected String encodeBitSet(BitList set) {
		String lenStr = Integer.toHexString(set.size());
		return lenStr + ":" + set.toString();
	}

	protected void sendCommand(String cmd) throws IOException {
		if (sessConnected) {
			String buf = encodeLength(cmd.length()) + " " + cmd;
			System.out.println("<" + buf + ">");
			fullWrite(encoder.encode(CharBuffer.wrap(buf)));
		}
	}

	protected void sendCommand(String cmd, String arg1) throws IOException {
		this.sendCommand(cmd + " " + encodeString(arg1));
	}

	protected void sendCommand(String cmd, String arg1, String arg2) throws IOException {
		this.sendCommand(cmd + " " + encodeString(arg1) + " " + encodeString(arg2));
	}

	protected void sendCommand(String cmd, String arg1, String arg2, String arg3) throws IOException {
		this.sendCommand(cmd + " " + encodeString(arg1) + " " + encodeString(arg2) + " " + encodeString(arg3));
	}

	protected void sendCommand(String cmd, String[] args) throws IOException {
		for (int i = 0; i < args.length; i++)
			cmd += " " + encodeString(args[i]);
		
		this.sendCommand(cmd);
	}

	protected void sendCommand(String cmd, String arg1, String[] args) throws IOException {
		cmd += " " + encodeString(arg1);
		
		for (int i = 0; i < args.length; i++)
			cmd += " " + encodeString(args[i]);
		
		this.sendCommand(cmd);
	}

	protected void sendCommand(String cmd, String arg1, String arg2, String[] args) throws IOException {
		cmd += " " + encodeString(arg1) + " " + encodeString(arg2);
		
		for (int i = 0; i < args.length; i++)
			cmd += " " + encodeString(args[i]);
		
		this.sendCommand(cmd);
	}

	protected void sendCommand(String cmd, String arg1, String arg2, String arg3, String[] args) throws IOException {
		cmd += " " + encodeString(arg1) + " " + encodeString(arg2) + " " + encodeString(arg3);
		
		for (int i = 0; i < args.length; i++)
			cmd += " " + encodeString(args[i]);
		
		this.sendCommand(cmd);
	}

	public void addEventListener(IProxyEventListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public void removeEventListener(IProxyEventListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public int sessionConnect() {
		return 0;
	}

	public void sessionCreate() throws IOException {
		sessionCreate(0);
	}
	
	public void sessionCreate(int port) throws IOException {
		System.out.println("sessionCreate("+port+")");
		sessSvrSock = ServerSocketChannel.open();
		InetSocketAddress isa = new InetSocketAddress(port);
		System.out.println("bind("+isa.toString()+")");
		sessSvrSock.socket().bind(isa);
		sessPort = sessSvrSock.socket().getLocalPort();
		sessHost = sessSvrSock.socket().getLocalSocketAddress().toString();
		System.out.println("port=" + sessPort);
		acceptThread = new Thread("Proxy Client Accept Thread") {
			public void run() {
				try {
					System.out.println("accept thread starting...");
					sessSock = sessSvrSock.accept();
					sessConnected = true;
					fireProxyEvent(new ProxyConnectedEvent());
					startEventThread();
				} catch (IOException e) {
					// TODO: what happens if the accept() fails?
				}
				try {
					sessSvrSock.close();
				} catch (IOException e) {
				}
				System.out.println("accept thread exiting...");
			}
		};
		acceptThread.start();
	}

	public int getSessionPort() {
		return sessPort;
	}
	
	public String getSessionHost() {
		return sessHost;
	}

	private void startEventThread() throws IOException {
		eventThread = new Thread("Proxy Client Event Thread") {
			public void run() {
				System.out.println("event thread starting...");
				try {
					exitThread = false;
					while (!exitThread) {
						sessionProgress();
					}
				} catch (IOException e) {
				} 
				sessConnected = false;
				try {
					sessSock.close();
				} catch (IOException e) {
				} 
				System.out.println("event thread exiting...");
			}
		};
		eventThread.start();
	}
	
	protected void fireProxyEvent(IProxyEvent event) {
		if (listeners == null)
			return;
		synchronized (listeners) {
			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				IProxyEventListener listener = (IProxyEventListener) i.next();
				listener.handleEvent(event);
			}
		}
	}
	
	private boolean fullRead(ByteBuffer buf) throws IOException {
		buf.clear();
		while (buf.remaining() > 0) {
			int n = sessSock.read(buf);
			if (n < 0)
				return false;
		}
		buf.flip();
		return true;
	}
	
	private boolean fullWrite(ByteBuffer buf) throws IOException {
		while (buf.remaining() > 0) {
			int n = sessSock.write(buf);
			if (n < 0)
				return false;
		}
		return true;
	}
	
	private void sessionProgress() throws IOException {
		ByteBuffer len_bytes = ByteBuffer.allocate(9);
		
		if (!fullRead(len_bytes)) {
			exitThread = true;
			return;
		}
		
		CharBuffer len_str = decoder.decode(len_bytes);
		int len = Integer.parseInt(len_str.subSequence(0, 8).toString(), 16);
		
		ByteBuffer event_bytes = ByteBuffer.allocate(len);

		if (!fullRead(event_bytes)) {
			exitThread = true;
			return;
		}

		CharBuffer event_str = decoder.decode(event_bytes);
		fireProxyEvent(ProxyEvent.toEvent(event_str.toString()));
	}

	public void sessionFinish() throws IOException {
		this.sendCommand("QUI");
		if (acceptThread.isAlive())
			acceptThread.interrupt();
	}
}
