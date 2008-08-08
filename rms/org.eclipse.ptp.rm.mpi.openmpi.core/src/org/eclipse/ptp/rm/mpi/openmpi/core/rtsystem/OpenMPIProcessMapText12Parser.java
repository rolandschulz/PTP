/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIApplicationAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIJobAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPINodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIJobAttributes.MappingMode;

public class OpenMPIProcessMapText12Parser {
	private OpenMPIProcessMapText12Parser() {
		// Do not allow instances.
	}

	OpenMPIProcessMap map = new OpenMPIProcessMap();
	int numApplications;
	int numNodes;

	public static OpenMPIProcessMap parse(InputStream is) throws IOException {
		OpenMPIProcessMapText12Parser parser = new OpenMPIProcessMapText12Parser();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1);

		parser.readLine1(reader);
		parser.readLine2(reader);
		for (int i = 0; i < parser.numApplications; i++) {
			parser.readAppContext(reader);
		}
		parser.readLine3(reader);
		for (int i = 0; i < parser.numNodes; i++) {
			parser.readMappedNode(reader, i);
		}

		return parser.map;
	}

	private void readMappedNode(BufferedReader reader, int nodeCounter) throws IOException {
		// Mapped node:
		// ignore
		String line = reader.readLine();

		// Cell: 0 Nodename: dyn531995.br.ibm.com Launch id: -1 Username: NULL
		line = reader.readLine();
		Pattern p = Pattern.compile("[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*).*");
		Matcher m = p.matcher(line);
		int nodeIndex;
		String nodeName;
		if (!m.matches() || m.groupCount() != 4)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			nodeIndex = Integer.parseInt(s);
			s = m.group(2);
			nodeName = s;
			// Ignore username and launch id.
			s = m.group(3);
			s = m.group(4);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

		OpenMPIProcessMap.Node node = new OpenMPIProcessMap.Node(nodeIndex, nodeName);
		map.addNode(node);

		// Daemon name:
		// Data type: ORTE_PROCESS_NAME Data Value: NULL
		line = reader.readLine();
		line = reader.readLine();
		p = Pattern.compile("[^:]*:[^:]*:\\s*(\\S*).*");
		m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 1)
			throw new IOException("Invalid line: " + line);
		// Ignore deamon name

		// Oversubscribed: True Num elements in procs list: 6
		line = reader.readLine();
		p = Pattern.compile("[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*).*");
		m = p.matcher(line);
		int numProcesses;
		if (!m.matches() || m.groupCount() != 2)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			if (s.equalsIgnoreCase("true")) {
				node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getOversubscribedDefinition().create(true));
			} else if (s.equalsIgnoreCase("false")) {
				node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getOversubscribedDefinition().create(false));
			} else {
				throw new IOException("Invalid line: " + line);
			}
			s = m.group(2);
			numProcesses = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

		// Mapped proc:
		// Proc Name:
		// Data type: ORTE_PROCESS_NAME Data Value: [0,1,0]
		// Proc Rank: 0 Proc PID: 0 App_context index: 0
		// (empty line)
//		for (int i = 0; i < node.num_procs; i ++) {
		for (int i = 0; i < numProcesses; i ++) {
			String processName;
			int processIndex;
			int processPid;
			int applicationIndex;

			line = reader.readLine();
			line = reader.readLine();
			line = reader.readLine();
			p = Pattern.compile("[^:]*:[^:]*:\\s*(\\S*).*");
			m = p.matcher(line);
			if (!m.matches() || m.groupCount() != 1)
				throw new IOException("Invalid line: " + line);
			processName = m.group(1);

			line = reader.readLine();
			p = Pattern.compile("[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*).*");
			m = p.matcher(line);
			if (!m.matches() || m.groupCount() != 3)
				throw new IOException("Invalid line: " + line);
			try {
				String s = m.group(1);
				processIndex = Integer.parseInt(s);
				s = m.group(2);
				processPid = Integer.parseInt(s);
				s = m.group(3);
				applicationIndex = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				throw new IOException("Invalid line: " + line);
			}

			/*
			 * This is tricky:
			 * There is no empty line after the lass process.
			 * Attempting the read one more line after the last process line will block the parser thread
			 * and prevent the proper setting of job in the model.
			 */
			if ((i < numProcesses-1) || (nodeCounter < numNodes-1)) {
				line = reader.readLine();
			}

			OpenMPIProcessMap.Process proc = new OpenMPIProcessMap.Process(node, processIndex, processName, applicationIndex);
			map.addProcess(proc);
			try {
				proc.getAttributeManager().addAttribute(ProcessAttributes.getPIDAttributeDefinition().create(processPid));
			} catch (IllegalValueException e) {
				// This is not possible.
				assert false;
			}

		}
	}

	private void readLine3(BufferedReader reader) throws IOException {
		// Num elements in nodes list: 1
		String line = reader.readLine();
		Pattern p = Pattern.compile("[^:]*:\\s*(\\d*).*");
		Matcher m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 1)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			numNodes = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}
	}

	private void readAppContext(BufferedReader reader) throws IOException {
		// Data for app_context: index 0 app: hellio
		String line = reader.readLine();
		Pattern p = Pattern.compile("[^:]*:\\s*\\w*\\s*(\\d*)[^:]*:\\s*(\\w*).*");
		Matcher m = p.matcher(line);

		int applicationIndex;
		String applicationName;
		int numberOfProcessors;

		if (!m.matches() || m.groupCount() != 2)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			applicationIndex = Integer.parseInt(s);
			s = m.group(2);
			applicationName = s;
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

		// Num procs: 4
		line = reader.readLine();
		p = Pattern.compile("[^:]*:\\s*(\\d*).*");
		m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 1)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			numberOfProcessors = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

		OpenMPIProcessMap.Application application = new OpenMPIProcessMap.Application(applicationIndex, applicationName, numberOfProcessors);
		map.addApplication(application);

		// Argv[0]: hellio
		line = reader.readLine();
		p = Pattern.compile("\\s*Argv\\[\\d*\\]\\s*:\\s*(.*)");
		m = p.matcher(line);
		List<String> arguments = new ArrayList<String>();
		while (m.matches()) {
			arguments.add(m.group(1));
			line = reader.readLine();
			m = p.matcher(line);
		}
		application.getAttributeManager().addAttribute(OpenMPIApplicationAttributes.getEffectiveOpenMPIProgArgsAttrDef().create(arguments.toArray(new String[arguments.size()])));

		// Env[0]: OMPI_MCA_rmaps_base_display_map=1
		line = reader.readLine();
		p = Pattern.compile("\\s*Env\\[\\d*\\]\\s*:\\s*(.*)");
		m = p.matcher(line);
		List<String> environment = new ArrayList<String>();
		while (m.matches()) {
			environment.add(m.group(1));
			line = reader.readLine();
			m = p.matcher(line);
		}
		application.getAttributeManager().addAttribute(OpenMPIApplicationAttributes.getEffectiveOpenMPIEnvAttrDef().create(environment.toArray(new String[environment.size()])));

		// Working dir: /home/dfferber/EclipseWorkspaces/runtime-New_configuration/hellio/Debug (user: 0)
		// NO line = reader.readLine();
		// Line was alread read.
		p = Pattern.compile("[^:]*:\\s*(.*)");
		m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 1)
			throw new IOException("Invalid line: " + line);
		{
			String s = m.group(1).trim();
			application.getAttributeManager().addAttribute(OpenMPIApplicationAttributes.getEffectiveOpenMPIWorkingDirAttrDef().create(s));
		}

		// Num maps: 1
		// ... lines of maps ... (ignored by now)
		line = reader.readLine();
		p = Pattern.compile("[^:]*:\\s*(\\d*).*");
		m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 1)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			// Ignore this information.
			int num_maps = Integer.parseInt(s);
			for (int i = 0; i < num_maps; i++) {
				line = reader.readLine();
			}
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

	}

	private void readLine2(BufferedReader reader) throws IOException {
		// Starting vpid: 0 Vpid range: 6 Num app_contexts: 2
		String line = reader.readLine();
		Pattern p = Pattern.compile("[^:]*:\\s*(\\d*)[^:]*:\\s*(\\w*)[^:]*:\\s*(\\w*).*");
		Matcher m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 3)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			try {
				map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getVpidStart().create(Integer.parseInt(s)));
			} catch (IllegalValueException e) {
			// This is not possible.
				assert false;
			}
			s = m.group(2);
			try {
				map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getVpidRange().create(Integer.parseInt(s)));
			} catch (IllegalValueException e) {
			// This is not possible.
				assert false;
			}
			s = m.group(3);
			numApplications = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}
	}

	private void readLine1(BufferedReader reader) throws IOException {
		// [dyn531995.br.ibm.com:12281] Map for job: 1 Generated by mapping mode: bynode
		String line = reader.readLine();
		Pattern p = Pattern.compile("\\[([^:]*):(\\d*)\\][^:]*:\\s*(\\d*)[^:]*:\\s*(\\w*).*");
		Matcher m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 4)
			throw new IOException("Invalid line: " + line);
		try {
			map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getHostname().create(m.group(1)));
			try {
				map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getMpiJobId().create(Integer.parseInt(m.group(3))));
			} catch (IllegalValueException e) {
				// This is not possible.
				assert false;
			}
			String mode = m.group(4);
			if (mode.equalsIgnoreCase("bynode")) {
				map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getMappingModeDefinition().create(MappingMode.BY_NODE));
			} else if (mode.equalsIgnoreCase("byslot")) {
				map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getMappingModeDefinition().create(MappingMode.BY_SLOT));
			} else {
				throw new IOException("Invalid line: " + line);
			}
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}
	}

	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("test.txt");
			OpenMPIProcessMapText12Parser.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
