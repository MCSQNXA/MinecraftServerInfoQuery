#include "MinecraftServerInfoQuery.h"

#include <WinSock2.h>
#include <WS2tcpip.h>
#include <Windows.h>

#pragma comment(lib,"ws2_32.lib")

#pragma warning(disable:4996)


std::string MinecraftServerInfoQuery::queryJava(std::string ipv4, int port)
{
	WSADATA wsadata;
	if (WSAStartup(MAKEWORD(2, 2), &wsadata) != 0)
	{
		return "WSAStartup error";
	}

	if (LOBYTE(wsadata.wVersion) != 2 || HIBYTE(wsadata.wHighVersion) != 2) {
		return "LOBYTE error";
	}

	SOCKET client;

	SOCKADDR_IN in;
	in.sin_family = AF_INET;
	in.sin_port = htons(port);
	in.sin_addr.S_un.S_addr = inet_addr(ipv4.c_str());

	if ((client = socket(AF_INET, SOCK_STREAM, 0)) == SOCKET_ERROR) {
		return "Socket error";
	}

	if (connect(client, (struct sockaddr*)&in, sizeof(in)) == INVALID_SOCKET) {
		return "无法连接至服务器";
	}

	unsigned char head[2][5] = { {0x10, 0x00, 0x6B, 0x0A},{0xDD, 0x63, 0x01, 0x01, 0x00} };

	send(client, (char*)head[0], sizeof(head[0]), 0);
	send(client, (char*)ipv4.data(), (int)ipv4.size(), 0);
	send(client, (char*)head[1], sizeof(head[1]), 0);

	char buf[3];
	if (sizeof(buf) != recv(client, buf, sizeof(buf), 0)) {//读取头部无用三个字节
		closesocket(client); WSACleanup(); return "网络错误";
	}

	Stream stream;

	char buff[1];
	while (sizeof(buff) == recv(client, buff, sizeof(buff), 0)) {
		stream.write(buff[0]);
	}




	closesocket(client); WSACleanup();return stream.toString();
}


Stream::Stream()
{
	this->_data = new char[this->_data_size];
}

Stream::~Stream()
{
	delete[] this->_data;
}

void Stream::write(char b)
{
	if (this->_data_size < this->_size + 1) {
		char* buff = new char[this->_data_size = (this->_data_size << 1)];

		for (int i = 0; i < this->_size; i++) {
			buff[i] = this->_data[i];
		}

		delete[] this->_data; this->_data = buff;
	}

	this->_data[this->_size++] = b;
}

std::string Stream::toString()
{
	return std::string(this->_data, this->_size);
}
