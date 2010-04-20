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

		Map<String, Object> params = parseArguments(args);
		if (params == null) {
			return;
		}
		new PBSProxyRuntimeServer((String) params.get("host"), (Integer) params //$NON-NLS-1$
				.get("port")); //$NON-NLS-1$
	}

	private Controller nodeController;
	private Controller queueController;

	private Controller jobController;

	String user = null;

	private PBSProxyRuntimeServer(String host, int port) {
		super(host, port, new ProxyRuntimeEventFactory());
	}

	private String getUser() {
		if (debugReadFromFiles) {
			user = debugUser;
		}
		if (user == null) {
			try {
				Process p = Runtime.getRuntime().exec("whoami"); //$NON-NLS-1$
				p.waitFor();
				user = new BufferedReader(new InputStreamReader(p
						.getInputStream())).readLine();
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

	// private boolean procDone(Process p) {
	// try {
	// int v = p.exitValue();
	// return true;
	// } catch (IllegalThreadStateException e) {
	// return false;
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#
	 * startEventThread(int)
	 */
	@Override
	protected void startEventThread(final int transID) {

		int dummyMachineID = ElementIDGenerator.getInstance().getUniqueID();
		System.out.println(Messages.getString("PBSProxyRuntimeServer.2")); //$NON-NLS-1$

		// System.err.println(base_ID);
		// System.err.println(getElementID());
		// System.err.println(getElementID(12));

		// MACHINES
		int resourceManagerID = ElementIDGenerator.getInstance().getBaseID();

		try {
			sendEvent(getEventFactory().newProxyRuntimeNewMachineEvent(
					transID,
					new String[] { Integer.toString(resourceManagerID),
							"1", Integer.toString(dummyMachineID), //$NON-NLS-1$
							"2", //$NON-NLS-1$
							"machineState=UP", //$NON-NLS-1$
							"name=PBSdummy" //$NON-NLS-1$
					}));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		nodeController = new Controller("pbsnodes -x", // command //$NON-NLS-1$
				new AttributeDefinition(new PBSNodeClientAttributes()), // attributes.
																		// TODO:
																		// should
																		// include
																		// a
																		// flag
																		// whether
																		// mandatory.
				new NodeEventFactory(), new XMLReader(), // Parser
				dummyMachineID // BaseID
		);

		queueController = new Controller("qstat -Q -f -1", // command //$NON-NLS-1$
				new AttributeDefinition(new PBSQueueClientAttributes()), // attributes
				new QueueEventFactory(), new QstatQueuesReader(), // Parser
				resourceManagerID // BaseID
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
					System.err.println(Messages
							.getString("PBSProxyRuntimeServer.0")); //$NON-NLS-1$
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

		String template = "#!/bin/sh\n" + //$NON-NLS-1$
				"#PBS -A @Account_Name@\n" //$NON-NLS-1$
				+ "#PBS -l walltime=@Resource_List.walltime@,nodes=@Resource_List.nodes@\n" //$NON-NLS-1$
				+ // "#PBS -d @workingDir@\n" + //not supported by all PBS
					// version
					// (e.g. abe)
				"#PBS -N @Job_Name@\n" + //$NON-NLS-1$
				"cd @workingDir@\n" + //$NON-NLS-1$
				"mpiexec @execPath@/@execName@\n"; //$NON-NLS-1$

		String jobSubId = null;
		// Insert values into template and store special parameters
		for (String argument : arguments) {
			String[] keyValue = argument.split("=", 2); //$NON-NLS-1$
			if (keyValue[0].equals("jobSubId")) {
				jobSubId = keyValue[1];
			} else { // any other parameter is used for the template
				template = template.replaceAll(
						"@" + keyValue[0] + "@", keyValue[1]); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// Write template into job-script as a temporary file
		File tmp = null;
		try {
			tmp = File.createTempFile("job", "qsub"); //$NON-NLS-1$ //$NON-NLS-2$
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write(template);
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Call Qsub with job-script
		String args[] = { "qsub", tmp.getAbsolutePath() }; //$NON-NLS-1$
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(args);
			p.waitFor();

			try {
				System.out.println("submitJob: exit:" + p.exitValue()); //$NON-NLS-1$
				// Check that is was succesful
				if (p.exitValue() == 0) {
					sendEvent(getEventFactory().newOKEvent(transID));
				} else {
					// if error get error messaes from stderr
					BufferedReader err = new BufferedReader(
							new InputStreamReader(p.getErrorStream()));
					String line, errMsg = ""; //$NON-NLS-1$
					while ((line = err.readLine()) != null) {
						errMsg += line;
					}
					String errArgs[] = { "jobSubId=" + jobSubId, //$NON-NLS-1$
							"errorCode=" + 0, //$NON-NLS-1$
							// p.exitValue()
							"errorMsg=" + errMsg }; //$NON-NLS-1$ 
					sendEvent(getEventFactory()
							.newProxyRuntimeSubmitJobErrorEvent(transID,
									errArgs)); // TODO: document in wiki -
												// following here
												// proxy_event:proxy_submitjob_error_event
					System.out.println("submitJob: err: " + errMsg); //$NON-NLS-1$
				}
			} catch (IOException e1) { // sendEvent, readLine
				e1.printStackTrace();
			}

		} catch (IOException e) { // exec
			String errArgs[] = {
					"jobSubId=" + jobSubId, "errorCode=" + 0, "errorMsg=" + e.getMessage() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				System.out.println("SubmitJobError: " + e.getMessage()); //$NON-NLS-1$
				sendEvent(getEventFactory().newProxyRuntimeSubmitJobErrorEvent(
						transID, errArgs));
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
		System.out
				.println(Messages.getString("PBSProxyRuntimeServer.25") + id + "," + job.getKey()); //$NON-NLS-1$ //$NON-NLS-2$
		String args[] = { "qdel", job.getKey() }; //$NON-NLS-1$
		try {
			Process p = Runtime.getRuntime().exec(args);
			p.waitFor();
			if (p.exitValue() == 0) {
				sendEvent(getEventFactory().newOKEvent(transID));
			} else {
				BufferedReader err = new BufferedReader(new InputStreamReader(p
						.getErrorStream()));
				String line, errMsg = ""; //$NON-NLS-1$
				while ((line = err.readLine()) != null) {
					errMsg += line;
				}
				String errArgs[] = {
						"errorCode=" + p.exitValue(), "errorMsg=" + errMsg }; //$NON-NLS-1$ //$NON-NLS-2$
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
