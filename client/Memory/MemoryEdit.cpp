#include "./MemoryEdit.h"
#include <vector>
#include <sstream>
#include <iostream>


namespace MemoryEdit {

using namespace std;


	std::vector<BYTE> stringToByteArray(const std::string& s) {
		std::vector<BYTE> result;
		std::istringstream ss(s);
		std::string byteString;
		while (ss >> byteString) {
			result.push_back(static_cast<BYTE>(std::stoi(byteString, nullptr, 16)));
		}
		return result;
	}

	
   void hookCall(DWORD* toHookAddy, DWORD jmpTo) {
		DWORD dwProtect;
		VirtualProtect((DWORD*)toHookAddy, 100, PAGE_EXECUTE_READWRITE, &dwProtect);
		DWORD hookAddy = jmpTo;
		DWORD offset = jmpTo - (DWORD)toHookAddy - 5;
		BYTE input[5] = {
			0xE8, 0x00, 0x00, 0x00, 0x00
		};
		memcpy(&input[1], &offset, 4);
		memcpy((DWORD*)toHookAddy, &input, sizeof(input));
		VirtualProtect((DWORD*)toHookAddy, 100, PAGE_EXECUTE, &dwProtect);
	}

	void hook(DWORD toHookAddy, DWORD jmpTo) {
		DWORD dwProtect;
		VirtualProtect((DWORD*)toHookAddy, 100, PAGE_EXECUTE_READWRITE, &dwProtect);
		DWORD hookAddy = jmpTo;
		DWORD offset = jmpTo - toHookAddy - 5;
		BYTE input[5] = {
			0xE9, 0x00, 0x00, 0x00, 0x00
		};
		memcpy(&input[1], &offset, 4);
		memcpy((DWORD*)toHookAddy, &input, sizeof(input));
		VirtualProtect((DWORD*)toHookAddy, 100, PAGE_EXECUTE, &dwProtect);
	}

	void changeByte(BYTE *address, BYTE opcode) {
		DWORD dwProtect;
		VirtualProtect(address, 1, PAGE_EXECUTE_READWRITE, &dwProtect);
		*address = opcode;
	}

	void nop(DWORD start, int length) {
		DWORD dwProtect;
		VirtualProtect((DWORD*)start, length + 1, PAGE_EXECUTE_READWRITE, &dwProtect);
		char * addy = (char*)start;
		for (int i = 0; i < length; i++) {
			*(addy++) = (BYTE)0x90;
		}
	}

	void ret(DWORD function) {
		DWORD dwProtect;
		VirtualProtect((DWORD*)function, 2, PAGE_EXECUTE_READWRITE, &dwProtect);
		*((char*)function) = (char)0xC3;
	}

	void ret(DWORD function, char retOffset) {
		DWORD dwProtect;
		VirtualProtect((DWORD*)function, 3, PAGE_EXECUTE_READWRITE, &dwProtect);
		*((char*)function) = (char) 0xC2;
		*((char*)function + 1) = retOffset;
		*((char*)function + 2) = (char)0x00;

	}


	void writeInt(DWORD addy, int value) {
		DWORD dwProtect;
		VirtualProtect((LPVOID)addy, 4, PAGE_EXECUTE_READWRITE, &dwProtect);
		*((int*)addy) = value;
	}

	void writeByte(DWORD addy, BYTE value) {
		DWORD dwProtect;
		VirtualProtect((LPVOID)addy, 1, PAGE_EXECUTE_READWRITE, &dwProtect);
		*((BYTE*)addy) = value;
	}

	void writeByteArray(DWORD addy, std::string arrayOfBytes) {
		DWORD dwProtect;
		std::vector<BYTE> byteArray = stringToByteArray(arrayOfBytes);
		size_t len = byteArray.size();
		VirtualProtect((LPVOID)addy, len, PAGE_EXECUTE_READWRITE, &dwProtect);
        memcpy((DWORD*)addy, &byteArray, len);
		VirtualProtect((DWORD*)addy, 100, PAGE_EXECUTE, &dwProtect);
	}

	DWORD unprotect(LPVOID start, size_t len) {
		DWORD dwProtect;
		VirtualProtect(start, len, PAGE_EXECUTE_READWRITE, &dwProtect);
		return dwProtect;
	}

	DWORD protect(LPVOID start, size_t len) {
		DWORD dwProtect;
		VirtualProtect(start, len, PAGE_EXECUTE, &dwProtect);
		return dwProtect;
	}


	
	
	

}