#include "MinecraftServerInfoQuery.h"

#include <WinSock2.h>
#include <WS2tcpip.h>
#include <Windows.h>

#pragma comment(lib,"ws2_32.lib")

#pragma warning(disable:4996)

#include <json/reader.h>

#include <iostream>



std::string MinecraftServerInfoQuery::queryJava(std::string ipv4, int port)
{
	WSADATA wsadata;
	if (WSAStartup(MAKEWORD(2, 2), &wsadata) != 0)
	{
		return "WSAStartup error";
	}

	if (LOBYTE(wsadata.wVersion) != 2 || HIBYTE(wsadata.wHighVersion) != 2) {
		WSACleanup(); return "LOBYTE error";
	}

	SOCKET client;

	SOCKADDR_IN in;
	in.sin_family = AF_INET;
	in.sin_port = htons(port);
	in.sin_addr.S_un.S_addr = inet_addr(ipv4.c_str());

	if ((client = socket(AF_INET, SOCK_STREAM, 0)) == SOCKET_ERROR) {
		WSACleanup(); return "Socket error";
	}

	if (connect(client, (struct sockaddr*)&in, sizeof(in)) == INVALID_SOCKET) {
		WSACleanup(); return "无法连接至服务器";
	}

	Stream stream;
	stream.write((char)(7 + ipv4.size()));
	stream.write(0x00);
	stream.write((char)0xF5);
	stream.write(0x05);
	stream.write((char)ipv4.size());
	stream.write((char*)ipv4.data(), 0, (int)ipv4.size());
	stream.write((port >> 0x08) & 0xff);
	stream.write((port >> 0x00) & 0xff);
	stream.write(0x01);
	stream.write(0x01);
	stream.write(0x00);

	send(client, stream._data, stream._size, 0);
	shutdown(client, SD_SEND);

	stream.reset();

	char buf[3];
	if (sizeof(buf) != recv(client, buf, sizeof(buf), 0)) {//读取头部无用三个字节
		closesocket(client); WSACleanup(); return "网络错误";
	}

	char buff[1];
	while (sizeof(buff) == recv(client, buff, sizeof(buff), 0)) {
		stream.write(buff[0]);
	}

	closesocket(client); WSACleanup();

	if (stream._size == 0) {
		return "网络错误";
	}

	Json::Value data;
	Json::Reader reader;

	if (!reader.parse(stream.toString(), data)) {
		return "无法解析应答包";
	}

	std::string info;
	info.append("服务器类型:Java").append("\n");
	info.append("服务器名称:").append(data["description"]["text"].asString()).append("\n");
	info.append("服务器在线:");
	info.append(std::to_string(data["players"]["online"].asInt())).append("/");
	info.append(std::to_string(data["players"]["max"].asInt())).append("\n");
	info.append("服务器版本:").append(data["version"]["name"].asString()).append("\n");
	info.append("服务器协议:").append(std::to_string(data["version"]["protocol"].asInt()));

	return info;
}

std::string MinecraftServerInfoQuery::queryBedrock(std::string ipv4, int port)
{
	WSADATA wsadata;
	if (WSAStartup(MAKEWORD(2, 2), &wsadata) != 0)
	{
		return "WSAStartup error";
	}

	if (LOBYTE(wsadata.wVersion) != 2 || HIBYTE(wsadata.wHighVersion) != 2) {
		WSACleanup(); return "LOBYTE error";
	}

	SOCKET client;

	sockaddr_in in;
	in.sin_family = AF_INET;
	in.sin_port = htons(port);
	in.sin_addr.S_un.S_addr = inet_addr(ipv4.c_str());

	if ((client = socket(AF_INET, SOCK_DGRAM, 0)) == SOCKET_ERROR) {
		WSACleanup(); return "Socket error";
	}

	unsigned char send[] = {
	0x01,0x00,0x00,0x00,0x00,0x00,0x01,0x70,
	0xC7,0x00,0xFF,0xFF,0x00,0xFE,0xFE,0xFE,
	0xFE,0xFD,0xFD,0xFD,0xFD,0x12,0x34,0x56,
	0x78,0xAF,0x36,0x9C,0xAC,0x81,0x36,0x1D,0x03 };

	int size = sizeof(SOCKADDR);

	if (sendto(client, (char*)send, sizeof(send), 0, (SOCKADDR*)&in, size) == SOCKET_ERROR) {
		return "网络错误";
	}

	char buff[1024];

	Stream stream;
	stream.write(buff, 40, recvfrom(client, buff, sizeof(buff), 0, (SOCKADDR*)&in, &size) - 40);

	closesocket(client); WSACleanup();

	if (stream._size == 0) {
		return "网络错误";
	}

	std::string info;
	info.append("服务器类型:Bedrock").append("\n");

	for (int c = 0, l = 0, i = 0; i < stream._size; i++) {
		if (stream._data[i] == ';') {
			int size = i - l;
			char* temp = new char[size];

			for (int p = 0; p < size; p++) {//拷贝char
				temp[p] = stream._data[l++];
			}

			std::string string(temp, size); delete[] temp;

			if (c == 0) {
				info.append("服务器简介:").append(string).append("\n");
			}
			else if (c == 1) {
				info.append("服务器协议:").append(string).append("\n");
			}
			else if (c == 2) {
				info.append("服务器版本:").append(string).append("\n");
			}
			else if (c == 3) {
				info.append("服务器在线:").append(string).append("/");
			}
			else if (c == 4) {
				info.append(string).append("\n");
			}
			else if (c == 6) {
				info.append("服务器存档:").append(string).append("\n");
			}
			else if (c == 7) {
				info.append("服务器模式:").append(string);
			}

			l++; c++;
		}
	}

	return info;
}


Stream::Stream()
{
	this->_data = new char[this->_data_size];
}

Stream::~Stream()
{
	delete[] this->_data;
}

void Stream::reset()
{
	delete[] this->_data;

	this->_size = 0;
	this->_data = new char[this->_data_size = 8];
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

void Stream::write(char* b, int offset, int len)
{
	for (int i = 0; i < len; i++) {
		this->write(b[offset++]);
	}
}

std::string Stream::toString()
{
	return std::string(this->_data, this->_size);
}
