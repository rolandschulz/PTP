/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation
 *    Benjamin Lindner (ben@benlabs.net) - Attribute Definitions and Mapping (bug 316671)
 *    Albert L. Rossi (arossi@ncsa.illinois.edu) - Added listener functionality and volatile map
 *                    to be able to associate client-generated jobSubId with the New Job event (10/11/2010)
 *******************************************************************************/

package org.eclipse.ptp.rm.pbs.jproxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent.Level;
import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.runtime.server.ElementIDGenerator;
import org.eclipse.ptp.rm.pbs.jproxy.parser.AttributeDefinitionReader;
import org.eclipse.ptp.rm.pbs.jproxy.parser.AttributeKeyMapReader;
import org.eclipse.ptp.rm.pbs.jproxy.parser.AttributeValueMapReader;
import org.eclipse.ptp.rm.pbs.jproxy.parser.QstatJobXMLReader;
import org.eclipse.ptp.rm.pbs.jproxy.parser.QstatQueuesReader;
import org.eclipse.ptp.rm.pbs.jproxy.parser.RequiredAttributeKeyReader;
import org.eclipse.ptp.rm.proxy.core.AbstractRMProxyRuntimeServer;
import org.eclipse.ptp.rm.proxy.core.Controller;
import org.eclipse.ptp.rm.proxy.core.element.IElement;
import org.eclipse.ptp.rm.proxy.core.event.JobEventFactory;
import org.eclipse.ptp.rm.proxy.core.event.NodeEventFactory;
import org.eclipse.ptp.rm.proxy.core.event.QueueEventFactory;
import org.eclipse.ptp.rm.proxy.core.parser.XMLReader;

/**
 * Proxy for PBS.
 */
public class PBSProxyRuntimeServer extends AbstractRMProxyRuntimeServer {
	private static final int STREAM_BUFFER_SIZE = 1024;
	private static final int EOF = -1;
	private static final boolean debugReadFromFiles = false;
	private static final String debugFolder = "helics" + File.separator; //$NON-NLS-1$
	// static final String debugUser = "alizade1";
	private static final String debugUser = "xli"; //$NON-NLS-1$

	private final Controller nodeController;

	private final Controller queueController;

	private final Controller jobController;

	private final Map<String, String> jobIdBindings = Collections.synchronizedMap(new HashMap<String, String>());

	String user = null;

	private PBSProxyRuntimeServer(String host, int port) {
		super(host, port, new ProxyRuntimeEventFactory());
		nodeController = new Controller("pbsnodes -x", // command //$NON-NLS-1$
				// TODO: should include a flag whether mandatory.
				new NodeEventFactory(), new XMLReader() // Parser
		);

		queueController = new Controller("qstat -Q -f -1", // command //$NON-NLS-1$
				new QueueEventFactory(), new QstatQueuesReader() // Parser
		);

		jobController = new Controller("qstat -x", // command //$NON-NLS-1$
				new JobEventFactory(), new QstatJobXMLReader(), // Parser
				queueController // Parent
		);

		jobController.setEventArgumentsHandler(new Controller.EventArgumentsHandler() {
			public void handle(List<String> eventArgs) {
				for (Iterator<String> i = eventArgs.iterator(); i.hasNext();) {
					String arg = i.next();
					if (arg.startsWith("PBSJOB_NAME=")) { //$NON-NLS-1$
						String batchId = arg.split("=")[1];//$NON-NLS-1$
						String jobSubId = jobIdBindings.remove(batchId);
						if (jobSubId != null) {
							int curr = Integer.parseInt(eventArgs.get(3));
							eventArgs.add("jobSubId=" + jobSubId);//$NON-NLS-1$
							eventArgs.set(3, Integer.toString(curr + 1));
						}
						break;
					}
				}
			}
		});

		if (debugReadFromFiles) {
			nodeController.setDebug(debugFolder + "pbsnodes_1.xml", //$NON-NLS-1$
					debugFolder + "pbsnodes_2.xml"); //$NON-NLS-1$
			queueController.setDebug(debugFolder + "qstat_Q_1.xml", //$NON-NLS-1$
					debugFolder + "qstat_Q_2.xml"); //$NON-NLS-1$
			jobController.setDebug(debugFolder + "qstat_1.xml", debugFolder //$NON-NLS-1$
					+ "qstat_2.xml"); //$NON-NLS-1$
		}

	}

	@Override
	protected List<IAttributeDefinition<?, ?, ?>> detectAttributeDefinitions() {

		// detect PBS specific attributes here
		// translate and add them for the attribute manager
		List<IAttributeDefinition<?, ?, ?>> attrDefList = new ArrayList<IAttributeDefinition<?, ?, ?>>();

		// query the controllers for a list of their assigned attributes
		attrDefList.addAll(jobController.getAttributeDefinitions());
		attrDefList.addAll(nodeController.getAttributeDefinitions());
		attrDefList.addAll(queueController.getAttributeDefinitions());

		return attrDefList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#
	 * initServer()
	 */
	@Override
	protected void initServer() throws Exception {
		Controller.ErrorHandler handler = new Controller.ErrorHandler() {
			public void handle(Level level, String msg) {
				try {
					sendEvent(getEventFactory().newProxyRuntimeMessageEvent(level, msg));
				} catch (IOException e) {
					e.printStackTrace(); // sendEvent failed - can't signal UI
				}
			}
		};

		// initialize Controllers:
		// Attribute Defintions
		InputStream AttrDefFile = getClass().getClassLoader().getResourceAsStream("PBSAttributes/Definitions.txt"); //$NON-NLS-1$
		if (AttrDefFile == null) {
			System.out.println("Unable to locate PBSAttributes/Definitions.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		List<IAttributeDefinition<?, ?, ?>> AttributeDefinitions = AttributeDefinitionReader.parse(AttrDefFile);
		nodeController.setAttributeDefinitions(AttributeDefinitions);
		queueController.setAttributeDefinitions(AttributeDefinitions);
		jobController.setAttributeDefinitions(AttributeDefinitions);

		// Required PBS Keys for an entry to be complete
		InputStream nodeParserRequiredKeyStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/RequiredPBSKeys-node.txt"); //$NON-NLS-1$
		InputStream queueParserRequiredKeyStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/RequiredPBSKeys-queue.txt"); //$NON-NLS-1$
		InputStream jobParserRequiredKeyStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/RequiredPBSKeys-job.txt"); //$NON-NLS-1$
		if (nodeParserRequiredKeyStream == null) {
			System.out.println("Unable to locate PBSAttributes/RequiredPBSKeys-node.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		if (queueParserRequiredKeyStream == null) {
			System.out.println("Unable to locate PBSAttributes/RequiredPBSKeys-queue.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		if (jobParserRequiredKeyStream == null) {
			System.out.println("Unable to locate PBSAttributes/RequiredPBSKeys-job.txt"); //$NON-NLS-1$
			throw new Exception();
		}

		nodeController.setRequiredAttributeKeys(RequiredAttributeKeyReader.parse(nodeParserRequiredKeyStream));
		queueController.setRequiredAttributeKeys(RequiredAttributeKeyReader.parse(queueParserRequiredKeyStream));
		jobController.setRequiredAttributeKeys(RequiredAttributeKeyReader.parse(jobParserRequiredKeyStream));

		// Parser2PBS KeyMap
		InputStream nodeParserKeyMapStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/Parser2PBS-KeyMap-node.txt"); //$NON-NLS-1$
		InputStream queueParserKeyMapStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/Parser2PBS-KeyMap-queue.txt"); //$NON-NLS-1$
		InputStream jobParserKeyMapStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/Parser2PBS-KeyMap-job.txt"); //$NON-NLS-1$
		if (nodeParserKeyMapStream == null) {
			System.out.println("Unable to locate PBSAttributes/Parser2PBS-KeyMap-node.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		if (queueParserKeyMapStream == null) {
			System.out.println("Unable to locate PBSAttributes/Parser2PBS-KeyMap-queue.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		if (jobParserKeyMapStream == null) {
			System.out.println("Unable to locate PBSAttributes/Parser2PBS-KeyMap-job.txt"); //$NON-NLS-1$
			throw new Exception();
		}

		nodeController.setParserKeyMap(AttributeKeyMapReader.parse(nodeParserKeyMapStream));
		queueController.setParserKeyMap(AttributeKeyMapReader.parse(queueParserKeyMapStream));
		jobController.setParserKeyMap(AttributeKeyMapReader.parse(jobParserKeyMapStream));

		// Parser2PBS ValueMap
		InputStream nodeParserValueMapStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/Parser2PBS-ValueMap-node.txt"); //$NON-NLS-1$
		InputStream queueParserValueMapStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/Parser2PBS-ValueMap-queue.txt"); //$NON-NLS-1$
		InputStream jobParserValueMapStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/Parser2PBS-ValueMap-job.txt"); //$NON-NLS-1$
		if (nodeParserValueMapStream == null) {
			System.out.println("Unable to locate PBSAttributes/Parser2PBS-ValueMap-node.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		if (queueParserValueMapStream == null) {
			System.out.println("Unable to locate PBSAttributes/Parser2PBS-ValueMap-queue.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		if (jobParserValueMapStream == null) {
			System.out.println("Unable to locate PBSAttributes/Parser2PBS-ValueMap-job.txt"); //$NON-NLS-1$
			throw new Exception();
		}

		nodeController.setParserValueMap(AttributeValueMapReader.parse(nodeParserValueMapStream));
		queueController.setParserValueMap(AttributeValueMapReader.parse(queueParserValueMapStream));
		jobController.setParserValueMap(AttributeValueMapReader.parse(jobParserValueMapStream));

		// PBS2Protocol KeyMap
		InputStream ProtocolKeyMapStream = getClass().getClassLoader().getResourceAsStream("PBSAttributes/PBS2Protocol-KeyMap.txt"); //$NON-NLS-1$
		List<List<Object>> ProtocolKeyMap = AttributeKeyMapReader.parse(ProtocolKeyMapStream);
		if (ProtocolKeyMap == null) {
			System.out.println("Unable to locate PBSAttributes/PBS2Protocol-KeyMap.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		nodeController.setProtocolKeyMap(ProtocolKeyMap);
		queueController.setProtocolKeyMap(ProtocolKeyMap);
		jobController.setProtocolKeyMap(ProtocolKeyMap);

		// PBS2Protocol ValueMap
		InputStream ProtocolValueMapStream = getClass().getClassLoader().getResourceAsStream(
				"PBSAttributes/PBS2Protocol-ValueMap.txt"); //$NON-NLS-1$
		List<List<Object>> ProtocolValueMap = AttributeValueMapReader.parse(ProtocolValueMapStream);
		if (ProtocolValueMap == null) {
			System.out.println("Unable to locate PBSAttributes/PBS2Protocol-ValueMap.txt"); //$NON-NLS-1$
			throw new Exception();
		}
		nodeController.setProtocolValueMap(ProtocolValueMap);
		queueController.setProtocolValueMap(ProtocolValueMap);
		jobController.setProtocolValueMap(ProtocolValueMap);

		// set key identifiers
		nodeController.setElementKeyID("PBSNODE_NAME"); //$NON-NLS-1$
		nodeController.setParentKeyID(null);

		queueController.setElementKeyID("PBSQUEUE_NAME"); //$NON-NLS-1$
		queueController.setParentKeyID(null);

		jobController.setElementKeyID("PBSJOB_NAME"); //$NON-NLS-1$
		jobController.setParentKeyID("PBSJOB_QUEUE"); //$NON-NLS-1$

		nodeController.setErrorHandler(handler);
		queueController.setErrorHandler(handler);
		jobController.setErrorHandler(handler);

		// Test whether all programs and parser work
		queueController.parse();
		nodeController.parse();
		jobController.parse();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer#
	 * startEventThread(int)
	 */
	@Override
	protected Thread startEventThread(final int transID) {
		int machineID = ElementIDGenerator.getInstance().getUniqueID();
		System.out.println(Messages.getString("PBSProxyRuntimeServer.2")); //$NON-NLS-1$

		// System.err.println(base_ID);
		// System.err.println(getElementID());
		// System.err.println(getElementID(12));

		// MACHINES
		int resourceManagerID = ElementIDGenerator.getInstance().getBaseID();

		try {
			Process p = null;
			String server = null;
			if (!debugReadFromFiles) {
				try {
					p = Runtime.getRuntime().exec("qstat -B -f -1");//$NON-NLS-1$
				} catch (IOException e1) {
					sendEvent(getEventFactory().newProxyRuntimeMessageEvent(Level.ERROR, e1.getMessage()));
					return null;
				}
				p.waitFor();
				server = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
			}
			if (server == null || server.split(" ").length < 2) //$NON-NLS-1$
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

		jobController.setFilter("PBSJOB_OWNER", Pattern.quote(getUser()) + "@.*"); //$NON-NLS-1$ //$NON-NLS-2$

		// TODO: the following could be moved to the abstract class.
		Thread eventThread = new Thread() {

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
								sendEvent(getEventFactory().newProxyRuntimeMessageEvent(Level.ERROR, e.getMessage()));
							}
							for (IProxyEvent e : events) {
								e.setTransactionID(transID);
								sendEvent(e);
								System.out.println(e.toString());
							}
						} catch (IOException e1) {
							e1.printStackTrace();
							System.out.println(Messages.getString("PBSProxyRuntimeServer.6")); //$NON-NLS-1$
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
				System.out.println(Messages.getString("PBSProxyRuntimeServer.0")); //$NON-NLS-1$
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
		return eventThread;
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
			if (key.equals("jobSubId")) //$NON-NLS-1$
				jobSubId = value;
			else if (key.equals("script")) //$NON-NLS-1$
				script = normalize(value);
		}

		if (jobSubId == null) {
			System.out.println("missing arguments!");//$NON-NLS-1$
			return;
		}

		if (script == null) {
			sendSubmitJobError(transID, jobSubId, "No script supplied");//$NON-NLS-1$
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
			sendSubmitJobError(transID, jobSubId, e1.getLocalizedMessage());
			return;
		}

		// Call Qsub with job-script
		String args[] = { "qsub", tmp.getAbsolutePath() }; //$NON-NLS-1$
		Process p = null;
		try {
			p = new ProcessBuilder(args).redirectErrorStream(true).start();
			StringBuffer out = new StringBuffer();
			StringBuffer err = new StringBuffer();
			streamConsumer(new InputStreamReader(p.getErrorStream()), true, err).start();
			streamConsumer(new InputStreamReader(p.getInputStream()), false, out).start();
			p.waitFor();

			try {
				//System.out.println("submitJob: exit:" + p.exitValue()); //$NON-NLS-1$
				// Check that is was succesful
				if (p.exitValue() == 0) {
					String batchId = parseBatchId(out.toString());
					if (batchId != null)
						jobIdBindings.put(batchId, jobSubId);
					sendEvent(getEventFactory().newOKEvent(transID));
				} else {
					sendSubmitJobError(transID, jobSubId, out.toString());
					/*
					 * document in wiki - following here
					 * proxy_event:proxy_submitjob_error_event
					 */
					System.out.println("submitJob: err: " + out.toString()); //$NON-NLS-1$
				}
			} catch (Throwable e1) { // sendEvent, readLine
				e1.printStackTrace();
			}

		} catch (IOException e) { // exec
			sendSubmitJobError(transID, jobSubId, e.getLocalizedMessage());
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
		if (job == null) {
			sendTerminateJobError(transID, id, "job not found on server");//$NON-NLS-1$
			return;
		}
		System.out.println(Messages.getString("PBSProxyRuntimeServer.25") + id + "," + job.getKey()); //$NON-NLS-1$ //$NON-NLS-2$
		String args[] = { "qdel", job.getKey() }; //$NON-NLS-1$
		try {
			Process p = new ProcessBuilder(args).redirectErrorStream(true).start();

			p.waitFor();
			if (p.exitValue() == 0)
				sendEvent(getEventFactory().newOKEvent(transID));
			else {
				BufferedReader err = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line, errMsg = ""; //$NON-NLS-1$
				while ((line = err.readLine()) != null)
					errMsg += line;
				err.close();
				sendTerminateJobError(transID, id, errMsg);
				System.out.println("qdel finished with exit status " + p.exitValue()); //$NON-NLS-1$
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// System.out.println("terminateJob: "+keyVal[1]);

	}

	private String getUser() {
		if (debugReadFromFiles)
			user = debugUser;
		if (user == null)
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
		return user;
	}

	public static void main(String[] args) {
		Map<String, Object> params = parseArguments(args);
		String host = (String) params.get("host"); //$NON-NLS-1$
		if (host == null) {
			System.out.println(Messages.getString("PBSProxyRuntimeServer.1")); //$NON-NLS-1$
			System.exit(1);
		}
		int port = 0;
		Integer portVal = ((Integer) params.get("port")); //$NON-NLS-1$
		if (portVal == null) {
			System.out.println(Messages.getString("PBSProxyRuntimeServer.3")); //$NON-NLS-1$
			System.exit(1);
			return; // just to avoid warnings
		}
		port = portVal.intValue();

		PBSProxyRuntimeServer server = new PBSProxyRuntimeServer(host, port);

		try {
			server.connect();
			System.out.println(PBSProxyRuntimeServer.class.getSimpleName() + Messages.getString("PBSProxyRuntimeServer.4")); //$NON-NLS-1$
			server.start();
		} catch (IOException e) {
			System.out.println(Messages.getString("PBSProxyRuntimeServer.5") + e.getMessage()); //$NON-NLS-1$
			System.exit(1);
		}
		System.out.println("PBSProxyRuntimeServer exited"); //$NON-NLS-1$
		/*
		 * Exit required or server does not terminate.
		 */
		System.exit(0);
	}

	/**
	 * @since 4.0
	 */
	protected static Map<String, Object> parseArguments(String args[]) {
		Map<String, Object> argsMap = new HashMap<String, Object>();

		for (int i = 0; i < args.length; i++)
			if (args[i].startsWith("--port")) //$NON-NLS-1$
				try {
					int port = new Integer(args[i].substring(7));
					argsMap.put("port", port); //$NON-NLS-1$
				} catch (NumberFormatException e) {
					System.out.println(org.eclipse.ptp.rm.pbs.jproxy.Messages.getString("AbstractProxyRuntimeServer_0") //$NON-NLS-1$
							+ args[i + 1].substring(7));
				}
			else if (args[i].startsWith("--host")) { //$NON-NLS-1$
				String host = args[i].substring(7);
				if (host != null)
					argsMap.put("host", host); //$NON-NLS-1$
			}

		return argsMap;
	}

	private static String normalize(String content) {
		content = content.replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll("\\\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll("\\\\s", " "); //$NON-NLS-1$ //$NON-NLS-2$
		return content;
	}

	/*
	 * 
	 */
	private static String parseBatchId(String out) throws Throwable {
		String[] lines = out.split("\n"); //$NON-NLS-1$
		if (lines == null || lines.length == 0)
			return null;
		return lines[lines.length - 1];
	}

	private static Thread streamConsumer(final InputStreamReader reader, final boolean err, final StringBuffer output) {
		return new Thread(reader + (err ? "err-thread" : "out-thread")) { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public void run() {
				char[] buffer = new char[STREAM_BUFFER_SIZE];
				int numBytes = 0;
				while (true) {
					try {
						numBytes = reader.read(buffer, 0, STREAM_BUFFER_SIZE);
					} catch (EOFException eofe) {
						break;
					} catch (IOException ioe) {
						return;
					}
					if (numBytes == EOF)
						break;
					if (output != null)
						output.append(buffer, 0, numBytes);
				}

				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		};
	}

	private void sendSubmitJobError(int transId, String jobSubId, String errorMsg) {
		String errArgs[] = { "jobSubId=" + jobSubId, "errorCode=" + 0, "errorMsg=" + errorMsg }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		try {
			System.out.println("SubmitJobError: " + errorMsg); //$NON-NLS-1$
			sendEvent(getEventFactory().newProxyRuntimeSubmitJobErrorEvent(transId, errArgs));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void sendTerminateJobError(int transId, int id, String errorMsg) {
		String errArgs[] = { "jobId=" + id, "errorCode=" + 0, "errorMsg=" + errorMsg }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		try {
			System.out.println("SubmitJobError: " + errorMsg); //$NON-NLS-1$
			sendEvent(getEventFactory().newProxyRuntimeTerminateJobErrorEvent(transId, errArgs));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
