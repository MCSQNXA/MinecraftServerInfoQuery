#pragma once

#include <string>


class MinecraftServerInfoQuery
{
public:
	std::string queryJava(std::string ipv4, int port);




};

class Stream {
private:
	int _size = 0;
	int _data_size = 8;
	char* _data = NULL;

public:
	Stream();
	~Stream();

public:
	void write(char b);

public:
	std::string toString();
};