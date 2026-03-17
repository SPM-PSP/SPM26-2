#include <iostream>
#include <fstream>
#include <string>
#include <cstdlib>
#include <cstdio>
using namespace std;

// 退出码定义 (SpringBoot OJ 依据此判断结果)
#define STATUS_AC     0   // Accepted
#define STATUS_FILE   1   // File/Argument Error
#define STATUS_CE     2   // Compile Error
#define STATUS_RE     3   // Runtime Error
#define STATUS_WA     4   // Wrong Answer

// 判断文件是否存在
bool fileExists(const string& filename) {
    ifstream file(filename);
    return file.good();
}

// 编译C++代码 (Windows)
bool compileCode(const string& cppFile, const string& execFile) {
    string cmd = "g++ " + cppFile + " -o " + execFile + ".exe -w";
    return system(cmd.c_str()) == 0;
}

// 运行程序，重定向输入输出
bool runCode(const string& execFile, const string& inFile, const string& tempOut) {
    string cmd = execFile + ".exe < " + inFile + " > " + tempOut;
    return system(cmd.c_str()) == 0;
}

// 对比输出结果（忽略行尾空格）
bool compareOutput(const string& tempOut, const string& ansFile) {
    ifstream temp(tempOut);
    ifstream ans(ansFile);
    
    if (!temp.is_open() || !ans.is_open()) {
        return false;
    }

    string tempLine, ansLine;
    while (getline(temp, tempLine) && getline(ans, ansLine)) {
        // 去除行尾空白字符
        tempLine.erase(tempLine.find_last_not_of(" \t\n\r") + 1);
        ansLine.erase(ansLine.find_last_not_of(" \t\n\r") + 1);
        
        if (tempLine != ansLine) {
            temp.close();
            ans.close();
            return false;
        }
    }

    // 检查是否有多余行
    bool result = !getline(temp, tempLine) && !getline(ans, ansLine);
    temp.close();
    ans.close();
    return result;
}

// 清理临时文件
void cleanFiles(const string& execFile, const string& tempOut) {
    remove((execFile + ".exe").c_str());
    remove(tempOut.c_str());
}

int main(int argc, char* argv[]) {
    const string TEMP_EXEC = "temp_exe";
    const string TEMP_OUTPUT = "temp_result.txt";

    // 参数校验
    if (argc != 4) {
        cout << "[ERROR] Invalid arguments" << endl;
        return STATUS_FILE;
    }

    string codeFile = argv[1];
    string inputFile = argv[2];
    string ansFile = argv[3];

    // 文件存在性校验
    if (!fileExists(codeFile) || !fileExists(inputFile) || !fileExists(ansFile)) {
        cout << "[ERROR] File not found" << endl;
        cleanFiles(TEMP_EXEC, TEMP_OUTPUT);
        return STATUS_FILE;
    }

    // 1. 编译代码
    if (!compileCode(codeFile, TEMP_EXEC)) {
        cout << "[RESULT] Compile Error (CE)" << endl;
        cleanFiles(TEMP_EXEC, TEMP_OUTPUT);
        return STATUS_CE;
    }

    // 2. 运行代码
    if (!runCode(TEMP_EXEC, inputFile, TEMP_OUTPUT)) {
        cout << "[RESULT] Runtime Error (RE)" << endl;
        cleanFiles(TEMP_EXEC, TEMP_OUTPUT);
        return STATUS_RE;
    }

    // 3. 对比答案
    if (compareOutput(TEMP_OUTPUT, ansFile)) {
        cout << "[RESULT] Accepted (AC)" << endl;
        cleanFiles(TEMP_EXEC, TEMP_OUTPUT);
        return STATUS_AC;
    } else {
        cout << "[RESULT] Wrong Answer (WA)" << endl;
        cleanFiles(TEMP_EXEC, TEMP_OUTPUT);
        return STATUS_WA;
    }
}