#include "ClientApp.h"

ClientApp* App = new ClientApp();


ClientApp *ClientApp::getInstance() {
	return App;
}

void ClientApp::AddLogMessage(std::string message) {
	SendMessage(this->hwndList, LB_ADDSTRING, 0, (LPARAM)message.c_str());
}


LRESULT CALLBACK ClientApp::WindowEventProcedure(HWND hWnd, UINT msg, WPARAM wp, LPARAM lp) {
	switch (msg) {
	case WM_CREATE:
		App->hwndList = CreateWindowW(L"LISTBOX", NULL, WS_CHILD | WS_VISIBLE | WS_VSCROLL, 10, 10, 460, 430, hWnd, (HMENU)1, NULL, NULL);
		break;
	case WM_DESTROY:
		PostQuitMessage(0);
		break;
	case WM_USER + 1:
		App->AddLogMessage((char*)lp);
		break;
	default:
		return DefWindowProcW(hWnd, msg, wp, lp);
	}
	return 0;
}


LRESULT CALLBACK ClientApp::WindowProcedure(HWND hWnd, UINT msg, WPARAM wp, LPARAM lp)
{
	
	switch (msg)
	{
	case WM_CREATE:
		App->hwndList = CreateWindowW(L"LISTBOX", NULL, WS_CHILD | WS_VISIBLE | WS_VSCROLL, 10, 10, 460, 430, hWnd, (HMENU)1, NULL, NULL);
		break;
	case WM_DESTROY:
		PostQuitMessage(0);
		break;
	default:
		return DefWindowProcW(hWnd, msg, wp, lp);
	}

	return 0;
}


void ClientApp::Setup(HMODULE hInst) {
	WNDCLASSW wc = { 0 };

	wc.hbrBackground = (HBRUSH)COLOR_WINDOW;
	wc.hCursor = LoadCursor(NULL, IDC_ARROW);
	wc.hInstance = hInst;
	wc.lpszClassName = L"LogWindowClass";
	wc.lpfnWndProc = ClientApp::WindowEventProcedure;

	RegisterClassW(&wc);

	hwndMain = CreateWindowW(L"ClientAppClass", L"Orion Server Logs", WS_OVERLAPPEDWINDOW | WS_VISIBLE, 100, 100, 500, 500, NULL, NULL, hInst, NULL);
}