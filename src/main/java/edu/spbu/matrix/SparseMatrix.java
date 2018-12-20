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
            if(!table.containsKey(i))
                return;
            table.get(i).remove(j);
            return;
        }
        if(!table.containsKey(i))
            table.put(i, new HashMap<>());
        table.get(i).put(j, value);
    }
    public void add_to_entry(int i, int j, double value)
    {
        if (value == 0)
            return; /* adding 0 does nothing */
        if (!table.containsKey(i) || !table.get(i).containsKey(j))
            set_entry(i, j, value); /* if get_entry was zero, make a new get_entry */
        else
            set_entry(i, j, get_entry(i, j) + value);
        if(get_entry(i,j)==0)
            table.get(i).remove(j); /* if get_entry ended up zero, remove it */
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
        table = new HashMap<>();
    }

    /**
     * loads matrix from file
     * @param file_name source file name
     */
    public SparseMatrix(String file_name)
    {
        if(file_name.trim().equals(""))
            return;

        int j;
        String[] row_string_array;
        String row_string;
        try
        {
            Scanner in = new Scanner(new File(file_name));


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
            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
                    for (int j : this.table.get(i).keySet()) {
                        for (int k : this.table.get(i).keySet()) {
                            result.add_to_entry(i, j, table.get(i).get(k) * o1.get_entry(k, j));
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
                if(this.table.containsKey(i))
                    for(int j = 0; j < result.get_col_count(); j++)
                    {
                        for(int k : this.table.get(i).keySet())
                        {
                            double addend = this.table.get(i).get(k) * o1.get_entry(k, j);
                            result.add_to_entry(i, j, addend);
                        }
                    }
            }
            return result;
        }
    }

    /**
     * multi-threaded multiplication
     * @param o the other matrix
     * @return the product
     */
    @Override public Matrix dmul(Matrix o)
    {
      final int THREAD_COUNT = 4;
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
      if(o instanceof SparseMatrix)
      {
          SparseMatrix o1 = (SparseMatrix) o;
          if (this.col_count != o1.get_row_count())
          {
              throw new IllegalArgumentException("Cannot multiply " + row_count + "*" + col_count + " matrix by "
                      + o1.get_row_count() + "*" + o1.get_col_count() + "matrix");
          }

          SparseMatrix result = new SparseMatrix(this.row_count, o1.get_col_count());
          class EntryCalc extends Thread
          {
              public List<Entry> list = new ArrayList<>();

              public void run()
              {
                  for(Entry e : list)
                  {
                      int i = e.i;
                      int j = e.j;

                      for(int k : table.get(i).keySet())
                      {
                          double addend = table.get(i).get(k) * o1.get_entry(k,j);
                          result.add_to_entry(i,j,addend);
                      }
                  }
              }
          }
          EntryCalc[] threads = new EntryCalc[THREAD_COUNT];
          for (int i = 0; i < threads.length; ++i)
              threads[i] = new EntryCalc();
          int counter = 0;
          /* distribute calculations across threads */
          for(int i : table.keySet())
          {
              for(int j = 0; j < o1.get_col_count(); j++)
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
      else
      {
          DenseMatrix o1 = (DenseMatrix) o;
          if (this.col_count != o1.get_row_count())
          {
              throw new IllegalArgumentException("Cannot multiply " + row_count + "*" + col_count + " matrix by "
                      + o1.get_row_count() + "*" + o1.get_col_count() + "matrix");
          }
          DenseMatrix result = new DenseMatrix(this.row_count, o1.get_col_count());
          class EntryCalc extends Thread
          {
              public List<Entry> list = new ArrayList<>();

              public void run()
              {
                  for(Entry e : list)
                  {
                      int i = e.i;
                      int j = e.j;

                      for(int k : table.get(i).keySet())
                      {
                          double addend = table.get(i).get(k) * o1.get_entry(k,j);
                          result.add_to_entry(i,j,addend);
                      }
                  }
              }
          }
          EntryCalc[] threads = new EntryCalc[THREAD_COUNT];
          for (int i = 0; i < threads.length; ++i)
              threads[i] = new EntryCalc();
          int counter = 0;
          /* distribute calculations across threads */
          for(int i : table.keySet())
          {
              for(int j = 0; j < o1.get_col_count(); j++)
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
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
    @Override public boolean equals(Object o)
    {
        if(o instanceof SparseMatrix)
        {
            SparseMatrix o1 = (SparseMatrix) o;
            if(row_count != o1.get_row_count())
                return false;
            if(col_count != o1.get_col_count())
                return false;
            if(this.table.keySet() != o1.table.keySet())
                return false;
            for(int i: this.table.keySet())
            {
                if (this.table.get(i).keySet() != o1.table.get(i).keySet())
                    return false;
                for (int j : this.table.get(i).keySet())
                {
                    if(this.table.get(i).get(j) != o1.table.get(i).get(j))
                        return false;
                }
            }
            return true;
        }
        if(o instanceof DenseMatrix)
        {
            DenseMatrix o1 = (DenseMatrix) o;
            return o1.equals(this);
        }
        return false;
    }
}
