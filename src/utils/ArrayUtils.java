package utils;

import java.lang.reflect.Array;

public class ArrayUtils {

    public static int[] doubleIntArray(int[] oldArray) {
        int[] newArray = new int[oldArray.length << 1];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }
    
    public static Object doubleRefArray(Object[] oldArray, Class<?> componentType) {
        Object newArray = Array.newInstance(componentType, oldArray.length << 1);
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }
    
}