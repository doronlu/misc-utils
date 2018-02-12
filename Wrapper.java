package net.katros.strategies.struct;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import net.katros.services.utils.CollectionUtils;

/**
 * A abstract wrapper that holds the actual payload.
 * Different implementations can be made to hold different types of payloads.
 * Based in part on RowMutable.
 * 
 * @author doron
 *
 * @param <T>
 */
public abstract class Wrapper<T>
{
	T payload;

	static Wrapper<?> createWrapper(Class<?> type)
	{
		if (type == Double.TYPE)
			return new DoubleWrapper();
		if (type == Long.TYPE)
			return new LongWrapper();
		if (type == Integer.TYPE)
			return new IntWrapper();
		if (type == Boolean.TYPE)
			return new BooleanWrapper();
		if (type == String.class)
			return new StringWrapper();
		if (type == Integer.class)
			return new IntegerWrapper();
		if (type == Object.class)
			return new ObjectWrapper();
		throw new IllegalArgumentException("The following type is not supported: " + type);
	}

	Wrapper(T content)
	{
		this.payload = content;
	}

	/**
	 * The class of the payload, if payload is array then the type of the
	 * array's elements.
	 * 
	 * @return
	 */
	Class<?> getElementType()
	{
		Class<?> payloadClass = payload.getClass();
		if (!payloadClass.isArray())
			return payload.getClass();
		return payloadClass.getComponentType();
	}

	/**
	 * The value of the payload.
	 * 
	 * @return
	 */
	public abstract String getValueAsString();

	public String getShortValue()
	{
		return getValueAsString();
	}

	/**
	 * The class of the payload, if payload is array then the type of the
	 * array's elements. Followed by:
	 * The value of the payload. If the payload is an array will show its
	 * elements in brackets, with the exception of an array of size 1 - won't
	 * enclose the value within brackets.
	 * 
	 * @return
	 */
	@Override
	public String toString()
	{
		return getElementType().getSimpleName() + " " + getValueAsString();
	}

	double getDouble()
	{
		throw new UnsupportedOperationException();
	}

	void set(double d)
	{
		throw new UnsupportedOperationException();
	}

	long getLong()
	{
		throw new UnsupportedOperationException();
	}

	void set(long l)
	{
		throw new UnsupportedOperationException();
	}

	int getInt()
	{
		throw new UnsupportedOperationException();
	}

	void set(int i)
	{
		throw new UnsupportedOperationException();
	}

	Integer getInteger()
	{
		throw new UnsupportedOperationException();
	}

	void set(Integer i)
	{
		throw new UnsupportedOperationException();
	}

	boolean getBoolean()
	{
		throw new UnsupportedOperationException();
	}

	void set(boolean i)
	{
		throw new UnsupportedOperationException();
	}

	String getString()
	{
		throw new UnsupportedOperationException();
	}

	void set(String d)
	{
		throw new UnsupportedOperationException();
	}

	Object getObject()
	{
		throw new UnsupportedOperationException();
	}

	void set(Object o)
	{
		throw new UnsupportedOperationException();
	}

	public static void main(String[] args)
	{
		Wrapper<?> wrapper = createWrapper(Double.TYPE);
		System.out.println(wrapper);
		wrapper.set(4.44);
		System.out.println(wrapper);
		System.out.println(wrapper.getClass());
		System.out.println(wrapper.getElementType());
		System.out.println(wrapper.getValueAsString());
	}
}

class DoubleWrapper extends Wrapper<double[]>
{
	DoubleWrapper()
	{
		super(new double[1]);
	}

	@Override
	double getDouble()
	{
		return payload[0];
	}

	@Override
	void set(double d)
	{
		payload[0] = d;
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(payload[0]);
	}
}

class LongWrapper extends Wrapper<long[]>
{
	LongWrapper()
	{
		super(new long[1]);
	}

	@Override
	long getLong()
	{
		return payload[0];
	}

	@Override
	void set(long l)
	{
		payload[0] = l;
	}

	@Override
	double getDouble()
	{
		return payload[0];
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(payload[0]);
	}
}

class IntWrapper extends Wrapper<int[]>
{
	IntWrapper()
	{
		super(new int[1]);
	}

	@Override
	int getInt()
	{
		return payload[0];
	}

	@Override
	void set(int i)
	{
		payload[0] = i;
	}

	@Override
	double getDouble()
	{
		return payload[0];
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(payload[0]);
	}
}

class IntegerWrapper extends Wrapper<Integer[]>
{
	IntegerWrapper()
	{
		super(new Integer[1]);
	}

	@Override
	Integer getInteger()
	{
		return payload[0];
	}

	@Override
	void set(Integer i)
	{
		payload[0] = i;
	}

	@Override
	double getDouble()
	{
		return payload[0] == null ? Double.NaN : payload[0];
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(payload[0]);
	}
}

class BooleanWrapper extends Wrapper<boolean[]>
{
	BooleanWrapper()
	{
		super(new boolean[1]);
	}

	@Override
	boolean getBoolean()
	{
		return payload[0];
	}

	@Override
	void set(boolean i)
	{
		payload[0] = i;
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(payload[0]);
	}

	@Override
	public String getShortValue()
	{
		return payload[0] == FALSE ? "F" : payload[0] == TRUE ? "T" : "null"; // in ledgers false is more prevalent than true
	}
}

class StringWrapper extends Wrapper<String[]>
{
	StringWrapper()
	{
		super(new String[1]);
	}

	@Override
	String getString()
	{
		return payload[0];
	}

	@Override
	void set(String d)
	{
		payload[0] = d;
	}

	@Override
	public String getValueAsString()
	{
		return payload[0];
	}
}

class ObjectWrapper extends Wrapper<Object[]>
{
	ObjectWrapper()
	{
		super(new Object[1]);
	}

	@Override
	Object getObject()
	{
		return payload[0];
	}

	@Override
	void set(Object o)
	{
		payload[0] = o;
	}

	/**
	 * The value of the payload. If the payload is an array will show its
	 * elements in brackets, with the exception of an array of size 1 - won't
	 * enclose the value within brackets.
	 * 
	 * @return
	 */
	@Override
	public String getValueAsString()
	{
		return CollectionUtils.toStringRecursive(payload[0]);
	}
}
