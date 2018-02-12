package net.katros.services.utils.io;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link DirectoryStructureBuilder}
 * 
 * @author doron
 */
@RunWith(JUnit4.class)
public class DirectoryStructureBuilderTest
{
	@Test
	public void testSplit1()
	{
		String[] expected = { "1", "23", "456", "789" };
		String[] actual = DirectoryStructureBuilder.split("123456789", 1, 2, 3);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testSplit2()
	{
		String[] expected = { "12", "34", "56789" };
		String[] actual = DirectoryStructureBuilder.split("123456789", 2, 2);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testSplit3()
	{
		String[] expected = { "1234567", "89" };
		String[] actual = DirectoryStructureBuilder.split("123456789", 7, 7, 8);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testSplit4()
	{
		String[] expected = { "123456789" };
		String[] actual = DirectoryStructureBuilder.split("123456789", 17, 20);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testSplit5()
	{
		String[] expected = { "1", "23456789" };
		String[] actual = DirectoryStructureBuilder.split("123456789", 1);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testSplit6()
	{
		String[] expected = { "123456789" };
		String[] actual = DirectoryStructureBuilder.split("123456789", 9);
		assertArrayEquals(expected, actual);
	}
}