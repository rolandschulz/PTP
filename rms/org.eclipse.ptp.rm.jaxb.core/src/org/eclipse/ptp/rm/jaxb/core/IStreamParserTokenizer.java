package org.eclipse.ptp.rm.jaxb.core;

import java.io.InputStream;
import java.io.OutputStream;

public interface IStreamParserTokenizer extends Runnable {

	Throwable getInternalError();

	void setInputStream(InputStream stream);

	void setRedirectStream(OutputStream stream);
}
