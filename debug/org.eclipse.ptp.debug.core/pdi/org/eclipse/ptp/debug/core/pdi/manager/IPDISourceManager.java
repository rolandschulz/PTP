package org.eclipse.ptp.debug.core.pdi.manager;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMixedInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;

/**
 * Interface for managing source
 * 
 */
public interface IPDISourceManager extends IPDIManager {

	/**
	 * Get detail type name
	 * 
	 * @param target
	 * @param typename
	 * @return
	 * @throws PDIException
	 */
	public String getDetailTypeName(IPDITarget target, String typename) throws PDIException;

	/**
	 * Get detail type name from variable
	 * 
	 * @param frame
	 * @param variable
	 * @return
	 * @throws PDIException
	 */
	public String getDetailTypeNameFromVariable(IPDIStackFrame frame, String variable) throws PDIException;

	/**
	 * Get instructions for address range
	 * 
	 * @param qTasks
	 * @param start
	 * @param end
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public IPDIInstruction[] getInstructions(TaskSet qTasks, BigInteger start, BigInteger end) throws PDIException;

	/**
	 * Get instructions for source line
	 * 
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public IPDIInstruction[] getInstructions(TaskSet qTasks, String filename, int linenum) throws PDIException;

	/**
	 * Get instructions for a range of source lines
	 * 
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @param lines
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public IPDIInstruction[] getInstructions(TaskSet qTasks, String filename, int linenum, int lines) throws PDIException;

	/**
	 * Get mixed instructions for an address range
	 * 
	 * @param qTasks
	 * @param start
	 * @param end
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public IPDIMixedInstruction[] getMixedInstructions(TaskSet qTasks, BigInteger start, BigInteger end) throws PDIException;

	/**
	 * Get mixed instructions for a source line
	 * 
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public IPDIMixedInstruction[] getMixedInstructions(TaskSet qTasks, String filename, int linenum) throws PDIException;

	/**
	 * Get mixed instructions for a range of source lines
	 * 
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @param lines
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public IPDIMixedInstruction[] getMixedInstructions(TaskSet qTasks, String filename, int linenum, int lines) throws PDIException;

	/**
	 * Get source paths
	 * 
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public String[] getSourcePaths(TaskSet qTasks) throws PDIException;

	/**
	 * Get type name for variable
	 * 
	 * @param target
	 * @param variable
	 * @return
	 * @throws PDIException
	 */
	public String getTypeName(IPDITarget target, String variable) throws PDIException;

	/**
	 * Get type name from variable
	 * 
	 * @param frame
	 * @param variable
	 * @return
	 * @throws PDIException
	 */
	public String getTypeNameFromVariable(IPDIStackFrame frame, String variable) throws PDIException;

	/**
	 * Set source paths
	 * 
	 * @param qTasks
	 * @param dirs
	 * @throws PDIException
	 * @since 4.0
	 */
	public void setSourcePaths(TaskSet qTasks, String[] dirs) throws PDIException;

}