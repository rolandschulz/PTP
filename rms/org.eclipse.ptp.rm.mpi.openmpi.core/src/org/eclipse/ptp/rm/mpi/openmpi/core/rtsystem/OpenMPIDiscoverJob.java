package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProcess;
import org.eclipse.ptp.remote.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob;
import org.eclipse.ptp.rm.mpi.openmpi.core.Activator;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiMachineAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiNodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMpiResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMpiHostMap.Host;

public class OpenMPIDiscoverJob extends AbstractRemoteCommandJob {
	OpenMpiRuntimeSystem rts;

	public OpenMPIDiscoverJob(OpenMpiRuntimeSystem rts) {
		super(rts,
			NLS.bind("Discover on {0}", rts.getRmConfiguration().getName()),
			rts.getRmConfiguration().getDiscoverCmd(),
			"Interrupted while running discover command.",
			"Failed to create remote process for discover command.",
			"Failed to parse output of discover command.");
		this.rts = rts;
	}

	@Override
	protected void parse(BufferedReader output) throws CoreException {
		/*
		 * Local copy of attributes from the RuntimeSystem
		 */
		IRemoteConnection connection = rts.getConnection();
		IRemoteServices remoteServices = rts.getRemoteServices();
		IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);
		Parameters params = rts.getParameters();
		Map<String, String> hostToElementMap = rts.getHostToElementMap();
		OpenMpiResourceManagerConfiguration rmConfiguration = (OpenMpiResourceManagerConfiguration) rts.getRmConfiguration();
		assert connection != null;
		assert remoteServices != null;
		assert fileMgr != null;
		assert params != null;
		assert hostToElementMap != null;

		/*
		 * MPI resource manager have only one machine and one queue.
		 * There they are implicitly "discovered".
		 */
		IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rts.getRmID());
		String machineID = rts.createMachine(rm.getName());
		rts.setMachineID(machineID);
		String queueID = rts.createQueue("default");
		rts.setQueueID(queueID);

		IPMachine machine = rm.getMachineById(machineID);
		assert machine != null;

		/*
		 * Any exception from now on is caught in order to add the error message as an attribute to the machine.
		 * Then, the exception is re-thrown.
		 */
		try {
			/*
			 * STEP 1:
			 * Parse output of command.
			 * TODO: validate lines and write to log if invalid lines were found.
			 */
			parseParameters(output, params);

			/*
			 * STEP 2:
			 * Read file that describes machine geography.
			 * If no nodes are given, then we assume MPI default when host are not configured: there is only one node on the machine.
			 * This part is a bit tricky.
			 * OpenMPI 1.2 has a RDS (resource discovery system) that knows the default hostfile as rds_hostfile_path parameter.
			 * But the RDS was dropped by version 1.3.
			 * Then the orte_default_hostfile parameter might be used instead, as long as it was defined in the system wide MCA parameters.
			 */
			OpenMpiHostMap hostMap = readHostFile(connection, remoteServices, fileMgr, params, rmConfiguration);

			/*
			 * Create model according to data from discover.
			 */
			int rankCounter = 0;
			boolean hasSomeError = false;
			assert hostMap != null;

			for (OpenMpiHostMap.Host host : hostMap.getHosts()) {

				// Add node to model
				String nodeId = rts.createNode(machineID, host.getName(), rankCounter++);
				IPNode node = machine.getNodeById(nodeId);
				hostToElementMap.put(host.getName(), nodeId);

				// Add processor information to node.
				AttributeManager attrManager = new AttributeManager();
				if (host.getNumProcessors() != 0) {
					try {
						attrManager.addAttribute(OpenMpiNodeAttributes.getNumberOfNodesAttributeDefinition().create(host.getNumProcessors()));
					} catch (IllegalValueException e) {
						// This situation is not possible since host.getNumProcessors() is always valid.
						assert false;
					}
				}
				if (host.getMaxNumProcessors() != 0) {
					try {
						attrManager.addAttribute(OpenMpiNodeAttributes.getMaximalNumberOfNodesAttributeDefinition().create(host.getMaxNumProcessors()));
					} catch (IllegalValueException e) {
						// This situation is not possible since host.getMaxNumProcessors() is always valid.
						assert false;
					}
				}
				if (host.getErrors() != 0) {
					if ((host.getErrors() & Host.ERR_MAX_NUM_SLOTS) != 0) {
						attrManager.addAttribute(OpenMpiNodeAttributes.getStatusMessageDefinition().create("Invalid 'max-slots' parameter was ignored for this host."));
					} else if ((host.getErrors() & Host.ERR_NUM_SLOTS) != 0) {
						attrManager.addAttribute(OpenMpiNodeAttributes.getStatusMessageDefinition().create("Invalid 'slots/cpus/count' parameter was ignored for this host."));
					} else if ((host.getErrors() & Host.ERR_UNKNOWN_ATTR) != 0) {
						attrManager.addAttribute(OpenMpiNodeAttributes.getStatusMessageDefinition().create("Invalid parameter was ignored for this host."));
					}
					attrManager.addAttribute(NodeAttributes.getStateAttributeDefinition().create(NodeAttributes.State.UP));
					hasSomeError = true;
				}
				rts.changeNode(nodeId, attrManager);
			}
			if (hostMap.hasErrors) {
				machine.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
				machine.addAttribute(OpenMpiMachineAttributes.getStatusMessageDefinition().create("Parse error(s) in hostfile."));
			}
			if (hostMap.hasParseErrors() || hasSomeError) {
				throw new CoreException(new Status(IStatus.WARNING, Activator.getDefault().getBundle().getSymbolicName(), "There are errors in the hostfile."));
			}

		} catch (CoreException e) {
			/*
			 * Show message of core exception and change machine status to error.
			 */
			if (e.getStatus().getSeverity() == IStatus.ERROR) {
				AttributeManager attrManager = new AttributeManager();
				attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
				attrManager.addAttribute(OpenMpiMachineAttributes.getStatusMessageDefinition().create(NLS.bind("Error while running discover command: {0}.", e.getMessage())));
				rts.changeMachine(machineID, attrManager);
			}
			throw e;
		} catch (Exception e) {
			/*
			 * Show message of all other exceptions and change machine status to error.
			 */
			AttributeManager attrManager = new AttributeManager();
			attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
			attrManager.addAttribute(OpenMpiMachineAttributes.getStatusMessageDefinition().create(NLS.bind("Internal error while running discover command: {0}", e.getMessage())));
			rts.changeMachine(machineID, attrManager);
		}
	}

	private OpenMpiHostMap readHostFile(IRemoteConnection connection,
			IRemoteServices remoteServices, IRemoteFileManager fileMgr,
			Parameters params,
			OpenMpiResourceManagerConfiguration rmConfiguration)
			throws CoreException, IOException {

		/*
		 * OpenMpi 1.2 uses rds_hostfile_path. Open 1.3 uses orte_default_hostfile.
		 * For 1.2, path must not be empty. For 1.3 it may be empty and default host is assumed.
		 */
		OpenMpiHostMap hostMap = null;
		String hostFilePath = null;

		Parameters.Parameter rds_param = params.getParameter("rds_hostfile_path");
		Parameters.Parameter orte_param = params.getParameter("orte_default_hostfile");

		if (rds_param != null) {
			hostFilePath = rds_param.getValue();
			if (hostFilePath.trim().length() == 0) {
				hostFilePath = null;
			}
		}
		if (hostFilePath == null) {
			if (orte_param != null) {
				hostFilePath = orte_param.getValue();
				if (hostFilePath.trim().length() == 0) {
					hostFilePath = null;
				}
			}
		}

		// Validate.
		if (rmConfiguration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_12)) {
			if (hostFilePath == null) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Discover command did not inform path to default hostfile. If necessary, set MCA parameters to define default hostfile path."));
			}
		} else if (rmConfiguration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_13)) {
			hostMap = new OpenMpiHostMap();
			String hostname = getRemoteHostname(connection, remoteServices);
			hostMap.addDefaultHost(hostname);
			return hostMap;
		} else {
			assert false;
		}

		IPath path = new Path(hostFilePath);
		if (! path.isAbsolute()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind("Discover command informed a path to hostfile that is not an absolute path ({0}).", hostFilePath)));
		}

		// Try to read.
		assert hostFilePath != null;
		IProgressMonitor monitor = new NullProgressMonitor();
		IFileStore hostfile;
		try {
			hostfile = fileMgr.getResource(new Path(hostFilePath), monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind("Failed find hostfile ({0}).", hostFilePath), e));
		}

		InputStream is = null;
		try {
			is = hostfile.openInputStream(EFS.NONE, monitor);
		} catch (CoreException e) {
			Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind("Failed read hostfile ({0}).", hostfile), e);
			throw new CoreException(s);
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			hostMap = OpenMpiHostMapParser.parse(reader);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind("Failed to parse hostfile ({0}).", hostfile), e));
		}

		/*
		 * If no host information was found in the hostfile, add default.
		 * Only for Open MPI 1.2. On 1.3, there is no default host file assumed.
		 */
		if (hostMap.count() == 0) {
			if (rmConfiguration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_12)) {
				// This was not correct for remote hosts. Worked only for local hosts.
//					try {
//						InetAddress localhost = InetAddress.getLocalHost();
//						hostMap.addDefaultHost(localhost.getHostName());
//					} catch (UnknownHostException e) {
//						throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Cannot retrive network information for local machine. Check network configuration."));
//					}
				String hostname = getRemoteHostname(connection, remoteServices);
				hostMap.addDefaultHost(hostname);
			} else if (rmConfiguration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_12)) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind("Empty hostfile is not allowed ({0}).", hostfile)));
			} else {
				assert false;
			}
		}
		return hostMap;
	}

	private String getRemoteHostname(IRemoteConnection connection,
			IRemoteServices remoteServices) throws CoreException, IOException {
		IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, "hostname");
		IRemoteProcess process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to run command to get hostname.", e));
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// Ignore
		}
		if (process.exitValue() != 0) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind("Command to get hostname failed with exit code {0}", process.exitValue())));
		}
		String hostname = br.readLine();
		if (hostname == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to parse command for hostname."));
		}
		return hostname;
	}

	private void parseParameters(BufferedReader output, Parameters params)
			throws CoreException {

		try {
			String line;
			while ((line = output.readLine()) != null) {
				int nameStart = line.indexOf(":param:");
				if (nameStart >= 0) {
					nameStart += 7;
					int pos = line.indexOf(":", nameStart);
					if (pos >= 0) {
						/*
						 * If parameter is already in list, then update, otherwise add.
						 */
						String name = line.substring(nameStart, pos);
						Parameters.Parameter param = params.getParameter(name);
						if (param == null) {
							param = params.addParameter(name);
						}
						int pos2;
						if ((pos2 = line.indexOf(":value:", pos)) >= 0) {
							param.setValue(line.substring(pos2 + 7));
						} else if ((pos2 = line.indexOf(":status:", pos)) >= 0) {
							if (line.substring(pos2 + 8).equals("read-only")) {
								param.setReadOnly(true);
							}
						} else if ((pos2 = line.indexOf(":help:", pos)) >= 0) {
							param.setHelp(line.substring(pos2 + 6));
						}
					}
				}
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to parse output of discover command.", e));
		}
	}
}
