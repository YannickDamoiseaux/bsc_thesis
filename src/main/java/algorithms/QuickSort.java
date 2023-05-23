package algorithms;

public class QuickSort<V> {
    private int partition (SortObject<V> a[], int start, int end) {
        SortObject<V> pivot = a[end];
        int i = (start - 1);

        for (int j = start; j <= end - 1; j++) {
            if (a[j].sortParameter < pivot.sortParameter) {
                i++;
                SortObject<V> t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
        }
        SortObject<V> t = a[i+1];
        a[i+1] = a[end];
        a[end] = t;
        return (i + 1);
    }

    public SortObject<V>[] sort(SortObject<V> a[], int start, int end) {
        if (start < end) {
            int p = partition(a, start, end);  //p is partitioning index
            sort(a, start, p - 1);
            sort(a, p + 1, end);
        }
        return a;
    }
}