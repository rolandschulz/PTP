#ifndef PTP_LSF_PROXY_H_
#define PTP_LSF_PROXY_H_

/* obsolete */
int LSF_StartDaemon(char **);
int LSF_Discover(char **);

/* proxy methods */
int LSF_Initialize(char **);
int LSF_SendEvents(char **);
int LSF_HaltEvents(char **);
int LSF_Run(char **);
int LSF_TerminateJob(char **);
int LSF_Quit(char **);

/* polling frequencies (global variables) */
float gLSF_host_poll_freq;
float gLSF_queue_poll_freq;

/* polling frequency defaults (per second) */
#define LSF_HOST_POLL_FREQ_DEFAULT	.01
#define LSF_QUEUE_POLL_FREQ_DEFAULT	.5

/* flag for turning on/off event stream */
int gSendEvents = 0;

/* flag on when LSF has been initialized */
int gInitialized = 0;

#endif /*PTP_LSF_PROXY_H_*/
