#include <iostream>
using namespace std;

signed main(int argc, char** argv, char** envp)
{
    ios_base::sync_with_stdio(0);
    cin.tie(0);
    cout.tie(0);
    if (argc != 3) {
        cerr << "usage: <binary> <n> <m>\n";
        exit(EXIT_FAILURE);
    }
    int n = atoi(argv[1]);
    int m = atoi(argv[2]);

    // n - number of nodes in tail graph
    // m - number of nodes in connected graph
    for (int i = 0; i < n; i++) {
        cout << i << ' ' << i + 1 << '\n';
    }
    for (int i = n + 1; i < n + m; i++) {
            cout << n << ' ' << i << '\n';
            if (i + 1 != n + m) {
                cout << i << ' ' << i + 1 << '\n';
            }
    }
    cout << n + m - 1 << ' ' << n << '\n';
    return 0;
}
