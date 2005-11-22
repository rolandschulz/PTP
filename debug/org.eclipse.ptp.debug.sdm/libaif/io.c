/*
 * Routines for managing file input/output for AIF objects.
 *
 * Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

#ifdef HAVE_CONFIG_H
#include	<config.h>
#endif /* HAVE_CONFIG_H */

#include	<assert.h>
#include	<stdio.h>
#include	<errno.h>
#include	<string.h>
#include	<sys/types.h>
#include	<sys/stat.h>
#include	<fcntl.h>

#ifdef WIN32
#include	<io.h>
#else /* WIN32 */
#include	<unistd.h>
#endif /* WIN32 */

#include	"aif.h"
#include	"aifint.h"
#include	"aiferr.h"

#define AIFFILE_MAGIC	"AIF1.0"

#define AIFFILE_DATA	'D'
#define AIFFILE_EOF	'E'

struct aifhdr
{
	char	hdr_magic[6];
	char	hdr_flag;
	char	hdr_namelen[6];
	char	hdr_fdslen[6];
	char	hdr_datalen[11];
};
typedef struct aifhdr	aifhdr;

extern int	errno;

int	write_data(FILE *, char *, int);

int
ToNum(char *str, int len)
{
	int	i;
	int	val = 0;

	for ( i = 0 ; i < len ; i++ )
		val = val * 10 + str[i] - '0';

	return val;
}

int
aif_write_set(AIFFILE *af, char *name, char *fds, char *data, int len)
{
	int	nlen;
	int	flen;
	aifhdr	hdr;
	char	buf[BUFSIZ];
	char	nbuf[BUFSIZ];

	memcpy(hdr.hdr_magic, AIFFILE_MAGIC, sizeof(hdr.hdr_magic));

	if ( fds == NULL )
	{
		hdr.hdr_flag = AIFFILE_EOF;
		nlen = 0;
		flen = 0;
		len = 0;
	}
	else
	{
		hdr.hdr_flag = AIFFILE_DATA;

		if ( name == NULL )
			snprintf(nbuf, BUFSIZ, "_ar_%d", af->af_cnt++);
		else
			strcpy(nbuf, name);

		nlen = strlen(nbuf) + 1;
		flen = strlen(fds) + 1;
	}

	snprintf(buf, BUFSIZ, "%06d", nlen);
	memcpy(hdr.hdr_namelen, buf, sizeof(hdr.hdr_namelen));

	snprintf(buf, BUFSIZ, "%06d", flen);
	memcpy(hdr.hdr_fdslen, buf, sizeof(hdr.hdr_fdslen));

	snprintf(buf, BUFSIZ, "%011d", len);
	memcpy(hdr.hdr_datalen, buf, sizeof(hdr.hdr_datalen));

	if ( write_data(af->af_fp, (char *)&hdr, sizeof(aifhdr)) < 0 )
		return -1;

	if ( fds == NULL )
		return 0;

	if
	(
		write_data(af->af_fp, nbuf, nlen) < 0
		||
		write_data(af->af_fp, fds, flen) < 0
		||
		write_data(af->af_fp, data, len) < 0
	)
		return -1;

	return 0;
}

int
write_data(FILE *fp, char *buf, int len)
{
	int	n;

	while ( (n = fwrite(buf, 1, len, fp)) < len )
	{
		if ( n < 0 )
		{
			SetAIFError(AIFERR_WRITE, strerror(errno));
			return -1;
		}

		len -= n;
		buf += n;
	}

	return 0;
}

int
AIFWriteSet(AIFFILE *af, AIF *a, char *name)
{
	if ( a == (AIF *)NULL || af == (AIFFILE *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if
	(
		af->af_mode != AIFMODE_CREATE
		&&
		af->af_mode != AIFMODE_APPEND
	)
	{
		SetAIFError(AIFERR_MODE, NULL);
		return -1;
	}

	return aif_write_set(af, name, AIF_FORMAT(a), AIF_DATA(a), AIF_LEN(a));
}

int
aif_read_set(AIFFILE *af, AIF **a, char **name)
{
	int		l;
	int		flen;
	int		nlen;
	int		dlen;
	aifhdr		hdr;
	char *		fds;
	char *		data;
	static AIF *	_aif_read_aif = (AIF *)NULL;
	static char *	_aif_read_name = NULL;

	if ( (l = fread((char *)&hdr, sizeof(aifhdr), 1, af->af_fp)) < 0 )
	{
		SetAIFError(AIFERR_READ, strerror(errno));
		return -1;
	}

	if
	(
		l < 1
		||
		strncmp(hdr.hdr_magic, AIFFILE_MAGIC, sizeof(hdr.hdr_magic)) != 0 )
	{
		SetAIFError(AIFERR_BADHDR, NULL);
		return -1;
	}

	if ( hdr.hdr_flag == AIFFILE_EOF )
		return 0;

	nlen = ToNum(hdr.hdr_namelen, sizeof(hdr.hdr_namelen));
	flen = ToNum(hdr.hdr_fdslen, sizeof(hdr.hdr_fdslen));
	dlen = ToNum(hdr.hdr_datalen, sizeof(hdr.hdr_datalen));

	if ( a == (AIF **)NULL )
	{
		/*
		** Skip over data.
		*/
		if ( fseek(af->af_fp, nlen + flen + dlen, SEEK_CUR) < 0 )
		{
			SetAIFError(AIFERR_SEEK, strerror(errno));
			return -1;
		}

		return 1;
	}

	if ( _aif_read_name != NULL )
		_aif_free(_aif_read_name);

	_aif_read_name = (char *)_aif_alloc(nlen);

	fds = (char *)_aif_alloc(flen);
	data = (char *)_aif_alloc(dlen);

	if ( (l = fread(_aif_read_name, 1, nlen, af->af_fp)) < 0 || l < nlen )
	{
		SetAIFError(AIFERR_READ, strerror(errno));
		return -1;
	}

	if ( (l = fread(fds, 1, flen, af->af_fp)) < 0 || l < flen )
	{
		SetAIFError(AIFERR_READ, strerror(errno));
		return -1;
	}

	if ( (l = fread(data, 1, dlen, af->af_fp)) < 0 || l < dlen )
	{
		SetAIFError(AIFERR_READ, strerror(errno));
		return -1;
	}

	if ( _aif_read_aif != (AIF *)NULL )
		AIFFree(_aif_read_aif);

	*a = _aif_read_aif = MakeAIF(fds, data);

	if ( name != (char **)NULL )
		*name = _aif_read_name;

	return 1;
}

AIF *
AIFReadSet(AIFFILE *af, char **name)
{
	AIF *	a;

	if ( af == (AIFFILE *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	if ( af->af_mode != AIFMODE_READ )
	{
		SetAIFError(AIFERR_MODE, NULL);
		return (AIF *)NULL;
	}

	if ( aif_read_set(af, &a, name) <= 0 )
		return (AIF *)NULL;

	return a;
}

AIFFILE *
AIFOpenSet(char *file, int mode)
{
	int		n;
	int		cnt = 0;
	int		fd;
	int		created = 0;
	char *		fmode;
	FILE *		fp;
	AIFFILE *	af;

	switch ( mode )
	{
	case AIFMODE_READ:
		fmode = "r";
		break;

	case AIFMODE_CREATE:
		fmode = "w";
		break;

	case AIFMODE_APPEND:
		fmode = "r+";

		if ( access(file, R_OK) < 0 )
		{
			if ( (fd = creat(file, 0644)) < 0 )
			{
				SetAIFError(AIFERR_OPEN, strerror(errno));
				return (AIFFILE *)NULL;
			}

			(void)close(fd);
			created = 1;
		}

		break;

	default:
		/* should never happen */
		fmode = "";
		assert(0);
	}

	if ( (fp = fopen(file, fmode)) == (FILE *)NULL )
	{
		SetAIFError(AIFERR_OPEN, strerror(errno));
		return (AIFFILE *)NULL;
	}

	af = (AIFFILE *)_aif_alloc(sizeof(AIFFILE));
	af->af_fp = fp;
	af->af_mode = mode;

	if ( mode == AIFMODE_APPEND )
	{
		/*
		** Count entries.
		*/
		while ( (n = aif_read_set(af, (AIF **)NULL, (char **)NULL)) > 0 )
			cnt++;

		if ( n < 0 && !created )
		{
			fclose(af->af_fp);
			_aif_free(af);
			return (AIFFILE *)NULL;
		}

		/*
		** Back up over EOF header.
		*/
		if
		(
			cnt > 0
			&&
			fseek(af->af_fp, -((int)sizeof(aifhdr)), SEEK_CUR) < 0
		)
		{
			SetAIFError(AIFERR_SEEK, strerror(errno));
			fclose(af->af_fp);
			_aif_free(af);
			return (AIFFILE *)NULL;
		}
	}

	af->af_cnt = cnt;

	return af;
}

int
AIFCloseSet(AIFFILE *af)
{
	if ( af == (AIFFILE *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if
	(
		(
			af->af_mode == AIFMODE_CREATE
			||
			af->af_mode == AIFMODE_APPEND
		)
		&&
		aif_write_set(af, NULL, NULL, NULL, 0) < 0
	)
		return -1;

	fclose(af->af_fp);
	_aif_free(af);

	return 0;
}
