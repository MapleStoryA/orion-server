#include <string>


using namespace std;

class CLog {

private:
	string *filePath;
public:
	CLog(const char *file) {
		remove(file);
		this->filePath = new string(file);
	}
	~CLog() {
		delete filePath;
	}

	void Log(string format, ...);
};