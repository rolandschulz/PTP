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

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIApplicationAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIJobAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPINodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIJobAttributes.MappingMode;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
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

		parser.readStart(reader);
		parser.readVpid(reader);
		for (int i = 0; i < parser.numApplications; i++) {
			parser.readAppContext(reader);
		}
		parser.readNumElements(reader);
		for (int i = 0; i < parser.numNodes; i++) {
			parser.readMappedNode(reader, i);
		}

		return parser.map;
	}

	/*
	 * Find mapped node information, ignoring anything else. Format should be:
	 * 
	 * 		Mapped node:
	 * 			Cell: 0 Nodename: dyn531995.br.ibm.com Launch id: -1 Username: NULL
	 * 			Daemon name:
	 * 				Data type: ORTE_PROCESS_NAME Data Value: NULL
	 * 			Oversubscribed: True Num elements in procs list: 6
	 * 			Mapped proc:
	 * 				Proc Name:
	 * 				Data type: ORTE_PROCESS_NAME Data Value: [0,1,0]
	 * 				Proc Rank: 0 Proc PID: 0 App_context index: 0
	 */
	private void readMappedNode(BufferedReader reader, int nodeCounter) throws IOException {
		Pattern p = Pattern.compile("\\s*Mapped node:"); //$NON-NLS-1$
		String line;
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches()) {
				break;
			}
		}
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}

		p = Pattern.compile("\\s*Cell:\\s*(\\S*)\\s*Nodename:\\s*(\\S*)\\s*Launch id:\\s*(\\S*)\\s*Username:\\s*(\\S*)"); //$NON-NLS-1$
		String nodeName = ""; //$NON-NLS-1$
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 4) {
				try {
					nodeName = m.group(2);
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
				
				break;
			}
		}
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
		
		OpenMPIProcessMap.Node node = new OpenMPIProcessMap.Node(nodeName);
		map.addNode(node);

		p = Pattern.compile("\\s*Daemon name:"); //$NON-NLS-1$
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches()) {
				break;
			}
		}
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
		
		p = Pattern.compile("\\s*Data type:\\s*\\S*\\s*Data Value:\\s*\\S*"); //$NON-NLS-1$
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches()) {
				break;
			}
		}
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
		
		p = Pattern.compile("\\s*Oversubscribed:\\s*(\\S*)\\s*Num elements in procs list:\\s*(\\d*)"); //$NON-NLS-1$
		
		int numProcesses = 0;
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 2) {
				try {
					String s = m.group(1);
					if (s.equalsIgnoreCase("true")) { //$NON-NLS-1$
						node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getOversubscribedAttributeDefinition().create(true));
					} else if (s.equalsIgnoreCase("false")) { //$NON-NLS-1$
						node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getOversubscribedAttributeDefinition().create(false));
					} else {
						throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
					}
					s = m.group(2);
					numProcesses = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
				
				break;
			}
		}
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}

		p = Pattern.compile("\\s*Mapped proc:"); //$NON-NLS-1$
		Pattern p2 = Pattern.compile("\\s*Proc Name:\\s*(\\S*)"); //$NON-NLS-1$
		Pattern p3 = Pattern.compile("\\s*Data type:\\s*\\S*\\s*Data Value:\\s*\\S*"); //$NON-NLS-1$
		Pattern p4 = Pattern.compile("\\s*Proc Rank:\\s*(\\d*)\\s*Proc PID:\\s*(\\d*)\\s*App_context index:\\s*(\\d*)"); //$NON-NLS-1$

		for (int i = 0; i < numProcesses; i ++) {
			String processName = ""; //$NON-NLS-1$
			int processIndex = 0;
			int processPid = 0;
			int applicationIndex = 0;

			// Mapped proc:
			while ((line = reader.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.matches()) {
					break;
				}
			}
			if (line == null) {
				throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
			}
			
			// Proc Name:
			while ((line = reader.readLine()) != null) {
				Matcher m = p2.matcher(line);
				if (m.matches() && m.groupCount() == 1) {
					processName = m.group(1);
					break;
				}
			}
			if (line == null) {
				throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
			}

			// Data type:
			while ((line = reader.readLine()) != null) {
				Matcher m = p3.matcher(line);
				if (m.matches()) {
					break;
				}
			}
			if (line == null) {
				throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
			}
			
			// Proc Rank:
			while ((line = reader.readLine()) != null) {
				Matcher m = p4.matcher(line);
				if (m.matches() && m.groupCount() == 3) {
					try {
						String s = m.group(1);
						processIndex = Integer.parseInt(s);
						s = m.group(2);
						processPid = Integer.parseInt(s);
						s = m.group(3);
						applicationIndex = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
					}
					
					break;
				}
			}
			if (line == null) {
				throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
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

	/*
	 * Find num elements line, ignoring anything else. Format should be:
	 *
	 *		Num elements in nodes list: 1
	 */
	private void readNumElements(BufferedReader reader) throws IOException {
		Pattern p = Pattern.compile("\\s*Num elements in nodes list:\\s*(\\d*)"); //$NON-NLS-1$
		String line;
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 1) {
				try {
					String s = m.group(1);
					numNodes = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
				
				break;
			}
		}
		
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
	}
	
	/*
	 * Find app context, ignoring anything else. Format should be:
	 * 
	 * 		Data for app_context: index 0 app: hello
	 * 			Num procs: 4
	 * 			Argv[0]: hello
	 * 			Env[0]: OMPI_MCA_rmaps_base_display_map=1
	 *			Env[1]: OMPI_MCA_orte_precondition_transports=e91ae2fd9a00796a-e5685cefcdbaa089
	 *			...
	 *			Working dir: /home/dfferber/EclipseWorkspaces/runtime-New_configuration/hello/Debug (user: 0)
	 *			Num maps: 0
	 *			
	 */
	private void readAppContext(BufferedReader reader) throws IOException {
		String line;
		int applicationIndex = 0;
		String applicationName = ""; //$NON-NLS-1$
		int numberOfProcessors = 0;

		Pattern p = Pattern.compile("\\s*Data for app_context:\\s*\\w*\\s*(\\d*)[^:]*:\\s*(.*)"); //$NON-NLS-1$
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
	
			if (m.matches() && m.groupCount() == 2) {
				try {
					String s = m.group(1);
					applicationIndex = Integer.parseInt(s);
					s = m.group(2);
					applicationName = s;
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
			}
			
			break;
		}
		
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
		
		p = Pattern.compile("\\s*Num procs:\\s*(\\d*)"); //$NON-NLS-1$
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 1) {
				try {
					String s = m.group(1);
					numberOfProcessors = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
				
				break;
			}
		}
		
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}

		OpenMPIProcessMap.Application application = new OpenMPIProcessMap.Application(applicationIndex, applicationName, numberOfProcessors);
		map.addApplication(application);

		/*
		 * Collect Argv and Env lines until we match Working Dir
		 */
		Pattern p1 = Pattern.compile("\\s*Argv\\[\\d*\\]\\s*:\\s*(.*)"); //$NON-NLS-1$
		Pattern p2 = Pattern.compile("\\s*Env\\[\\d*\\]\\s*:\\s*(.*)"); //$NON-NLS-1$
		p = Pattern.compile("\\s*Working dir:\\s*(.*)"); //$NON-NLS-1$

		List<String> arguments = new ArrayList<String>();
		List<String> environment = new ArrayList<String>();

		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 1) {
				String s = m.group(1).trim();
				application.getAttributeManager().addAttribute(OpenMPIApplicationAttributes.getEffectiveOpenMPIWorkingDirAttributeDefinition().create(s));
				break;
			}
			
			m = p1.matcher(line);
			if (m.matches() && m.groupCount() == 1) {
				arguments.add(m.group(1));
			} else {
				m = p2.matcher(line);
				if (m.matches() && m.groupCount() == 1) {
					environment.add(m.group(1));
				}
			}
		}
		
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
		
		application.getAttributeManager().addAttribute(OpenMPIApplicationAttributes.getEffectiveOpenMPIProgArgsAttributeDefinition().create(arguments.toArray(new String[arguments.size()])));
		application.getAttributeManager().addAttribute(OpenMPIApplicationAttributes.getEffectiveOpenMPIEnvAttributeDefinition().create(environment.toArray(new String[environment.size()])));

		int num_maps = 0;
		p = Pattern.compile("\\s*Num maps:\\s*(\\d*).*"); //$NON-NLS-1$
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 1) {
				try {
					String s = m.group(1);
					num_maps = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
				
				break;
			}
		}
		
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
		
		/*
		 * Skip map information
		 */
		for (int i = 0; i < num_maps; i++) {
			line = reader.readLine();
		}
	}

	/*
	 * Find second line, ignoring anything else. Format should be:
	 * 
	 * 		Starting vpid: 0 Vpid range: 6 Num app_contexts: 2
	 */
	private void readVpid(BufferedReader reader) throws IOException {
		Pattern p = Pattern.compile("\\s*Starting vpid:\\s*(\\d*)\\s*Vpid range:\\s*(\\w*)\\s*Num app_contexts:\\s*(\\w*).*"); //$NON-NLS-1$
		String line;
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 3) {
				try {
					String s = m.group(1);
					try {
						map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getVpidStartAttributeDefinition().create(Integer.parseInt(s)));
					} catch (IllegalValueException e) {
						// This is not possible.
						assert false;
					}
					s = m.group(2);
					try {
						map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getVpidRangeAttributeDefinition().create(Integer.parseInt(s)));
					} catch (IllegalValueException e) {
						// This is not possible.
						assert false;
					}
					s = m.group(3);
					numApplications = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
				
				break;
			}
		}
		
		if (line == null) {		
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation);
		}
	}
	
	/*
	 * Find start of map output. This is a line formatted like the one below. Ignore everything
	 * before this line.
	 * 
	 *		[dyn531995.br.ibm.com:12281] Map for job: 1 Generated by mapping mode: bynode
	 * 
	 */
	private void readStart(BufferedReader reader) throws IOException {
		Pattern p = Pattern.compile("\\[([^:]*):(\\d*)\\]\\s*Map for job:\\s*(\\d*)[^:]*:\\s*(\\w*).*"); //$NON-NLS-1$
		String line;
		
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.matches() && m.groupCount() == 4) {
				try {
					map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getHostnameAttributeDefinition().create(m.group(1)));
					try {
						map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getMpiJobIdAttributeDefinition().create(Integer.parseInt(m.group(3))));
					} catch (IllegalValueException e) {
						// This is not possible.
						assert false;
					}
					String mode = m.group(4);
					if (mode.equalsIgnoreCase("bynode")) { //$NON-NLS-1$
						map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getMappingModeAttributeDefinition().create(MappingMode.BY_NODE));
					} else if (mode.equalsIgnoreCase("byslot")) { //$NON-NLS-1$
						map.getAttributeManager().addAttribute(OpenMPIJobAttributes.getMappingModeAttributeDefinition().create(MappingMode.BY_SLOT));
					} else {
						throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
					}
				} catch (NumberFormatException e) {
					throw new IOException(NLS.bind(Messages.OpenMPIProcessMapText12Parser_Exception_InvalidLine, line));
				}
				
				break;
			}
		}
		
		if (line == null) {
			throw new IOException(Messages.OpenMPIProcessMapText12Parser_Exception_MissingDisplayMapInformation);
		}
	}
	
	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("test.txt"); //$NON-NLS-1$
			OpenMPIProcessMapText12Parser.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
