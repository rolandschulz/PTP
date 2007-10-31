package org.eclipse.ptp.debug.internal.core.pdi.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;
import org.eclipse.ptp.debug.internal.core.pdi.request.DataEvaluateExpressionRequest;

/**
 * @author clement
 *
 */
public class MultiExpressions extends SessionObject implements IPDIMultiExpressions {
	private String expr;
	private Map<Integer,IPDIExpression> expressions = new HashMap<Integer,IPDIExpression>();
	
	public MultiExpressions(Session session, BitList tasks, String ex) {
		super(session, tasks);
		this.expr = ex;
		initital();
	}
	public void shutdown() {
		expressions.clear();
	}
	private void initital() {
		for (int task : getTasks().toArray()) {
			addExpression(new MExpression(task));
		}
	}
	public String getExpressionText() {
		return expr;
	}
	public void addExpression(IPDIExpression expression) {
		if (expression instanceof MExpression) {
			Integer id = new Integer(((MExpression)expression).getId());
			expressions.put(id, expression);
		}
	}
	public IAIF getAIF(IPDIExpression expression) throws PDIException {
		return expression.getAIF();
	}
	public IPDIExpression[] getExpressions() {
		return expressions.values().toArray(new IPDIExpression[0]);
	}
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
	public IPDIExpression getExpression(int task) {
		return expressions.get(new Integer(task));
	}
	public void updateExpressionsValue(BitList tasks, IProgressMonitor monitor) throws PDIException {
		monitor.setTaskName("Updating expression ["+expr+"]...");
		DataEvaluateExpressionRequest request = new DataEvaluateExpressionRequest(tasks, expr);
		session.getEventRequestManager().addEventRequest(request);
		Map<BitList, Object> results = request.getResultMap(tasks);
		for (Iterator<BitList> i = results.keySet().iterator(); i.hasNext();) {
			BitList sTasks = i.next();
			IAIF aif = (IAIF)results.get(sTasks);
			for (int task : sTasks.toArray()) {
				if (monitor.isCanceled()) {
					throw new PDIException(tasks, "Updating is cancelled by user");
				}
				IPDIExpression expression = getExpression(task);
				if (expression != null) {
					expression.setAIF(aif);
				}
				monitor.worked(1);
			}
		}
	}
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
	class MExpression implements IPDIExpression {
		private int id;
		private IAIF aif;
		public MExpression(int id) {
			this.id = id;
		}
		public int getId() {
			return id;
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
		public String getExpressionText() {
			return expr;
		}
		public IAIF getAIF() throws PDIException {
			return aif;
		}
		public void setAIF(IAIF aif) {
			this.aif = aif;
		}
	}
}

