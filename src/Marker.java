import java.util.*;

/**
 * Missions 3 and 4. Find a word in a long text.
 */
public class Marker {

    /**
     * Mission 3a. The prefix function of p. pi[i] = the length of the longest proper prefix of p[0..i] that is
     * also a suffix of p[0..i] ("proper" means shorter than p[0..i] itself).
     * Example: p = "ACGAC" gives [0, 0, 0, 1, 2]. The last number is the "Border length" line.
     */
    public static int[] prefixFunction(String p) {
        int m = p.length();
        int[] pi = new int[m];
        int k = 0; // length of the current border
        for (int i = 1; i < m; i++) {
            while (k > 0 && p.charAt(i) != p.charAt(k)) k = pi[k - 1]; // shorten the border
            if (p.charAt(i) == p.charAt(k)) k++;                       // extend the border
            pi[i] = k;
        }
        return pi;
    }

    /**
     * Mission 3b. KMP. All start positions (counting from 0, in increasing order) where p occurs in text.
     * Overlapping occurrences count: "ACG" occurs in "ACGACG" at 0 and 3, and "AA" in "AAA" at 0 and 1.
     * Use the prefix function: the text pointer never goes back. The number of positions is "Marker hits",
     * the first one is "First hit" (-1 when there is none).
     */
    public static int[] findAll(String text, String p) {
        int n = text.length(), m = p.length();
        if (m == 0 || m > n) return new int[0];
        int[] pi = prefixFunction(p);
        int[] buf = new int[n];
        int cnt = 0;
        int k = 0; // how many letters of p are matched now
        for (int i = 0; i < n; i++) {          // i only goes forward
            while (k > 0 && text.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (text.charAt(i) == p.charAt(k)) k++;
            if (k == m) {                       // full match ending at i
                buf[cnt++] = i - m + 1;
                k = pi[k - 1];                  // not 0: the next match may overlap this one
            }
        }
        return Arrays.copyOf(buf, cnt);
    }

    private static final long MOD = 1_000_000_007L;
    private static final long BASE = 131L;

    private static int code(char c) {
        switch (c) {
            case 'A': return 1;
            case 'C': return 2;
            case 'G': return 3;
            default:  return 4; // T
        }
    }

    /**
     * Mission 4. Rabin-Karp. The number of positions i in a such that the word of k letters starting at i
     * (a[i..i+k-1]) also occurs somewhere in b. Count positions, not different words. This is the "Shared words" line.
     * The idea: give every window of k letters a number (a rolling hash, updated in O(1) when the window moves by one),
     * look the number up among the windows of b, and when it is found COMPARE THE LETTERS, because two different words
     * can have the same hash.
     * Example: a = "ACGTAC", b = "TTACGT", k = 4: the words of a are ACGT, CGTA, GTAC; only ACGT is in b, so the answer is 1.
     */
    public static int sharedWords(String a, String b, int k) {
        int n = a.length(), m = b.length();
        if (k <= 0 || n < k || m < k) return 0;

        long top = 1;                                    // BASE^(k-1) mod MOD
        for (int i = 1; i < k; i++) top = top * BASE % MOD;

        // 1. rolling hashes of all windows of b -> list of start positions with this hash
        Map<Long, List<Integer>> windowsOfB = new HashMap<>();
        long h = 0;
        for (int i = 0; i < k; i++) h = (h * BASE + code(b.charAt(i))) % MOD;
        windowsOfB.computeIfAbsent(h, x -> new ArrayList<>()).add(0);
        for (int s = 1; s + k <= m; s++) {
            h = (h - code(b.charAt(s - 1)) * top % MOD + MOD) % MOD; // remove the old letter (+MOD: no negative)
            h = (h * BASE + code(b.charAt(s + k - 1))) % MOD;          // shift and add the new letter
            windowsOfB.computeIfAbsent(h, x -> new ArrayList<>()).add(s);
        }

        // 2. slide over a, look up the hash, compare the letters
        int count = 0;
        h = 0;
        for (int i = 0; i < k; i++) h = (h * BASE + code(a.charAt(i))) % MOD;
        for (int s = 0; s + k <= n; s++) {
            if (s > 0) {
                h = (h - code(a.charAt(s - 1)) * top % MOD + MOD) % MOD;
                h = (h * BASE + code(a.charAt(s + k - 1))) % MOD;
            }
            List<Integer> candidates = windowsOfB.get(h);
            if (candidates == null) continue;
            for (int t : candidates) {
                if (sameLetters(a, s, b, t, k)) { count++; break; } // count the position once
            }
        }
        return count;
    }

    private static boolean sameLetters(String a, int s, String b, int t, int k) {
        for (int i = 0; i < k; i++) if (a.charAt(s + i) != b.charAt(t + i)) return false;
        return true;
    }
}