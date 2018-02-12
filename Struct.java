package net.katros.strategies.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds {@link Wrapper}s for different types of payloads.
 * Can be used to pass payloads between methods where the payloads types are not
 * known at compile time.
 * Based in part on RowMutable.
 * Support may be added for boolean, byte, float, short as needed.
 * 
 * @author doron
 */
public class Struct implements Cloneable
{
	private final Column[] columns;
	private final Wrapper<?>[] wrappers;
	private final Map<String, Integer> nameToPositionMap = new HashMap<>();

	public Struct(Collection<Column> c)
	{
		this(c.toArray(new Column[0]));
	}

	public Struct(Column... columns)
	{
		this.columns = columns;
		wrappers = new Wrapper[columns.length];
		for (int i = 0; i < columns.length; i++)
		{
			wrappers[i] = Wrapper.createWrapper(columns[i].getType());
			nameToPositionMap.put(columns[i].getName(), i);
		}
	}

	/**
	 * Copy c'tor.
	 * 
	 * @param struct
	 */
	private Struct(Struct struct)
	{
		this.columns = struct.columns;
		wrappers = new Wrapper[columns.length];
		for (int i = 0; i < columns.length; i++)
		{
			Class<?> type = columns[i].getType();
			wrappers[i] = Wrapper.createWrapper(type);
			setValue(type, i, struct, i);
			nameToPositionMap.put(columns[i].getName(), i);
		}
	}

	public void setValue(int indexTo, Struct structFrom, int indexFrom)
	{
		setValue(columns[indexTo].getType(), indexTo, structFrom, indexFrom);
	}

	public void setValue(Class<?> type, int indexTo, Struct structFrom, int indexFrom)
	{
		// trying to do the more prevailing types first
		if (type == Double.TYPE)
			set(indexTo, structFrom.getDouble(indexFrom));
		else if (type == Long.TYPE)
			set(indexTo, structFrom.getLong(indexFrom));
		else if (type == Integer.TYPE)
			set(indexTo, structFrom.getInt(indexFrom));
		else if (type == Boolean.TYPE)
			set(indexTo, structFrom.getBoolean(indexFrom));
		else if (type == String.class)
			set(indexTo, structFrom.getString(indexFrom));
		else if (type == Integer.class)
			set(indexTo, structFrom.getInteger(indexFrom));
		else if (type == Object.class)
			set(indexTo, structFrom.getObject(indexFrom));
		else
			throw new IllegalArgumentException("Type " + type + " is not supported");
	}

	/**
	 * TODO consider utilizing {@link Object#clone}.
	 */
	@Override
	public Struct clone()
	{
		return new Struct(this);
	}

	public Column[] getColumns()
	{
		return columns;
	}

	public Column getColumnByName(String name)
	{
		for (Column column : columns)
			if (column.getName().equals(name))
				return column;
		return null;
	}

	public String[] getColumnNames()
	{
		String[] columnNames = new String[columns.length];
		for (int i = 0; i < columns.length; i++)
			columnNames[i] = columns[i].getName();
		return columnNames;
	}

	public String[] getValues()
	{
		String[] values = new String[wrappers.length];
		for (int i = 0; i < wrappers.length; i++)
			values[i] = wrappers[i].getValueAsString();
		return values;
	}

	public String[] getShortValues()
	{
		String[] values = new String[wrappers.length];
		for (int i = 0; i < wrappers.length; i++)
			values[i] = wrappers[i].getShortValue();
		return values;
	}

	/**
	 * The purpose of this method is supporting object reuse.
	 * 
	 * @param outValues	assumed to be same size as this Struct.
	 */
	public void getShortValues(String[] outValues)
	{
		for (int i = 0; i < wrappers.length; i++)
			outValues[i] = wrappers[i].getShortValue();
	}

	/**
	 * It's the responsibility of the caller to call this method only if all values are <code>double</code>s.
	 * @return
	 */
	public double[] getAsDoubles()
	{
		double[] values = new double[wrappers.length];
		for (int i = 0; i < wrappers.length; i++)
			values[i] = wrappers[i].getDouble();
		return values;
	}

	/**
	 * It's the responsibility of the caller to call this method only if all values are <code>double</code>s.
	 * The purpose of this method is supporting object reuse.
	 * 
	 * @param outValues	assumed to be same size as this Struct.
	 */
	public void getAsDoubles(double[] outValues)
	{
		for (int i = 0; i < wrappers.length; i++)
			outValues[i] = wrappers[i].getDouble();
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws NullPointerException	if the specified name doesn't exist.
	 */
	public int nameToPosition(String name)
	{
		try
		{
			return nameToPositionMap.get(name);
		}
		catch (NullPointerException e)
		{
			throw new IllegalArgumentException("Could not find name '" + name + "' in Struct: " + this, e);
		}
	}

	/**
	 * 
	 * @param name
	 * @return	the position of the specified name, -1 if it doesn't exist.
	 */
	public int nameToPositionIfExists(String name)
	{
		Integer position = nameToPositionMap.get(name);
		return position == null ? -1 : position;
	}

	public int size()
	{
		return columns.length;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(columns.length * 30);
		sb.append("[");
		for (int i = 0; i < columns.length; i++)
		{
			if (i > 0)
				sb.append(", ");
			sb.append(columns[i].getName()).append(" ").append(wrappers[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	// double

	public void set(int position, double value)
	{
		wrappers[position].set(value);
	}

	public void set(String name, double value)
	{
		wrappers[nameToPositionMap.get(name)].set(value);
	}

	public double getDouble(int position)
	{
		return wrappers[position].getDouble();
	}

	public double getDouble(String name)
	{
		return wrappers[nameToPositionMap.get(name)].getDouble();
	}

	// long

	public void set(int position, long value)
	{
		wrappers[position].set(value);
	}

	public void set(String name, long value)
	{
		wrappers[nameToPositionMap.get(name)].set(value);
	}

	public long getLong(int position)
	{
		return wrappers[position].getLong();
	}

	public long getLong(String name)
	{
		return wrappers[nameToPositionMap.get(name)].getLong();
	}

	// int

	public void set(int position, int value)
	{
		wrappers[position].set(value);
	}

	public void set(String name, int value)
	{
		wrappers[nameToPositionMap.get(name)].set(value);
	}

	public int getInt(int position)
	{
		return wrappers[position].getInt();
	}

	public int getInt(String name)
	{
		return wrappers[nameToPositionMap.get(name)].getInt();
	}

	// Integer

	public void set(int position, Integer value)
	{
		wrappers[position].set(value);
	}

	public void set(String name, Integer value)
	{
		wrappers[nameToPositionMap.get(name)].set(value);
	}

	public Integer getInteger(int position)
	{
		return wrappers[position].getInteger();
	}

	public Integer getInteger(String name)
	{
		return wrappers[nameToPositionMap.get(name)].getInteger();
	}

	// boolean

	public void set(int position, boolean value)
	{
		wrappers[position].set(value);
	}

	public void set(String name, boolean value)
	{
		wrappers[nameToPositionMap.get(name)].set(value);
	}

	public boolean getBoolean(int position)
	{
		return wrappers[position].getBoolean();
	}

	public boolean getBoolean(String name)
	{
		return wrappers[nameToPositionMap.get(name)].getBoolean();
	}

	// String

	public void set(int position, String value)
	{
		wrappers[position].set(value);
	}

	public void set(String name, String value)
	{
		wrappers[nameToPositionMap.get(name)].set(value);
	}

	public String getString(int position)
	{
		return wrappers[position].getString();
	}

	public String getString(String name)
	{
		return wrappers[nameToPositionMap.get(name)].getString();
	}

	// Object

	public void set(int position, Object value)
	{
		wrappers[position].set(value);
	}

	public void set(Object name, Object value)
	{
		wrappers[nameToPositionMap.get(name)].set(value);
	}

	public Object getObject(int position)
	{
		return wrappers[position].getObject();
	}

	public Object getObject(Object name)
	{
		return wrappers[nameToPositionMap.get(name)].getObject();
	}

	//// main ////

	public static void main(String... args)
	{
		testClone();
		testPerformance();
	}

	/**
	 * Should be:
OK
original=[bidV1 double 4.4, pos Integer 8, obj Object B]
  cloned=[bidV1 double 3.3, pos Integer 7, obj Object A]
	 */
	public static void testClone()
	{
		List<String> list = new ArrayList<>();
		list.add("A");
		List<String> list2 = new ArrayList<>();
		list2.add("B");
		final Struct struct = new Struct(new Column[]
			{
				new Column("bidV1"),
				new Column("pos", Integer.class),
				new Column("obj", Object.class)
			});
		struct.set("bidV1", 3.3);
		struct.set("pos", new Integer(7));
		struct.set("obj", list);

		Struct cloned = struct.clone();
		String msg = cloned != struct && cloned.getClass() == struct.getClass() && cloned.toString().equals(struct.toString()) ? "OK" : "FAIL";
		System.out.println(msg);

		struct.set("bidV1", 4.4);
		struct.set("pos", new Integer(8));
		struct.set("obj", list2);
		System.out.println("original=" + struct);
		System.out.println("  cloned=" + cloned);
	}

	public static void testPerformance()
	{
		final int repeats = Integer.MAX_VALUE;

		class Test
		{
			private final Struct struct = new Struct(new Column[] { new Column("bidV1") });

			private void run()
			{
				System.out.print("Running...");
				long start = System.currentTimeMillis();
				for (int i = 1; i < repeats; i++)
				{
					struct.set(0, i + 0.1d);
					receive(struct);
				}
				long end = System.currentTimeMillis();
				System.out.println(" took " + (end - start) + "ms");
			}

			private void receive(Struct struct)
			{
				double value = struct.getDouble(0);
				if (value == 3d)
					System.out.println("can't be!");
			}
		};

		new Test().run();
	}
}