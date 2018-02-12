package net.katros.services.clojure;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;

/**
 * Misc. Clojure utilities.
 * 
 * @author doron
 */
public abstract class ClojureUtils
{
	public static IFn readClojureIFn(String fileNamespace, String method)
	{
		IFn require = Clojure.var("clojure.core", "require");
		require.invoke(Clojure.read(fileNamespace));
		return Clojure.var(fileNamespace, method);
	}

	public static PersistentArrayMap readClojureMap(String filename) throws IOException
	{
		return readClojureMap(filename, null);
	}

	public static PersistentArrayMap readClojureMap(String filename, String defaultMap) throws IOException
	{
		String s;
		if (filename != null)
			s = FileUtils.readFileToString(new File(filename));
		else if (defaultMap == null)
			return null;
		else
			s = defaultMap;
		Object o = Clojure.read(s);
		return (PersistentArrayMap)o;
	}
}