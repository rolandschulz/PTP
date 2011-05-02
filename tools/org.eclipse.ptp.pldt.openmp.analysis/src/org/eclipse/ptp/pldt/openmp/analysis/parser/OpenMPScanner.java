/**********************************************************************
 * Copyright (c) 2006,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.analysis.parser;

import java.util.Arrays;

import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Class to scan #pragma string for OpenMP syntax (adapted from cdt's BaseScanner)
 * 
 * @author Pazel
 * 
 */

public class OpenMPScanner
{
	// Char buffer
	private char[] inputBuffer_ = null;
	// Current position into buffer
	private int currentPos_ = -1;
	private int endPos_ = 0; // index just past last char

	// These need to be set
	protected final boolean supportMinAndMax = true;
	protected final boolean support$Initializers = true;

	protected static char[] EMPTY_CHAR_ARRAY = new char[0];

	// protected ScannerCallbackManager callbackManager_ = null; // unused; removed for CDT5.0

	// **
	// OpenMPScanner - Object to scan OpenMP pragma line
	// **
	public OpenMPScanner(String inputBuffer)
	{
		inputBuffer_ = inputBuffer.toCharArray();
		endPos_ = inputBuffer.length();
	}

	public OpenMPToken nextToken()
	{

		return fetchToken();
	}

	protected OpenMPToken fetchToken()
	{

		while (currentPos_ < endPos_) {

			skipOverWhiteSpace();

			currentPos_++;

			if (currentPos_ >= endPos_)
				return null;

			switch (inputBuffer_[currentPos_]) {
			case '\r':
			case '\n':
				continue;

			case 'L':
				if (currentPos_ + 1 < endPos_ && inputBuffer_[currentPos_ + 1] == '"')
					return scanString();
				if (currentPos_ + 1 < endPos_ && inputBuffer_[currentPos_ + 1] == '\'')
					return scanCharLiteral();

				return scanIdentifier();

			case '"':
				return scanString();

			case '\'':
				return scanCharLiteral();

			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case '_':
				return scanIdentifier();

			case '\\':
				if (currentPos_ + 1 < endPos_
						&& (inputBuffer_[currentPos_ + 1] == 'u' ||
						inputBuffer_[currentPos_ + 1] == 'U')) {
					return scanIdentifier();
				}
				// handleProblem(IProblem.SCANNER_BAD_CHARACTER,
				// bufferPos[bufferStackPos], new char[] { '\\' });
				continue;

			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return scanNumber();

			case '.':
				if (currentPos_ + 1 < endPos_) {
					switch (inputBuffer_[currentPos_ + 1]) {
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						return scanNumber();

					case '.':
						if (currentPos_ + 2 < endPos_) {
							if (inputBuffer_[currentPos_ + 2] == '.') {
								currentPos_ += 2;
								return newToken(IToken.tELLIPSIS);
							}
						}
					case '*':
						++currentPos_;
						return newToken(IToken.tDOTSTAR);
					}
				}
				return newToken(IToken.tDOT);

			case '#':
				if (currentPos_ + 1 < endPos_ && inputBuffer_[currentPos_ + 1] == '#') {
					currentPos_++;
					return newToken(IToken.tPOUNDPOUND);
				}
				return newToken(mpPound);

			case '{':
				currentPos_++;
				return newToken(IToken.tLBRACE);

			case '}':
				currentPos_++;
				return newToken(IToken.tRBRACE);

			case '[':
				currentPos_++;
				return newToken(IToken.tLBRACKET);

			case ']':
				currentPos_++;
				return newToken(IToken.tRBRACKET);

			case '(':
				return newToken(IToken.tLPAREN);

			case ')':
				return newToken(IToken.tRPAREN);

			case ';':
				return newToken(IToken.tSEMI);

			case ':':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == ':') {
						++currentPos_;
						return newToken(IToken.tCOLONCOLON);
					}
				}
				return newToken(IToken.tCOLON);

			case '?':
				return newToken(IToken.tQUESTION);

			case '+':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '+') {
						++currentPos_;
						return newToken(IToken.tINCR);
					} else if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tPLUSASSIGN);
					}
				}
				return newToken(IToken.tPLUS);

			case '-':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '>') {
						if (currentPos_ + 2 < endPos_) {
							if (inputBuffer_[currentPos_ + 2] == '*') {
								currentPos_ += 2;
								return newToken(IToken.tARROWSTAR);
							}
						}
						++currentPos_;
						return newToken(IToken.tARROW);
					} else if (inputBuffer_[currentPos_ + 1] == '-') {
						++currentPos_;
						return newToken(IToken.tDECR);
					} else if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tMINUSASSIGN);
					}
				}
				return newToken(IToken.tMINUS);

			case '*':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tSTARASSIGN);
					}
				}
				return newToken(IToken.tSTAR);

			case '/':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tDIVASSIGN);
					}
				}
				return newToken(IToken.tDIV);

			case '%':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tMODASSIGN);
					}
				}
				return newToken(IToken.tMOD);

			case '^':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tXORASSIGN);
					}
				}
				return newToken(IToken.tXOR);

			case '&':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '&') {
						++currentPos_;
						return newToken(IToken.tAND);
					} else if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tAMPERASSIGN);
					}
				}
				return newToken(IToken.tAMPER);

			case '|':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '|') {
						++currentPos_;
						return newToken(IToken.tOR);
					} else if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tBITORASSIGN);
					}
				}
				return newToken(IToken.tBITOR);

			case '~':
				return newToken(IToken.tCOMPL);

			case '!':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tNOTEQUAL);
					}
				}
				return newToken(IToken.tNOT);

			case '=':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tEQUAL);
					}
				}
				return newToken(IToken.tASSIGN);

			case '<':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tLTEQUAL);
					} else if (inputBuffer_[currentPos_ + 1] == '<') {
						if (currentPos_ + 2 < endPos_) {
							if (inputBuffer_[currentPos_ + 2] == '=') {
								currentPos_ += 2;
								return newToken(IToken.tSHIFTLASSIGN);
							}
						}
						++currentPos_;
						return newToken(IToken.tSHIFTL);
					} else if (inputBuffer_[currentPos_ + 1] == '?' && supportMinAndMax) {
						++currentPos_;
						return newToken(CharArrayUtils.extract(
								inputBuffer_, currentPos_, 2), IGCCToken.tMIN);
					}
				}
				return newToken(IToken.tLT);

			case '>':
				if (currentPos_ + 1 < endPos_) {
					if (inputBuffer_[currentPos_ + 1] == '=') {
						++currentPos_;
						return newToken(IToken.tGTEQUAL);
					} else if (inputBuffer_[currentPos_ + 1] == '>') {
						if (currentPos_ + 2 < endPos_) {
							if (inputBuffer_[currentPos_ + 2] == '=') {
								currentPos_ += 2;
								return newToken(IToken.tSHIFTRASSIGN);
							}
						}
						++currentPos_;
						return newToken(IToken.tSHIFTR);
					} else if (inputBuffer_[currentPos_ + 1] == '?' && supportMinAndMax) {
						++currentPos_;
						return newToken(CharArrayUtils.extract(
								inputBuffer_, currentPos_, 2), IGCCToken.tMAX);
					}

				}
				return newToken(IToken.tGT);

			case ',':
				return newToken(IToken.tCOMMA);

			default:
				if (Character.isLetter(inputBuffer_[currentPos_]) || inputBuffer_[currentPos_] == '_'
						|| (support$Initializers && inputBuffer_[currentPos_] == '$')) {
					return scanIdentifier();
				}

				// skip over anything we don't handle
				// char [] x = new char [1];
				// x[0] = buffer[pos];
				// handleProblem( IASTProblem.SCANNER_BAD_CHARACTER, pos, x );
			}
		}

		// We've run out of contexts, our work is done here
		// return contentAssistMode ? eocToken : null;
		return null;
	}

	protected OpenMPToken scanString()
	{
		char[] buffer = inputBuffer_;

		int tokenType = IToken.tSTRING;
		if (buffer[currentPos_] == 'L') {
			++currentPos_;
			tokenType = IToken.tLSTRING;
		}

		int stringStart = currentPos_ + 1;
		int stringLen = 0;
		boolean escaped = false;
		boolean foundClosingQuote = false;
		loop: while (++currentPos_ < endPos_) {
			++stringLen;
			char c = buffer[currentPos_];
			if (c == '"') {
				if (!escaped) {
					foundClosingQuote = true;
					break;
				}
			} else if (c == '\\') {
				escaped = !escaped;
				continue;
			} else if (c == '\n') {
				// unescaped end of line before end of string
				if (!escaped)
					break;
			} else if (c == '\r') {
				if (currentPos_ + 1 < endPos_
						&& buffer[currentPos_ + 1] == '\n') {
					++currentPos_;
					if (!escaped)
						break;
				}
			}
			escaped = false;
		}
		--stringLen;

		// We should really throw an exception if we didn't get the terminating
		// quote before the end of buffer
		char[] result = CharArrayUtils.extract(buffer, stringStart, stringLen);
		// if (!foundClosingQuote) {
		// handleProblem(IProblem.SCANNER_UNBOUNDED_STRING, stringStart,
		// result);
		// }
		return newToken(result, tokenType);
	}

	protected OpenMPToken scanCharLiteral()
	{
		char[] buffer = inputBuffer_;
		int start = currentPos_;
		int limit = endPos_;

		int tokenType = IToken.tCHAR;
		int length = 1;
		if (inputBuffer_[currentPos_] == 'L') {
			++currentPos_;
			tokenType = IToken.tLCHAR;
			++length;
		}

		if (start >= limit) {
			return newToken(EMPTY_CHAR_ARRAY, tokenType);
		}

		boolean escaped = false;
		while (++currentPos_ < endPos_) {
			++length;
			int pos = currentPos_;
			if (buffer[pos] == '\'') {
				if (!escaped)
					break;
			} else if (buffer[pos] == '\\') {
				escaped = !escaped;
				continue;
			}
			escaped = false;
		}

		if (currentPos_ == endPos_) {
			// handleProblem(IProblem.SCANNER_BAD_CHARACTER, start, CharArrayUtils
			// .extract(buffer, start, length));
			return newToken(EMPTY_CHAR_ARRAY, tokenType);
		}

		char[] image = length > 0 ? CharArrayUtils.extract(buffer, start,
				length) : EMPTY_CHAR_ARRAY;

		return newToken(image, tokenType);
	}

	protected OpenMPToken scanNumber()
	{
		char[] buffer = inputBuffer_;
		int start = currentPos_;
		int limit = endPos_;

		boolean isFloat = buffer[start] == '.';
		boolean hasExponent = false;

		boolean isHex = false;
		boolean isOctal = false;
		boolean isMalformedOctal = false;

		if (buffer[start] == '0' && start + 1 < limit) {
			switch (buffer[start + 1]) {
			case 'x':
			case 'X':
				isHex = true;
				++currentPos_;
				break;
			default:
				if (buffer[start + 1] > '0' && buffer[start + 1] < '7')
					isOctal = true;
				else if (buffer[start + 1] == '8' || buffer[start + 1] == '9') {
					isOctal = true;
					isMalformedOctal = true;
				}
			}
		}

		while (++currentPos_ < limit) {
			int pos = currentPos_;
			switch (buffer[pos]) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if ((buffer[pos] == '8' || buffer[pos] == '9') && isOctal) {
					isMalformedOctal = true;
					break;
				}

				continue;

			case '.':
				// if (isLimitReached())
				// handleNoSuchCompletion();

				if (isFloat) {
					// second dot
					// handleProblem(IProblem.SCANNER_BAD_FLOATING_POINT, start,
					// null);
					break;
				}

				isFloat = true;
				continue;

			case 'E':
			case 'e':
				if (isHex)
					// a hex 'e'
					continue;

				if (hasExponent)
					// second e
					break;

				if (pos + 1 >= limit)
					// ending on the e?
					break;

				switch (buffer[pos + 1]) {
				case '+':
				case '-':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					// looks like a good exponent
					isFloat = true;
					hasExponent = true;
					++currentPos_;
					continue;
				default:
					// no exponent number?
					break;
				}
				break;

			case 'a':
			case 'A':
			case 'b':
			case 'B':
			case 'c':
			case 'C':
			case 'd':
			case 'D':
				if (isHex)
					continue;

				// not ours
				break;

			case 'f':
			case 'F':
				if (isHex)
					continue;

				// must be float suffix
				++currentPos_;

				if (currentPos_ < buffer.length
						&& buffer[currentPos_] == 'i')
					continue; // handle GCC extension 5.10 Complex Numbers

				break; // fix for 77281 (used to be continue)

			case 'p':
			case 'P':
				// Hex float exponent prefix
				if (!isFloat || !isHex) {
					--currentPos_;
					break;
				}

				if (hasExponent)
					// second p
					break;

				if (pos + 1 >= limit)
					// ending on the p?
					break;

				switch (buffer[pos + 1]) {
				case '+':
				case '-':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					// looks like a good exponent
					isFloat = true;
					hasExponent = true;
					++currentPos_;
					continue;
				default:
					// no exponent number?
					break;
				}
				break;

			case 'u':
			case 'U':
			case 'L':
			case 'l':
				// unsigned suffix
				suffixLoop: while (++currentPos_ < limit) {
					switch (buffer[currentPos_]) {
					case 'U':
					case 'u':
					case 'l':
					case 'L':
						break;
					default:

						break suffixLoop;
					}
				}
				break;

			default:
				/*
				 * boolean success = false;
				 * for (int iter = 0; iter < suffixes.length; iter++)
				 * if (buffer[pos] == suffixes[iter]) {
				 * success = true;
				 * break;
				 * }
				 * if (success)
				 * continue;
				 */
			}

			// If we didn't continue in the switch, we're done
			break;
		}

		--currentPos_;

		char[] result = CharArrayUtils.extract(buffer, start,
				currentPos_ - start + 1);
		int tokenType = isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER;

		// if (tokenType == IToken.tINTEGER && isHex && result.length == 2) {
		// handleProblem(IProblem.SCANNER_BAD_HEX_FORMAT, start, result);
		// } else if (tokenType == IToken.tINTEGER && isOctal && isMalformedOctal) {
		// handleProblem(IProblem.SCANNER_BAD_OCTAL_FORMAT, start, result);
		// }

		return newToken(result, tokenType);
	}

	// **
	// skipOverWhiteSpace - based on org.eclipse.cdt.internal.parser.scanner2.BaseScanner
	// **
	protected void skipOverWhiteSpace() {

		char[] buffer = inputBuffer_;

		while (++currentPos_ < endPos_) {
			switch (buffer[currentPos_]) {
			case ' ':
			case '\t':
			case '\r':
				continue;
			case '/':
				if (currentPos_ + 1 < endPos_) {
					if (buffer[currentPos_ + 1] == '/') {
						// C++ comment, skip rest of line
						currentPos_ = endPos_;
						return;
					} else if (buffer[currentPos_ + 1] == '*') {
						// C comment, find closing */
						boolean foundEnd = false;
						for (currentPos_ += 2; currentPos_ < endPos_; ++currentPos_) {
							if (buffer[currentPos_] == '*' && currentPos_ + 1 < endPos_
									&& buffer[currentPos_ + 1] == '/') {
								currentPos_ += 2;
								foundEnd = true;
								break;
							}
						}
						if (!foundEnd) // we are at end of line - nothing to do (odd case)
							return;
						continue;
					}
				}
				break;
			case '\\':
				if (currentPos_ + 1 < endPos_ && buffer[currentPos_ + 1] == '\n') {
					// \n is a whitespace
					++currentPos_;
					continue;
				}
				if (currentPos_ + 1 < endPos_ && buffer[currentPos_ + 1] == '\r') {
					if (currentPos_ + 2 < endPos_ && buffer[currentPos_ + 2] == '\n') {
						currentPos_ += 2;
						continue;
					}
				}
				break;
			}

			// fell out of switch without continuing, we're done
			--currentPos_;
			return;
		}

		--currentPos_;
		return;
	}

	protected OpenMPToken scanIdentifier()
	{
		char[] buffer = inputBuffer_;
		boolean escapedNewline = false;
		int start = currentPos_;
		int len = 1;

		while (++currentPos_ < endPos_) {
			char c = buffer[currentPos_];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
					|| (c >= '0' && c <= '9')
					|| Character.isUnicodeIdentifierPart(c)) {
				++len;
				continue;
			} else if (c == '\\' && currentPos_ + 1 < endPos_
					&& buffer[currentPos_ + 1] == '\n') {
				// escaped newline
				++endPos_;
				len += 2;
				escapedNewline = true;
				continue;
			} else if (c == '\\' && (currentPos_ + 1 < endPos_)) {
				if ((buffer[currentPos_ + 1] == 'u')
						|| buffer[currentPos_ + 1] == 'U') {
					++currentPos_;
					len += 2;
					continue;
				}
			}
			// } else if ((support$Initializers && c == '$')) {
			// ++len;
			// continue;
			// }
			break;
		}

		--currentPos_;

		char[] result = escapedNewline ? removedEscapedNewline(buffer, start,
				len) : CharArrayUtils.extract(buffer, start, len);
		int tokenType = escapedNewline ? mpKeywords.get(result, 0, result.length)
				: mpKeywords.get(buffer, start, len);

		/*
		 * if (tokenType == CharArrayIntMap.undefined) {
		 * tokenType = escapedNewline ? additionalKeywords.get(result, 0,
		 * result.length) : additionalKeywords.get(buffer, start, len);
		 * 
		 * if (tokenType == additionalKeywords.undefined) {
		 * result = (result != null) ? result : CharArrayUtils.extract(
		 * buffer, start, len);
		 * return newToken(IToken.tIDENTIFIER, result);
		 * }
		 * result = (result != null) ? result : CharArrayUtils.extract(buffer,
		 * start, len);
		 * return newToken(result, tokenType);
		 * }
		 */
		return newToken(result, tokenType);
	}

	/**
	 * @return
	 */
	protected OpenMPToken newToken(int signal) {
		return new OpenMPToken(signal, currentPos_);
	}

	/**
	 * @return
	 */
	protected OpenMPToken newToken(char[] image, int signal) {
		return new OpenMPToken(new String(image), currentPos_, signal);
	}

	/**
	 * @param text
	 * @return
	 */
	protected char[] removedEscapedNewline(char[] text, int start, int len) {
		if (CharArrayUtils.indexOf('\n', text, start, len) == -1)
			return text;
		char[] result = new char[text.length];
		Arrays.fill(result, ' ');
		int counter = 0;
		for (int i = 0; i < text.length; ++i) {
			if (text[i] == '\\' && i + 1 < text.length && text[i + 1] == '\n')
				++i;
			else if (text[i] == '\\' && i + 1 < text.length
					&& text[i + 1] == '\r' && i + 2 < text.length
					&& text[i + 2] == '\n')
				i += 2;
			else
				result[counter++] = text[i];
		}
		return CharArrayUtils.trim(result);
	}

	/**
	 * handleProblem - will create problems to report through eclipse
	 * 
	 * @param id
	 * @param startOffset
	 */
	private void handleProblem(int id, int startOffset)
	{
		// TBD
	}

	public static final int mpAtomic = 0;
	public static final int mpBarrier = 1;
	public static final int mpCopyin = 2;
	public static final int mpCopyprivate = 3;
	public static final int mpCritical = 4;
	public static final int mpDefault = 5;
	public static final int mpFirstprivate = 6;
	public static final int mpFlush = 7;
	public static final int mpFor = 8;
	public static final int mpIf = 9;
	public static final int mpLastprivate = 10;
	public static final int mpMaster = 11;
	public static final int mpNone = 12;
	public static final int mpNowait = 13;
	public static final int mpNumthreads = 14;
	public static final int mpOmp = 15;
	public static final int mpOrdered = 16;
	public static final int mpParallel = 17;
	public static final int mpPrivate = 18;
	public static final int mpReduction = 19;
	public static final int mpSchedule = 20;
	public static final int mpSection = 21;
	public static final int mpSections = 22;
	public static final int mpShared = 23;
	public static final int mpSingle = 24;
	public static final int mpThreadPrivate = 25;
	public static final int mpPound = 26;
	public static final int mpPragma = 27;
	public static final int mpDynamic = 28;
	public static final int mpGuided = 29;
	public static final int mpRuntime = 30;
	public static final int mpStatic = 31;

	public static final CharArrayIntMap mpKeywords;

	static {
		mpKeywords = new CharArrayIntMap(16, -1);

		// Common keywords
		mpKeywords.put(OpenMPKeywords.ATOMIC, mpAtomic); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.BARRIER, mpBarrier); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.COPYIN, mpCopyin); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.COPYPRIVATE, mpCopyprivate); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.CRITICAL, mpCritical); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.DEFAULT, mpDefault); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.FIRSTPRIVATE, mpFirstprivate); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.FLUSH, mpFlush); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.FOR, mpFor); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.IF, mpIf); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.LASTPRIVATE, mpLastprivate); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.MASTER, mpMaster); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.NONE, mpNone); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.NOWAIT, mpNowait); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.NUMTHREADS, mpNumthreads); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.OMP, mpOmp); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.ORDERED, mpOrdered); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.PARALLEL, mpParallel); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.PRIVATE, mpPrivate); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.REDUCTION, mpReduction); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.SCHEDULE, mpSchedule); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.SECTION, mpSection); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.SECTIONS, mpSections); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.SHARED, mpShared); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.SINGLE, mpSingle); //$NON-NLS-1$
		mpKeywords.put(OpenMPKeywords.POUND, mpPound);
		mpKeywords.put(OpenMPKeywords.PRAGMA, mpPragma);
		mpKeywords.put(OpenMPKeywords.THREADPRIVATE, mpThreadPrivate);
		mpKeywords.put(OpenMPKeywords.DYNAMIC, mpDynamic);
		mpKeywords.put(OpenMPKeywords.STATIC, mpStatic); // not sure of this - DPP
	}

}
