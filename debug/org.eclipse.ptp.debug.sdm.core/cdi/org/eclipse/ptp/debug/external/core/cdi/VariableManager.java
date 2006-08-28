/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.external.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.ptp.debug.core.aif.AIF;
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueArray;
import org.eclipse.ptp.debug.core.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.aif.IAIFValueReference;
import org.eclipse.ptp.debug.core.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.aif.IValueAggregate;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIArgument;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIGlobalVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocalVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocalVariableDescriptor;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.external.core.cdi.event.VarChangedEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.ArgumentDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.GlobalVariable;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.LocalVariableDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.ThreadStorage;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.ThreadStorageDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.Variable;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.VariableDescriptor;
import org.eclipse.ptp.debug.external.core.commands.ListArgumentsCommand;
import org.eclipse.ptp.debug.external.core.commands.ListGlobalVariablesCommand;
import org.eclipse.ptp.debug.external.core.commands.ListLocalVariablesCommand;

/**
 * @author Clement chu
 *
 */
public class VariableManager extends Manager {
	static final IPCDIVariable[] EMPTY_VARIABLES = {};
	int MAX_STACK_DEPTH = Thread.STACKFRAME_DEFAULT_DEPTH;
	private Map variablesMap;

	public VariableManager(Session session) {
		super(session, true);
		variablesMap = new Hashtable();
	}
	public void shutdown() {
		variablesMap.clear();
	}	
	synchronized List getVariablesList(Target target) {
		List variablesList = (List) variablesMap.get(target);
		if (variablesList == null) {
			variablesList = Collections.synchronizedList(new ArrayList());
			variablesMap.put(target, variablesList);
		}
		return variablesList;
	}
	public Variable getVariable(int task_id, String varName) {
		Target target = (Target)((Session)getSession()).getTarget(task_id);
		return getVariable(target, varName);
	}
	public Variable getVariable(Target target, String varName) {
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getName().equals(varName)) {
				return vars[i];
			}
			/*
			Variable v = vars[i].getChild(varName);
			if (v != null) {
				return v;
			}
			*/
		}
		return null;
	}
	Variable findVariable(VariableDescriptor v) throws PCDIException {
		Target target = (Target)v.getTarget();
		IPCDIStackFrame vstack = v.getStackFrame();
		IPCDIThread vthread = v.getThread();
		String name = v.getName();
		int position = v.getPosition();
		int depth = v.getStackDepth();
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getName().equals(name) && vars[i].getCastingArrayStart() == v.getCastingArrayStart() && vars[i].getCastingArrayEnd() == v.getCastingArrayEnd() && VariableDescriptor.equalsCasting(vars[i], v)) {
				// check threads
				IPCDIThread thread = vars[i].getThread();
				if ((vthread == null && thread == null) || (vthread != null && thread != null && thread.equals(vthread))) {
					// check stackframes
					IPCDIStackFrame frame = vars[i].getStackFrame();
					if (vstack == null && frame == null) {
						vars[i].setAIF(v.getAIF());
						return vars[i];
					} else if (frame != null && vstack != null && frame.equals(vstack)) {
						if (vars[i].getPosition() == position) {
							if (vars[i].getStackDepth() == depth) {
								vars[i].setAIF(v.getAIF());
								return vars[i];
							}
						}
					}
				}
			}
		}
		return null;
	}	
	Variable[] getVariables(Target target) {
		List variableList = (List)variablesMap.get(target);
		if (variableList != null) {
			return (Variable[]) variableList.toArray(new Variable[variableList.size()]);
		}
		return new Variable[0];
	}
	public void checkType(StackFrame frame, String type) throws PCDIException {
		if (type != null && type.length() > 0) {
			Target target = (Target)frame.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);

			try {
				//TODO -- dunno how to do here?
				//incorrect type will throw exception
				throw new PCDIException("Not implemented yet - VariableManager: checkType");
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		} else {
			throw new PCDIException("VariableManager Unknown_type");
		}
	}
	void removeVar(Target target, Variable var) throws PCDIException {
		//TODO - not implement yet
		//target.getDebugger().deletevar(((Session)getSession()).createBitList(target.getTargetID()), var.getName());
		throw new PCDIException("Not implement yet - VariableManager: removeVar");		
	}
	public Variable removeVariableFromList(Target target, String varName) {
		List varList = getVariablesList(target);
		synchronized (varList) {
			for (Iterator iterator = varList.iterator(); iterator.hasNext();) {
				Variable variable = (Variable)iterator.next();
				if (variable.getName().equals(varName)) {
					iterator.remove();
					return variable;
				}
			}
		}
		return null;
	}
	private IPCDIVariable[] getVariables(IValueAggregate aggrValue, VariableDescriptor varDesc) throws PCDIException {
		ITypeAggregate aggrType = (ITypeAggregate)aggrValue.getType();
		try {
			int length = aggrValue.getChildrenNumber();
			IPCDIVariable[] vars = new IPCDIVariable[length];
			for (int i=0; i<length; i++) {
				IAIF newAIF = null;
				IAIFValue aifValue = aggrValue.getValue(i);
				if (aifValue instanceof IAIFValueReference) {
					IAIFValue parentValue = ((IAIFValueReference)aifValue).getParent();
					if (parentValue == null) {
						newAIF = new AIF(AIFFactory.UNKNOWNTYPE, AIFFactory.UNKNOWNVALUE);
					}
					else {
						newAIF = new AIF(parentValue.getType(), parentValue);
					}
				}
				else {
					newAIF = new AIF(aifValue.getType(), aggrValue.getValue(i));
				}
				vars[i] = createVariable(createVariableDescriptor(varDesc, newAIF, aggrType.getField(i), false));				
			}
			return vars;
		} catch (AIFException e) {
			throw new PCDIException(e);
		}
	}
	public IPCDIVariable[] getVariables(VariableDescriptor varDesc) throws PCDIException {
		IAIFValue parentValue = varDesc.getAIF().getValue();
		
		if (parentValue instanceof IValueAggregate) {
			return getVariables((IValueAggregate)parentValue, varDesc);
		}
		else if (parentValue instanceof IAIFValuePointer) {
			IAIFValuePointer valuePointer = (IAIFValuePointer)parentValue;
			IAIFValue aifValue = valuePointer.getValue();
			if (aifValue instanceof IValueAggregate) {
				return getVariables((IValueAggregate)aifValue, varDesc);
			}
			else {
				AIF newAIF = new AIF(aifValue.getType(), aifValue);
				return new IPCDIVariable[] { createVariable(createVariableDescriptor(varDesc, newAIF, "* " + varDesc.getName(), false)) };				
			}
		}
		return new IPCDIVariable[0];
	}

	public IPCDIVariable[] getVariablesAsArray(VariableDescriptor varDesc, int start, int length) throws PCDIException {
		IAIFValue parrentValue = varDesc.getAIF().getValue();
		
		if (parrentValue instanceof IAIFValueArray) {
			IAIFValueArray parentArrayValue = (IAIFValueArray)parrentValue;
			try {
				Object[] values = parentArrayValue.getCurrentValues();
				boolean hasMoreDim = parentArrayValue.hasMoreDimension(values);
				IPCDIVariable[] vars = new IPCDIVariable[values.length];
				for (int i=0; i<values.length; i++) {
					if (hasMoreDim) {
						IAIF newAIF = AIFFactory.createAIFIndexedArray(parentArrayValue, i);
						vars[i] = createVariable(getVariableDescriptorAsArray(varDesc, newAIF, "["+i+"]", start, ((Object[])values[i]).length));
					}
					else {
						IAIFValue aifValue = (IAIFValue)values[i];
						IAIF newAIF = new AIF(aifValue.getType(), aifValue);
						vars[i] = createVariable(getVariableDescriptorAsArray(varDesc, newAIF, "["+i+"]", 0, 1));
					}
				}
				return vars;
			} catch (AIFException e) {
				throw new PCDIException(e);
			}
		}
		return new IPCDIVariable[0];
	}
	private VariableDescriptor createVariableDescriptor(VariableDescriptor varDesc, IAIF aif, String eName, boolean extName) throws PCDIException {
		Target target = (Target)varDesc.getTarget();
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		
		String name = extName?(varDesc.getName() + eName) : eName;
		String fullName = extName?(varDesc.getFullName() + eName) : eName;
		
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();
		
		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			return new ArgumentDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			return new LocalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			return new GlobalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			return new ThreadStorageDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else {
			throw new PCDIException("VariableManager.Unknown_variable_object");			
		}	
	}

	private VariableDescriptor createVariableDescriptor(VariableDescriptor varDesc, IAIF aif) throws PCDIException {
		Target target = (Target)varDesc.getTarget();
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();

		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			return new ArgumentDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			return new LocalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			return new GlobalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			return new ThreadStorageDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else {
			throw new PCDIException("VariableManager.Unknown_variable_object");			
		}	
	}
	public VariableDescriptor getVariableDescriptorAsArray(VariableDescriptor varDesc, IAIF aif, String eName, int start, int length) throws PCDIException {
		VariableDescriptor vo = createVariableDescriptor(varDesc, aif, eName, true);
		vo.setCastingArrayStart(start);
		vo.setCastingArrayEnd(length);
		return vo;
	}
	public VariableDescriptor getVariableDescriptorAsArray(VariableDescriptor varDesc, IAIF aif, int start, int length) throws PCDIException {
		VariableDescriptor vo = createVariableDescriptor(varDesc, aif);
		vo.setCastingArrayStart(varDesc.getCastingArrayStart() + start);
		vo.setCastingArrayEnd(length);
		return vo;
	}
	public VariableDescriptor getVariableDescriptorAsArray(VariableDescriptor varDesc, int start, int length) throws PCDIException {
		return getVariableDescriptorAsArray(varDesc, varDesc.getAIF(), start, length);
	}
	public VariableDescriptor getVariableDescriptorAsType(VariableDescriptor varDesc, String type) throws PCDIException {
		// throw an exception if not a good type.
		Target target = (Target)varDesc.getTarget();
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();
		IAIF aif = varDesc.getAIF();		

		// Check the type validity.
		{
			StackFrame f = frame;
			if (f == null) {
				if (thread != null) {
					f = thread.getCurrentStackFrame();
				} else {
					Thread t = (Thread)target.getCurrentThread();
					f = t.getCurrentStackFrame();
				}
			}
			checkType(f, type);
		}

		VariableDescriptor vo = null;

		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			vo = new ArgumentDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			vo = new LocalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			vo = new GlobalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			vo = new ThreadStorageDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else {
			throw new PCDIException("VariableManager.Unknown_variable_object");			
		}

		String[] castings = varDesc.getCastingTypes();
		if (castings == null) {
			castings = new String[] { type };
		} else {
			String[] temp = new String[castings.length + 1];
			System.arraycopy(castings, 0, temp, 0, castings.length);
			temp[castings.length] = type;
			castings = temp;
		}
		vo.setCastingTypes(castings);
		return vo;
	}
	public Variable createVariable(VariableDescriptor varDesc) throws PCDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			return createLocalVariable((LocalVariableDescriptor)varDesc);
		} else if (varDesc instanceof GlobalVariableDescriptor) {
			return createGlobalVariable((GlobalVariableDescriptor)varDesc);
		} else if (varDesc instanceof ThreadStorageDescriptor) {
			return createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		throw new PCDIException("VariableManager.Unknown_variable_object");			
	}
	public Argument createArgument(ArgumentDescriptor argDesc) throws PCDIException {
		Variable variable = findVariable(argDesc);
		Argument argument = null;
		if (variable != null && variable instanceof Argument) {
			argument = (Argument) variable;
		}
		if (argument == null) {
			//String name = argDesc.getQualifiedName();
			StackFrame stack = (StackFrame)argDesc.getStackFrame();
			Target target = (Target)argDesc.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(stack.getThread(), false);
			((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
			try {
				argument = new Argument(argDesc);
				List variablesList = getVariablesList(target);
				if (!variablesList.contains(argument))
					variablesList.add(argument);
				/*
				ICDIArgument[] args = target.getDebugger().listArguments(((Session)getSession()).createBitList(target.getTargetID()), currentFrame);
				for (int i=0; i<args.length; i++) {
					if (name.equals(args[i].getQualifiedName())) {
						argument = new Argument(argDesc);
						List variablesList = getVariablesList(target);
						variablesList.add(argument);
					}	
				}
				*/
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		}
		return argument;
	}
	public IPCDIArgumentDescriptor[] getArgumentDescriptors(StackFrame frame) throws PCDIException {
		List argObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			ListArgumentsCommand argCmd = new ListArgumentsCommand(target.getTask(), frame, frame.getThread().getStackFrameCount());
			target.getDebugger().postCommand(argCmd);
			IPCDIArgument[] args = argCmd.getArguments();
			for (int i = 0; i < args.length; i++) {
				VariableDescriptor varDesc = (VariableDescriptor) args[i];
				Thread thread = (Thread)varDesc.getThread();
				String name = varDesc.getName();
				String fName = varDesc.getQualifiedName();
				int pos = varDesc.getPosition();
				int depth = varDesc.getStackDepth();
				/*
				IAIF aif = varDesc.getAIF();
				if (aif == null) {
					GetAIFCommand aifCmd = new GetAIFCommand(tasks, fName);
					session.getDebugger().postCommand(aifCmd);
					aif = aifCmd.getAIF();
				}
				*/
				argObjects.add(new ArgumentDescriptor(target, thread, frame, name, fName, pos, depth, null));
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (IPCDIArgumentDescriptor[]) argObjects.toArray(new IPCDIArgumentDescriptor[0]);
	}
	public GlobalVariableDescriptor getGlobalVariableDescriptor(Target target, String filename, String function, String name) throws PCDIException {
		if (filename == null) {
			filename = new String();
		}
		if (function == null) {
			function = new String();
		}
		if (name == null) {
			name = new String();
		}
		StringBuffer buffer = new StringBuffer();
		if (filename.length() > 0) {
			buffer.append('\'').append(filename).append('\'').append("::");
		}
		if (function.length() > 0) {
			buffer.append(function).append("::");
		}
		buffer.append(name);
		//TODO - put null for AIF
		return new GlobalVariableDescriptor(target, null, null, buffer.toString(), null, 0, 0, null);
	}
	public GlobalVariable createGlobalVariable(GlobalVariableDescriptor varDesc) throws PCDIException {
		Variable variable = findVariable(varDesc);
		GlobalVariable global = null;
		if (variable instanceof GlobalVariable) {
			global = (GlobalVariable)variable;
		}
		if (global == null) {
			String name = varDesc.getQualifiedName();
			Target target = (Target)varDesc.getTarget();
			ListGlobalVariablesCommand varCmd = new ListGlobalVariablesCommand(target.getTask());
			target.getDebugger().postCommand(varCmd);
			IPCDIGlobalVariable[] vars = varCmd.getGlobalVariables();
			System.err.println(" ++++++++++++++++ listGlobalVariables: " + vars.length + " ++++++++++++++++");
			for (int i = 0; i < vars.length; i++) {
				if (name.equals(vars[i].getQualifiedName())) {
					global = new GlobalVariable(varDesc);
					List variablesList = getVariablesList(target);
					variablesList.add(global);
					break;
				}
			}
		}
		return global;
	}
	public IPCDILocalVariableDescriptor[] getLocalVariableDescriptors(StackFrame frame) throws PCDIException {
		List varObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		
		try {
			ListLocalVariablesCommand varCmd = new ListLocalVariablesCommand(target.getTask(), currentFrame);
			target.getDebugger().postCommand(varCmd);
			IPCDILocalVariable[] vars = varCmd.getLocalVariables();
			for (int i = 0; i < vars.length; i++) {
				VariableDescriptor varDesc = (VariableDescriptor)vars[i];
				Thread thread = (Thread)varDesc.getThread();
				String name = varDesc.getName();
				String fName = varDesc.getQualifiedName();
				int pos = varDesc.getPosition();
				int depth = varDesc.getStackDepth();
				/*
				IAIF aif = varDesc.getAIF();
				if (aif == null) {
					GetAIFCommand aifCmd = new GetAIFCommand(tasks, fName);
					session.getDebugger().postCommand(aifCmd);
					aif = aifCmd.getAIF();
				}
				*/
				varObjects.add(new LocalVariableDescriptor(target, thread, frame, name, fName, pos, depth, null));
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (IPCDILocalVariableDescriptor[]) varObjects.toArray(new IPCDILocalVariableDescriptor[0]);
	}
	public LocalVariable createLocalVariable(LocalVariableDescriptor varDesc) throws PCDIException {
		Variable variable = findVariable(varDesc);
		LocalVariable local = null;
		if (variable instanceof LocalVariable) {
			local = (LocalVariable)variable;
		}
		if (local == null) {
			//String name = varDesc.getQualifiedName();
			StackFrame stack = (StackFrame)varDesc.getStackFrame();
			Target target = (Target)varDesc.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(stack.getThread(), false);
			((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
			try {
				local = new LocalVariable(varDesc);
				List variablesList = getVariablesList(target);
				if (!variablesList.contains(local))
					variablesList.add(local);
				/*
				ICDILocalVariable[] vars = target.getDebugger().listLocalVariables(((Session)getSession()).createBitList(target.getTargetID()), currentFrame);
				for (int i = 0; i < vars.length; i++) {
					if (name.equals(vars[i].getQualifiedName())) {
						local = new LocalVariable(varDesc);
						List variablesList = getVariablesList(target);
						variablesList.add(local);
					}
				}
				*/
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		}
		return local;
	}
	public IPCDIThreadStorageDescriptor[] getThreadStorageDescriptors(Thread thread) throws PCDIException {
		return new IPCDIThreadStorageDescriptor[0];
	}
	public ThreadStorage createThreadStorage(ThreadStorageDescriptor desc) throws PCDIException {
		throw new PCDIException("cdi.VariableManager.Unknown_variable_object");
	}
	public void destroyVariable(Variable variable) throws PCDIException {
		/*
		Target target = (Target)variable.getTarget();
		VarDeletedEvent del = new VarDeletedEvent(target.getSession(), target.getTask(), null, variable.getName());
		target.getDebugger().fireEvent(del);
		*/
	}
	public void destroyAllVariables(Target target) throws PCDIException {
		/*
		Variable[] variables = getVariables(target);
		for (int i = 0; i < variables.length; ++i) {
			VarDeletedEvent del = new VarDeletedEvent(target.getSession(), target.getTask(), null, variables[i].getName());
			target.getDebugger().fireEvent(del);
		}
		*/
	}
	private boolean isMatchName(String vartext, String[] changedVars) {
		for (int j=0; j<changedVars.length; j++) {
			if (changedVars[j].equals(vartext)) {
				return true;
			}
		}
		return false;
	}		
	public void update(Target target, String[] changedVars) throws PCDIException {
		int highLevel = 0;
		int lowLevel = 0;
		IPCDIStackFrame[] frames = null;
		StackFrame currentStack = null;
		List eventList = new ArrayList();
		Variable[] vars = getVariables(target);
	
		Thread currentThread = (Thread)target.getCurrentThread();
		if (currentThread != null) {
			currentStack = currentThread.getCurrentStackFrame();
			if (currentStack != null) {
				highLevel = currentStack.getLevel();
			}
			if (highLevel > MAX_STACK_DEPTH) {
				highLevel = MAX_STACK_DEPTH;
			}
			lowLevel = highLevel - MAX_STACK_DEPTH;
			if (lowLevel < 0) {
				lowLevel = 0;
			}
			frames = currentThread.getStackFrames(0, highLevel);
		}

		for (int i = 0; i<vars.length; i++) {
			if (isVariableNeedsToBeUpdate(vars[i], currentStack, frames, lowLevel)) {
				vars[i].setUpdated(true);
				String varName = vars[i].getName();
				if (isMatchName("("+varName+")", changedVars)) {
					eventList.add(new VarChangedEvent(target.getSession(), target.getTask(), vars[i], varName));
				}
			} else {
				vars[i].setUpdated(false);
			}
		}
		IPCDIEvent[] events = (IPCDIEvent[]) eventList.toArray(new IPCDIEvent[0]);
		target.getDebugger().fireEvents(events);
	}
	public void update(Target target) throws PCDIException {}
	/*
	public void update(Variable variable) throws PCDIException {
		Target target = (Target)variable.getTarget();
		List eventList = new ArrayList();
		update(target, variable, eventList);
		IPCDIEvent[] events = (IPCDIEvent[]) eventList.toArray(new IPCDIEvent[0]);
		((Session)getSession()).getDebugger().fireEvents(events);		
	}
	public void update(Target target, Variable variable, List eventList) throws PCDIException {
		//String varName = variable.getName();
		//TODO how to implement ?
		//variable.setUpdated(true);
		throw new PCDIException("Not implement yet - VariableManager: update");
	}
	*/
	boolean isVariableNeedsToBeUpdate(Variable variable, IPCDIStackFrame current, IPCDIStackFrame[] frames, int lowLevel) throws PCDIException {
		IPCDIStackFrame varStack = variable.getStackFrame();
		boolean inScope = false;

		// Something wrong and the program terminated bail out here.
		if (current == null || frames == null) {
			return false;
		}
		// If the variable Stack is null, it means this is a global variable we should update
		if (varStack == null) {
			return true;
		} else if (varStack.equals(current)) {
			// The variable is in the current selected frame it should be updated
			return true;
		} else {
			if (varStack.getLevel() >= lowLevel) {
				for (int i = 0; i < frames.length; i++) {
					if (varStack.equals(frames[i])) {
						inScope = true;
					}
				}
			} else {
				inScope = true;
			}
		}
		return !inScope;
	}
}
