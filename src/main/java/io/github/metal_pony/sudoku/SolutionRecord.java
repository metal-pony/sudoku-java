package io.github.metal_pony.sudoku;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;


/**
 * A SolutionRecord contains a Sudoku solution and its fingerprints.
 */
public class SolutionRecord {
    Sudoku solution;
    Map<String,String> fingerprints;

    /**
     * Creates a new SolutionRecord with the given sudoku string.
     * @param str Sudoku string.
     */
    public SolutionRecord(String str) {
        str = Sudoku.conformGridStr(str);
        if (str == null) throw new IllegalArgumentException("Malformed solution.");
        this.solution = new Sudoku(str);
        if (!solution.isSolved()) throw new IllegalArgumentException("Invalid solution.");
        fingerprints = new HashMap<>();
    }

    /**
     * Creates a new SolutionRecord with the given sudoku string and a map of
     * its fingerprints.
     * @param str Sudoku string.
     * @param fingerprints Map of fingerprint name -> hash.
     */
    public SolutionRecord(String str, Map<String,String> fingerprints) {
        this(str);
        this.fingerprints.putAll(fingerprints);
    }

    @Override
    public String toString() {
        return solution.toString();
    }

    /**
     * Gets a copy of the solution.
     * @return Sudoku solution.
     */
    public Sudoku solution() { return new Sudoku(solution); }

    /**
     * Gets the dc2 fingerprint. The fingerprints map will be checked first,
     * and if the hash doesn't exist yet, it will be generated.
     * @return dc2 fingerprint.
     */
    public String dc2() {
        String dc2 = fingerprints.get("dc2");
        if (dc2 == null) {
            dc2 = solution.dc2();
            fingerprints.put("dc2", solution.dc2());
        }
        return dc2;
    }

    /**
     * Gets the dc3 fingerprint. The fingerprints map will be checked first,
     * and if the hash doesn't exist yet, it will be generated.
     * @return dc3 fingerprint.
     */
    public String dc3() {
        String dc3 = fingerprints.get("dc3");
        if (dc3 == null) {
            dc3 = solution.dc3();
            fingerprints.put("dc3", solution.dc3());
        }
        return dc3;
    }

    /**
     * Gets the dc4 fingerprint. The fingerprints map will be checked first,
     * and if the hash doesn't exist yet, it will be generated.
     * @return dc4 fingerprint.
     */
    public String dc4() {
        String dc4 = fingerprints.get("dc4");
        if (dc4 == null) {
            dc4 = solution.dc4();
            fingerprints.put("dc4", solution.dc4());
        }
        return dc4;
    }

    /**
     * Gets the fp2 fingerprint. The fingerprints map will be checked first,
     * and if the hash doesn't exist yet, it will be generated.
     * @return fp2 fingerprint.
     */
    public String fp2() {
        String fp2 = fingerprints.get("fp2");
        if (fp2 == null) {
            fp2 = solution.fp2();
            fingerprints.put("fp2", solution.fp2());
        }
        return fp2;
    }

    /**
     * Gets the fp3 fingerprint. The fingerprints map will be checked first,
     * and if the hash doesn't exist yet, it will be generated.
     * @return fp3 fingerprint.
     */
    public String fp3() {
        String fp3 = fingerprints.get("fp3");
        if (fp3 == null) {
            fp3 = solution.fp3();
            fingerprints.put("fp3", solution.fp3());
        }
        return fp3;
    }

    /**
     * Gets the fp4 fingerprint. The fingerprints map will be checked first,
     * and if the hash doesn't exist yet, it will be generated.
     * @return fp4 fingerprint.
     */
    public String fp4() {
        String fp4 = fingerprints.get("fp2");
        if (fp4 == null) {
            fp4 = solution.fp2();
            fingerprints.put("fp4", solution.fp2());
        }
        return fp4;
    }

    /**
     * Gets a JSON string representation for this record.
     * @return JSON string representation.
     */
    public String toJson() {
        return (new Gson()).toJson(this);
        // StringBuilder strb = new StringBuilder();
        // if (!fingerprints.isEmpty()) {
        //     fingerprints.forEach((key, val) -> {
        //         strb.append(String.format("  \"%s\": \"%s\",\n", key, val));
        //     });
        // }

        // return String.format(
        //     """
        //     {
        //       "solution": "%s",
        //     %s
        //     }
        //     """,
        //     toString(),
        //     strb.toString()
        // );
    }
}
