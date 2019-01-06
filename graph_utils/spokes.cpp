#include <iostream>
using namespace std;

signed main(int argc, char** argv, char** envp)
{
    ios_base::sync_with_stdio(0);
    cin.tie(0);
    cout.tie(0);
    if (argc != 2) {
        cerr << "usage: <binary> <n>\n";
        exit(EXIT_FAILURE);
    }
    int n = atoi(argv[1]);
    for (int i = 0; i < n - 1; i++) {
        cout << i << ' ' << i + 1 << '\n';
    }
    for (int i = 1; i < n; i++) {
        cout << 0 << ' ' << i << '\n';
    }
    cout << n - 1 << ' ' << 0 << '\n';
    return 0;
}
