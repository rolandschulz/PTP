package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Range;
import org.eclipse.ptp.rm.jaxb.core.data.StreamParser;
import org.eclipse.ptp.rm.jaxb.core.data.Token;
import org.eclipse.ptp.rm.jaxb.core.data.TokenImpl;

public class StreamParserImpl extends Thread implements IJAXBNonNLSConstants {

	private final TokenImpl token;
	private final InputStream in;
	private final boolean redirect;
	private OutputStreamWriter out;
	private Range lineRange;
	private Throwable internalError;

	public StreamParserImpl(StreamParser parser, InputStream in) throws Throwable {
		super(parser.getName());
		redirect = parser.isRedirect();
		this.in = in;
		String exp = parser.getRange();
		if (exp != null) {
			lineRange = new Range(exp);
		}
		Token t = parser.getToken();
		assert (null != t);
		token = new TokenImpl(t);
	}

	public Throwable getInternalError() {
		return internalError;
	}

	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		char[] buffer = new char[STREAM_BUFFER_SIZE];
		int currentLine = 0;
		int numBytes;
		if (redirect) {
			assert (null != out);
		}

		while (true) {
			numBytes = 0;
			try {
				numBytes = reader.read(buffer, 0, STREAM_BUFFER_SIZE);
				if (numBytes == EOF) {
					break;
				}
			} catch (EOFException eofe) {
				break;
			} catch (IOException ioe) {
				internalError = ioe;
				break;
			}
			if (redirect) {
				try {
					out.write(buffer);
					out.flush();
				} catch (IOException ioe) {
					internalError = ioe;
					break;
				}
			}
			try {
				parseLine(currentLine, new String(buffer));
			} catch (Throwable ioe) {
				ioe.printStackTrace();
				internalError = new ParseException(ioe.getMessage(), currentLine);
				break;
			}
			currentLine++;
		}

		try {
			reader.close();
			// output stream is not closed here!
		} catch (IOException ignored) {
		}
	}

	public void setOut(OutputStreamWriter out) {
		this.out = out;
	}

	private void parseLine(int line, String contents) throws Throwable {
		if (lineRange == null || lineRange.isInRange(line)) {
			token.tokenize(contents);
		}
	}
}
