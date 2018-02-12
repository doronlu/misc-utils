package net.katros.strategies.struct;

/**
 * Used to hold the type and name of a datum. Immutable.
 * 
 * @author doron
 */
public class Column
{
	private final String name;
	private final Class<?> clazz;

	public Column(String name)
	{
		this(name, Double.TYPE);
	}

	public Column(String name, Class<?> clazz)
	{
		this.name = name;
		this.clazz = clazz;
	}

	public String getName()
	{
		return name;
	}

	public Class<?> getType()
	{
		return clazz;
	}

	@Override
	public String toString()
	{
		return name + " " + clazz.getSimpleName();
	}
}