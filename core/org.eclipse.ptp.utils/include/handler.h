/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly  
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 ******************************************************************************/
 
#ifndef _HANDLER_H_
#define _HANDLER_H_

#include <sys/select.h>

#define HANDLER_FILE		1
#define HANDLER_SIGNAL		2
#define HANDLER_EVENT		3

#define READ_FILE_HANDLER	1
#define WRITE_FILE_HANDLER	2
#define EXCEPT_FILE_HANDLER	4

struct handler {
	int		htype;
	void *	data;
		
	/*
	 * HANDLER_FILE
	 */
	int		file_type; 							/* type of this file handler */
	int		fd;									/* file descriptor */
	int		error;								/* last operation result */
	int		(*file_handler)(int, void *);		/* handler callback function */
	
	/*
	 * HANDLER_SIGNAL
	 */
	int		signal;								/* signal to handle */

	/*
	 * HANDLER_EVENT
	 */
	int		event_type;							/* type of this event handler */
	void 	(*event_handler)(void *, void *);	/* handler callback function */
};
typedef struct handler	handler;

handler *	NewHandler(int, void *);
void		DestroyHandler(handler *);
void		SetHandler(void);
handler *	GetHandler(void);
void		RegisterEventHandler(int, void (*)(void *, void *), void *);
void		UnregisterEventHandler(int, void (*)(void *, void *));
void		RegisterFileHandler(int fd, int type, int (*)(int, void *), void *);
void		UnregisterFileHandler(int);
void		CallEventHandlers(int, void *);
int			CallFileHandlers(fd_set *rfds, fd_set *wfds, fd_set *efds);
void		GenerateFDSets(int *, fd_set *, fd_set *, fd_set *);
#endif /* !_HANDLER_H_ */
