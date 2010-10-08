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

 Classes: Socket, SocketException

 Description: Socket manipulation.
   
 Author: Tu HongJ, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#include <assert.h>
#include <errno.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/tcp.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>

#include "socket.hpp"
#include "ipconverter.hpp"


#define CONNECTING_TIMES 200

Socket::Socket(int sockfd)
    : socket(sockfd)
{
}

Socket::~Socket()
{
    ::close(socket);
}

int Socket::setMode(bool mode)
{
    int flags, newflags;

    flags = ::fcntl(socket, F_GETFL);
    if (flags < 0)
        throw SocketException(SocketException::NET_ERR_FCNTL, errno);

    if (mode)
        newflags = flags & ~O_NONBLOCK;
    else
        newflags = flags | O_NONBLOCK;

    if (newflags != flags) {
        if (::fcntl(socket, F_SETFL, newflags) < 0) {
            throw SocketException(SocketException::NET_ERR_FCNTL, errno);
        }
    }
    
    return 0;
}

int Socket::setFd(int fd)
{
    if (fd < 0) {
        throw SocketException(SocketException::NET_ERR_SOCKET, errno);
    }
    int nodelay = 1;
    ::setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, (char*)&nodelay, sizeof(nodelay));
    socket = fd;

    return 0;
}

int Socket::listen(int &port)
{
    int sockfd;
    int yes;
    struct addrinfo hints, *host, *ressave;
    char service[NI_MAXSERV] = {0};

    ::memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_flags = AI_PASSIVE;
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    ::sprintf(service, "%d", port);
    ::getaddrinfo(NULL, service, &hints, &host);
    ressave = host;

    bool binded = false;
    while (host) {
        sockfd = ::socket(host->ai_family, host->ai_socktype, host->ai_protocol);
        yes = 1;
        ::setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(yes));

        if (sockfd >= 0) {
            int rc = ::bind(sockfd, host->ai_addr, host->ai_addrlen);
            if (rc == 0) {
                struct sockaddr_storage sockaddr;
                socklen_t len = sizeof(sockaddr);

                ::getsockname(sockfd, (struct sockaddr *)&sockaddr, &len);
                ::getnameinfo((struct sockaddr *)&sockaddr, len, NULL, 0,
                        service, sizeof(service), NI_NUMERICSERV);
                port = ::atoi(service);

                binded = true;
                break;
            }
        }
        host = host->ai_next;
    }

    if (binded) {
        ::listen(sockfd, SOMAXCONN);
    } else {
        throw SocketException(SocketException::NET_ERR_BIND, errno);
    }
    ::freeaddrinfo(ressave);

    return sockfd;
}

int Socket::listen(int & port, const string & ifname)
{
    char service[NI_MAXSERV] = {0};
    ::sprintf(service, "%d", port);
    
    int sockfd = ::socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        throw SocketException(SocketException::NET_ERR_SOCKET, errno);
    }

    int yes = 1;
    ::setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(yes));

    IPConverter converter;
    struct sockaddr_in addr;
    converter.getIP(ifname, true, &addr);

    int rc = ::bind(sockfd, (struct sockaddr *)&addr, sizeof(addr));
    if (rc == 0) {
        struct sockaddr_storage sockaddr;
        socklen_t len = sizeof(sockaddr);

        ::getsockname(sockfd, (struct sockaddr *)&sockaddr, &len);
        ::getnameinfo((struct sockaddr *)&sockaddr, len, NULL, 0,
            service, sizeof(service), NI_NUMERICSERV);
        port = ::atoi(service);
    } else {
        throw SocketException(SocketException::NET_ERR_BIND, errno);
    }

    ::listen(sockfd, SOMAXCONN);
    return sockfd;
}

int Socket::connect(const char *hostName, in_port_t port)
{
    int rc = -1;
    int sockfd, nodelay;
    char service[NI_MAXSERV] = {0};
    struct addrinfo *host = NULL;
    int count = 0;

    while (count < CONNECTING_TIMES) {
        struct addrinfo hints = {0};
        ::sprintf(service, "%d", port);
        hints.ai_family = AF_UNSPEC;
        hints.ai_socktype = SOCK_STREAM;
        hints.ai_flags = AI_NUMERICHOST | AI_NUMERICSERV;

        rc = ::getaddrinfo(hostName, service, &hints, &host);
        if (rc == EAI_NONAME) {
            hints.ai_flags = 0;
            rc = ::getaddrinfo(hostName, service, &hints, &host);
        }
        if (!host) {
            throw SocketException(SocketException::NET_ERR_GETADDRINFO, errno);
        }

        sockfd = ::socket(host->ai_family, host->ai_socktype, host->ai_protocol);
        if (sockfd < 0) {
            ::freeaddrinfo(host);
            throw SocketException(SocketException::NET_ERR_SOCKET, errno);
        }
        nodelay = 1;
        rc = ::setsockopt(sockfd, IPPROTO_TCP, TCP_NODELAY, (char*)&nodelay, sizeof(nodelay));

        rc = ::connect(sockfd, host->ai_addr, host->ai_addrlen);
        if (rc == 0)
            break;
        ::sleep(1);
        count++;
        ::freeaddrinfo(host);
        ::close(sockfd);
    }
    if (rc < 0) {
        ::freeaddrinfo(host);
        throw SocketException(SocketException::NET_ERR_CONNECT, errno);
    }
    ::freeaddrinfo(host);
    socket = sockfd;

    return sockfd;
}

int Socket::accept(int sockfd)
{
    int client = -1;
    int nodelay = 1;
    struct sockaddr_storage sockaddr;
    socklen_t  len = sizeof(sockaddr);

    client = ::accept(sockfd, (struct sockaddr *)&sockaddr, &len);
    if (client < 0) {
        throw (SocketException(SocketException::NET_ERR_ACCEPT, errno));
    }
    ::setsockopt(client, IPPROTO_TCP, TCP_NODELAY, (char*)&nodelay, sizeof(nodelay));

    return client;
}

int Socket::send(const char *buf, int len)
{
    int n;
    char *pos = NULL;
    int left;

    pos = (char *) buf;
    left = len;

    while (left > 0) {
        n = ::send(socket, pos, left, 0);
        if (n < 0) {
            if ((errno == EAGAIN) || (errno == EWOULDBLOCK) || (errno == EINTR)) {
                continue;
            } 
            throw (SocketException(SocketException::NET_ERR_SEND, errno));
        }
        pos += n;
        left -= n;
    }

    return 0;
}

int Socket::recv(char *buf, int len)
{
    int n;
    int left;
    char *pos;

    pos = buf;
    left    = len;

    while (left > 0) {
        n = ::recv(socket, pos, left, 0);
        if (n < 0) {
            if (errno == EINTR) {
                continue;
            }
            if ((errno == EAGAIN) || (errno == EWOULDBLOCK)) {
                break;
            }
            throw (SocketException(SocketException::NET_ERR_RECV, errno));
        } else if (n == 0) {
            throw (SocketException(SocketException::NET_ERR_CLOSED));
        }

        pos += n;
        left -= n;
    }

    return (len - left);
}

void Socket::close(Socket::DIRECTION how)
{
    if (socket < 0)
        return;

    switch (how) {
        case READ:
            ::shutdown(socket, SHUT_RD); 
            break;
        case WRITE:
            ::shutdown(socket, SHUT_WR);
            break;
        case BOTH:
            ::shutdown(socket, SHUT_RDWR);
            ::close(socket);
        default:
            break;
    }
}

SocketException::SocketException(int code) throw()
    : errCode(code), errNum(0)
{
}

SocketException::SocketException(int code, int num) throw()
    : errCode(code), errNum(num)
{
}

int SocketException::getErrCode() const throw()
{
    return errCode;
}

int SocketException::getErrNum() const throw()
{
    return errNum;
}

string & SocketException::getErrMsg() throw()
{
    switch (errCode) {
        case NET_ERR_SOCKET:
            errMsg = "Function ::socket()";
            break;
        case NET_ERR_CONNECT:
            errMsg = "Function ::connect()";
            break;
        case NET_ERR_GETADDRINFO:
            errMsg = "Function ::getaddrinfo()";
            break;
        case NET_ERR_SEND:
            errMsg = "Function ::send()";
            break;
        case NET_ERR_RECV:
            errMsg = "Function ::recv()";
            break;
        case NET_ERR_FCNTL:
            errMsg = "Function ::fcntl()";
            break;
        case NET_ERR_CLOSED:
            errMsg = "Function ::recv() connection was closed by peer";
            break;
        case NET_ERR_DATA:
            errMsg = "Received unexpected data";
            break;
        case NET_ERR_BIND:
            errMsg = "Function ::bind()";
        default:
            errMsg = "Unknown error";
            break;
    }

    if (errNum != 0) {
        errMsg += "; system error: ";
        errMsg += ::strerror(errNum);
    }

    return errMsg;
}

