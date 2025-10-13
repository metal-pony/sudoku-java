package io.github.metal_pony.sudoku.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Contains utility functions pertaining to counting, including
 * functions for calculating permutation and combination sets from
 * given collections of objects.
 */
public class Counting {
	private static List<BigInteger> factMap = new ArrayList<>(Arrays.asList(BigInteger.ONE));

	/**
	 * Computes the factorial of the given number.
	 * Just a reminder because many forget: <code>0! = 1</code>.
	 *
	 * @param n - Number to compute the factorial of.
	 * @return <code>n!</code> as a BigInteger.
	 */
	public static BigInteger factorial(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("n must be nonnegative");
		}

		if (n < factMap.size()) {
			return factMap.get(n);
		}

		int i = factMap.size() - 1;
		BigInteger result = factMap.get(i);

		for (i++; i <= n; i++) {
			result = result.multiply(BigInteger.valueOf(i));
			if (factMap.size() < 1000) factMap.add(result);
		}

		return result;
	}

	/**
	 * Generates a random BigInteger in the interval [0, bound) with the given random generator.
	 */
	public static BigInteger random(BigInteger bound, Random rand) {
		if (bound.compareTo(BigInteger.ZERO) <= 0) {
			throw new IllegalArgumentException("bound must be positive");
		}

		BigInteger result = new BigInteger(bound.bitLength(), rand);
		while (result.compareTo(bound) >= 0) {
			result = new BigInteger(bound.bitLength(), rand);
		}
		return result;
	}

	/**
	 * Computes all permutations of the given list.
	 *
	 * @param <T> Type of object that the list contains.
	 * @param list - Contains items to compute permutations of.
	 * @return Set containing all permutations of the given list of items.
	 */
	public static <T> Set<List<T>> allPermutations(List<T> list) {
		HashSet<List<T>> resultSet = new HashSet<>();
		if (list.size() == 1) {
			resultSet.add(list);
		} else if (list.size() > 1) {
			for (T e : list) {
				List<T> sublist = new ArrayList<>(list);
				sublist.remove(e);
				Set<List<T>> r = allPermutations(sublist);
				for (List<T> x : r) {
					x.add(e);
					resultSet.add(x);
				}
			}
		}
		return resultSet;
	}

	/**
	 * Computes n choose k.
	 */
	public static BigInteger nChooseK(int n, int k) {
		if (n < 0 || k < 0 || n < k) {
			throw new IllegalArgumentException("n and k must both be >= 0 and n must be >= k.");
		}

		if (k == 0 || n == k) {
			return BigInteger.ONE;
		}

		return factorial(n).divide(factorial(k)).divide(factorial(n - k));
	}

	/**
	 * Generates a random combination, choosing k numbers randomly from the interval [0,n).
	 * The following must be true: <code>n >= k >= 0</code>.
	 */
	public static int[] randomCombo(int n, int k) {
		if (n < 0 || k < 0 || n < k) {
			throw new IllegalArgumentException("n and k must both be >= 0 and n must be >= k.");
		}

		if (k == 0) {
			return new int[0];
		}

		int[] items = ArraysUtil.shuffle(ArraysUtil.range(n));
		return Arrays.copyOfRange(items, 0, k);

		// This is an alternate approach in which a combination is generated via an index r.
		// BigInteger r = random(factorial(n), ThreadLocalRandom.current());
		// return combo(n, k, r);
	}

	/**
	 * Generates a subset of k numbers from the interval [0,n), given a number r from [0, n choose k).
	 */
	public static int[] combo(int n, int k, BigInteger r) {
		// validate r
		if (r.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalArgumentException("r must be nonnegative");
		}
		BigInteger nChooseK = nChooseK(n, k);
		if (r.compareTo(nChooseK) >= 0) {
			throw new IllegalArgumentException(
				String.format("r must be in interval [0, n choose k (%s))", nChooseK.toString())
			);
		}

		int[] result = new int[k];

		// Anything choose 0 is 1. There's only one way to choose nothing, i.e. the empty set.
		if (k == 0) {
			return result;
		}

		int _n = n - 1;
		int _k = k - 1;
		BigInteger _r = new BigInteger(r.toString());

		int index = 0;
		for (int i = 0; i < n; i++) {
			BigInteger _nChoose_k = nChooseK(_n, _k);
			if (_r.compareTo(_nChoose_k) < 0) {
				result[index++] = i;
				_k--;

				if (index == k) {
					break;
				}
			} else {
				_r = _r.subtract(_nChoose_k);
			}
			_n--;

		}

		return result;
	}

	/**
	 * Generates a bitstring (length n) with k bits set.
	 * If there are (n choose k) possible bitstrings, this generates the r-th.
	 * Represented as an array of unsigned bytes.
	 */
	public static byte[] bitCombo(int n, int k, BigInteger r) {
		if (r.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalArgumentException("r must be nonnegative");
		}

		// Throws if n < 0 or k < 0 or n < k
		BigInteger nck = nChooseK(n, k);

		if (r.compareTo(nck) >= 0) {
			throw new IllegalArgumentException("r must be in interval [0, (n choose k))");
		}

		BigInteger _r = new BigInteger(r.toByteArray());
		int nBytes = n / Byte.SIZE;
		int remBits = n % Byte.SIZE;
		if (remBits > 0) {
			nBytes++;
		}
		byte[] _result = new byte[nBytes];

		for (int _n = n - 1, _k = k - 1; _n >= 0 && _k >= 0; _n--) {
			BigInteger _nck = nChooseK(_n, _k);
			if (_r.compareTo(_nck) < 0) {
				int arrIndex = nBytes - 1 - (_n / Byte.SIZE);
				int bIndex = _n % Byte.SIZE;
				_result[arrIndex] |= (1 << bIndex);
				_k--;
			} else {
				_r = _r.subtract(_nck);
			}
		}

		return _result;
	}

	public static List<byte[]> allBitCombos(int n, int k) {
		List<byte[]> result = new ArrayList<>();
		BigInteger nck = nChooseK(n, k);
		BigInteger r = BigInteger.ZERO;
		while (r.compareTo(nck) < 0) {
			result.add(bitCombo(n, k, r));
			r = r.add(BigInteger.ONE);
		}
		return result;
	}

	public static BigInteger randomBitCombo(int n, int k) {
		BigInteger r = random(nChooseK(n, k), new Random());
		return new BigInteger(bitCombo(n, k, r));
	}

	/**
	 * Generates a random permutation of the numbers 0 to n.
	 */
	public static int[] randomPermutation(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("n must be nonnegative");
		}

		return ArraysUtil.shuffle(ArraysUtil.range(n));

		// This is an alternate approach in which a permutation is generated via an index r.
		// BigInteger r = random(factorial(n), ThreadLocalRandom.current());
		// return permutation(n, r);
	}

	/**
	 * Generates a permutation of the numbers [0,n), given a number r from [0, n!).
	 */
	public static int[] permutation(int n, BigInteger r) {
		if (n < 0) {
			throw new IllegalArgumentException("n must be nonnegative");
		}

		int[] result = new int[n];
		int[] items = ArraysUtil.range(n);

		for (int i = 0; i < n; i++) {
			r = r.mod(factorial(n - i));
			BigInteger dividend = factorial(n - i - 1);
			int q = r.divide(dividend).intValue();
			result[i] = items[q];

			// Shift items whose index > q to the left one place
			for (int j = q + 1; j < n - i; j++) {
				items[j - 1] = items[j];
			}
		}

		return result;
	}

	// precondition: each element in perms must be unique; and all elements cover integers in [0, perms.length).
	public static BigInteger permToR(int[] perm) {
		return null;
	}

	public static List<int[]> allPermutations(int n) {
		List<int[]> result = new ArrayList<>();

		BigInteger nFact = factorial(n);
		BigInteger r = BigInteger.ZERO;
		while (r.compareTo(nFact) < 0) {
			result.add(permutation(n, r));
			r = r.add(BigInteger.ONE);
		}

		return result;
	}

	public static void forEachPermutation(int n, Consumer<int[]> consumer) {
		BigInteger nFact = factorial(n);
		BigInteger r = BigInteger.ZERO;
		while (r.compareTo(nFact) < 0) {
			consumer.accept(permutation(n, r));
			r = r.add(BigInteger.ONE);
		}
	}

	public static void forEachCombo(int n, int k, Consumer<int[]> consumer) {
		BigInteger nChooseK = nChooseK(n, k);
		BigInteger r = BigInteger.ZERO;
		while (r.compareTo(nChooseK) < 0) {
			consumer.accept(combo(n, k, r));
			r = r.add(BigInteger.ONE);
		}
	}

	/**
	 * Generates the next bit combination (`n choose k`) given the current combo, `r`.
	 * `k` is the number of bits set in `r`.
	 * The result will be the next bit combination sequentilly after r.
	 * If `r` is already the last bit combination, this will 'wrap around'
	 * and return the first bit combination with the lowest-order bits.
	 * @param n - number of bits
	 * @param r -
	 * @returns
	 */
	public static byte[] nextBitCombo(int n, byte[] r) {
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}
		int expectedLength = n/8 + ((n%8)>0 ? 1 : 0);
		if (r.length != expectedLength) {
			throw new IllegalArgumentException(String.format(
				"Unexpected r.length (%d, expected %d for n = %d)",
				r.length, expectedLength, n
			));
		}
		// if (r >= (1n << BigInt(n))) {
		// 	throw new Error("r too large");
		// }

		// Find first '01' scanning right-to-left
		// (If there isn't any, 'r' is already at the max)
		int i = 0;
		while (i < n && ((1<<(i%8)) & r[i/8]) == 0) {
			i++;
		}

		// For the subsequent string of 1s... move them to the lowest bits.
		int j = i;
		while (i < n && ((1<<(i%8)) & r[i/8]) > 0) {
			r[i/8] ^= (1<<(i%8));
			// r[j/8] |= (1<<(j%8));
			i++;
			// j++;
		}
		// Now if i == n, then the bit combo would have 'wrapped around' and started over.
		// Otherwise, i is at the first 0 following the string of 1s that were flipped,
		// which should also now flip to 1.
		if (i < n) {
			r[i/8] ^= (1<<(i%8));
			for (int k = 0; k < i - j - 1; k++) {
				r[k/8] |= (1<<(k%8));
			}
		} else {
			for (int k = 0; k < i - j; k++) {
				r[k/8] |= (1<<(k%8));
			}
		}


		return r;

		// int ri = 0;
		// // let m = 1n;
		// // First, fast-forward past any tailing zeros
		// while (i < n && ((1<<(i%8)) & r[i/8]) == 0) {
		// 	i++;
		// 	// m <<= 1n;
		// }
		// int tailZeros = i;

		// // Then past the string of 1s
		// while (i < n && ((1<<(i%8)) & r[i/8]) > 0) {
		// 	i++;
		// 	// m <<= 1n;
		// }

		// At max? Then wrap around to beginning.
		// if (i >= n) {
		// 	int nBits = n - tailZeros;
		// 	i = 0;
		// 	for (i = 0; i < r.length; i++) {
		// 		int bitsToSet = (nBits >= 8) ? 8 : nBits;
		// 		nBits -= bitsToSet;
		// 		r[i] = (bitsToSet > 0) ? (byte)((1<<bitsToSet) - 1) : 0;
		// 	}
		// 	return r;
		// 	// return (1 << BigInt(nBits)) - 1;
		// }

		// // Set the bit at i
		// // Unset the bits to the right of i
		// r[i/8] |= (1<<(i%8));
		// r &= (((1 << n) - 1) - (m - 1));
		// // r += ((m >> BigInt(tailZeros + 1)) - 1n);

		// return r;
	}
}
