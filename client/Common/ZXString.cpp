#include "ZXString.h"

ZXString<char>::ZXString() {
	this->_m_pStr = 0;
}

ZXString<char>::ZXString(const char *s) {
	this->Assign(s);
}


ZXString<char>::~ZXString() {
	if (this->_m_pStr) {
		delete[] (this->_m_pStr - 4);
	}
	this->_m_pStr = 0;
}


ZXString<char> * ZXString<char>::operator=(const char *s) {
	return this->Assign(s);
}

ZXString<char> * ZXString<char>::operator=(ZXString<char> *s) {
	return this->Assign(s->_m_pStr);
}
template<>
ZXString<char> * ZXString<char>::AssignRaw(const char *s) {
	int len = strlen(s) + 1;
	char *data = new char[len + 4];
	*reinterpret_cast<size_t*>(data) = len - 1;
	this->_m_pStr = &data[4];
	strcpy(this->_m_pStr, s);
	return this;
}

template<>
ZXString<char> * ZXString<char>::Assign(const char *s) {
    if (this->_m_pStr) {
		delete[](this->_m_pStr - 4);
	}
    int len = strlen(s) + 1;
	char *data = new char[len + 4];
	*reinterpret_cast<size_t*>(data) = len;
	this->_m_pStr = &data[4];
	strcpy(this->_m_pStr, s);
	return this;
}
template<>
int ZXString<char>::GetLength() {
	if (this->_m_pStr) {
		return *reinterpret_cast<size_t*>(this->_m_pStr - 4);
	}
	return 0;
}
