package my.katros.trials.function;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PassFunction
{
	public static void main(String... args)
	{
		SubA src = new SubA();
		src.setStr("Mars");
		SubB dest = new SubB();
		copy(src::getStr, dest::setStr);
		System.out.println(dest.getStr());
	}

	private static <T> void copy(Supplier<T> getter, Consumer<T> setter)
	{
		setter.accept(getter.get());
	}
}

class A
{
	private String str;

	public final String getStr() { return str; }
	public final void setStr(String str) { this.str = str; }
}

class B
{
	private String str;

	public final String getStr() { return str; }
	public final void setStr(String str) { this.str = str; }
}

class SubA extends A
{
}

class SubB extends B
{
}