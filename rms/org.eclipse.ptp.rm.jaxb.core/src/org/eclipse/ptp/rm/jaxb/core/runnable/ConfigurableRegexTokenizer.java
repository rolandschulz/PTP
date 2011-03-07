package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.Read;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ReadImpl;

public class ConfigurableRegexTokenizer implements IStreamParserTokenizer, IJAXBNonNLSConstants, Runnable {

	public static final String EXT_ID = "org.eclipse.ptp.rm.jaxb.configurableRegexTokenizer"; //$NON-NLS-1$

	private Throwable error;
	private InputStream in;
	private OutputStream out;
	private List<ReadImpl> read;

	public ConfigurableRegexTokenizer() {
	}

	public ConfigurableRegexTokenizer(List<Read> read) {
		setRead(read);
	}

	public Throwable getInternalError() {
		return error;
	}

	public void run() {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			if (out != null) {
				bw = new BufferedWriter(new OutputStreamWriter(out));
			}
			boolean endOfStream = false;
			while (!endOfStream) {
				for (ReadImpl r : read) {
					endOfStream = r.read(br, bw);
					if (endOfStream) {
						break;
					}
				}
			}
		} catch (Throwable t) {
			error = t;
			// we do not close the out here because it probably is stdout
		}
	}

	public void setInputStream(InputStream stream) {
		in = stream;
	}

	public void setRead(List<Read> read) {
		this.read = new ArrayList<ReadImpl>();
		for (Read r : read) {
			this.read.add(new ReadImpl(r));
		}
	}

	public void setRedirectStream(OutputStream stream) {
		out = stream;
	}
}
