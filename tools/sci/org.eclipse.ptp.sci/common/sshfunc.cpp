#ifndef _PRAGMA_COPYRIGHT_
#define _PRAGMA_COPYRIGHT_
#pragma comment(copyright, "%Z% %I% %W% %D% %T%\0")
#endif /* _PRAGMA_COPYRIGHT_ */
/****************************************************************************

* Copyright (c) 2008, 2010 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0s
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html

 Classes: SshFunc

 Description: ssh functions
   
 Author: Tu HongJ

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D16661)

****************************************************************************/
#ifdef HAVE_CONFIG_H
#include "config.h"
#endif
#include <dlfcn.h>
#include <assert.h>
#include <string.h>

#include "sshfunc.hpp"
#include "tools.hpp"

SshFunc *SshFunc::instance = NULL;

SshFunc * SshFunc::getInstance()
{
    if (instance == NULL) {
        instance = new SshFunc();
        int rc = instance->load();
        if (rc != 0) {
            return NULL;
        }
    }
    return instance;
}

SshFunc::SshFunc()
    : dlopen_file(NULL), mdlhndl(0), set_auth_module_hndlr(NULL), get_id_token_hndlr(NULL), verify_id_token_hndlr(NULL), get_id_from_token_hndlr(NULL), free_id_token_hndlr(NULL), get_key_from_token_hndlr(NULL), sign_data_hndlr(NULL), verify_data_hndlr(NULL), free_signature_hndlr(NULL) 
{
    user_token.iov_base = NULL;
    user_token.iov_len = 0;
}

SshFunc::~SshFunc()
{
    if (dlopen_file) {
        ::dlclose(dlopen_file);
    }
    free_id_token(&user_token);
}

int SshFunc::load(char * libPath)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    int rc = -1;
    string path = "";
    string auth_mod = "";
    if (libPath) {
        path = libPath;
    } else {
#ifdef _SCI_LINUX // Linux
        path = "libpsec.so"; // default library name on Linux
#ifdef __64BIT__
        auth_mod = "/usr/lib64/libpsec_ossh.so";
#else
        auth_mod = "/usr/lib/libpsec_ossh.so";
#endif
#else
#ifdef __64BIT__
        path = "libpsec.a(shr_64.o)";
        auth_mod = "/usr/lib/libpsec_ossh64.so";
#else  // 32-bit
        path = "libpsec.a(shr.o)";
        auth_mod = "/usr/lib/libpsec_ossh.so"; 
#endif
#endif
    }

#ifdef _SCI_LINUX // Linux
    dlopen_file = ::dlopen(path.c_str(), RTLD_NOW | RTLD_GLOBAL | RTLD_DEEPBIND);
#else // aix
    dlopen_file = ::dlopen(path.c_str(), RTLD_NOW | RTLD_GLOBAL | RTLD_MEMBER);
#endif
    if (NULL == dlopen_file) {
        return -1;
    }

    set_auth_module_hndlr = (psec_set_auth_module_hndlr *) ::dlsym(dlopen_file, "psec_set_auth_module");
    if (NULL == set_auth_module_hndlr) {
        return -1;
    }
    get_id_token_hndlr = (psec_get_id_token_hndlr *) ::dlsym(dlopen_file, "psec_get_id_token");
    if (NULL == get_id_token_hndlr) {
        return -1;
    }
    verify_id_token_hndlr = (psec_verify_id_token_hndlr *) ::dlsym(dlopen_file, "psec_verify_id_token");
    if (NULL == verify_id_token_hndlr) {
        return -1;
    }
    get_id_from_token_hndlr = (psec_get_id_from_token_hndlr *) ::dlsym(dlopen_file, "psec_get_id_from_token");
    if (NULL == get_id_from_token_hndlr) {
        return -1;
    }
    free_id_token_hndlr = (psec_free_id_token_hndlr *) ::dlsym(dlopen_file, "psec_free_id_token");
    if (NULL == free_id_token_hndlr) {
        return -1;
    }

    get_key_from_token_hndlr = (psec_get_key_from_token_hndlr *) ::dlsym(dlopen_file, "psec_get_key_from_token");
    if (NULL == get_key_from_token_hndlr) {
        return -1;
    }

    sign_data_hndlr = (psec_sign_data_hndlr *) ::dlsym(dlopen_file, "psec_sign_data");
    if(NULL == sign_data_hndlr) {
        return -1;
    }

    verify_data_hndlr = (psec_verify_data_hndlr *) ::dlsym(dlopen_file, "psec_verify_data");
    if(NULL == verify_data_hndlr) {
        return -1;
    }

    free_signature_hndlr = (psec_free_signature_hndlr *) ::dlsym(dlopen_file, "psec_free_signature");
    if(NULL == free_signature_hndlr) {
        return -1;
    }
    
    rc = set_auth_module(NULL, (char *)auth_mod.c_str(), "m[t=-1]");
    if (rc == 0) {
        get_id_token(NULL, NULL, &user_token);
        key_len = sizeof(session_key);
        get_key_from_token(NULL, &user_token, session_key, &key_len);
    }

    return rc;
}

int SshFunc::set_auth_module(char *name, char *fpath, char *opts)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return set_auth_module_hndlr(name, fpath, opts, &mdlhndl);
}

int SshFunc::get_id_token(char *tname, char *thost, psec_idbuf_t idtok)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return get_id_token_hndlr(mdlhndl, tname, thost, idtok);
}

int SshFunc::verify_id_token(char *uname, psec_idbuf_t idtok)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return verify_id_token_hndlr(mdlhndl, uname, idtok);
}

int SshFunc::get_id_from_token(psec_idbuf_t idtok, char *usrid, size_t *usridlen)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return get_id_from_token_hndlr(mdlhndl, idtok, usrid, usridlen);
}

int SshFunc::free_id_token(psec_idbuf_t id)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return free_id_token_hndlr(mdlhndl, id);
}

int SshFunc::get_key_from_token(char *uname, psec_idbuf_t idtok , char *key, size_t *keylen) 
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return get_key_from_token_hndlr(mdlhndl, uname, idtok, key, keylen);
}

int SshFunc::sign_data(char *key, size_t keylen, struct iovec *inbufs, int num_bufs, struct iovec *sigbufs) 
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return sign_data_hndlr(mdlhndl, key, keylen, inbufs, num_bufs, sigbufs);
}

int SshFunc::verify_data(char *key, size_t keylen, struct iovec *inbufs, int num_bufs, struct iovec *sigbufs) 
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return verify_data_hndlr(mdlhndl, key, keylen, inbufs, num_bufs, sigbufs);
}

int SshFunc::sign_data(struct iovec *inbufs, int num_bufs, struct iovec *sigbufs) 
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return sign_data_hndlr(mdlhndl, session_key, key_len, inbufs, num_bufs, sigbufs);
}

int SshFunc::verify_data(struct iovec *inbufs, int num_bufs, struct iovec *sigbufs) 
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return verify_data_hndlr(mdlhndl, session_key, key_len, inbufs, num_bufs, sigbufs);
}

int SshFunc::free_signature(struct iovec *sigbufs) 
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    return free_signature_hndlr(mdlhndl, sigbufs);
}

int SshFunc::sign_data(char *key, size_t keylen, char *bufs[], int sizes[], int num_bufs, struct iovec *sigbufs)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    int i, rc;
    struct iovec *tmp_bufs = new struct iovec[num_bufs];

    for (i = 0; i < num_bufs; i++) {
        tmp_bufs[i].iov_base = bufs[i];
        tmp_bufs[i].iov_len = sizes[i];
    }
    rc = sign_data(key, keylen, tmp_bufs, num_bufs, sigbufs);
    delete tmp_bufs;

    return rc;
}

int SshFunc::verify_data(char *key, size_t keylen, char *bufs[], int sizes[], int num_bufs, struct iovec *sigbufs)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    int i, rc;
    struct iovec *tmp_bufs = new struct iovec[num_bufs];

    for (i = 0; i < num_bufs; i++) {
        tmp_bufs[i].iov_base = bufs[i];
        tmp_bufs[i].iov_len = sizes[i];
    }
    rc = verify_data(key, keylen, tmp_bufs, num_bufs, sigbufs);
    delete tmp_bufs;

    return rc;
} 

int SshFunc::sign_data(char *bufs[], int sizes[], int num_bufs, struct iovec *sigbufs)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    int i, rc;
    struct iovec *tmp_bufs = new struct iovec[num_bufs];

    for (i = 0; i < num_bufs; i++) {
        tmp_bufs[i].iov_base = bufs[i];
        tmp_bufs[i].iov_len = sizes[i];
    }
    rc = sign_data(session_key, key_len, tmp_bufs, num_bufs, sigbufs);
    delete tmp_bufs;

    return rc;
}

int SshFunc::verify_data(char *bufs[], int sizes[], int num_bufs, struct iovec *sigbufs)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    int i, rc;
    struct iovec *tmp_bufs = new struct iovec[num_bufs];

    for (i = 0; i < num_bufs; i++) {
        tmp_bufs[i].iov_base = bufs[i];
        tmp_bufs[i].iov_len = sizes[i];
    }
    rc = verify_data(session_key, key_len, tmp_bufs, num_bufs, sigbufs);
    delete tmp_bufs;

    return rc;
} 

int SshFunc::set_user_token(struct iovec *token)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    free_id_token(&user_token);
    user_token.iov_len = token->iov_len;
    user_token.iov_base = new char [token->iov_len];
    memcpy(user_token.iov_base, token->iov_base, token->iov_len);
    get_key_from_token(NULL, &user_token, session_key, &key_len);

    return 0;
}

int SshFunc::set_session_key(struct iovec *sskey)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    key_len = sskey->iov_len;
    memcpy(session_key, sskey->iov_base, key_len);

    return 0;
}
