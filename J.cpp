#include <iostream>
#include <algorithm>
#include <vector>
#include <fstream>


using namespace std;

int main() {
    ifstream cin ("p1p1sumu.in");
    ofstream cout ("p1p1sumu.out");
    long long d1, d2, a, b, c, d;
    int n;
    cin >> n >> d1 >> d2 >> a >> b >> c >> d;
    a %= d;
    b %= d;
    c %= d;
    vector<int> deadlines;
    deadlines.push_back((int) d1);
    deadlines.push_back((int) d2);
    for (int i = 2; i < n; i++) {
        long long dead = (a * d1 + b * d2 + c) % d;
        d1 = d2;
        d2 = dead;
        deadlines.push_back((int) dead);
    }
    int time = 0;
    sort(deadlines.begin(), deadlines.end());
    for (auto num : deadlines) {
        if (num > time) {
            time++;
        }
    }
    cout << time;
    return 0;
}