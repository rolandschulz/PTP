#ifndef PTP_LSF_PROXY_H_
#define PTP_LSF_PROXY_H_

/* proxy methods */
int LSF_Initialize(int trans_id, char **);
int LSF_ModelDef(int trans_id, char**);
int LSF_StartEvents(int trans_id, char **);
int LSF_StopEvents(int trans_id, char **);
int LSF_SubmitJob(int trans_id, char **);
int LSF_TerminateJob(int trans_id, char **);
int LSF_Quit(int trans_id, char **);

/* polling frequencies (global variables) */
float gLSF_host_poll_freq;
float gLSF_queue_poll_freq;

/* polling frequency defaults (per second) */
#define LSF_HOST_POLL_FREQ_DEFAULT	.01
#define LSF_QUEUE_POLL_FREQ_DEFAULT	.5

/* transaction id for start of event stream, is 0 when events are off */
int gStartEventsID = 0;

/* flag on when LSF has been initialized */
int gInitialized = 0;

#endif /*PTP_LSF_PROXY_H_*/
