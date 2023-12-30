#pragma once
#include "Windows.h"
#include "stdio.h"

namespace MemoryEdit {

	void hookCall(DWORD* toHookAddy, DWORD jmpTo);

	void hook(DWORD toHookAddy, DWORD jmpTo);

	void changeByte(BYTE *address, BYTE opcode);

	void nop(DWORD start, int length);

	void ret(DWORD function);

	void ret(DWORD function, char retOffset);

	void writeInt(DWORD addy, int value);

	void writeByte(DWORD addy, BYTE value);

	DWORD unprotect(LPVOID start, size_t len);

	DWORD protect(LPVOID start, size_t len);

   template<class T> void edit(LPVOID *addr, size_t len, T edit) {
		unprotect(addr, len);
		edit(addr);
		protect(addr, len);
	}

}