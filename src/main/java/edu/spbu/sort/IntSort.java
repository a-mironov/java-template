package edu.spbu.sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class IntSort
{
    /* Merge sort */
    public static void sort (int array[])
    {
        if(array.length <= 1)
            return;

        int i, j;
        int len1 = array.length/2;
        int len2 = array.length - len1;
        int[] m1 = new int[len1];
        int[] m2 = new int[len2];

        /* Fill arrays */
        for(i=0;i<len1;i++)
        {
            m1[i] = array[i];
        }
        for(j=0;j<len2;j++)
        {
            m2[j] = array[len1+j];
        }

        sort(m1);
        sort(m2);

        i = 0;
        j = 0;

        while((i < len1) && (j < len2))
        {
            if(m1[i] < m2[j])
            {
                array[i + j] = m1[i];
                i++;
            }
            else
            {
                array[i+j] = m2[j];
                j++;
            }
        }
        if(i==len1)
        {
            for(;j<len2;j++)
                array[len1+j] = m2[j];
        }
        if(j==len2)
        {
            for(;i<len1;i++)
                array[len2+i] = m1[i];
        }
    }

    public static void sort (List<Integer> list)
    {
        Collections.sort(list);
    }
}
