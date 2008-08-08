package org.eclipse.ptp.rm.mpi.openmpi.core;

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;

public class OpenMPIApplicationAttributes {
	private static final String EFFECTIVE_OPEN_MPI_ENV_ATTR_ID = "Open_MPI_env";
	private static final String EFFECTIVE_OPEN_MPI_PROG_ARGS_ATTR_ID = "Open_MPI_progArgs";
	private static final String EFFECTIVE_OPEN_MPI_WORKING_DIR_ATTR_ID = "Open_MPI_workingDir";

 	private final static ArrayAttributeDefinition<String> effectiveOpenMPIEnvAttrDef = 
		new ArrayAttributeDefinition<String>(EFFECTIVE_OPEN_MPI_ENV_ATTR_ID, "Environment for Open MPI processes",
				"Effective environment supplied by Open MPI to executable on launch", true, null);

	private final static ArrayAttributeDefinition<String> effectiveOpenMPIProgArgsAttrDef = 
		new ArrayAttributeDefinition<String>(EFFECTIVE_OPEN_MPI_PROG_ARGS_ATTR_ID, "Program Arguments for Open MPI processes",
				"Effective command-line arguments by OpenMPI supplied to executable", true, null);
	
	private final static StringAttributeDefinition effectiveOpenMPIWorkingDirAttrDef = 
		new StringAttributeDefinition(EFFECTIVE_OPEN_MPI_WORKING_DIR_ATTR_ID, "Working Directory for Open MPI processes",
				"Effective working directory where Open MPI launched the processes", true, "");
	
	/**
	 * Environment variables that Open MPI has passed to the processes.
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static ArrayAttributeDefinition<String> getEffectiveOpenMPIEnvAttrDef() {
		return effectiveOpenMPIEnvAttrDef;
	}

	/**
	 * Program arguments that Open MPI has passed to the processes.
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static ArrayAttributeDefinition<String> getEffectiveOpenMPIProgArgsAttrDef() {
		return effectiveOpenMPIProgArgsAttrDef;
	}
	
	/**
	 * Working directory where Open MPI has started the processes.
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static StringAttributeDefinition getEffectiveOpenMPIWorkingDirAttrDef() {
		return effectiveOpenMPIWorkingDirAttrDef;
	}
}
