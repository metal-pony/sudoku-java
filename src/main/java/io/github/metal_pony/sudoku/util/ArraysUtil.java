package io.github.metal_pony.sudoku.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Additional array and list utilities.
 */
public class ArraysUtil {
    /**
     * Returns a copy of the given array.
     * @param source Array to copy.
     * @return A copy of the array.
     */
    public static int[] copy(int[] source) {
        int[] arr = new int[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /**
     * Returns a copy of the given array.
     * @param source Array to copy.
     * @return A copy of the array.
     */
    public static long[] copy(long[] source) {
        long[] arr = new long[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /**
     * Returns a copy of the given array.
     * @param source Array to copy.
     * @return A copy of the array.
     */
    public static float[] copy(float[] source) {
        float[] arr = new float[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /**
     * Returns a copy of the given array.
     * @param source Array to copy.
     * @return A copy of the array.
     */
    public static double[] copy(double[] source) {
        double[] arr = new double[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /**
     * Returns a copy of the given array.
     * @param source Array to copy.
     * @return A copy of the array.
     */
    public static boolean[] copy(boolean[] source) {
        boolean[] arr = new boolean[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /**
     * Creates a new array with sequential elements from 0 to n, exclusive.
     * @param n Number of elements to create.
     * @return New array containing elements from 0 to n.
     */
    public static int[] range(int n) {
        int[] arr = new int[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /**
     * Creates a new array with sequential elements from 0 to n, exclusive.
     * @param n Number of elements to create.
     * @return New array containing elements from 0 to n.
     */
    public static long[] rangeLong(int n) {
        long[] arr = new long[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /**
     * Creates a new array with sequential elements from 0 to n, exclusive.
     * @param n Number of elements to create.
     * @return New array containing elements from 0 to n.
     */
    public static float[] rangeFloat(int n) {
        float[] arr = new float[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /**
     * Creates a new array with sequential elements from 0 to n, exclusive.
     * @param n Number of elements to create.
     * @return New array containing elements from 0 to n.
     */
    public static double[] rangeDouble(int n) {
        double[] arr = new double[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /**
     * Creates a new array with sequential elements from 0 to n, exclusive.
     * @param n Number of elements to create.
     * @return New array containing elements from 0 to n.
     */
    public static List<Integer> rangeList(int n) {
        List<Integer> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) list.add(i);
		return list;
    }

    /**
     * Fills the array with random numbers between origin and bound (exclusive).
     * @param arr Array to be filled.
     * @param origin Lower bound for generated array values.
     * @param bound Upper bound (exclusive) for generated array values.
     * @return The given array, for convenience.
     */
    public static int[] randoms(int[] arr, int origin, int bound) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = rand.nextInt(origin, bound);
		}
		return arr;
    }

    /**
     * Fills the array with random numbers between 0 and bound (exclusive).
     * @param arr Array to be filled.
     * @param bound Upper bound (exclusive) for generated array values.
     * @return The given array, for convenience.
     */
    public static int[] randoms(int[] arr, int bound) {
        return randoms(arr, 0, bound);
    }

    /**
     * Returns a random element from the array.
     * @param arr Array to choose an element from.
     * @return Random element chosen from array.
     */
    public static int chooseRandom(int[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /**
     * Chooses a specified number of random integers from the given array.
     * @param arr Array of integers to choose from.
     * @param amount Number of items to choose.
     * @return A new array containing the choices.
     */
    public static int[] chooseRandom(int[] arr, int amount) {
        if (amount < 0) throw new RuntimeException("Amount is negative");
        if (amount > arr.length) throw new RuntimeException("Amount is more than array length");

        int[] choices = Counting.randomCombo(arr.length, amount);
        for (int i = 0; i < choices.length; i++) {
            choices[i] = arr[choices[i]];
        }
        return choices;
    }

    /**
     * Returns a random element from the array.
     * @param arr Array to choose an element from.
     * @return Random element chosen from array.
     */
    public static long chooseRandom(long[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /**
     * Returns a random element from the array.
     * @param arr Array to choose an element from.
     * @return Random element chosen from array.
     */
    public static float chooseRandom(float[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /**
     * Returns a random element from the array.
     * @param arr Array to choose an element from.
     * @return Random element chosen from array.
     */
    public static double chooseRandom(double[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /**
     * Returns a random element from the array.
     * @param <T> Array element type.
     * @param arr Array to choose an element from.
     * @return Random element chosen from array.
     */
    public static <T> T chooseRandom(T[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /**
     * Returns a random element from the list.
     * @param list List to choose an element from.
     * @return Random element chosen from list.
     */
    public static <T> T chooseRandom(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Chooses a specified number of random elements from the given list.
     * @param list List to choose from.
     * @param amount Number of items to choose.
     * @return A new list containing the chosen items.
     */
    public static <T> List<T> chooseRandom(List<T> list, int amount) {
        if (amount < 0) throw new RuntimeException("Amount is negative");
        if (amount > list.size()) throw new RuntimeException("Amount is more than list size");
        return shuffle(new ArrayList<>(list)).subList(0, amount);
    }

    /**
     * Shuffles the given array in-place.
     * @param arr Array to shuffle.
     * @return The given array for convenience.
     */
	public static int[] shuffle(int[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /**
     * Shuffles the given array in-place.
     * @param arr Array to shuffle.
     * @return The given array for convenience.
     */
	public static long[] shuffle(long[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /**
     * Shuffles the given array in-place.
     * @param arr Array to shuffle.
     * @return The given array for convenience.
     */
	public static float[] shuffle(float[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /**
     * Shuffles the given array in-place.
     * @param arr Array to shuffle.
     * @return The given array for convenience.
     */
	public static double[] shuffle(double[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /**
     * Shuffles the given list in-place.
     * @param list Array to shuffle.
     * @return The given list for convenience.
     */
	public static <T> List<T> shuffle(List<T> list) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = list.size() - 1; i > 0; i--) {
			swapList(list, i, rand.nextInt(i + 1));
		}
		return list;
	}

    /**
     * Swaps two elements in the given array, by index.
     * @param arr Array to swap in.
     * @param a Index of first element.
     * @param b Index of the second element.
     */
    public static <T> void swap(T[] arr, int a, int b) {
		T temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /**
     * Swaps two elements in the given array, by index.
     * @param arr Array to swap in.
     * @param a Index of first element.
     * @param b Index of the second element.
     */
	public static void swap(char[] arr, int a, int b) {
		char temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /**
     * Swaps two elements in the given array, by index.
     * @param arr Array to swap in.
     * @param a Index of first element.
     * @param b Index of the second element.
     */
	public static void swap(int[] arr, int a, int b) {
		int temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /**
     * Swaps two elements in the given array, by index.
     * @param arr Array to swap in.
     * @param a Index of first element.
     * @param b Index of the second element.
     */
	public static void swap(long[] arr, int a, int b) {
		long temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /**
     * Swaps two elements in the given array, by index.
     * @param arr Array to swap in.
     * @param a Index of first element.
     * @param b Index of the second element.
     */
	public static void swap(float[] arr, int a, int b) {
		float temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /**
     * Swaps two elements in the given array, by index.
     * @param arr Array to swap in.
     * @param a Index of first element.
     * @param b Index of the second element.
     */
	public static void swap(double[] arr, int a, int b) {
		double temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /**
     * Swaps two elements in the given list, by index.
     * @param list List to swap in.
     * @param a Index of first element.
     * @param b Index of the second element.
     */
	public static <T> void swapList(List<T> list, int a, int b) {
		T temp = list.get(a);
        list.set(a, list.get(b));
        list.set(b, temp);
	}

    /**
     * Reverses the contents of the given array.
     * @param arr Array to reverse.
     * @return The given array for convenience.
     */
    public static int[] reverse(int[] arr) {
        int last = arr.length - 1;
        for (int i = 0; i < arr.length / 2; i++) {
            swap(arr, i, last - i);
        }
        return arr;
    }

    /**
     * Reverses the contents of the given array.
     * @param arr Array to reverse.
     * @return The given array for convenience.
     */
    public static byte[] reverse(byte[] arr) {
        int last = arr.length - 1;
        for (int i = 0; i < arr.length / 2; i++) {
            arr[i] ^= arr[last - i];
            arr[last - i] ^= arr[i];
            arr[i] ^= arr[last - i];
        }
        return arr;
    }

    /**
     * Reverses the contents of the given array.
     * @param arr Array to reverse.
     * @return The given array for convenience.
     */
    public static long[] reverse(long[] arr) {
        int last = arr.length - 1;
        for (int i = 0; i < arr.length / 2; i++) {
            arr[i] ^= arr[last - i];
            arr[last - i] ^= arr[i];
            arr[i] ^= arr[last - i];
        }
        return arr;
    }

    /**
     * Reverses the contents of the given array.
     * @param arr Array to reverse.
     * @return The given array for convenience.
     */
    public static char[] reverse(char[] arr) {
        int last = arr.length - 1;
        for (int i = 0; i < arr.length / 2; i++) {
            arr[i] ^= arr[last - i];
            arr[last - i] ^= arr[i];
            arr[i] ^= arr[last - i];
        }
        return arr;
    }

    /**
     * Reverses the contents of the given array.
     * @param arr Array to reverse.
     * @return The given array for convenience.
     */
    public static float[] reverse(float[] arr) {
        int last = arr.length - 1;
        for (int i = 0; i < arr.length / 2; i++) {
            float a = arr[i];
            arr[i] = arr[last - i];
            arr[last - i] = a;
        }
        return arr;
    }

    /**
     * Reverses the contents of the given array.
     * @param arr Array to reverse.
     * @return The given array for convenience.
     */
    public static double[] reverse(double[] arr) {
        int last = arr.length - 1;
        for (int i = 0; i < arr.length / 2; i++) {
            double a = arr[i];
            arr[i] = arr[last - i];
            arr[last - i] = a;
        }
        return arr;
    }

    /**
     * Reverses the contents of the given list.
     * @param list List to reverse.
     * @return The given list for convenience.
     */
    public static <T> List<T> reverse(List<T> list) {
        int len = list.size();
        int last = len - 1;
        for (int i = 0; i < len / 2; i++) {
            T a = list.get(i);
            list.set(i, list.get(last - i));
            list.set(last - i, a);
        }
        return list;
    }

    /**
     * Returns a binary string representation of the given byte array.
     * @param bytes Byte array to convert.
     * @return String representing the given byte array.
     */
    public static String toBinaryString(byte[] bytes) {
        StringBuilder strb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; i--) {
            strb.append(
                StringsUtil.padLeft(
                    Integer.toBinaryString(Byte.toUnsignedInt(bytes[i])),
                    Byte.SIZE,
                    '0'
                )
            );
        }
        return strb.toString();
    }
}
