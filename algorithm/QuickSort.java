/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

/**
 *
 * @author Administrator
 */
public final class QuickSort {
    
    private static int mark = 0;
    public QuickSort(){
        
    }
    private static void swap(double[] array, int a, int b) {
        if (a != b) {
            double temp = array[a];
            array[a] = array[b];
            array[b] = temp;
        }
    }
    /**
     * 新一轮分隔
     * @param array
     * @param low
     * @param high
     * @return
     */
    private static int partition(double array[], int low, int high) {
        double base = array[low];
        mark++;
        while (low < high) {
            while (low < high && array[high] >= base) {
                high--;
            }
            swap(array, low, high);
            while (low < high && array[low] <= base) {
                low++;
            }
            swap(array, low, high);
        }
        return low;
    }
    /**
     * 对数组进行快速排序，递归调用
     * @param array
     * @param low
     * @param heigh
     * @return
     */
    private static double[] quickSort(double[] array, int low, int heigh) {
        if (low < heigh) {
            int division = partition(array, low, heigh);
            quickSort(array, low, division - 1);
            quickSort(array, division + 1, heigh);
        }
        return array;
    }
    /**
     * 快排序
     * @param array
     * @return
     */
    public static double[] sort(double[] array) {
        return quickSort(array, 0, array.length - 1);
    }
    public static void main1(String[] args) {
        double[] array = {3, 5, 2, 6, 2};
        double[] sorted = sort(array);
        System.out.println("最终结果");
        for (double i : sorted) {
            System.out.print(i + " ");
        }
    }

}
