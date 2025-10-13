package io.github.metal_pony.sudoku.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Helpers for arrays. */
public class ArraysUtil {
    /** Returns a copy of the given array. */
    public static int[] copy(int[] source) {
        int[] arr = new int[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /** Returns a copy of the given array. */
    public static long[] copy(long[] source) {
        long[] arr = new long[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /** Returns a copy of the given array. */
    public static float[] copy(float[] source) {
        float[] arr = new float[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /** Returns a copy of the given array. */
    public static double[] copy(double[] source) {
        double[] arr = new double[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /** Returns a copy of the given array. */
    public static boolean[] copy(boolean[] source) {
        boolean[] arr = new boolean[source.length];
        System.arraycopy(source, 0, arr, 0, source.length);
        return arr;
    }

    /** Creates a new array with sequential elements from 0 to n exclusive. */
    public static int[] range(int n) {
        int[] arr = new int[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /** Creates a new array with sequential elements from 0 to n exclusive. */
    public static long[] rangeLong(int n) {
        long[] arr = new long[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /** Creates a new array with sequential elements from 0 to n exclusive. */
    public static float[] rangeFloat(int n) {
        float[] arr = new float[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /** Creates a new array with sequential elements from 0 to n exclusive. */
    public static double[] rangeDouble(int n) {
        double[] arr = new double[n];
		for (int i = 0; i < n; i++) arr[i] = i;
		return arr;
    }

    /** Creates a new array with sequential elements from 0 to n exclusive. */
    public static List<Integer> rangeList(int n) {
        List<Integer> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) list.add(i);
		return list;
    }

    /** Fills the array with random numbers between origin and bound (exclusive). */
    public static int[] randoms(int[] arr, int origin, int bound) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = rand.nextInt(origin, bound);
		}
		return arr;
    }

    /** Fills the array with random numbers between 0 and bound (exclusive). */
    public static int[] randoms(int[] arr, int bound) {
        return randoms(arr, 0, bound);
    }

    /** Fills the array with random (positive) numbers. */
    public static int[] randoms(int[] arr) {
        return randoms(arr, 0, Integer.MAX_VALUE);
    }

    /** Returns a random element from the array. */
    public static int chooseRandom(int[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /** Returns a random element from the array. */
    public static long chooseRandom(long[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /** Returns a random element from the array. */
    public static float chooseRandom(float[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /** Returns a random element from the array. */
    public static double chooseRandom(double[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /** Shuffles the given array in-place. */
	public static int[] shuffle(int[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /** Shuffles the given array in-place. */
	public static long[] shuffle(long[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /** Shuffles the given array in-place. */
	public static float[] shuffle(float[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /** Shuffles the given array in-place. */
	public static double[] shuffle(double[] arr) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = arr.length - 1; i > 0; i--) {
			swap(arr, i, rand.nextInt(i + 1));
		}
		return arr;
	}

    /** Shuffles the given list in-place. */
	public static <T> List<T> shuffle(List<T> list) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for(int i = list.size() - 1; i > 0; i--) {
			swapList(list, i, rand.nextInt(i + 1));
		}
		return list;
	}

    /** Swaps array elements in the specified positions. */
    public static <T> void swap(T[] arr, int a, int b) {
		T temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /** Swaps array elements in the specified positions. */
	public static void swap(char[] arr, int a, int b) {
		char temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /** Swaps array elements in the specified positions. */
	public static void swap(int[] arr, int a, int b) {
		int temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /** Swaps array elements in the specified positions. */
	public static void swap(long[] arr, int a, int b) {
		long temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /** Swaps array elements in the specified positions. */
	public static void swap(float[] arr, int a, int b) {
		float temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /** Swaps array elements in the specified positions. */
	public static void swap(double[] arr, int a, int b) {
		double temp = arr[a];
		arr[a] = arr[b];
		arr[b] = temp;
	}

    /** Swaps list elements in the specified positions. */
	public static <T> void swapList(List<T> list, int a, int b) {
		T temp = list.get(a);
        list.set(a, list.get(b));
        list.set(b, temp);
	}
}
