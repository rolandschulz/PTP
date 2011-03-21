/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rmsystem;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration;
import org.eclipse.ptp.rm.smoa.core.attrib.SMOAJobAttributes;
import org.eclipse.ptp.rm.smoa.core.attrib.StringMapAttribute;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOAConnection;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOARemoteServices;
import org.eclipse.ptp.rm.smoa.core.rtsystem.SMOARuntimeSystem;
import org.eclipse.ptp.rm.smoa.core.util.NotifyShell;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerMonitor;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

import com.smoa.comp.sdk.SMOAFactory;
import com.smoa.comp.sdk.exceptions.InvalidRequestMessageException;
import com.smoa.comp.sdk.exceptions.NotAuthorizedException;
import com.smoa.comp.sdk.exceptions.UnsupportedFeatureException;
import com.smoa.comp.sdk.jsdl.JSDL;
import com.smoa.comp.sdk.types.ActivityEndpointReference;
import com.smoa.comp.stubs.factory.ApplicationsType.Application;

public class SMOAResourceManagerMonitor extends AbstractRuntimeResourceManagerMonitor {

	/**
	 * Extracts from the {@link AttributeManager} proper executable name and
	 * arguments
	 * 
	 * Returns the list containing program path and executable (as first
	 * element) and all arguments (as next arguments)
	 */
	private static List<String> getExecAndArgs(AttributeManager attrs, IRemoteConnection conn, Application app) {
		final List<String> result = new Vector<String>();

		final BooleanAttribute debug = attrs.getAttribute(JobAttributes.getDebugFlagAttributeDefinition());

		// // // // // // // //
		// // // debug // // //

		if (debug != null && debug.getValue() == true) {
			String execPath = attrs.getAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition()).getValue();

			if (execPath == null || execPath.isEmpty()) {
				execPath = "."; //$NON-NLS-1$
			}

			final String execName = attrs.getAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition()).getValue();

			final String execPathAndName = execPath + "/" + execName; //$NON-NLS-1$

			result.add(execPathAndName);

			/* Arguments */

			final List<String> appArguments = attrs.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition())
					.getValue();

			if (conn.supportsTCPPortForwarding()) {
				final Pattern portRegex = Pattern.compile("^--port=(\\d+)$"); //$NON-NLS-1$
				final Pattern hostRegex = Pattern.compile("^--host=(.+)$"); //$NON-NLS-1$

				Integer port = null;
				String host = null;

				for (final String arg : appArguments) {
					Matcher m = portRegex.matcher(arg);
					if (m.matches()) {
						port = Integer.parseInt(m.group(1));
					}
					m = hostRegex.matcher(arg);
					if (m.matches()) {
						host = m.group(1);
					}
				}
				if (port != null && host != null) {
					try {
						conn.forwardRemotePort(port, host, port);
					} catch (final RemoteConnectionException e) {
						NotifyShell.open(Messages.SMOAResourceManager_PortForwardingFailed_title,
								Messages.SMOAResourceManager_PortForwardingFailed_text + e.getLocalizedMessage());
					}
				}
			}

			result.addAll(appArguments);

			final String appString = attrs.getAttribute(SMOAJobAttributes.getAppNameDef()).getValue();
			if (SMOAJobAttributes.NO_WRAPPER_SCRIPT.equals(appString) || app == null) {
				result.add("--server=0"); //$NON-NLS-1$
			}
		}

		// // // // // // // //
		// // / / run / / // //
		else {
			String execPath = attrs.getAttribute(JobAttributes.getExecutablePathAttributeDefinition()).getValue();

			if (execPath == null || execPath.isEmpty()) {
				execPath = "."; //$NON-NLS-1$
			}

			final String execName = attrs.getAttribute(JobAttributes.getExecutableNameAttributeDefinition()).getValue();

			final String execPathAndName = execPath + "/" + execName; //$NON-NLS-1$

			result.add(execPathAndName);

			/* Arguments */

			final List<String> appArguments = attrs.getAttribute(JobAttributes.getProgramArgumentsAttributeDefinition()).getValue();
			result.addAll(appArguments);
		}

		return result;
	}

	/** Current configuration of this RM */
	/* package access */SMOAConfiguration configuration;

	/** Assigns sequential run number for each configuration */
	private final Map<String, Integer> executeCount = new HashMap<String, Integer>();

	public SMOAResourceManagerMonitor(SMOAResourceManagerConfiguration config) {
		super(config);
		configuration = config;
	}

	/**
	 * Creates script if the user selected an application only.
	 * 
	 * @param args
	 *            - user arguments
	 * @param app
	 *            - user application
	 * @param out
	 *            - file name for stdout
	 * @param err
	 *            - file name for stderr
	 * @param os
	 *            - output stream for the script
	 * @param make
	 *            - if the user wants us to run 'make'
	 * @param customMakeCommand
	 *            - non-null if user wants to use custom run command
	 */
	private void createScript(List<String> args, String app, String out, String err, DataOutputStream os, boolean make,
			String customMakeCommand) throws IOException {
		os.writeBytes("rm -f " + out + " " + err + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final StringBuilder redirect = new StringBuilder();
		redirect.append(" >> "); //$NON-NLS-1$
		redirect.append(out);
		redirect.append(" 2>> "); //$NON-NLS-1$
		redirect.append(err);

		if (make) {
			os.writeBytes("echo $(date)   Running make >> " + err + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			os.writeBytes("{ "); //$NON-NLS-1$
			if (customMakeCommand != null) {
				os.writeBytes(customMakeCommand);
			} else {
				os.writeBytes("make clean && make"); //$NON-NLS-1$
			}
			os.writeBytes(" ; } "); //$NON-NLS-1$
			os.writeBytes(redirect.toString());
			os.writeBytes("\n"); //$NON-NLS-1$
			os.writeBytes("MAKEOUT=$?\n"); //$NON-NLS-1$
			os.writeBytes("if [ ${MAKEOUT} -ne 0 ]\n"); //$NON-NLS-1$
			os.writeBytes("then\n"); //$NON-NLS-1$
			os.writeBytes("  echo $(date)   Make failed! Quitting. >> " + err //$NON-NLS-1$
					+ "\n"); //$NON-NLS-1$
			os.writeBytes("  exit ${MAKEOUT}\n"); //$NON-NLS-1$
			os.writeBytes("fi\n"); //$NON-NLS-1$
		}

		os.writeBytes("STDBUF=$(which stdbuf)\n"); //$NON-NLS-1$
		os.writeBytes("if [ \"${STDBUF}\" ]\n"); //$NON-NLS-1$
		os.writeBytes("then\n"); //$NON-NLS-1$
		os.writeBytes("  STDBUF=\"${STDBUF} -oL -eL \"\n"); //$NON-NLS-1$
		os.writeBytes("fi\n"); //$NON-NLS-1$
		os.writeBytes("echo $(date)   Starting task >> " + err + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		os.writeBytes("{ time ${STDBUF}"); //$NON-NLS-1$

		// app
		os.writeBytes(app);

		// arguments
		for (final String arg : args) {
			os.write(' ');
			os.writeBytes(arg);
		}

		os.writeBytes(" ; }"); //$NON-NLS-1$

		os.writeBytes(redirect.toString());
		os.write('\n');

		os.writeBytes("OUT=$?\n"); //$NON-NLS-1$
		os.writeBytes("echo $(date)   Finished! >> " + err + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		os.writeBytes("exit ${OUT}\n"); //$NON-NLS-1$

	}

	/**
	 * In case the submission failed, we set the job state and forward error
	 */
	private IPJob jobControlFromJobException(Exception e, String jobId, AttributeManager attrs) {
		final String subId = attrs.getAttribute(JobAttributes.getSubIdAttributeDefinition()).getValue();

		attrs.addAttribute(PoolingIntervalsAndStatic.exceptionAttrDef.create(e.getLocalizedMessage()));
		attrs.addAttribute(JobAttributes.getStateAttributeDefinition().create(JobAttributes.State.COMPLETED));
		attrs.addAttribute(JobAttributes.getStatusAttributeDefinition().create(Messages.SMOAResourceManager_JobSubmissionFailed));

		getRuntimeSystem().addJobSubmissionError(subId, e);

		final IPJob newJob = super.doCreateJob(jobId, attrs);
		return newJob;
	}

	/**
	 * Adds the SMOA UUID of the activity to job properties
	 */
	private void setSmoaUid(String smoaUid, IPJob newJob) {
		final AttributeManager am = new AttributeManager();

		final Collection<IPJob> jc = new Vector<IPJob>();
		jc.add(newJob);

		am.addAttribute(SMOAJobAttributes.getSmoaUuidDef().create(smoaUid));

		doUpdateJobs(jc, am);
	}

	/**
	 * Triggered by fireCreateJobEvent by {@link SMOARuntimeSystem}, creates the
	 * job and submits it to the SMOA Computing service.
	 */
	@Override
	protected IPJob doCreateJob(String jobId, AttributeManager attrs) {

		// all temporary files are timestamped with the time generated below:
		final long time = System.currentTimeMillis();

		final SMOAConnection connection = configuration.getConnection();
		final SMOAFactory factory = connection.getFactory();

		final String originalName = attrs.getAttribute(ElementAttributes.getNameAttributeDefinition()).getValueAsString();

		// Name for SMOA Computing
		final JSDL jsdl = new JSDL(originalName);
		final List<String> arguments = jsdl.getArguments();

		Integer count = executeCount.get(originalName);
		if (count == null) {
			executeCount.put(originalName, 0);
			count = 0;
		}

		// Label for Eclipse
		attrs.addAttribute(ElementAttributes.getNameAttributeDefinition().create(originalName + " (" + count + ")")); //$NON-NLS-1$ //$NON-NLS-2$

		executeCount.put(originalName, ++count);

		// Working directory
		final String workDir = attrs.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue();
		if (workDir != null) {
			jsdl.setWorkingDirectory(workDir);
		}

		// Setting program
		final SMOARemoteServices remoteServices = (SMOARemoteServices) PTPRemoteCorePlugin.getDefault().getRemoteServices(
				configuration.getRemoteServicesId());

		final String appString = attrs.getAttribute(SMOAJobAttributes.getAppNameDef()).getValue();
		final Application app = configuration.getAppForName(appString);

		final List<String> execArgsAndPath = getExecAndArgs(attrs, connection, app);

		final String execPathAndName = execArgsAndPath.get(0);
		final List<String> appArguments = execArgsAndPath.subList(1, execArgsAndPath.size());

		jsdl.setOutput("stdout"); //$NON-NLS-1$
		jsdl.setError("stderr"); //$NON-NLS-1$

		// filenames for out and err, and filename for shell script to be
		// executed or for the machine file
		final String out = connection.getHomeDir() + "/.ptp_smoa_out_" + time; //$NON-NLS-1$
		final String err = connection.getHomeDir() + "/.ptp_smoa_err_" + time; //$NON-NLS-1$
		String machinefile = null;
		String sh = null;

		// Custom Make command
		String customMakeCommand = null;

		final BooleanAttribute ifCustomMake = attrs.getAttribute(SMOAJobAttributes.getIfCustomMakeDef());
		if (ifCustomMake != null && ifCustomMake.getValue() != null && ifCustomMake.getValue()) {
			final StringAttribute makeCommand = attrs.getAttribute(SMOAJobAttributes.getCustomMakeCommandDef());
			if (makeCommand != null && makeCommand.getValue() != null && !makeCommand.getValue().isEmpty()) {
				customMakeCommand = makeCommand.getValue();
			}
		}

		// If wrapper script has to be used
		if (SMOAJobAttributes.NO_WRAPPER_SCRIPT.equals(appString) || app == null) {

			sh = connection.getHomeDir() + "/.ptp_smoa_sh_" + time; //$NON-NLS-1$

			final BooleanAttribute makeAttr = attrs.getAttribute(SMOAJobAttributes.getMakeDef());
			final boolean make = (makeAttr == null ? false : makeAttr.getValue());

			// Creating script
			try {
				final DataOutputStream os = new DataOutputStream(remoteServices.getFileManager(connection).getResource(sh)
						.openOutputStream(0, null));
				createScript(appArguments, execPathAndName, out, err, os, make, customMakeCommand);
				os.close();
			} catch (final IOException e) {
				NotifyShell.open(Messages.SMOAResourceManager_7, e.getLocalizedMessage());
				return jobControlFromJobException(e, jobId, attrs);
			} catch (final CoreException e) {
				NotifyShell.open(Messages.SMOAResourceManager_8, e.getLocalizedMessage());
				return jobControlFromJobException(e, jobId, attrs);
			}

			jsdl.setExecutable("/bin/sh"); //$NON-NLS-1$
			arguments.add(sh);
		}
		// If the built-in application has to be used
		else {
			machinefile = connection.getHomeDir() + "/.ptp_smoa_machinefile_" //$NON-NLS-1$
					+ time;

			jsdl.setApplicationName(app.getName());
			jsdl.setApplicationVersion(app.getVersion());

			arguments.add(execPathAndName);
			arguments.addAll(appArguments);

			jsdl.getEnvironment().put(SMOAJobAttributes.ENV_STDOUT, out);
			jsdl.getEnvironment().put(SMOAJobAttributes.ENV_STDERR, err);
			jsdl.getEnvironment().put(SMOAJobAttributes.ENV_MACHINEFILE, machinefile);

			if (customMakeCommand != null) {
				jsdl.getEnvironment().put(SMOAJobAttributes.ENV_MAKE_COMMAND, customMakeCommand);
			}

		}

		// Others

		final StringAttribute attribute1 = attrs.getAttribute(SMOAJobAttributes.getDescDef());
		if (attribute1 != null && attribute1.getValue() != null && (!attribute1.getValue().isEmpty())) {
			jsdl.setJobDescription(attribute1.getValue());
		}

		final StringAttribute attribute2 = attrs.getAttribute(SMOAJobAttributes.getNativeSpecDef());
		if (attribute2 != null && attribute2.getValue() != null && (!attribute2.getValue().isEmpty())) {
			jsdl.setNativeSpecification(attribute2.getValue());
		}

		final IntegerAttribute attribute3 = attrs.getAttribute(SMOAJobAttributes.getMinCpuDef());
		if (attribute3 != null && attribute3.getValue() != null) {
			jsdl.setMinCpu(attribute3.getValue());
		}

		final IntegerAttribute attribute4 = attrs.getAttribute(SMOAJobAttributes.getMaxCpuDef());
		if (attribute4 != null && attribute4.getValue() != null) {
			jsdl.setMaxCpu(attribute4.getValue());
		}

		final ArrayAttribute<String> attribute5 = attrs.getAttribute(SMOAJobAttributes.getPrefferedDef());
		if (attribute5 != null && attribute5.getValue() != null) {
			for (final String machineName : attribute5.getValue()) {
				jsdl.getCandidateHosts().add(machineName);
			}
		}

		final StringMapAttribute attribute6 = attrs.getAttribute(SMOAJobAttributes.getEnvDef());
		if (attribute6 != null && (!attribute6.getValue().isEmpty())) {
			jsdl.getEnvironment().putAll(attribute6.getValue());
		}

		final BooleanAttribute attribute7 = attrs.getAttribute(SMOAJobAttributes.getMakeDef());
		if (attribute7 != null && attribute7.getValue()) {
			jsdl.getEnvironment().put(SMOAJobAttributes.ENV_IF_MAKE, "1"); //$NON-NLS-1$
		} else {
			jsdl.getEnvironment().remove(SMOAJobAttributes.ENV_IF_MAKE);
		}

		final StringAttribute attribute8 = attrs.getAttribute(SMOAJobAttributes.getQueueNameDef());
		if (attribute8 != null && attribute8.getValue() != null && (!attribute8.getValue().isEmpty())) {
			jsdl.setQueueName(attribute8.getValue());
		}

		ActivityEndpointReference activityReference;

		// Submitting the job
		try {
			activityReference = factory.createActivity(jsdl);
		} catch (final InvalidRequestMessageException e) {
			return jobControlFromJobException(e, jobId, attrs);
		} catch (final NotAuthorizedException e) {
			return jobControlFromJobException(e, jobId, attrs);
		} catch (final UnsupportedFeatureException e) {
			return jobControlFromJobException(e, jobId, attrs);
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}

		final IPJob jobControl = super.doCreateJob(jobId, attrs);

		setSmoaUid(activityReference.getActivityUUID(), jobControl);

		// Starting job monitoring thread
		final JobThread jobThread = new JobThread(getResourceManager(), factory, activityReference, jobControl, out, err, sh,
				machinefile);
		getResourceManager().addJobThread(jobControl.getID(), jobThread);
		jobThread.start();

		return jobControl;
	}

	@Override
	protected SMOAResourceManager getResourceManager() {
		return (SMOAResourceManager) super.getResourceManager();
	}

	@Override
	protected SMOARuntimeSystem getRuntimeSystem() {
		final IRuntimeSystem rs = super.getRuntimeSystem();
		if (rs instanceof SMOARuntimeSystem) {
			return (SMOARuntimeSystem) rs;
		}
		throw new RuntimeException();
	}

}