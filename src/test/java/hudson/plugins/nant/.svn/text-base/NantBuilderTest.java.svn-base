package hudson.plugins.nant;

import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;
import org.jvnet.hudson.test.HudsonTestCase;

@RunWith(Enclosed.class)
public class NantBuilderTest
{
	/**
	 * Tests the behavior of NantBuilder during construction
	 * 
	 * @author Justin Holzer (jsholzer@gmail.com)
	 */
	public static class WhenConstructingNantBuilder extends HudsonTestCase
	{
		/**
		 * The nantBuildFile property should be set as the empty string if
		 * the value passed to the constructor is null.
		 */
		public void testShouldSetNantBuildFileToEmptyStringIfNull()
		{
			NantBuilder nantBuilder = new NantBuilder(null, "nant-0.85", "clean build", "foo=bar");
			assertEquals("", nantBuilder.getNantBuildFile());
		}
		
		/**
		 * The nantBuildFile property should be set as the empty string if
		 * the value passed to the constructor contains nothing but whitespace
		 */
		public void testShouldSetNantBuildFileToEmptyStringIfAllWhitespace()
		{
			String nantBuild = "  \t\n\n\t\t   ";
			NantBuilder nantBuilder = new NantBuilder(nantBuild, "", "", "");
			assertEquals("", nantBuilder.getNantBuildFile());
		}
		
		/**
		 * The targets property should be set as an empty string if null
		 * is passed in to the constructor 
		 */
		public void testShouldSetTargetsToEmptyStringIfNull()
		{
			NantBuilder nantBuilder = new NantBuilder("foo.build", "nant-0.85", null, "foo=bar");
			assertEquals("", nantBuilder.getTargets());
		}
		
		/**
		 * The targets property should be set as the empty string if
		 * the value passed to the constructor contains nothing but whitespace.
		 */
		public void testShouldSetTargetsToEmptyStringIfAllWhitespace()
		{
			String targets = "  \t\n\n\t\t   ";
			NantBuilder nantBuilder = new NantBuilder("foo.build", "nant-0.85", targets, "foo=bar");
			assertEquals("", nantBuilder.getTargets());
		}
	}
}
