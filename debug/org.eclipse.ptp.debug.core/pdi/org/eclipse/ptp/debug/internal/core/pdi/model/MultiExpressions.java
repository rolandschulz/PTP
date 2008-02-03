package org.eclipse.ptp.debug.internal.core.pdi.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.SessionObject;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataEvaluateExpressionRequest;

/**
 * @author clement
 *
 */
/**
 * @author greg
 *
 */
public class MultiExpressions extends SessionObject implements IPDIMultiExpressions {
	class MExpression implements IPDIExpression {
		private int id;
		private IAIF aif;
		public MExpression(int id) {
			this.id = id;
		}
		public void dispose() throws PDIException {
			aif = null;
		}
		public boolean equals(IPDIExpression expr) {
			if (expr instanceof MExpression) {
				return ((MExpression)expr).getId() == id;
			}
			return false;
		}
		public IAIF getAIF() throws PDIException {
			return aif;
		}
		public String getExpressionText() {
			return expr;
		}
		public int getId() {
			return id;
		}
		public void setAIF(IAIF aif) {
			this.aif = aif;
		}
	}
	
	private String expr;
	private boolean enabled;
	private Map<Integer,IPDIExpression> expressions = new HashMap<Integer,IPDIExpression>();
	
	public MultiExpressions(IPDISession session, BitList tasks, String ex, boolean enabled) {
		super(session, tasks);
		this.expr = ex;
		this.enabled = enabled;
		initital();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#addExpression(org.eclipse.ptp.debug.core.pdi.model.IPDIExpression)
	 */
	public void addExpression(IPDIExpression expression) {
		if (expression instanceof MExpression) {
			Integer id = new Integer(((MExpression)expression).getId());
			expressions.put(id, expression);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#cleanExpressionsValue(org.eclipse.ptp.core.util.BitList, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void cleanExpressionsValue(BitList tasks, IProgressMonitor monitor) {
		monitor.setTaskName("Cleaning expression ["+expr+"]...");
		for (int task : tasks.toArray()) {
			IPDIExpression expression = getExpression(task);
			if (expression != null) {
				expression.setAIF(null);
			}
			monitor.worked(1);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getAIF(org.eclipse.ptp.debug.core.pdi.model.IPDIExpression)
	 */
	public IAIF getAIF(IPDIExpression expression) throws PDIException {
		return expression.getAIF();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getExpression(int)
	 */
	public IPDIExpression getExpression(int task) {
		return expressions.get(new Integer(task));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getExpressions()
	 */
	public IPDIExpression[] getExpressions() {
		return expressions.values().toArray(new IPDIExpression[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getExpressionText()
	 */
	public String getExpressionText() {
		return expr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#removeExpression(org.eclipse.ptp.core.util.BitList)
	 */
	public void removeExpression(BitList tasks) {
		for (int task : tasks.toArray()) {
			expressions.remove(new Integer(task));
		}
	}
	
	public void removeExpression(IPDIExpression expression) {
		if (expression instanceof MExpression) {
			Integer id = new Integer(((MExpression)expression).getId());
			expressions.remove(id);
		}
	}
	
	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#shutdown()
	 */
	public void shutdown() {
		expressions.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#updateExpressionsValue(org.eclipse.ptp.core.util.BitList, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void updateExpressionsValue(BitList tasks, IProgressMonitor monitor) throws PDIException {
		monitor.setTaskName("Updating expression ["+expr+"]...");
		IPDIDataEvaluateExpressionRequest request = session.getRequestFactory().getDataEvaluateExpresionRequest(tasks, expr);
		session.getEventRequestManager().addEventRequest(request);
		Map<BitList, Object> results = request.getResultMap(tasks);
		for (Iterator<BitList> i = results.keySet().iterator(); i.hasNext();) {
			BitList sTasks = i.next();
			Object obj = results.get(sTasks);
			if (!(obj instanceof IAIF)) {
				//set unknown aif for tasks
				for (int task : sTasks.toArray()) {
					IPDIExpression expression = getExpression(task);
					if (expression == null)
						continue;
					expression.setAIF(AIFFactory.UNKNOWNAIF());
				}
				continue;
				//throw new PDIException(tasks, "Updating expression value error");
			}
				
			IAIF aif = (IAIF)obj;
			for (int task : sTasks.toArray()) {
				IPDIExpression expression = getExpression(task);
				if (expression == null)
					continue;
				if (monitor.isCanceled()) {
					expression.setAIF(AIFFactory.UNKNOWNAIF());
					throw new PDIException(tasks, "Updating is cancelled by user");
				}
				expression.setAIF(aif);
				monitor.worked(1);
			}
		}
	}	
	
	/**
	 * 
	 */
	private void initital() {
		for (int task : getTasks().toArray()) {
			addExpression(new MExpression(task));
		}
	}
}

