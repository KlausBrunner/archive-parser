package net.e175.klaus.archiveparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import net.e175.klaus.archiveparser.id.ArchiveID;
import net.e175.klaus.archiveparser.id.ArchiveType;
import net.e175.klaus.archiveparser.id.MavenArchiveID;

import org.junit.Test;

public class ArchiveIDTest {

	@Test
	public void canCompareEqualEARNames() {
		final String ear1 = "fooR032.ear";

		final MavenArchiveID ear = new MavenArchiveID();

		final ArchiveID id1 = ear.parse(ear1);
		final ArchiveID id2 = ear.parse(ear1);

		assertEquals(id1, id2);
	}

	@Test
	public void canUnderstandEARNames() {
		final String ear1 = "foo.ear";
		final String ear2 = "foo.EAR";

		final MavenArchiveID ear = new MavenArchiveID();

		final ArchiveID id1 = ear.parse(ear1);
		final ArchiveID id2 = ear.parse(ear2);

		assertEquals("foo", id1.getName());
		assertEquals("foo", id2.getName());
		assertEquals(ArchiveType.EAR, id1.getType());
		assertEquals(ArchiveType.EAR, id2.getType());
	}

	@Test
	public void canUnderstandMavenNames() {
		final String ar1 = "junit-4.8.1.jar";
		final MavenArchiveID mvn = new MavenArchiveID();
		final ArchiveID id1 = mvn.parse(ar1);

		assertEquals("junit", id1.getName());
		assertEquals("4.8.1", id1.getVersion());
		assertEquals(ArchiveType.JAR, id1.getType());

		final String ar2 = "fooblabbquaxi-002871-SNAPSHOT.war";
		final ArchiveID id2 = mvn.parse(ar2);
		assertEquals("fooblabbquaxi", id2.getName());
		assertEquals("002871-SNAPSHOT", id2.getVersion());
		assertEquals(ArchiveType.WAR, id2.getType());
	}

	@Test(expected = IllegalArgumentException.class)
	public void canRejectInvalidMavenNames() {
		final String ar1 = "junit.jax";
		final MavenArchiveID mvn = new MavenArchiveID();
		assertFalse(mvn.isApplicable(ar1));

		mvn.parse(ar1);
	}

	@Test
	public void canUnderstandIncompleteMavenNames() {
		final String ar1 = "junit.jar";
		final MavenArchiveID mvn = new MavenArchiveID();
		final ArchiveID id1 = mvn.parse(ar1);

		assertEquals("junit", id1.getName());
		assertEquals(ArchiveID.UNKNOWN_VERSION, id1.getVersion());

	}

}
