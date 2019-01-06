#include "bits/stdc++.h"
using namespace std;

vector<int> g;

int main(int argv, char** argc) {
    int n = 0;
    int a, b;
    int start;
    while ((cin >> a) and (cin >> b)) {
        n = max(n, a + 1); n = max(n, b + 1);
        g.resize(n);
        g[a] += 1;
    }

    int max_deg = 0;
    int max_deg_node = -1;

    for (int i = 0; i < n; i++) {
        if (max_deg < g[i]) {
            max_deg = g[i];
            max_deg_node = i;
        }
    }

    cout << "Degree of node 0 is: " << g[0] << '\n';
    cout << "Maximum degree is: " << max_deg << " of " << max_deg_node << '\n';

    return 0;
}

