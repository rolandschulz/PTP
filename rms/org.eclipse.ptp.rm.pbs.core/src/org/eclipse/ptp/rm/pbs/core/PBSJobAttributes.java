/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - Initial API and implementatio Albert L. Rossi (NCSA) -
 * Updated attributes (bug 310189) Updated attributes 04/30/2010
 * Updated attributes 05/11/2010
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;

/**
 * Hard-coded mapping of known (static) PBS Job Attributes. These conform with
 * PBS's qsub, rather than representing all the attributes internal to the PBS
 * API.
 * 
 * @author arossi
 */
public class PBSJobAttributes {

	private static final Map<String, IAttributeDefinition<?, ?, ?>> attrMap;

	static {
		attrMap = new TreeMap<String, IAttributeDefinition<?, ?, ?>>();
		attrMap.put("Account_Name", // ACCOUNT_NAME //$NON-NLS-1$
				new StringAttributeDefinition("Account_Name", Messages.PBSJobAttributes_0, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_0, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Checkpoint", // CHECKPOINT //$NON-NLS-1$
				new StringAttributeDefinition("Checkpoint", Messages.PBSJobAttributes_1, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_1, true, "".trim())); //$NON-NLS-1$
		attrMap.put("depend", // DEPEND //$NON-NLS-1$
				new StringAttributeDefinition("depend", Messages.PBSJobAttributeName_2, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_2, true, "".trim())); //$NON-NLS-1$
		attrMap.put("destination", // DESTINATION //$NON-NLS-1$
				new StringAttributeDefinition("destination", Messages.PBSJobAttributeName_3, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_3, true, "".trim())); //$NON-NLS-1$
		attrMap.put("directive", // DIRECTIVE //$NON-NLS-1$
				new StringAttributeDefinition("directive", Messages.PBSJobAttributeName_4, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_4, true, "".trim())); //$NON-NLS-1$
		attrMap.put("export_all", // EXPORT_ALL //$NON-NLS-1$
				new BooleanAttributeDefinition("export_all", Messages.PBSJobAttributeName_5, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_5, true, new Boolean("true"))); //$NON-NLS-1$
		attrMap.put("Error_Path", // ERROR_PATH //$NON-NLS-1$
				new StringAttributeDefinition("Error_Path", Messages.PBSJobAttributeName_6, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_6, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Execution_Time", // EXECUTION_TIME //$NON-NLS-1$
				new StringAttributeDefinition("Execution_Time", Messages.PBSJobAttributeName_7, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_7, true, "".trim())); //$NON-NLS-1$
		attrMap.put("group_list", // GROUP_LIST //$NON-NLS-1$
				new StringAttributeDefinition("group_list", Messages.PBSJobAttributeName_8, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_8, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Hold_Types", // HOLD_TYPES //$NON-NLS-1$
				new StringAttributeDefinition("Hold_Types", Messages.PBSJobAttributeName_9, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_9, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Job_Name", // JOB_NAME //$NON-NLS-1$
				new StringAttributeDefinition("Job_Name", Messages.PBSJobAttributeName_10, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_10, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Join_Path", // JOIN_PATH //$NON-NLS-1$
				new BooleanAttributeDefinition("Join_Path", Messages.PBSJobAttributeName_11, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_11, true, new Boolean("false"))); //$NON-NLS-1$
		attrMap.put("Keep_Files", // KEEP_FILES //$NON-NLS-1$
				new StringAttributeDefinition("Keep_Files", Messages.PBSJobAttributeName_12, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_12, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Mail_Points", // MAIL_POINTS //$NON-NLS-1$
				new StringAttributeDefinition("Mail_Points", Messages.PBSJobAttributeName_13, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_13, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Mail_Users", // MAIL_USERS //$NON-NLS-1$
				new StringAttributeDefinition("Mail_Users", Messages.PBSJobAttributeName_14, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_14, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Output_Path", // OUTPUT_PATH //$NON-NLS-1$
				new StringAttributeDefinition("Output_Path", Messages.PBSJobAttributeName_15, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_15, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Priority", // PRIORITY //$NON-NLS-1$
				new StringAttributeDefinition("Priority", Messages.PBSJobAttributeName_16, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_16, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Rerunnable", // RERUNNABLE //$NON-NLS-1$
				new StringAttributeDefinition("Rerunnable", Messages.PBSJobAttributeName_17, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_17, true, "n".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.arch", // ARCH //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.arch", Messages.PBSJobAttributeName_18, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_18, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.cput", // CPUT //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.cput", Messages.PBSJobAttributeName_19, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_19, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.file", // FILE //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.file", Messages.PBSJobAttributeName_20, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_20, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.host", // HOST //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.host", Messages.PBSJobAttributeName_21, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_21, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.mem", // MEM //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.mem", Messages.PBSJobAttributeName_22, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_22, true, "")); //$NON-NLS-1$
		attrMap.put("Resource_List.ncpus", // NCPUS //$NON-NLS-1$
				new IntegerAttributeDefinition("Resource_List.ncpus", Messages.PBSJobAttributeName_23, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_23, true, new Integer("1"))); //$NON-NLS-1$
		attrMap.put("Resource_List.nice", // NICE //$NON-NLS-1$
				new IntegerAttributeDefinition("Resource_List.nice", Messages.PBSJobAttributeName_24, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_24, true, new Integer("0"))); //$NON-NLS-1$
		attrMap.put("Resource_List.nodes", // NODES //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.nodes", Messages.PBSJobAttributeName_25, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_25, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.ompthreads", // OMPTHREADS //$NON-NLS-1$
				new IntegerAttributeDefinition("Resource_List.ompthreads", Messages.PBSJobAttributeName_26, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_26, true, new Integer("1"))); //$NON-NLS-1$
		attrMap.put("Resource_List.pcput", // PCPUT //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.pcput", Messages.PBSJobAttributeName_27, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_27, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.pmem", // PMEM //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.pmem", Messages.PBSJobAttributeName_28, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_28, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.pvmem", // PVMEM //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.pvmem", Messages.PBSJobAttributeName_29, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_29, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.vmem", // VMEM //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.vmem", Messages.PBSJobAttributeName_30, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_30, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Resource_List.walltime", // WALLTIME //$NON-NLS-1$
				new StringAttributeDefinition("Resource_List.walltime", Messages.PBSJobAttributeName_31, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_31, true, "00:30:00")); //$NON-NLS-1$
		attrMap.put("Shell_Path_List", // SHELL_PATH_LIST //$NON-NLS-1$
				new StringAttributeDefinition("Shell_Path_List", Messages.PBSJobAttributeName_32, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_32, true, "".trim())); //$NON-NLS-1$
		attrMap.put("stagein", // STAGEIN //$NON-NLS-1$
				new StringAttributeDefinition("stagein", Messages.PBSJobAttributeName_33, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_33, true, "".trim())); //$NON-NLS-1$
		attrMap.put("stageout", // STAGEOUT //$NON-NLS-1$
				new StringAttributeDefinition("stageout", Messages.PBSJobAttributeName_34, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_34, true, "".trim())); //$NON-NLS-1$
		attrMap.put("User_List", // USER_LIST //$NON-NLS-1$
				new StringAttributeDefinition("User_List", Messages.PBSJobAttributeName_35, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_35, true, "".trim())); //$NON-NLS-1$
		attrMap.put("Variable_List", // VARIABLE_LIST //$NON-NLS-1$
				new StringAttributeDefinition("Variable_List", Messages.PBSJobAttributeName_36, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_36, true, "".trim())); //$NON-NLS-1$
		// non-standard, added internally
		attrMap.put("mpiOptions", // MPIOPTIONS //$NON-NLS-1$
				new StringAttributeDefinition("mpiOptions", Messages.PBSJobAttributeName_37, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_37, true, "-n 1".trim())); //$NON-NLS-1$
		// non-standard, added internally
		attrMap.put("script", // SCRIPT //$NON-NLS-1$
				new StringAttributeDefinition("script", Messages.PBSJobAttributeName_38, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_38, true, "".trim())); //$NON-NLS-1$
		// non-standard, added internally
		attrMap.put("mpiCommand", // MPICOMMAND //$NON-NLS-1$
				new StringAttributeDefinition("mpiCommand", Messages.PBSJobAttributeName_39, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_39, true, "mpiexec".trim())); //$NON-NLS-1$
		// non-standard, added internally
		attrMap.put("prependedBash", // PREPEND //$NON-NLS-1$
				new StringAttributeDefinition("prependedBash", Messages.PBSJobAttributeName_40, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_40, true, "".trim())); //$NON-NLS-1$
		// non-standard, added internally
		attrMap.put("postpendedBash", // POSTPEND //$NON-NLS-1$
				new StringAttributeDefinition("postpendedBash", Messages.PBSJobAttributeName_41, //$NON-NLS-1$
						Messages.PBSJobAttributeDescription_41, true, "".trim())); //$NON-NLS-1$
	}

	/**
	 * This is a provisional method, and will be replaced by one in which the
	 * attributes are obtained from the proxy during model definition.
	 * 
	 * @return a mapping of the known (static) PBS Job Attributes.
	 * @since 4.0
	 */
	public static Map<String, IAttributeDefinition<?, ?, ?>> getAttributeDefinitionMap() {
		return attrMap;
	}
}