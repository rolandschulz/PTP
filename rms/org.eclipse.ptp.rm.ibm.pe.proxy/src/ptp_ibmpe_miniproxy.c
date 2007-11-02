/*
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <stdio.h>
#include <errno.h>

int main(int argc, char *argv[]);
static void *stdout_writer(void *fd);
static void *stderr_writer(void *fd);
static void common_writer(int fd, char *file_suffix);

static char *stdout_prefix;
static char *stderr_prefix;
static FILE *trace_fd;
static int thread_count;
static pthread_t tid;
static pthread_mutex_t count_lock = PTHREAD_MUTEX_INITIALIZER;

int
main(int argc, char *argv[])
{
    /*
     * Parse command line parameter and redirect stdin, stdout and stderr
     * to the specified target file.
     * In principle, this code could be part of the main proxy module, in
     * which case, the main proxy would just fork a copy of itself and run
     * this code in the new child. It turns out this does not work, since 
     * at the point that the proxy is shut down, there may still be data 
     * queued to be sent to the front end. Attempts to write this data 
     * result in I/O errors once the front end has closed the proxy
     * connection, and there is not a safe way to shut down the queued I/O.
     * A separate process is likely safer anyway, since if the proxy has
     * other problems, they could also interfere with the I/O handling.
     * Since this miniproxy cannot assume a communication path to the front
     * end, the miniproxy will silently fail on errors, unless tracing is
     * enabled. 
     * The command line consists of a single parameter which this function
     * must parse. The parameter contains blank delimited tokens as follows
     * trace flag (y or n). Tracing is active when flag is 'y'
     * Pathname prefix for stdout files, or /dev/null 
     * Pathname prefix for stderr files, or /dev/null
     * Number of stdin file descriptors (fds) following, may be 0
     * Set of stdin fds
     * Number of stdout file descriptors following, may be 0
     * Set of stdout fds
     * Number of stderr file descriptors following, may be 0
     * Set of stderr fds
     */
    char *token;
    int num_fds;
    int current_fd;
    int i;

    if (argc != 2) {
	exit(1);
    }
    token = strtok(argv[1], " ");
    if (token == NULL) {
	exit(1);
    }
    if (*token == 'y') {
	trace_fd = fopen("/tmp/ptp_ibmpe_miniproxy.log", "w");
    }
    stdout_prefix = strtok(NULL, " ");
    if (stdout_prefix == NULL) {
	exit(1);
    }
    stderr_prefix = strtok(NULL, " ");
    if (stderr_prefix == NULL) {
	exit(1);
    }
    token = strtok(NULL, " ");
    if (token == NULL) {
	exit(1);
    }
    num_fds = atoi(token);
    /*
     * For stdin, just redirect stdin to /dev/null. The application will
     * get an unexpected eof, which it will hopefully handle more
     * gracefully than the SIGPIPE it would get without attempting
     * redirection. There is really no other alternative here.
     */
    for (i = 0; i < num_fds; i++) {
	int stdin_fd;

	token = strtok(NULL, " ");
	if (token == NULL) {
	    exit(1);
	}
	current_fd = atoi(token);
	stdin_fd - open("/dev/null", O_RDONLY);
	if (stdin_fd != -1) {
	    if (trace_fd != NULL) {
		fprintf(trace_fd, "Redirect stdin pipe fd %d to /dev/null\n",
			current_fd);
		fflush(trace_fd);
	    }
	    dup2(current_fd, stdin_fd);
	}
    }
    token = strtok(NULL, " ");
    if (token == NULL) {
	exit(1);
    }
    /*
     * Start a thread for each stdout file descriptor, where output will be
     * redirected to /dev/null or to a user specified target file
     */
    num_fds = atoi(token);
    for (i = 0; i < num_fds; i++) {
	token = strtok(NULL, " ");
	if (token == NULL) {
	    exit(1);
	}
	current_fd = atoi(token);
	if (trace_fd != NULL) {
	    fprintf(trace_fd, "Starting stdout writer thread for fd %d\n",
		    current_fd);
	    fflush(trace_fd);
	}
	pthread_mutex_lock(&count_lock);
	thread_count = thread_count + 1;
	pthread_mutex_unlock(&count_lock);
	pthread_create(&tid, NULL, stdout_writer, (void *) current_fd);
    }
    token = strtok(NULL, " ");
    if (token == NULL) {
	exit(1);
    }
    /*
     * Start a thread for each stdout file descriptor, where output will be
     * redirected to /dev/null or to a user specified target file
     */
    num_fds = atoi(token);
    for (i = 0; i < num_fds; i++) {
	token = strtok(NULL, " ");
	if (token == NULL) {
	    exit(1);
	}
	current_fd = atoi(token);
	if (trace_fd != NULL) {
	    fprintf(trace_fd, "Starting stderr writer thread for fd %d\n",
		    current_fd);
	    fflush(trace_fd);
	}
	pthread_mutex_lock(&count_lock);
	thread_count = thread_count + 1;
	pthread_mutex_unlock(&count_lock);
	pthread_create(&tid, NULL, stderr_writer, (void *) current_fd);
    }
    /*
     * Wait until there are no more I/O threads
     */
    while (thread_count > 0) {
	sleep(60);
    }
    exit(0);
}

void *
stdout_writer(void *fd)
{
    common_writer((int) fd, "stdout");
    return NULL;
}

void *
stderr_writer(void *fd)
{
    common_writer((int) fd, "stderr");
    return NULL;
}

void
common_writer(int fd, char *file_suffix)
{
    /*
     * Handle redirection of output to /dev/null or to user-specified
     * file. This function first changes the file descriptor from 
     * non-blocking I/O to blocking I/O then loops, copying data from
     * input to output until eof is reached.
     */
    int flags;
    int status;
    int out_fd;
    char buf[256];
    char path[PATH_MAX];

    flags = fcntl((int) fd, F_GETFL);
    flags = flags & (0xffffffff ^ O_NONBLOCK);
    status = fcntl((int) fd, F_SETFL, flags);
    if (status != -1) {
	if (strcmp(stdout_prefix, "/dev/null") == 0) {
	    out_fd = open(stdout_prefix, O_RDWR | O_CREAT | O_TRUNC, 0644);
	}
	else {
	    snprintf(path, sizeof path, "%s_fd%d.%s", stdout_prefix, (int) fd,
		     file_suffix);
	    path[sizeof path - 1] = '\0';
	    out_fd = open(path, O_RDWR | O_CREAT | O_TRUNC, 0666);
	}
	if (out_fd != -1) {
	    status = read((int) fd, buf, sizeof buf);
	    while (status > 0) {
		status = write(out_fd, buf, status);
		if (status == -1) {
		    break;
		}
		status = read((int) fd, buf, sizeof buf);
	    }
	    if ((status == -1) && (trace_fd != NULL)) {
		fprintf(trace_fd, "I/O error in writer thread for fd %d: %s\n",
			(int) fd, strerror(errno));
		fflush(trace_fd);
	    }
	    close(out_fd);
	}
    }
    if (trace_fd != NULL) {
	fprintf(trace_fd, "Writer thread for fd %d complete\n", (int) fd);
	fflush(trace_fd);
    }
    pthread_mutex_lock(&count_lock);
    thread_count = thread_count - 1;
    pthread_mutex_unlock(&count_lock);
}
