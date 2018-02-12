package net.katros.services.utils;

import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link CollectionUtils}
 * 
 * @author doron
 */
@RunWith(JUnit4.class)
public class CollectionUtilsTest
{
	@Test
	public void test()
	{
		List<Long> longs = CollectionUtils.toList((long[])null);
		assertNull(longs);
	}
}