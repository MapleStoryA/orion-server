#include "Common\ZXString.h";
#include "Memory\MemoryEdit.h";

class StringPool {
	
public:
	ZXString<char>* GetString(ZXString<char>* ret, int idx, int formal);
	const char * GetFromIndex(int idx);



	static const char *POOL[5657];

	static const void initialize() {
		StringPool::POOL[2895] = "Versão editada.";
		StringPool::POOL[12] = "Michel Temer";
	}

	static const void hook() {
		initialize();
		auto addy = &StringPool::GetString;
	}
	
	

	
};