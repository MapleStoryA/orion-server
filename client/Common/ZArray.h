#pragma once
#include <initializer_list>
#include <vector>

template <typename T>
class ZArray
{
	T* a;

public:
	ZArray() : a(nullptr) {}

	ZArray(const T* data, size_t count)
	{
		assign(data, count);
	}

	ZArray& operator=(const ZArray& that)
	{
		if (this == &that) {
			clear();

			const size_t size = bytes_size(that.count());
			auto ptr = new char[size];
			a = &ptr[4];

			std::memcpy(ptr, reinterpret_cast<void*>(that.a) - 1, size);
		}
		return *this;
	}

	ZArray(const ZArray& that) : ZArray(that.a, that.count()) { }

	explicit ZArray(const std::vector<T>& data) : ZArray(data.data(), data.size()) { }

	explicit ZArray(std::initializer_list<T> list) : ZArray(list.begin(), list.size()) { }

	void assign(T* data, size_t length)
	{
		if (data != nullptr && length > 0) {
			clear();

			const size_t size = bytes_size(length);
			char* ptr = new char[size];

			*reinterpret_cast<size_t*>(ptr) = length;
			a = reinterpret_cast<T*>(&ptr[4]);
			std::memcpy(a, data, size);
		}
	}

	~ZArray()
	{
		clear();
	}

	T& operator[](int index)
	{
		return (T&)*a[index];
	}

	static size_t bytes_size(size_t count)
	{
		return sizeof(T) * count + sizeof(count);
	}

	size_t count() const
	{
		if (a == nullptr) return 0;
		return *(reinterpret_cast<size_t*>(a) - 1);
	}

	void clear()
	{
		if (a) {
			delete[](reinterpret_cast<uint8_t*>(a) - sizeof(count()));
		}
		a = nullptr;
	}

	// required by range based for loops
	T* begin()
	{
		return a;
	}

	T* end()
	{
		return &a[count()];
	}
};