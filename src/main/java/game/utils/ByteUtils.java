package game.utils;

/**
 * Author pengyi
 * Date 17-4-27.
 */
public class ByteUtils {
    public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return new byte[0];
            } else {
                byte[] subarray = new byte[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static byte[] addAll(byte[] array1, byte[] array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            byte[] joinedArray = new byte[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static byte[] clone(byte[] array) {
        return array == null ? null : (byte[]) ((byte[]) array.clone());
    }
}
