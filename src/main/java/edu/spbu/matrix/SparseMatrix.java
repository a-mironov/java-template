package edu.spbu.matrix;

import java.util.*;
import java.io.*;

import static java.lang.Double.parseDouble;
/**
 * Разреженная матрица
 */
public class SparseMatrix implements Matrix
{
    private static final int ARRAY_SIZE = 3000; /* max array size */
    private int row_count = 0;
    private int col_count = 0;
    private Map<Integer, Map<Integer, Double>> table = new HashMap<>();

    /* auxiliary methods */
    public double get_entry(int i, int j)
    {
        if(!table.containsKey(i) || !table.get(i).containsKey(j))
            return 0;
        return table.get(i).get(j);
    }
    public void set_entry(int i, int j, double value)
    {
        if(value==0)
        {
            table.get(i).remove(j);
            return;
        }
        table.get(i).put(j, value);
    }
    public void add_to_entry(int i, int j, double value)
    {
        if (value == 0)
            return; /* adding 0 does nothing */
        if (!table.containsKey(i) || !table.get(i).containsKey(j))
            set_entry(i, j, value); /* if entry was zero, make a new entry */
        else
            set_entry(i, j, get_entry(i, j) + value);
        if(get_entry(i,j)==0)
            table.get(i).remove(j); /* if entry ended up zero, remove it */
    }
    public int get_row_count()
    {
        return this.row_count;
    }
    public int get_col_count()
    {
        return this.col_count;
    }

    public SparseMatrix(int r, int c) /* zero matrix with fixed size */
    {
        row_count = r;
        col_count = c;
    }

    /**
     * loads matrix from file
     * @param file_name source file name
     */
    public SparseMatrix(String file_name) throws FileNotFoundException
    {
        int j;
        String[] row_string_array;
        Scanner in = new Scanner(new File(file_name));
        String row_string = "1";

        while(in.hasNextLine())
        {
            row_count += 1;
            row_string = in.nextLine();
            row_string_array = row_string.split(" ");
            if (row_count == 1)
                col_count = row_string_array.length;
            for (j = 0; j < col_count; j++)
                set_entry(row_count - 1, j, parseDouble(row_string_array[j]));
        }
    }

    /**
     * single-threaded matrix multiplication
     * @param o the other matrix
     * @return the product
     */
    @Override public Matrix mul(Matrix o)
    {
        if(o instanceof SparseMatrix)
        {
            SparseMatrix o1 = (SparseMatrix) o;

            if (this.col_count != o1.get_row_count())
            {
                throw new IllegalArgumentException("Cannot multiply " + row_count + "*" + col_count + " matrix by "
                        + o1.get_row_count() + "*" + o1.get_col_count() + "matrix");
            }

            SparseMatrix result = new SparseMatrix(this.row_count, o1.get_col_count());

            for (int i : table.keySet()) {
                if (table.containsKey(i))
                    for (int j : table.get(i).keySet()) {
                        for (int k = 0; k < this.col_count; k++) {
                            result.add_to_entry(i, j, table.get(i).get(k) + o1.get_entry(k, j));
                        }
                    }
            }
            return result;
        }
        else
        {
            DenseMatrix o1 = (DenseMatrix) o;
            if(this.col_count != o1.get_row_count())
            {
                throw new IllegalArgumentException("Cannot multiply " + row_count + "*" + col_count + " matrix by "
                        + o1.get_row_count() + "*" + o1.get_col_count() + "matrix");
            }

            DenseMatrix result = new DenseMatrix(this.row_count, o1.get_col_count());

            for(int i = 0; i < this.row_count; i++)
            {
                for(int j = 0; j < result.get_col_count(); j++)
                {
                    for(int k = 0; k < this.col_count; k++)
                    {
                        double addend = this.table.get(i).get(k) * o1.entry(k, j);
                        result.add_to_entry(i, j, addend);
                    }
                }
            }
            return result;
        }
    }

  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override public Matrix dmul(Matrix o)
  {
    return null;
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
    return false;
  }
}
