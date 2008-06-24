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
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMpiProcessMap.MappingMode;

public class OpenMpiProcessMapParser {
	private OpenMpiProcessMapParser() {
		// Do not allow instances.
	}

	OpenMpiProcessMap map = new OpenMpiProcessMap();

	public static OpenMpiProcessMap parse(BufferedReader reader) throws IOException {
		OpenMpiProcessMapParser parser = new OpenMpiProcessMapParser();

		parser.readLine1(reader);
		parser.readLine2(reader);
		for (int i = 0; i < parser.map.num_app_contexts; i++) {
			parser.readAppContext(reader);
		}
		parser.readLine3(reader);
		for (int i = 0; i < parser.map.num_nodes; i++) {
			parser.readMappedNode(reader);
		}

		return parser.map;
	}

	private void readMappedNode(BufferedReader reader) throws IOException {
		OpenMpiProcessMap.MappedNode node = new OpenMpiProcessMap.MappedNode();
		map.mappedNodes.add(node);

		// Mapped node:
		// ignore
		String line = reader.readLine();

		// Cell: 0 Nodename: dyn531995.br.ibm.com Launch id: -1 Username: NULL
		line = reader.readLine();
		Pattern p = Pattern.compile("[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*).*");
		Matcher m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 4)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			node.cell = Integer.parseInt(s);
			s = m.group(2);
			node.nodename = s;
			s = m.group(3);
			node.launch_id = Integer.parseInt(s);
			s = m.group(4);
			node.username = s;
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

		// Daemon name:
		// Data type: ORTE_PROCESS_NAME Data Value: NULL
		line = reader.readLine();
		line = reader.readLine();
		p = Pattern.compile("[^:]*:[^:]*:\\s*(\\S*).*");
		m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 1)
			throw new IOException("Invalid line: " + line);
		node.daemon_name = m.group(1);

		// Oversubscribed: True Num elements in procs list: 6
		line = reader.readLine();
		p = Pattern.compile("[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*).*");
		m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 2)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			if (s.equalsIgnoreCase("true")) {
				node.overSubscribed = true;
			} else if (s.equalsIgnoreCase("false")) {
				node.overSubscribed = false;
			} else {
				throw new IOException("Invalid line: " + line);
			}
			s = m.group(2);
			node.num_procs = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

		// Mapped proc:
		// Proc Name:
		// Data type: ORTE_PROCESS_NAME Data Value: [0,1,0]
		// Proc Rank: 0 Proc PID: 0 App_context index: 0
		// (empty line)
		for (int i = 0; i < node.num_procs; i ++) {
			OpenMpiProcessMap.MappedProc proc = new OpenMpiProcessMap.MappedProc();
			node.procs.add(proc);

			line = reader.readLine();
			line = reader.readLine();
			line = reader.readLine();
			p = Pattern.compile("[^:]*:[^:]*:\\s*(\\S*).*");
			m = p.matcher(line);
			if (!m.matches() || m.groupCount() != 1)
				throw new IOException("Invalid line: " + line);
			proc.name = m.group(1);

			line = reader.readLine();
			p = Pattern.compile("[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*)[^:]*:\\s*(\\S*).*");
			m = p.matcher(line);
			if (!m.matches() || m.groupCount() != 3)
				throw new IOException("Invalid line: " + line);
			try {
				String s = m.group(1);
				proc.rank = Integer.parseInt(s);
				s = m.group(2);
				proc.pid = Integer.parseInt(s);
				s = m.group(3);
				proc.app_context_index = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				throw new IOException("Invalid line: " + line);
			}
			line = reader.readLine();
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
			map.num_nodes = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}
	}

	private void readAppContext(BufferedReader reader) throws IOException {
		OpenMpiProcessMap.AppContext context = new OpenMpiProcessMap.AppContext();
		map.appContexts.add(context);

		// Data for app_context: index 0 app: hellio
		String line = reader.readLine();
		Pattern p = Pattern.compile("[^:]*:\\s*\\w*\\s*(\\d*)[^:]*:\\s*(\\w*).*");
		Matcher m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 2)
			throw new IOException("Invalid line: " + line);
		try {
			String s = m.group(1);
			context.index = Integer.parseInt(s);
			s = m.group(2);
			context.app = s;
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
			context.num_procs = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid line: " + line);
		}

		// Argv[0]: hellio
		line = reader.readLine();
		p = Pattern.compile("\\s*Argv\\[\\d*\\]\\s*:\\s*(.*)");
		m = p.matcher(line);
		while (m.matches()) {
			context.argv.add(m.group(1));
			line = reader.readLine();
			m = p.matcher(line);
		}

		// Env[0]: OMPI_MCA_rmaps_base_display_map=1
		line = reader.readLine();
		p = Pattern.compile("\\s*Env\\[\\d*\\]\\s*:\\s*(.*)");
		m = p.matcher(line);
		while (m.matches()) {
			context.env.add(m.group(1));
			line = reader.readLine();
			m = p.matcher(line);
		}

		// Working dir: /home/dfferber/EclipseWorkspaces/runtime-New_configuration/hellio/Debug (user: 0)
		// NO line = reader.readLine();
		// Line was alread read.
		p = Pattern.compile("[^:]*:\\s*(.*)");
		m = p.matcher(line);
		if (!m.matches() || m.groupCount() != 1)
			throw new IOException("Invalid line: " + line);
		{
			String s = m.group(1);
			context.workDir = s.trim();
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
			context.num_maps = Integer.parseInt(s);
			for (int i = 0; i < context.num_maps; i++) {
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
			map.starting_vpid = Integer.parseInt(s);
			s = m.group(2);
			map.vpid_range = Integer.parseInt(s);
			s = m.group(3);
			map.num_app_contexts = Integer.parseInt(s);
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
			map.hostname = m.group(1);
			String s = m.group(2);
			map.job_id = Integer.parseInt(s);
			s = m.group(3);
			map.map_for_job = Integer.parseInt(s);
			String mode = m.group(4);
			if (mode.equalsIgnoreCase("bynode")) {
				map.mapping_mode = MappingMode.bynode;
			} else if (mode.equalsIgnoreCase("byslot")) {
				map.mapping_mode = MappingMode.byslot;
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
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			OpenMpiProcessMapParser p = new OpenMpiProcessMapParser();
			p.parse(br);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
