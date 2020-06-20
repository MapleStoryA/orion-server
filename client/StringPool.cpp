#include <intrin.h>
#include "StringPool.h"
#include "Config.h"

const char *StringPool::POOL[];

const char * StringPool::GetFromIndex(int idx) {
	if (idx > sizeof(StringPool::POOL)) {
		return 0;
	}
	return StringPool::POOL[idx];
}

ZXString<char>* StringPool::GetString(ZXString<char> *result, int idx, int formal) {
	ZXString<char>* str = reinterpret_cast<ZXString<char>*(__thiscall*)(StringPool*, ZXString<char>*, int, bool)>(0x0079E993)(this, result, idx, formal);
	auto custom = GetFromIndex(idx);
	if (custom) {
		result->AssignRaw(custom);
        #if ENABLE_CONSOLE
			printf("Loading string  Len: %d -- %d ----- %s\n", result->GetLength(), idx, str->_m_pStr);
		#endif
	}
	return str;
}
