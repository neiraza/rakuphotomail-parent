package jp.co.fttx.rakuphotomail.controller;

import java.util.Comparator;

public class UidComparator implements Comparator {

    public static final int ASC = 1;
    public static final int DESC = -1;
    private int sort = ASC;

    public UidComparator() {

    }

    public UidComparator(int sort) {
        this.sort = sort;
    }

    public int compare(Object arg0, Object arg1) {
        if (!(arg0 instanceof Comparable) || !(arg1 instanceof Comparable)) {
            throw new IllegalArgumentException("arg0 & arg1 must implements interface of java.lang.Comparable.");
        }
        if (arg0 == null && arg1 == null) {
            return 0;   // arg0 = arg1
        } else if (arg0 == null) {
            return 1 * sort;   // arg0 > arg1
        } else if (arg1 == null) {
            return -1 * sort;  // arg0 < arg1
        }

        return ((Comparable)arg0).compareTo((Comparable)arg1) * sort;
    }
}
