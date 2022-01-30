#pragma once

#include <string>


class MinecraftServerInfoQuery
{
public:
	std::string queryJava(std::string ipv4, int port);

public:
	std::string queryBedrock(std::string ipv4, int port);
};

class Stream {
public:
	int _size = 0;
	int _data_size = 8;
	char* _data = NULL;

public:
	Stream();
	~Stream();

public:
	void reset();

public:
	void write(char b);
	void write(char* b, int offset, int len);

public:
	std::string toString();
};