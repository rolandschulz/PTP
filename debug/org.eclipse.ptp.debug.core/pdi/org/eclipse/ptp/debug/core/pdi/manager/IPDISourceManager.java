package org.eclipse.ptp.debug.core.pdi.manager;

import java.math.BigInteger;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMixedInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;

public interface IPDISourceManager extends IPDIManager {

	/**
	 * @param target
	 * @param typename
	 * @return
	 * @throws PDIException
	 */
	public String getDetailTypeName(IPDITarget target, String typename)
			throws PDIException;

	/**
	 * @param frame
	 * @param variable
	 * @return
	 * @throws PDIException
	 */
	public String getDetailTypeNameFromVariable(IPDIStackFrame frame,
			String variable) throws PDIException;

	/**
	 * @param qTasks
	 * @param start
	 * @param end
	 * @return
	 * @throws PDIException
	 */
	public IPDIInstruction[] getInstructions(BitList qTasks, BigInteger start,
			BigInteger end) throws PDIException;

	/**
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @return
	 * @throws PDIException
	 */
	public IPDIInstruction[] getInstructions(BitList qTasks, String filename,
			int linenum) throws PDIException;

	/**
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @param lines
	 * @return
	 * @throws PDIException
	 */
	public IPDIInstruction[] getInstructions(BitList qTasks, String filename,
			int linenum, int lines) throws PDIException;

	/**
	 * @param qTasks
	 * @param start
	 * @param end
	 * @return
	 * @throws PDIException
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks,
			BigInteger start, BigInteger end) throws PDIException;

	/**
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @return
	 * @throws PDIException
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks,
			String filename, int linenum) throws PDIException;

	/**
	 * @param qTasks
	 * @param filename
	 * @param linenum
	 * @param lines
	 * @return
	 * @throws PDIException
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks,
			String filename, int linenum, int lines) throws PDIException;

	/**
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 */
	public String[] getSourcePaths(BitList qTasks) throws PDIException;

	/**
	 * @param target
	 * @param variable
	 * @return
	 * @throws PDIException
	 */
	public String getTypeName(IPDITarget target, String variable)
			throws PDIException;

	/**
	 * @param frame
	 * @param variable
	 * @return
	 * @throws PDIException
	 */
	public String getTypeNameFromVariable(IPDIStackFrame frame, String variable)
			throws PDIException;

	/**
	 * @param qTasks
	 * @param dirs
	 * @throws PDIException
	 */
	public void setSourcePaths(BitList qTasks, String[] dirs)
			throws PDIException;

}