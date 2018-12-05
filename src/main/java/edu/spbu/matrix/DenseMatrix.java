package edu.spbu.matrix;

import java.util.*;
import java.io.*;

import static java.lang.Double.parseDouble;

public class DenseMatrix implements Matrix
{
    public static final int ARRAY_SIZE = 3000; /* max array size */
    private int row_count = 0;                 /* row count */
    private int col_count = 0;                 /* column count */
    private double[][] entries;                 /* contents */

    /* auxiliary methods */
    public boolean is_nondegenerate()
    {
        return (row_count!=0 && col_count!=0);
    }
    public double entry(int i, int j)
    {
        return entries[i][j];
    }
    public int rows()
    {
        return this.row_count;
    }
    public int cols()
    {
        return this.col_count;
    }
    public void set_entry(int i, int j, double value)
    {
        this.entries[i][j] = value;
    }
    public void add_to_entry(int i, int j, double value)
    {
        this.entries[i][j] += value;
    }
    /**
    * loads matrix from file
    * @param file_name source file name
    */
    public DenseMatrix(String file_name) throws IOException
    {
        if(file_name.trim().equals(""))
            return;

        int j;
        String[] row_string_array;
        Scanner in = new Scanner(new File(file_name));
        String row_string="1";

        while(in.hasNextLine()&&!row_string.trim().equals(""))
        {
            row_count += 1;
            row_string = in.nextLine().trim();
            row_string_array = row_string.split(" ");
            if(row_count==1)
            {
                col_count = row_string_array.length;
                entries = new double[ARRAY_SIZE][col_count];
            }
            for(j=0;j<col_count;j++)
            {
                entries[row_count-1][j] = parseDouble(row_string_array[j]);
            }
        }
        in.close();
    }
    public DenseMatrix(int r, int c)
    {
        row_count = r;
        col_count = c;
        entries = new double[row_count][col_count];
    }
    /**
     * single-threaded multiplication
     *
     * @param o the other matrix
     * @return the product
     */
    @Override public Matrix mul(Matrix o)
    {
        if(o instanceof DenseMatrix)
        {
            DenseMatrix o1 = (DenseMatrix) o;

            if(this.col_count != o1.rows())
            {
                throw new IllegalArgumentException("Cannot multiply " + row_count + "*" + col_count + " matrix by "
                                                    + o1.rows() + "*" + o1.cols() + "matrix");
            }

            DenseMatrix result = new DenseMatrix(this.row_count, o1.cols());
            for(int i = 0; i < this.row_count; i++)
            {
                for (int j = 0; j < o1.cols(); j++)
                {
                    result.entries[i][j] = 0;
                    for (int k = 0; k < row_count; k++)
                    {
                        result.entries[i][j] += this.entries[i][k] + o1.entry(i, j);
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * multi-threaded multiplication
     * @param o the other matrix
     * @return the product
     */
    @Override public Matrix dmul(Matrix o)
    {
        class Entry
        {
            public int i;
            public int j;
            Entry(int i, int j)
            {
                this.i = i;
                this.j = j;
            }
        }
        if(o instanceof DenseMatrix)
        {
            DenseMatrix o1 = (DenseMatrix) o;
            final int THREAD_COUNT = 4;

            if(this.col_count != o1.rows())
            {
                throw new IllegalArgumentException("Cannot multiply " + row_count + "*" + col_count + " matrix by "
                        + o1.rows() + "*" + o1.cols() + "matrix");
            }
            DenseMatrix result = new DenseMatrix(this.row_count, o1.cols());

            class EntryCalc extends Thread
            {
                public List<Entry> list = new ArrayList<>();

                public void run()
                {
                    for(Entry e:list)
                    {
                        int i = e.i;
                        int j = e.j;
                        result.entries[i][j] = 0;
                        for(int k = 0; k < col_count; k++)
                        {
                            result.entries[i][j] += entries[i][k] * o1.entry(k,j);
                        }
                    }
                }
            }
            EntryCalc[] threads = new EntryCalc[THREAD_COUNT];
            for (int i = 0; i < threads.length; ++i)
                threads[i] = new EntryCalc();
            int counter = 0;
            for(int i = 0; i < row_count; i++)
            {
                for(int j = 0; j < o1.cols(); j++)
                {
                    threads[counter].list.add(new Entry(i,j));
                    counter = (counter + 1) % THREAD_COUNT;
                }
            }

            for(EntryCalc t : threads)
            {
                try
                {
                    t.join();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            return result;
        }
        return null;
    }

  @Override public boolean equals(Object o) {
    return false;
  }


}
