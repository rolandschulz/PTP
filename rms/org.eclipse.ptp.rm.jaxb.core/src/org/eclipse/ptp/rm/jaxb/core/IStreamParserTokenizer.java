package org.eclipse.ptp.rm.jaxb.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface IStreamParserTokenizer extends Runnable {

	void addTarget(Object target);

	List<Object> getCreatedTargets();

	Throwable getInternalError();

	void setInputStream(InputStream stream);

	void setRedirectStream(OutputStream stream);
}
