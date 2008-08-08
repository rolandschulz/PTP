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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.core.attributes.AttributeManager;

public class OpenMPIProcessMap {

	/**
	 * An application launched by mpirun.
	 * 
	 * @author dfferber
	 */
	static public class Application {
		private int index;
		private String name = new String();
		final private AttributeManager attributeManager = new AttributeManager();
		private int numberOfProcessors;
		
		public Application(int index, String applicationName, int numberOfProcessors) {
			super();
			this.index = index;
			this.name = applicationName;
			this.numberOfProcessors = numberOfProcessors;
		}

		public int getIndex() {
			return index;
		}
		
		public String getName() {
			return name;
		}
		
		public int getNumberOfProcessors() {
			return numberOfProcessors;
		}
		
		public AttributeManager getAttributeManager() {
			return attributeManager;
		}
	}

	/**
	 * A host that is executing processes from mpirun.
	 * 
	 * @author dfferber
	 */
	static public class Node {
		// Information provided only by openmpi 1.2
		 int index;

		// Information provided by openmpi 1.2 and 1.3
		private String name = null;
		 
		final private AttributeManager attributeManager = new AttributeManager();
		
		// References to processes.
		private List<Process> processes = new ArrayList<Process>();

		public Node(int index, String name) {
			super();
			this.index = index;
			this.name = name;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String getName() {
			return name;
		}
		
		public AttributeManager getAttributeManager() {
			return attributeManager;
		}
		
//		public void addProcessor(Process process) {
//			processes.add(process);
//		}
		
		public List<Process> getProcesses() {
			return Collections.unmodifiableList(processes);
		}

	}

	/**
	 * A process spawned by mpirun for a certain application on a certain node.
	 */
	static public class Process {
		private String name;
		private int index;
		private int applicationIndex;
		private Node node;
		
		final private AttributeManager attributeManager = new AttributeManager();
		
		public Process(Node node, int index, String name, int applicationIndex) {
			super();
			this.node = node;
			this.name = name;
			this.index = index;
			this.applicationIndex = applicationIndex;
		}

		public String getName() {
			return name;
		}
		
		public int getIndex() {
			return index;
		}
		
		public int getApplicationIndex() {
			return applicationIndex;
		}

		public AttributeManager getAttributeManager() {
			return attributeManager;
		}
		
		public Node getNode() {
			return node;
		}
	}

	final private AttributeManager attributeManager = new AttributeManager();
	final private List<Application> applications = new ArrayList<Application>();
	final private List<Node> nodes = new ArrayList<Node>();
	final private List<Process> processes = new ArrayList<Process>();
	
	public OpenMPIProcessMap() {
		// Nothing.
	}
	
	public List<Process> getProcesses() {
		return Collections.unmodifiableList(processes);
	}
	
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	public List<Application> getAppContexts() {
		return Collections.unmodifiableList(applications);
	}
	
	public AttributeManager getAttributeManager() {
		return attributeManager;
	}
	
	void addApplication(Application application) {
		this.applications.add(application);
	}
	
	void addNode(Node node) {
		nodes.add(node);
	}
	
	void addProcess(Process process) {
		processes.add(process);
		process.node.processes.add(process);
	}
}
