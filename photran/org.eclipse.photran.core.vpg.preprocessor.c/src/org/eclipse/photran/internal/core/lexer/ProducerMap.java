/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import java.util.ArrayList;

import org.eclipse.photran.internal.core.preprocessor.c.CppHelper;
import org.eclipse.photran.internal.core.preprocessor.c.IToken;

/**
 * This class contains a mapping of CPP directives and macros 
 * (referred to in this class as "producers") to their expansions.
 * It is designed as a helper class to assign CPP directives
 * and macros to Photran tokens, and it takes into account complicated
 * overlapping corner cases. Iteration over the Photran tokens and
 * the mapping should be done together.
 * See CPreprocessingFreeFormLexerPhase1 for how this class is used.
 * 
 * @author Matthew Michelotti
 */
public class ProducerMap {
	/**
	 * A mapping of offsets to producers. Each element in this array
	 * is a String and integer pair. The string of an array element is
	 * considered the producer of all offsets between the offset value of
	 * that element and the offset value of the next element. If the string
	 * of an element is null, that means that all offsets between the offset
	 * value of that element and the offset value of the next element have
	 * no producer. The following conditions are guaranteed after the
	 * ProducerMap is constructed:
	 * <li>the offsets in the array are in increasing order, including
	 *  the possibility of equality</li>
	 * <li>the first element has an offset of zero</li>
	 * <li>the second-to-last element has an offset of finalOffset</li>
	 * <li>the last element has an offset greater than finalOffset and
	 *  a string value of null</li>
	 * <li>all non-null strings refer to different producers. Thus,
	 *  no two strings should be equal using the "==" operator</li>
	 * <li>there are no two consecutive elements where the string is
	 *  null, except possibly among the last three elements</li>
	 */
	private final StringWithOffset[] mapping;
	/**the combined length of the CPP tokens. This is the largest possible
	 * value for markA or markB.*/
	private final int finalOffset;
	
	/**
	 * an offset marking the start of the interval concerned.
	 * This offset can only be increased unless the ProducerMap is reset.
	 * This value must be between zero and finalOffset, and <= markB.
	 */
	private int markA;
	/**
	 * an offset marking the end of the interval concerned.
	 * This offset can only be increased unless the ProducerMap is reset.
	 * This value must be between zero and finalOffset, and >= markA.
	 */
	private int markB;
	/**
	 * an index in the mapping corresponding to markA. It marks the first
	 * element in the mapping that is needed to analyze the interval
	 * between markA and markB. Specifically, if there is an offset
	 * in the mapping equal to markA, indexPreA is the index
	 * of the first such array element. Otherwise, indexPreA is the index
	 * of the last array element with a smaller offset than markA.
	 */
	private int indexPreA;
	/**
	 * an index in the mapping corresponding to markB. It marks the last
	 * element in the mapping that is needed to analyze the interval
	 * between markA and markB. Specifically, if there is an offset
	 * in the mapping equal to markB, indexPostB is the index
	 * of the last such array element. Otherwise, indexPostB is the index
	 * of the first array element with a larger offset than markA.
	 */
	private int indexPostB;
	
	/**
	 * A constructor which creates a producer mapping from a series
	 * of CPP tokens.
	 * @param token - the first token in the series. The remaining tokens
	 *        are assumed to be attainable using IToken.getNext().
	 */
	public ProducerMap(IToken token) {
		ArrayList<StringWithOffset> mappingAL
				= new ArrayList<StringWithOffset>();
		
		int offset = 0;
		IToken lastProducer = null;
		/*for(IToken tok = token; tok != null; tok = tok.getNext())*/
		IToken tok = token;
		IToken prevTok = null;
		while(tok != null)
		{
			IToken producer = CppHelper.getAncestor(tok, true);
			if(producer == tok) producer = null;
			
			if(lastProducer != null && producer != lastProducer) {
				mappingAL.add(new StringWithOffset(null, offset));
			}
			offset += CppHelper.getPreWhiteSpaceLength(tok);
			if(producer != null && producer != lastProducer) {
				mappingAL.add(new StringWithOffset(
						CppHelper.getImage(producer), offset));
			}
			offset += CppHelper.getImageLength(tok);
			
			lastProducer = producer;
			prevTok = tok;
			tok = tok.getNext();
		}

		finalOffset = offset;
		
		//append one or two elements to the mapping in order to conform with
		//the requirements specified in the comment of the field "mapping"
		if(mappingAL.size() > 0) {
			StringWithOffset lastSWO = mappingAL.get(mappingAL.size()-1);
			if(lastSWO.offset < finalOffset || lastSWO.string != null)
				mappingAL.add(new StringWithOffset(null, finalOffset));
		}

		else mappingAL.add(new StringWithOffset(null, finalOffset));
		mappingAL.add(new StringWithOffset(null, finalOffset+1));
		
		//when translating the mapping ArrayList into an array, possibly
		//add an element onto the beginning to conform with the requirements
		//specified in the comment of the field "mapping"
		if(mappingAL.get(0).offset == 0) {
			mapping = new StringWithOffset[mappingAL.size()];
			for(int i = 0; i < mapping.length; i++)
				mapping[i] = mappingAL.get(i);
		}
		else {
			mapping = new StringWithOffset[mappingAL.size()+1];
			mapping[0] = new StringWithOffset(null, 0);
			for(int i = 1; i < mapping.length; i++)
				mapping[i] = mappingAL.get(i-1);
		}
		
		reset();
	}
	
	/**
	 * Make a copy of a ProducerMap without needing to reconstruct
	 * the map. This new map will be reset upon construction.
	 * @param original - map to base new map on
	 */
	public ProducerMap(ProducerMap original) {
		mapping = original.mapping;
		finalOffset = original.finalOffset;
		reset();
	}
	
	/**move the values of markA and markB back to zero*/
	public void reset() {
		markA = 0;
		markB = 0;
		indexPreA = 0;
		indexPostB = 0;
	}
	
	/**
	 * Shift markA further along in the mapping. markA is the start
	 * of the analyzed interval. If markA exceeds markB, then markB
	 * will be shifted as well to match markA.
	 * @param newMarkA - new value for markA. Should be >= current value.
	 * @throws IllegalArgumentException - when newMarkA is less than
	 *         the current markA or newMarkA is greater than the final
	 *         offset in the mapping
	 */
	public void setMarkA(int newMarkA) {
		if(newMarkA < markA) throw new IllegalArgumentException(
				"newMarkA must be >= markA"); //$NON-NLS-1$
		if(newMarkA > finalOffset) throw new IllegalArgumentException(
				"newMarkA must be <= the final offset"); //$NON-NLS-1$
		
		while(true) {
			int offset = mapping[indexPreA].offset;
			if(offset == newMarkA) break;
			if(offset > newMarkA) {
				indexPreA--;
				break;
			}
			indexPreA++;
		}
		
		markA = newMarkA;
		if(newMarkA > markB) setMarkB(newMarkA);
	}
	
	/**
	 * Shift markB further along in the mapping. markB is the end
	 * of the analyzed interval.
	 * @param newMarkB - new value for markB. Should be >= current value.
	 * @throws IllegalArgumentException - when newMarkB is less than
	 *         the current markB or newMarkB is greater than the final
	 *         offset in the mapping
	 */
	public void setMarkB(int newMarkB) {
		if(newMarkB < markB) throw new IllegalArgumentException(
				"newMarkB must be >= markB"); //$NON-NLS-1$
		if(newMarkB > finalOffset) throw new IllegalArgumentException(
				"newMarkB must be <= the final offset"); //$NON-NLS-1$
		
		boolean wasEqual = false;
		while(true) {
			int offset = mapping[indexPostB].offset;
			if(offset > newMarkB) break;
			wasEqual = (offset == newMarkB);
			indexPostB++;
		}
		if(wasEqual) indexPostB--;
		
		markB = newMarkB;
	}
	
	/*public String expandWhite(String image) {
		System.out.print("w|" + markA + "|" + markB + "|" + image + "|");
		String result = expandWhitea(image);
		System.out.println(result);
		return result;
	}*/
	/**
	 * A function intended to be used to obtain an expanded string
	 * for whitespace before a Photran token. markA and markB should
	 * specify the interval of that white space.
	 * @param image - the original whitespace
	 * @return a new string for the new whitespace, or null if there
	 *         shouldn't be a change. This new string will replace
	 *         text in "image" with producers when appropriate.
	 *         A producer that contains an interval boundary (such
	 *         that the boundary does not lie on the edge of it)
	 *         IS NOT reproduced and the text from "image" in
	 *         that region is removed. A producer mapping to a
	 *         region of length zero on an edge of the interval
	 *         IS reproduced.
	 * @throws IllegalArgumentException - when the length of image
	 *         is not the same as the length of the interval
	 *         (i.e., markB-markA)
	 */
	public String expandWhite(String image) {
		if(image.length() != markB-markA) throw new IllegalArgumentException(
				"the length of image must equal markB-markA"); //$NON-NLS-1$
		
		if(indexPreA == indexPostB) return null;
		if(indexPreA + 1 == indexPostB) {
			if(mapping[indexPreA].string == null) return null;
			if(mapping[indexPreA].offset == markA && mapping[indexPostB].offset == markB)
				return mapping[indexPreA].string;
			return ""; //$NON-NLS-1$
		}
		
		//at this point, indexPreA and indexPostB are at least 2 apart
		
		StringBuffer buffer = new StringBuffer(256);
		int index = indexPreA;
		
		if(mapping[index].offset < markA) {
			if(mapping[index].string == null)
				buffer.append(image.substring(0, mapping[index+1].offset-markA));
			index++;
		}

		for(; index < indexPostB; index++) {
			if(index+1 == indexPostB && mapping[index+1].offset > markB) {
				if(mapping[index].string == null)
					buffer.append(image.substring(mapping[index].offset-markA));
				break;
			}
			if(mapping[index].string == null) buffer.append(image.substring(
				mapping[index].offset-markA, mapping[index+1].offset-markA));
			else buffer.append(mapping[index].string);
		}
		
		return buffer.toString();
	}

	
	/*public String expandNormal(String image) {
		System.out.print("n|" + markA + "|" + markB + "|" + image + "|");
		String result = expandNormala(image);
		System.out.println(result);
		return result;
	}*/
	/**
	 * A function intended to be used to obtain an expanded string
	 * for the text of a Photran tokens, or possibly several Photran
	 * tokens (but not including the surrounding white space).
	 * markA and markB should specify the interval of that text.
	 * @param image - the original text
	 * @return a new string for the new text, or null if there
	 *         shouldn't be a change. This new string will replace
	 *         text in "image" with producers when appropriate.
	 *         A producer that contains an interval boundary
	 *         IS reproduced. A producer mapping to a
	 *         region of length zero on an edge of the interval
	 *         IS NOT reproduced.
	 * @throws IllegalArgumentException - when the length of image
	 *         is not the same as the length of the interval
	 *         (i.e., markB-markA)
	 */
	public String expandNormal(String image) {
		if(image.length() != markB-markA) throw new IllegalArgumentException(
				"the length of image must equal markB-markA"); //$NON-NLS-1$
		
		int index = indexPreA;
		int endIndex = indexPostB;
		while(index < endIndex) {
			if(mapping[index+1].offset == markA) index++;
			else break;
		}
		while(index < endIndex) {
			if(mapping[endIndex-1].offset == markB) endIndex--;
			else break;
		}
		
		if(index == endIndex) return null;
		if(index + 1 == endIndex) {
			if(mapping[index].string == null) return null;
			return mapping[index].string;
		}
		
		//at this point, index and endIndex are at least 2 apart
		
		StringBuffer buffer = new StringBuffer(256);
		
		for(; index < endIndex; index++) {
			if(mapping[index].string == null) {
				int subStart = mapping[index].offset-markA;
				if(subStart < 0) subStart = 0;
				int subEnd = mapping[index+1].offset-markA;
				if(subEnd > image.length()) subEnd = image.length();
				buffer.append(image.substring(subStart, subEnd));
			}
			else buffer.append(mapping[index].string);
		}
		
		return buffer.toString();
	}
	
	/**
	 * @return true iff markB is not contained within a producer
	 *         (which implies markB is also not on the edge of a producer)
	 */
	public boolean isMarkBInProducer() {
		if(mapping[indexPostB].offset == markB) return false;
		StringWithOffset preSWO = mapping[indexPostB-1];
		if(preSWO.offset == markB) return false;
		if(preSWO.string == null) return false;
		return true;
	}
	
	/**
	 * Examine an interval between markB and endOffset. Determine
	 * if there is a producer break in this interval.
	 * @param endOffset - the end of the interval to consider. Must
	 *        be >= markB.
	 * @return true iff there is a producer break in this interval.
	 *         This includes when the producer of this interval is
	 *         null. This also includes when the producer ends
	 *         at markB or endOffset
	 * @throws IllegalArgumentException - when endOffset is less than markB or
	 *         endOffset is greater than the final offset in the mapping
	 */
	public boolean isBreakAfterMarkB(int endOffset) {
		if(endOffset < markB) throw new IllegalArgumentException(
				"endOffset must be >= markB"); //$NON-NLS-1$
		
		if(mapping[indexPostB].offset <= endOffset) return true;
		if(mapping[indexPostB-1].string == null) return true;
		return false;
	}
	
	/**@return the maximum allowed offset. This will be the maximum
	 *         legal input to setMarkA and setMarkB.*/
	public int getFinalOffset() {
		return finalOffset;
	}
	
	/**
	 * A class containing a string and int pair.
	 * @author Matthew Michelotti
	 */
	private static class StringWithOffset {
		private final String string;
		private final int offset;
		
		private StringWithOffset(String string, int offset) {
			if(string == null) this.string = null;
			else this.string = new String(string);
			this.offset = offset;
		}
	}
}
