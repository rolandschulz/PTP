#ifndef PTP_LSF_PROXY_H_
#define PTP_LSF_PROXY_H_

int LSF_StartDaemon(char **);
int LSF_Run(char **);
int LSF_Discover(char **);
int LSF_TerminateJob(char **);
int LSF_Quit(char **);

#endif /*PTP_LSF_PROXY_H_*/
