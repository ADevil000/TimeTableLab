#include <iostream>
#include <algorithm>
#include <vector>
#include <unordered_set>
#include <fstream>
#include <unordered_map>

using namespace std;

struct Job {
    Job(int i, int i1) {
        d = i;
        ind = i1;
    }

    int d, ind;

    bool operator < (const Job& o) const {
        if (d == o.d) {
            return ind < o.ind;
        } else {
            return d < o.d;
        }
    }

    bool operator == (const Job& o) const {
        return d == o.d && ind == o.ind;
    }

};

template<>
struct std::hash<Job>
{
    std::size_t operator()(Job const& s) const noexcept
    {
        std::size_t h1 = std::hash<int>{}(s.d);
        std::size_t h2 = std::hash<int>{}(s.ind);
        return h1 ^ (h2 << 1); // or use boost::hash_combine
    }
};

template<>
struct std::hash<pair<int, int>>
{
    std::size_t operator()(pair<int, int> const& s) const noexcept
    {
        std::size_t h1 = std::hash<int>{}(s.first);
        std::size_t h2 = std::hash<int>{}(s.second);
        return h1 ^ (h2 << 1); // or use boost::hash_combine
    }
};

unordered_map<int, Job> numToJob;
unordered_map<Job, int> jobToNum;
unordered_map<int, int> numToTime, timeToNum;
unordered_map<pair<int, int>, unordered_set<int>> fakeEdgesInd;

bool dfs(int v, vector<bool>& was, vector<int>& matching, vector<unordered_set<pair<int, int>>>& edgesNum) {
    if (was[v]) {
        return false;
    }
    was[v] = true;
    for (auto edge : (edgesNum)[v]) {
        int u = edge.first;
        if (matching[u] == -1 || dfs(matching[u], was, matching, edgesNum)) {
            matching[u] = v;
            return true;
        }
    }
    return false;
}

void createTable(vector<Job>& jobs, int k, int m, int dMax, vector<unordered_set<Job>>& table) {
    for (int i = 0; i < dMax; i++) {
        unordered_set<Job> tmp;
        table.emplace_back(tmp);
    }
    for (int i = (int) jobs.size() - 1; i > ((int) jobs.size() - 1) - k; i--) {
        Job job = jobs[i];
        for (int j = 1; j <= m; j++) {
            unordered_set<Job>& set1 = table[min(job.d, dMax) - j];
            set1.insert(job);
        }
    }
    for (int i = (int) table.size() - 1; i >= 0; i--) {
        unordered_set<Job>& set1 = table[i];
        while (set1.size() > m) {
            for (int ind = i - 1; ind >= 0 && set1.size() > m; ind--) {
                unordered_set<Job>& earlierSet = table[ind];
                while (set1.size() > m && earlierSet.size() < m) {
                    Job* swaped;
                    for (Job tmp : set1) {
                        if (earlierSet.end() == earlierSet.find(tmp)) {
                            swaped = &tmp;
                            earlierSet.insert(tmp);
                            break;
                        }
                    }
                    set1.erase(*swaped);
                }
            }
        }
    }
}

bool checkForK(vector<Job> &deadlines, int k, int m, int maxTime) {
    if (k == 0) {
        return true;
    }
    if (maxTime - m < 0) {
        return false;
    }
    vector<int> added(maxTime, 0);
    for (int i = (int) deadlines.size() - 1; i > ((int) deadlines.size() - 1) - k; i--) {
        int pos = min(deadlines[i].d, maxTime);
        for (int j = 1; j <= m; j++) {
            // ind always >= 0
            added[pos - j]++;
        }
    }
    int h = 0;
    for (int i = maxTime - 1; i >= 0; i--) {
        if (added[i] + h > m) {
            h = added[i] + h - m;
        } else {
            h = 0;
        }
    }
    return h == 0;
}

int findK(vector<Job> &deadlines, int m, int maxTime) {
    int left = 0;
    int right = deadlines.size() + 1;
    while (right - left > 1) {
        int med = (left + right) / 2;
        if (checkForK(deadlines, med, m, maxTime)) {
            left = med;
        } else {
            right = med;
        }
    }
    return (left + right) / 2;
}

void createGraph(vector<unordered_set<Job>>& table, vector<Job> jobs, int k, int m, int dMax, vector<unordered_set<pair<int, int>>>& graph) {
    int graphPos = 0;
    for (int i = (int) jobs.size() - 1; i > ((int) jobs.size() - 1) - k; i--) {
        unordered_set<pair<int, int>> tmp;
        graph.emplace_back(tmp);
        numToJob.insert({ graphPos, jobs[i]});
        jobToNum.insert({jobs[i], graphPos});
        graphPos++;
    }
    for (int i = 0; i < dMax; i++) {
        unordered_set<pair<int, int>> tmp;
        graph.emplace_back(tmp);
        numToTime.insert({graphPos, i});
        timeToNum.insert({i, graphPos});
        graphPos++;
    }
    int startFake = graphPos;
    for (int i = 1; i <= dMax - k; i++) {
        unordered_set<pair<int, int>> tmp;
        graph.emplace_back(tmp);
        Job job(-1 ,-i);
        numToJob.insert({graphPos, job});
        jobToNum.insert({job, graphPos});
        graphPos++;
    }
    for (int i = 0; i < table.size(); i++) {
        const unordered_set<Job>& time = (table)[i];
        int right = timeToNum[i];
        for (auto job : time) {
            int left = jobToNum[job];
            (graph[left]).insert({right, 0});
            (graph[right]).insert({left, 0});
        }
        int freeFake = startFake;
        while ((graph[right]).size() < m) {
            int x = 0;
            while (freeFake < graph.size() && (graph[freeFake]).size() < m && (graph[right]).size() < m) {
                auto found = fakeEdgesInd.find({right, freeFake});
                if (found != fakeEdgesInd.end()) {
                    found->second.insert(x);
                } else {
                    fakeEdgesInd.insert({{right, freeFake}, {x}});
                }
                graph[freeFake].insert({right, x});
                graph[right].insert({freeFake, x});
                x++;
            }
            freeFake++;
        }
    }
}

void findParSoch(vector<unordered_set<pair<int, int>>>& edgesNum, int k, bool last, vector<pair<int, int>>& res) {
    vector<int> matching(edgesNum.size(), -1);
    for (auto entry : numToJob) {
        vector<bool> was(edgesNum.size(), false);
        dfs(entry.first, was, matching, edgesNum);
    }
    for (auto entry : numToTime) {
        int left = matching[entry.first];
        int right = entry.first;
        if (last) {
            Job job = numToJob.find(left)->second;
            if (job.ind >= 0) {
                int time = numToTime[right] + 1;
                res.emplace_back(job.ind, time);
            }
        } else {
            int delK = 0;
            Job job = numToJob.find(left)->second;
            if (job.ind < 0) {
                unordered_set<int>& lol = fakeEdgesInd[{right, left}];
                for (int x : lol) {
                    if ((edgesNum)[left].end() != (edgesNum)[left].find({right, x})) {
                        delK = x;
                        break;
                    }
                }
                lol.erase(delK);
            } else {
                int time = numToTime[right] + 1;
                res.emplace_back(job.ind, time);
            }
            (edgesNum)[right].erase({left, delK});
            (edgesNum)[left].erase({right, delK});
        }
    }
}

int main() {
    ios_base::sync_with_stdio( 0);
    ifstream cin ("furniture.in");
    ofstream cout ("furniture.out");
    int n, m, v;
    cin >> n >> m >> v;
    vector<Job> jobs;
    for (int i = 0; i < n; i++) {
        int d;
        cin >> d;
        jobs.emplace_back(d, i);
    }
    sort(jobs.begin(), jobs.end());
    int maxTime = min(jobs.back().d, n * m);
    int possibleComplete = findK(jobs, m, maxTime);
    vector<unordered_set<Job>> table;
    createTable(jobs, possibleComplete, m, maxTime, table);
    vector<unordered_set<pair<int, int>>> edgesNum;
    createGraph(table, jobs, possibleComplete, m, maxTime, edgesNum);
    vector<vector<pair<int, int>>> machines;
    for (int i = 0; i < m; i++) {
        vector<pair<int, int>> tmp;
        findParSoch(edgesNum, possibleComplete, i + 1 == m, tmp);
        machines.emplace_back(tmp);
    }
    vector<vector<int>> schedule(n, vector<int>());
    for (vector<pair<int, int>>& machine : machines) {
        for (pair<int, int>& p : machine) {
            schedule[p.first].emplace_back(p.second);
        }
    }
    int time = maxTime + 1;
    for (vector<int>& times : schedule) {
        if (times.size() == 0) {
            for (int i = 0; i < m; i++) {
                times.emplace_back(time++);
            }
        }
    }
    cout << v * (n - possibleComplete) << endl;
    for (vector<int>& list : schedule) {
        for (int i : list) {
            cout << i << " ";
        }
        cout << endl;
    }
    return 0;
}