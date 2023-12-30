#pragma once

template <class T>
class ZXString
{
	

public:

	T* str_;
	ZXString() : str_(nullptr) { }

	ZXString(const T* s, int n)
	{
		Assign(s, n);
	}

	explicit ZXString(const std::string& text) : ZXString(text.data(), text.length() + 1) { }

	~ZXString()
	{
		delete[] str_;
	}

	operator const T* ()
	{
		return str_;
	}

	ZXString<T>& operator=(ZXString<T> str)
	{
		delete[] str_;
		str_ = str.str_;
		return *this;
	}

	void Assign(const T* s, size_t lenght = -1)
	{
		if (s) {
			delete[] str_;

			if (lenght == -1) lenght = std::strlen(s) + 1;

			T* data = new char[lenght + 4];
			*reinterpret_cast<size_t*>(data) = lenght;
			str_ = &data[4];
			std::strcpy(str_, s);
		}
	}

	size_t GetLength()
	{
		if (str_) return *reinterpret_cast<size_t*>(str_ - 4);
		return 0;
	}
};

static_assert(sizeof(ZXString<char>) == 4, "sizeof(ZXString<char> != 4");