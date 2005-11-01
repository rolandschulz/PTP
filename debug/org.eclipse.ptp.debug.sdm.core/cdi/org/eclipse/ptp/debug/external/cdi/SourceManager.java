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
package org.eclipse.ptp.debug.external.cdi;

import java.util.StringTokenizer;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.cdi.model.type.BoolType;
import org.eclipse.ptp.debug.external.cdi.model.type.CharType;
import org.eclipse.ptp.debug.external.cdi.model.type.DoubleType;
import org.eclipse.ptp.debug.external.cdi.model.type.EnumType;
import org.eclipse.ptp.debug.external.cdi.model.type.FloatType;
import org.eclipse.ptp.debug.external.cdi.model.type.IntType;
import org.eclipse.ptp.debug.external.cdi.model.type.LongLongType;
import org.eclipse.ptp.debug.external.cdi.model.type.LongType;
import org.eclipse.ptp.debug.external.cdi.model.type.ShortType;
import org.eclipse.ptp.debug.external.cdi.model.type.StructType;
import org.eclipse.ptp.debug.external.cdi.model.type.Type;
import org.eclipse.ptp.debug.external.cdi.model.type.VoidType;
import org.eclipse.ptp.debug.external.cdi.model.type.WCharType;

/**
 * @author Clement chu
 * 
 */
public class SourceManager extends Manager {
	public SourceManager(Session session) {
		super(session, false);
	}
	public void update(Target target) throws CDIException {
		//Do dothing here
	}
	public void shutdown() {
		
	}
	public Type getType(Target target, String name) throws CDIException {
		if (name == null) {
			name = new String();
		}
		String typename = name.trim();
		//FIXME ignore GDBType 
		//GDBType gdbType = gdbTypeParser.parse(typename);
		//FIXME - NOTE: Mapping AIF type string
		return toCDIType(target, typename);
	}
	Type toCDIType(Target target, String name) throws CDIException {
		if (name == null) {
			name = new String();
		}
		String typename = name.trim();
		// Check the primitives.
		if (typename.equals("char")) { 
			return new CharType(target, typename);
		} else if (typename.equals("wchar_t")) { 
			return new WCharType(target, typename);
		} else if (typename.equals("short")) { 
			return new ShortType(target, typename);
		} else if (typename.equals("int")) { 
			return new IntType(target, typename);
		} else if (typename.equals("long")) { 
			return new LongType(target, typename);
		} else if (typename.equals("unsigned")) { 
			return new IntType(target, typename, true);
		} else if (typename.equals("signed")) { 
			return new IntType(target, typename);
		} else if (typename.equals("bool")) { 
			return new BoolType(target, typename);
		} else if (typename.equals("_Bool")) { 
			return new BoolType(target, typename);
		} else if (typename.equals("float")) { 
			return new FloatType(target, typename);
		} else if (typename.equals("double")) { 
			return new DoubleType(target, typename);
		} else if (typename.equals("void")) { 
			return new VoidType(target, typename);
		} else if (typename.equals("enum")) { 
			return new EnumType(target, typename);
		} else if (typename.equals("union")) { 
			return new StructType(target, typename);
		} else if (typename.equals("struct")) { 
			return new StructType(target, typename);
		} else if (typename.equals("class")) { 
			return new StructType(target, typename);
		}

		// GDB has some special types for int
		if (typename.equals("int8_t")) { 
			return new CharType(target, typename);
		} else if (typename.equals("int16_t")) { 
			return new ShortType(target, typename);
		} else if (typename.equals("int32_t")) { 
			return new LongType(target, typename);
		} else if (typename.equals("int64_t")) { 
			return new LongLongType(target, typename);
		} else if (typename.equals("int128_t")) { 
			return new IntType(target, typename); // ????
		}

		if (typename.equals("int8_t")) { 
			return new CharType(target, typename);
		} else if (typename.equals("uint8_t")) { 
			return new CharType(target, typename, true);
		} else if (typename.equals("int16_t")) { 
			return new ShortType(target, typename);
		} else if (typename.equals("uint16_t")) { 
			return new ShortType(target, typename, true);
		} else if (typename.equals("int32_t")) { 
			return new LongType(target, typename);
		} else if (typename.equals("uint32_t")) { 
			return new LongType(target, typename, true);
		} else if (typename.equals("int64_t")) { 
			return new LongLongType(target, typename);
		} else if (typename.equals("uint64_t")) { 
			return new LongLongType(target, typename, true);
		} else if (typename.equals("int128_t")) { 
			return new IntType(target, typename); // ????
		} else if (typename.equals("uint128_t")) { 
			return new IntType(target, typename, true); // ????			
		}
		StringTokenizer st = new StringTokenizer(typename);
		int count = st.countTokens();

		if (count == 2) {
			String first = st.nextToken();
			String second = st.nextToken();

			// ISOC allows permutations:
			// "signed int" and "int signed" are equivalent
			boolean isUnsigned =  (first.equals("unsigned") || second.equals("unsigned"));  
			boolean isSigned =    (first.equals("signed") || second.equals("signed"));  
			boolean isChar =      (first.equals("char") || second.equals("char"));  
			boolean isInt =       (first.equals("int") || second.equals("int"));  
			boolean isLong =      (first.equals("long") || second.equals("long"));  
			boolean isShort =     (first.equals("short") || second.equals("short"));  
			boolean isLongLong =  (first.equals("long") && second.equals("long"));  
			
			boolean isDouble =    (first.equals("double") || second.equals("double"));  
			boolean isFloat =     (first.equals("float") || second.equals("float"));  
			boolean isComplex =   (first.equals("complex") || second.equals("complex") ||  
			                       first.equals("_Complex") || second.equals("_Complex"));  
			boolean isImaginery = (first.equals("_Imaginary") || second.equals("_Imaginary"));  

			boolean isStruct =     first.equals("struct"); 
			boolean isClass =      first.equals("class"); 
			boolean isUnion =      first.equals("union"); 
			boolean isEnum =       first.equals("enum"); 

			if (isChar && (isSigned || isUnsigned)) {
				return new CharType(target, typename, isUnsigned);
			} else if (isShort && (isSigned || isUnsigned)) {
				return new ShortType(target, typename, isUnsigned);
			} else if (isInt && (isSigned || isUnsigned)) {
				return new IntType(target, typename, isUnsigned);
			} else if (isLong && (isInt || isSigned || isUnsigned)) {
				return new LongType(target, typename, isUnsigned);
			} else if (isLongLong) {
				return new LongLongType(target, typename);
			} else if (isDouble && (isLong || isComplex || isImaginery)) {
				return new DoubleType(target, typename, isComplex, isImaginery, isLong);
			} else if (isFloat && (isComplex || isImaginery)) {
				return new FloatType(target, typename, isComplex, isImaginery);
			} else if (isStruct) {
				return new StructType(target, typename);
			} else if (isClass) {
				return new StructType(target, typename);
			} else if (isUnion) {
				return new StructType(target, typename);
			} else if (isEnum) {
				return new EnumType(target, typename);
			}
		} else if (count == 3) {
			// ISOC allows permutation. replace short by: long or short
			// "unsigned short int", "unsigned int short"
			// "short unsigned int". "short int unsigned"
			// "int unsinged short". "int short unsigned"
			//
			// "unsigned long long", "long long unsigned"
			// "signed long long", "long long signed"
			String first = st.nextToken();
			String second = st.nextToken();
			String third = st.nextToken();

			boolean isSigned =    (first.equals("signed") || second.equals("signed") || third.equals("signed"));   
			boolean unSigned =    (first.equals("unsigned") || second.equals("unsigned") || third.equals("unsigned"));   
			boolean isInt =       (first.equals("int") || second.equals("int") || third.equals("int"));   
			boolean isLong =      (first.equals("long") || second.equals("long") || third.equals("long"));   
			boolean isShort =     (first.equals("short") || second.equals("short") || third.equals("short"));   
			boolean isLongLong =  (first.equals("long") && second.equals("long")) || (second.equals("long") && third.equals("long"));  
			boolean isDouble =    (first.equals("double") || second.equals("double") || third.equals("double"));   
			boolean isComplex =   (first.equals("complex") || second.equals("complex") || third.equals("complex") || first.equals("_Complex") || second.equals("_Complex") || third.equals("_Complex"));   
			boolean isImaginery = (first.equals("_Imaginary") || second.equals("_Imaginary") || third.equals("_Imaginary"));   

			if (isShort && isInt && (isSigned || unSigned)) {
				return new ShortType(target, typename, unSigned);
			} else if (isLong && isInt && (isSigned || unSigned)) {
				return new LongType(target, typename, unSigned);
			} else if (isLongLong && (isSigned || unSigned)) {
				return new LongLongType(target, typename, unSigned);
			} else if (isDouble && isLong && (isComplex || isImaginery)) {
				return new DoubleType(target, typename, isComplex, isImaginery, isLong);
			}
		} else if (count == 4) {
			// ISOC allows permutation:
			// "unsigned long long int", "unsigned int long long"
			// "long long unsigned int". "long long int unsigned"
			// "int unsigned long long". "int long long unsigned"
			String first = st.nextToken();
			String second = st.nextToken();
			String third = st.nextToken();
			String fourth = st.nextToken();

			boolean unSigned = (first.equals("unsigned") || second.equals("unsigned") || third.equals("unsigned") || fourth.equals("unsigned"));    
			boolean isSigned = (first.equals("signed") || second.equals("signed") || third.equals("signed") || fourth.equals("signed"));    
			boolean isInt =    (first.equals("int") || second.equals("int") || third.equals("int") || fourth.equals("int"));
			boolean isLongLong =   (first.equals("long") && second.equals("long")) || (second.equals("long") && third.equals("long")) || (third.equals("long") && fourth.equals("long")); 

			if (isLongLong && isInt && (isSigned || unSigned)) {
				return new LongLongType(target, typename, unSigned);
			}
		}
		throw new CDIException("SourceManager.Unknown_type");
	}
	public String getDetailTypeNameFromVariable(StackFrame frame, String variable) throws CDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			return getDetailTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
	public String getDetailTypeName(Target target, String typeName) throws CDIException {
		throw new CDIException("Not implement yet - SourceManager: getDetailsTypeName");
		//return target.getDebugger().getVariableType(((Session)getSession()).createBitList(target.getTargetID()), typeName);
	}
	public String getTypeNameFromVariable(StackFrame frame, String variable) throws CDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			return getTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
	public String getTypeName(Target target, String variable) throws CDIException {
		return target.getDebugger().getVariableType(((Session)getSession()).createBitList(target.getTargetID()), variable);
	}
}
