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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer;
import org.eclipse.ptp.proxy.runtime.server.ElementIDGenerator;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSJobClientAttributes;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSNodeClientAttributes;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSQueueClientAttributes;
import org.eclipse.ptp.rm.pbs.jproxy.parser.QstatJobXMLReader;
import org.eclipse.ptp.rm.pbs.jproxy.parser.QstatQueuesReader;
import org.eclipse.ptp.rm.proxy.core.Controller;
import org.eclipse.ptp.rm.proxy.core.attributes.AttributeDefinition;
import org.eclipse.ptp.rm.proxy.core.element.IElement;
import org.eclipse.ptp.rm.proxy.core.event.JobEventFactory;
import org.eclipse.ptp.rm.proxy.core.event.NodeEventFactory;
import org.eclipse.ptp.rm.proxy.core.event.QueueEventFactory;
import org.eclipse.ptp.rm.proxy.core.parser.XMLReader;

/**
 * Proxy for PBS.
 */
public class PBSProxyRuntimeServer extends AbstractProxyRuntimeServer {

	private static final boolean debugReadFromFiles = false;
	private static final String debugFolder = "helics"; //$NON-NLS-1$
	// static final String debugUser = "alizade1";
	private static final String debugUser = "xli"; //$NON-NLS-1$

	public static void main(String[] args) {
		Map<String, Object> params = parseArguments(args);
		String host = (String) params.get("host"); //$NON-NLS-1$
		if (host == null) {
			System.err.println(Messages.getString("PBSProxyRuntimeServer.1")); //$NON-NLS-1$
			return;
		}
		int port = 0;
		Integer portVal = ((Integer) params.get("port")); //$NON-NLS-1$
		if (portVal == null) {
			System.err.println(Messages.getString("PBSProxyRuntimeServer.3")); //$NON-NLS-1$
			return;
		}
		port = portVal.intValue();

		PBSProxyRuntimeServer server = new PBSProxyRuntimeServer(host, port);

		try {
			server.connect();
			System.out.println(PBSProxyRuntimeServer.class.getSimpleName() + Messages.getString("PBSProxyRuntimeServer.4")); //$NON-NLS-1$
			server.start();
		} catch (IOException e) {
			System.err.println(Messages.getString("PBSProxyRuntimeServer.5") + e.getMessage()); //$NON-NLS-1$
			return;
		}
	}

	private static String normalize(String content) {
		content = content.replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll("\\\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll("\\\\s", " "); //$NON-NLS-1$ //$NON-NLS-2$
		return content;
	}

	/**
	 * @since 4.0
	 */
	protected static Map<String, Object> parseArguments(String args[]) {
		Map<String, Object> argsMap = new HashMap<String, Object>();

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("--port")) { //$NON-NLS-1$
				try {
					int port = new Integer(args[i].substring(7));
					argsMap.put("port", port); //$NON-NLS-1$
				} catch (NumberFormatException e) {
					System.err.println(org.eclipse.ptp.proxy.messages.Messages.AbstractProxyRuntimeServer_0
							+ args[i + 1].substring(7));
				}
			} else if (args[i].startsWith("--host")) { //$NON-NLS-1$
				String host = args[i].substring(7);
				if (host != null) {
					argsMap.put("host", host); //$NON-NLS-1$
				}
			}
		}

		return argsMap;
	}

	private final Controller nodeController;

	private final Controller queueController;

	private final Controller jobController;

	String user = null;

	private PBSProxyRuntimeServer(String host, int port) {
		super(host, port, new ProxyRuntimeEventFactory());

		nodeController = new Controller("pbsnodes -x", // command //$NON-NLS-1$
				new AttributeDefinition(new PBSNodeClientAttributes()), // attributes.
				// TODO:
				// should
				// include
				// a
				// flag
				// whether
				// mandatory.
				new NodeEventFactory(), new XMLReader() // Parser
		);

		queueController = new Controller("qstat -Q -f -1", // command //$NON-NLS-1$
				new AttributeDefinition(new PBSQueueClientAttributes()), // attributes
				new QueueEventFactory(), new QstatQueuesReader() // Parser
		);

		jobController = new Controller("qstat -x", // command //$NON-NLS-1$
				new AttributeDefinition(new PBSJobClientAttributes()), // attributes
				new JobEventFactory(), new QstatJobXMLReader(), // Parser
				queueController // Parent
		);

		if (debugReadFromFiles) {
			nodeController.setDebug(debugFolder + "/pbsnodes_1.xml", //$NON-NLS-1$
					debugFolder + "/pbsnodes_2.xml"); //$NON-NLS-1$
			queueController.setDebug(debugFolder + "/qstat_Q_1.xml", //$NON-NLS-1$
					debugFolder + "/qstat_Q_2.xml"); //$NON-NLS-1$
			jobController.setDebug(debugFolder + "/qstat_1.xml", debugFolder //$NON-NLS-1$
					+ "/qstat_2.xml"); //$NON-NLS-1$
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

	private String getUser() {
		if (debugReadFromFiles) {
			user = debugUser;
		}
		if (user == null) {
			try {
				Process p = Runtime.getRuntime().exec("whoami"); //$NON-NLS-1$
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
	 * @see org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#
	 * initServer()
	 */
	@Override
	protected void initServer() throws Exception {
		// Test whether all programs and parser work
		nodeController.parse();
		queueController.parse();
		jobController.parse();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#
	 * startEventThread(int)
	 */
	@Override
	protected void startEventThread(final int transID) {

		int machineID = ElementIDGenerator.getInstance().getUniqueID();
		System.out.println(Messages.getString("PBSProxyRuntimeServer.2")); //$NON-NLS-1$

		// System.err.println(base_ID);
		// System.err.println(getElementID());
		// System.err.println(getElementID(12));

		// MACHINES
		int resourceManagerID = ElementIDGenerator.getInstance().getBaseID();

		try {
			Process p = Runtime.getRuntime().exec("qstat -B -f -1");//$NON-NLS-1$ //TODO: read std-err for errors
			p.waitFor();
			String server = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
			if (server==null || server.split(" ").length<2) //$NON-NLS-1$
				server = "UNKNOWN"; //$NON-NLS-1$
			else
				server = server.split(" ")[1]; //$NON-NLS-1$
			sendEvent(getEventFactory().newProxyRuntimeNewMachineEvent(transID,
					new String[] { Integer.toString(resourceManagerID), "1", Integer.toString(machineID), //$NON-NLS-1$
					"2", //$NON-NLS-1$
					"machineState=UP", //$NON-NLS-1$
					"name=" + server //$NON-NLS-1$
			}));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		nodeController.setBaseID(machineID);
		queueController.setBaseID(resourceManagerID);

		jobController.setFilter("job_owner", Pattern.quote(getUser()) + "@.*"); //$NON-NLS-1$ //$NON-NLS-2$

		// TODO: the following could be moved to the abstract class.
		if (eventThread == null) {
			eventThread = new Thread() {

				@Override
				public void run() {

					// Event Loop
					while (state != ServerState.SHUTDOWN) {
						{
							List<IProxyEvent> events = new ArrayList<IProxyEvent>();
							try {
								try {
									events.addAll(nodeController.update());
									events.addAll(queueController.update());
									events.addAll(jobController.update());
								} catch (Exception e) {
									e.printStackTrace();
									sendEvent(getEventFactory().newErrorEvent(transID, 0, e.getMessage()));
								}
								for (IProxyEvent e : events) {
									e.setTransactionID(transID);
									sendEvent(e);
								}
							} catch (IOException e1) {
								e1.printStackTrace();
								System.err.println(Messages.getString("PBSProxyRuntimeServer.6")); //$NON-NLS-1$
								state = ServerState.SHUTDOWN;
								stateMachineThread.interrupt();
							}
						}
						try {
							// System.err.println("Event Loop sleeps...");
							Thread.sleep(2000);
							// System.err.println("Event Loop continues...");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					System.err.println(Messages.getString("PBSProxyRuntimeServer.0")); //$NON-NLS-1$
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
			eventThread.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#submitJob
	 * (int, java.lang.String[])
	 */
	@Override
	protected void submitJob(int transID, String[] arguments) {

		String jobSubId = null;
		String script = null;
		// Insert values into template and store special parameters
		for (String argument : arguments) {
			String[] keyValue = argument.split("=", 2); //$NON-NLS-1$
			String key = keyValue[0];
			String value = keyValue[1];
			if (key.equals("jobSubId")) { //$NON-NLS-1$
				jobSubId = value;
			} else if (key.equals("script")) { //$NON-NLS-1$ // any other parameter is used for the template
				script = normalize(value);
			} /*
			 * else { System.out.println("Parameter " + key + " with value \""
			 * //$NON-NLS-1$ //$NON-NLS-2$ + value +
			 * "\" not used in template."); //$NON-NLS-1$ }
			 */
		}

		if (jobSubId == null || script == null) {
			System.err.println("missing arguments!");//$NON-NLS-1$
			return;
		}

		// Write template into job-script as a temporary file
		File tmp = null;
		try {
			tmp = File.createTempFile("job", "qsub"); //$NON-NLS-1$ //$NON-NLS-2$
			//			System.out.println("script: "+tmp); //$NON-NLS-1$
			// System.out.println(script);
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write(script);
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Call Qsub with job-script
		String args[] = { "qsub", tmp.getAbsolutePath() }; //$NON-NLS-1$
		Process p = null;
		try {
			p = new ProcessBuilder(args).redirectErrorStream(true).start();
			p.waitFor();

			try {
				//System.out.println("submitJob: exit:" + p.exitValue()); //$NON-NLS-1$
				// Check that is was succesful
				if (p.exitValue() == 0) {
					sendEvent(getEventFactory().newOKEvent(transID));
				} else {
					// if error get error messaes from stderr
					BufferedReader err = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line, errMsg = ""; //$NON-NLS-1$
					while ((line = err.readLine()) != null) {
						errMsg += line;
					}
					err.close();
					String errArgs[] = { "jobSubId=" + jobSubId, //$NON-NLS-1$
							"errorCode=" + 0, //$NON-NLS-1$
							// p.exitValue()
							"errorMsg=" + errMsg }; //$NON-NLS-1$
					sendEvent(getEventFactory().newProxyRuntimeSubmitJobErrorEvent(transID, errArgs)); // TODO:
					// document
					// in
					// wiki
					// -
					// following
					// here
					// proxy_event:proxy_submitjob_error_event
					System.out.println("submitJob: err: " + errMsg); //$NON-NLS-1$
				}
			} catch (IOException e1) { // sendEvent, readLine
				e1.printStackTrace();
			}

		} catch (IOException e) { // exec
			String errArgs[] = { "jobSubId=" + jobSubId, "errorCode=" + 0, "errorMsg=" + e.getMessage() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				System.out.println("SubmitJobError: " + e.getMessage()); //$NON-NLS-1$
				sendEvent(getEventFactory().newProxyRuntimeSubmitJobErrorEvent(transID, errArgs));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// e.printStackTrace();
		} catch (InterruptedException e) { // waitFor
			e.printStackTrace();
		}

		tmp.delete();
		// System.out.print(template);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#terminateJob
	 * (int, java.lang.String[])
	 */
	@Override
	protected void terminateJob(int transID, String[] arguments) {
		// CHECK: threading issues of jobController.currentElements
		/*
		 * is probably OK because if ID is already gone - then job doesn't need
		 * to be terminated anymore. And it can't be that terminateJob is called
		 * before the job is in list - because it is in the list before the
		 * event is send (thus the UI doesn't know about he job earlier)
		 */
		//
		int id = Integer.parseInt(arguments[0].split("=")[1]); //$NON-NLS-1$
		IElement job = jobController.currentElements.getElementByElementID(id);
		System.out.println(Messages.getString("PBSProxyRuntimeServer.25") + id + "," + job.getKey()); //$NON-NLS-1$ //$NON-NLS-2$
		String args[] = { "qdel", job.getKey() }; //$NON-NLS-1$
		try {
			Process p = new ProcessBuilder(args).redirectErrorStream(true).start();
			p.waitFor();
			if (p.exitValue() == 0) {
				sendEvent(getEventFactory().newOKEvent(transID));
			} else {
				BufferedReader err = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line, errMsg = ""; //$NON-NLS-1$
				while ((line = err.readLine()) != null) {
					errMsg += line;
				}
				err.close();
				String errArgs[] = { "errorCode=" + p.exitValue(), "errorMsg=" + errMsg }; //$NON-NLS-1$ //$NON-NLS-2$
				sendEvent(getEventFactory().newErrorEvent(transID, errArgs));
			}
			System.out.println(p.exitValue());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// System.out.println("terminateJob: "+keyVal[1]);

	}

}
