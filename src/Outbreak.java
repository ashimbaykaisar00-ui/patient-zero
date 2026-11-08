
/**
 * Mission 5. Dynamic programming on the tree of infections. Root the tree in person 0.
 * Input.Tree.neighbours() gives the neighbours of every person. The number of people is at most about 100, so a recursive DFS is fine here.
 */
public class Outbreak {

    /** parent[v] and the order of people from the root down (BFS). parent[0] = -1. */
    private static int[] bfsOrder(int n, int[][] nb, int[] parent) {
        int[] order = new int[n];
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = 0; seen[0] = true; parent[0] = -1;
        while (head < tail) {
            int v = order[head++];
            for (int u : nb[v]) {
                if (!seen[u]) { seen[u] = true; parent[u] = v; order[tail++] = u; }
            }
        }
        return order;
    }

    /**
     * Choose people to test so that for every link (somebody infected somebody) at least one of the two is tested,
     * and the total cost of testing is as small as possible. cost[v] is the price of testing person v.
     * Returns tested[v] = true if person v is tested.
     *
     * in[v]  = the smallest cost of the subtree of v when v IS tested  (each child is free to be either)
     * out[v] = the smallest cost of the subtree of v when v is NOT tested (every child must be tested)
     */
    public static boolean[] cheapestTests(int n, int[][] nb, int[] cost) {
        boolean[] tested = new boolean[n];
        if (n == 0) return tested;
        int[] parent = new int[n];
        int[] order = bfsOrder(n, nb, parent);
        long[] in = new long[n], out = new long[n];
        for (int v = 0; v < n; v++) in[v] = cost[v];       // add the children below
        // from the leaves up: the reversed BFS order visits every child before its parent
        for (int idx = n - 1; idx >= 1; idx--) {
            int c = order[idx], p = parent[c];
            in[p] += Math.min(in[c], out[c]);
            out[p] += in[c];
        }
        // rebuild: go down from the root
        tested[0] = in[0] <= out[0];
        for (int idx = 1; idx < n; idx++) {
            int c = order[idx], p = parent[c];
            if (!tested[p]) tested[c] = true;               // parent not tested: child must be
            else tested[c] = in[c] <= out[c];               // parent tested: take the cheaper state
        }
        return tested;
    }

    /**
     * The number of links on the longest chain of infections, that is the longest path in the tree (counted in links).
     * A path goes up from one person and down to another; for every person you need the two longest ways down.
     * Example: a line 0-1-2-3 has chain 3; a star with centre 0 and leaves 1, 2, 3 has chain 2.
     */
    public static int longestChain(int n, int[][] nb) {
        if (n <= 1) return 0;
        int[] parent = new int[n];
        int[] order = bfsOrder(n, nb, parent);
        int[] best1 = new int[n], best2 = new int[n]; // two longest ways down (in links) from v
        int answer = 0;
        for (int idx = n - 1; idx >= 0; idx--) {
            int v = order[idx];
            answer = Math.max(answer, best1[v] + best2[v]); // the best chain whose top is v
            if (idx == 0) break;
            int p = parent[v];
            int len = best1[v] + 1;                         // way down from p through v
            if (len > best1[p]) { best2[p] = best1[p]; best1[p] = len; }
            else if (len > best2[p]) best2[p] = len;
        }
        return answer;
    }
}