/*
 * This file is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 */

package com.joa_ebert.apparat.tests.taas;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.swc.Swc;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.tests.FlashPlayerTest;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * @author Joa Ebert
 * 
 */
public class TaasCompilerTests
{
	static private Swc playerGlobal;

	@BeforeClass
	public static void parsePlayerGlobal() throws Exception
	{
		final File file = new File( "assets/playerglobal.swc" );

		Assert.assertTrue( file.exists() );

		playerGlobal = new Swc();
		playerGlobal.read( file );
	}

	public void compile( final File input ) throws Exception
	{
		Assert.assertTrue( input.exists() );

		if( input.getName().endsWith( ".abc" ) )
		{
			throw new UnsupportedOperationException();

			// final Abc abc = new Abc();
			//
			// abc.read( inputFile );
			//
			// TODO
		}
		else
		{
			final TagIO tagIO = new TagIO( input );
			final Map<Abc, DoABCTag> abcMap = new LinkedHashMap<Abc, DoABCTag>();

			tagIO.read();

			for( final ITag tag : tagIO.getTags() )
			{
				if( tag.getType() == Tags.DoABC )
				{
					final DoABCTag doABC = (DoABCTag)tag;

					final Abc abc = new Abc();

					abc.read( doABC );

					abcMap.put( abc, doABC );
				}
			}

			final TaasCompiler compiler = new TaasCompiler();

			compiler.addLibrary( playerGlobal );

			compiler.getAbcEnvironment().addAll( abcMap.keySet() );

			for( final Entry<Abc, DoABCTag> entry : abcMap.entrySet() )
			{
				entry.getKey().accept( compiler );
				entry.getKey().write( entry.getValue() );
			}

			final String name = input.getName();
			final String extension = name.substring( name.length() - 3, name
					.length() );
			final String newname = name.substring( 0, name.length() - 3 )
					+ "output." + extension;
			final File output = new File( input.getParentFile()
					.getAbsolutePath()
					+ File.separator + newname );

			tagIO.write( output );
			tagIO.close();

			final FlashPlayerTest playerTest = new FlashPlayerTest();

			playerTest.spawn( input, 5000 );
			playerTest.assertNoError();

			final String[] logBefore = playerTest.getLog();

			playerTest.spawn( output, 5000 );
			playerTest.assertNoError();

			final String[] logAfter = playerTest.getLog();

			Assert.assertArrayEquals( logBefore, logAfter );
		}
	}

	public void compile( final String input ) throws Exception
	{
		compile( new File( input ) );
	}

	@Test
	public void test() throws Exception
	{
		compile( "assets/Test9.swf" );
	}
}