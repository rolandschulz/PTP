package org.eclipse.ptp.bluegene.db;

public class JobInfo {
	public String	ptpJobId;
	public String	bgJobId;
	public String	userName;
	public String	blockId;
	public String	status;
	
	public JobInfo(String ptpJobId, String bgJobId, String userName, String blockId, String status) {
		this.ptpJobId = ptpJobId;
		this.bgJobId = bgJobId;
		this.userName = userName;
		this.blockId = blockId;
		this.status = status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the jobId
	 */
	public String getBGJobId() {
		return bgJobId;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the blockId
	 */
	public String getBlockId() {
		return blockId;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the ptpJobId
	 */
	public String getPTPJobId() {
		return ptpJobId;
	}
}

