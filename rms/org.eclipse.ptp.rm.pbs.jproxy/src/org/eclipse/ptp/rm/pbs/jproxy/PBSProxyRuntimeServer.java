/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation

 *******************************************************************************/

package org.eclipse.ptp.rm.pbs.jproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.jproxy.Controller;
import org.eclipse.ptp.proxy.jproxy.attributes.AttributeDefinition;
import org.eclipse.ptp.proxy.jproxy.element.IElement;
import org.eclipse.ptp.proxy.jproxy.event.JobEventFactory;
import org.eclipse.ptp.proxy.jproxy.event.NodeEventFactory;
import org.eclipse.ptp.proxy.jproxy.event.QueueEventFactory;
import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer;
import org.eclipse.ptp.proxy.runtime.server.ElementIDGenerator;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSJobClientAttributes;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSNodeClientAttributes;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSQueueClientAttributes;
import org.eclipse.ptp.rm.pbs.jproxy.parser.ModelQstatQueuesReader;
import org.eclipse.ptp.rm.pbs.jproxy.parser.UnmarshallingUtil;

public class PBSProxyRuntimeServer extends AbstractProxyRuntimeServer {

	private static final boolean debugReadFromFiles = true;
	private static final String debugFolder = "helics";
	// static final String debugUser = "alizade1";
	private static final String debugUser = "xli";
	
	public static void main(String[] args) {
		System.err.println(PBSProxyRuntimeServer.class.getSimpleName());

		// try {
		// System.err.println("PBSProxyRuntimeServer sleeps...");
		// Thread.currentThread().sleep(3000);
		// System.err.println("PBSProxyRuntimeServer continues...");
		// } catch (Exception e) {
		// e.printStackTrace();
		// };

		for (String arg : args) {
			System.out.println(arg);
		}

		int port = -1;
		String host = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--port")) {
				port = new Integer(args[i + 1]);
				i++;
			} else if (args[i].equals("--host")) {
				host = args[i + 1];
				i++;
			}
		}

		if (port == -1) {
			System.err.println("port argument missing");
			return;
		}
		if (host == null) {
			System.err.println("host argument missing");
			return;
		}

		PBSProxyRuntimeServer server = new PBSProxyRuntimeServer(host, port);

		try {
			server.connect();
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}
	
	private Controller nodeController;
	private Controller queueController;
	private Controller jobController;
	private String user = null;

	public PBSProxyRuntimeServer(String host, int port) {
		super(host, port, new ProxyRuntimeEventFactory());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#startEventThread(int)
	 */
	@Override
	public void startEventThread(final int transID) {

		int dummyMachineID = ElementIDGenerator.getInstance().getUniqueID();
		System.out.println("Proxy Server Event Thread started...");

		// System.err.println(base_ID);
		// System.err.println(getElementID());
		// System.err.println(getElementID(12));

		// MACHINES
		int resourceManagerID = ElementIDGenerator.getInstance().getBaseID();

		try {
			sendEvent(getEventFactory().newProxyRuntimeNewMachineEvent(
					transID,
					new String[] { Integer.toString(resourceManagerID), "1", Integer.toString(dummyMachineID), "2",
							"machineState=UP", "name=PBSdummy" }));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		nodeController = new Controller("pbsnodes -x", // command
				new AttributeDefinition(new PBSNodeClientAttributes()), // attributes
				new NodeEventFactory(), new UnmarshallingUtil(), // Parser
				dummyMachineID // BaseID
		);

		queueController = new Controller("qstat -Q -f -1", // command
				new AttributeDefinition(new PBSQueueClientAttributes()), // attributes
				new QueueEventFactory(), new ModelQstatQueuesReader(), // Parser
				resourceManagerID // BaseID
		);

		jobController = new Controller("qstat -x", // command
				new AttributeDefinition(new PBSJobClientAttributes()), // attributes
				new JobEventFactory(), new UnmarshallingUtil(), // Parser
				queueController // Parent
		);
		if (debugReadFromFiles) {
			nodeController.setDebug(debugFolder + "/pbsnodes_1.xml", debugFolder + "/pbsnodes_2.xml");
			queueController.setDebug(debugFolder + "/qstat_Q_1.xml", debugFolder + "/qstat_Q_2.xml");
			jobController.setDebug(debugFolder + "/qstat_1.xml", debugFolder + "/qstat_2.xml");
		}

		jobController.setFilter("job_owner", Pattern.quote(getUser()) + "@.*");

		if (getEventThread() == null) {
			Thread thread = new Thread() {

				public void run() {

					// Event Loop
					while (state != ServerState.SHUTDOWN) {
						{
							List<IProxyEvent> events = new ArrayList<IProxyEvent>();
							events.addAll(nodeController.update());
							events.addAll(queueController.update());
							events.addAll(jobController.update());
							try {
								for (IProxyEvent e : events) {
									e.setTransactionID(transID);
									sendEvent(e);
								}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						try {
							// System.err.println("Event Loop sleeps...");
							Thread.sleep(2000);
							// System.err.println("Event Loop continues...");
						} catch (Exception e) {
							e.printStackTrace();
						}
						;
					}
					System.err.println("Exit Proxy Server Event Loop");
				}

				// private String escape(String input, String what, String with)
				// {
				// if (input.contains(what)) input = input.replaceAll(what,
				// with);
				// return input;
				// }

				// private void sendEvent(PBSProxyRuntimeServer server,
				// IProxyRuntimeEvent ev) {
				// try {
				// server.sendEvent(ev);
				// } catch (IOException ex) {
				// ex.printStackTrace();
				// }
				// };
			};
			setEventThread(thread);
			getEventThread().start();
		}
	}

	// private boolean procDone(Process p) {
	// try {
	// int v = p.exitValue();
	// return true;
	// } catch (IllegalThreadStateException e) {
	// return false;
	// }
	// }

	/**
	 * @return
	 */
	private String getUser() {
		if (debugReadFromFiles) {
			user = debugUser;
		}
		if (user == null) {
			try {
				Process p = Runtime.getRuntime().exec("whoami");
				p.waitFor();
				user = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#terminateJob
	 * (java.lang.String[])
	 */
	@Override
	protected void terminateJob(String[] arguments) {
		// CHECK: threading issues of jobController.currentElements
		/*
		 * is probably OK because if ID is already gone - then job doesn't need
		 * to be terminated anymore. And it can't be that terminateJob is called
		 * before the job is in list - because it is in the list before the
		 * event is send (thus the UI doesn't know about he job earlier)
		 */
		//
		int id = Integer.parseInt(arguments[0].split("=")[1]);
		IElement job = jobController.currentElements.getElementByElementID(id);
		System.out.println("terminateJob: " + id + "," + job.getKey());
		String args[] = { "qdel", job.getKey() };
		try {
			Process p = Runtime.getRuntime().exec(args);
			p.waitFor();
			System.out.println(p.exitValue());
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("terminateJob: "+keyVal[1]);

	}

}
