#include	"MinecraftServerInfoQuery.h"

#include <iostream>


int main() {
	MinecraftServerInfoQuery query;

	std::cout << query.queryJava("106.52.16.139", 25565) << std::endl;

	std::cout << std::endl;

	std::cout << query.queryBedrock("106.52.16.139", 19132) << std::endl;


}
