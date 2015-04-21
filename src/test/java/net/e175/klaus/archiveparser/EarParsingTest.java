package net.e175.klaus.archiveparser;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.e175.klaus.archiveparser.core.ArchiveFile;
import net.e175.klaus.archiveparser.id.ArchiveID;
import net.e175.klaus.archiveparser.id.ArchiveType;
import net.e175.klaus.archiveparser.id.MavenArchiveID;
import net.e175.klaus.archiveparser.id.MavenArchiveIDProvider;
import net.e175.klaus.archiveparser.report.EarFilter;
import net.e175.klaus.archiveparser.report.EarJarParser;

import org.junit.Test;

public class EarParsingTest {

	private static final String SAMPLES_DIR = "src/test/resources/samples";

	@Test
	public void canFilterEars() {
		final EarFilter lister = new EarFilter();
		final Collection<File> filteredFiles = lister.filterEarFiles(new File(SAMPLES_DIR));

		assertEquals(8, filteredFiles.size());
		assertTrue(filteredFiles.contains(new File(SAMPLES_DIR, "fuffiR007.ear")));
		assertTrue(filteredFiles.contains(new File(SAMPLES_DIR, "dummyR519.ear")));
	}

	@Test
	public void canFilterEarNames() {
		final EarFilter lister = new EarFilter();

		final String s = File.pathSeparator;

		final List<String> names = Arrays.asList(new String[] { "foo" + s + "fuffiR001.ear",
				"foo" + s + "fuffiR007.ear", "dummyR020.ear", "dummyR022.ear", "dummyR519.ear" });

		final Collection<String> filteredNames = lister.filterEarFilenames(names);

		assertEquals(5, filteredNames.size());
		assertThat(filteredNames, hasItem("foo" + s + "fuffiR007.ear"));
		assertThat(filteredNames, hasItem("dummyR519.ear"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionForBadFiles() {
		new ArchiveFile(new File("foo"), new MavenArchiveIDProvider());
	}

	@Test
	public void ignoresNestedJars() {
		final ArchiveFile f = new ArchiveFile(new File(SAMPLES_DIR, "illegally-nested-jar.jar"),
				new MavenArchiveIDProvider());
		assertEquals(0, f.contains().size());
	}

	@Test
	public void canCompareExceptVersion() {
		final ArchiveFile f1 = new ArchiveFile(new File(SAMPLES_DIR, "dummy-1.2.3.jar"), new MavenArchiveIDProvider());
		final ArchiveFile f2 = new ArchiveFile(new File(SAMPLES_DIR, "dummy-1.2.8.jar"), new MavenArchiveIDProvider());
		final ArchiveFile f3 = new ArchiveFile(new File(SAMPLES_DIR, "dummy.jar"), new MavenArchiveIDProvider());

		assertFalse(f1.equalsExceptVersion((ArchiveFile) null));

		assertFalse(f1.equals(f2));
		assertTrue(f1.equalsExceptVersion(f2));
		assertTrue(f2.equalsExceptVersion(f3));
		assertTrue(f1.equalsExceptVersion(f3));
	}

	@Test(expected = IllegalStateException.class)
	public void throwsExceptionForNestedEars() {
		new ArchiveFile(new File(SAMPLES_DIR + "/badthings", "illegally-nested-earR001.ear"),
				new MavenArchiveIDProvider());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void doesNotExposeInternals() {
		final ArchiveFile archive = new ArchiveFile(new File(SAMPLES_DIR, "simpleearR001.ear"),
				new MavenArchiveIDProvider());

		archive.contains().clear();
	}

	@Test
	public void canParseFilteredEars() {
		final EarJarParser lister = new EarJarParser();
		final EarFilter filter = new EarFilter();
		final Collection<File> filteredFiles = filter.filterEarFiles(new File(SAMPLES_DIR));

		final Collection<ArchiveFile> archiveFiles = lister.parseFiles(filteredFiles);

		assertEquals(8, archiveFiles.size());
		for (final ArchiveFile af : archiveFiles) {
			assertNotNull(af);
		}
	}

	@Test
	public void canListRelevantArchivesInEarNonRecursively() {
		final ArchiveFile archive = new ArchiveFile(new File(SAMPLES_DIR, "simpleearR001.ear"),
				new MavenArchiveIDProvider());
		final Collection<ArchiveFile> contained = archive.contains();
		assertEquals(6, contained.size());
		assertEquals("simpleearr001", archive.getArchiveID().getName());
	}

	@Test
	public void canListRelevantArchivesInEarRecursively() {
		final ArchiveFile archive = new ArchiveFile(new File(SAMPLES_DIR, "simpleearR001.ear"),
				new MavenArchiveIDProvider());
		final Collection<ArchiveFile> contained = archive.contains();

		assertEquals(6, contained.size());

		for (final ArchiveFile af : contained) {
			if (af.getArchiveID().getType().equals(ArchiveType.WAR)) {
				fail("there should be no WARs listed");
			}
		}
	}

	@Test
	public void canMapJarsToEars() {
		final EarJarParser lister = new EarJarParser();
		final EarFilter filter = new EarFilter();
		final Collection<File> filteredFiles = filter.filterEarFiles(new File(SAMPLES_DIR));

		final Collection<ArchiveFile> archiveFiles = lister.parseFiles(filteredFiles);

		final Map<ArchiveID, List<ArchiveFile>> jarEarMap = lister.getJarEarIdMap(archiveFiles);

		final ArchiveID jarId = new MavenArchiveID("dummy", "*", ArchiveType.JAR);

		assertTrue(jarEarMap.containsKey(jarId));

		final List<ArchiveFile> ears = jarEarMap.get(jarId);

		assertEquals(2, ears.size());

	}

	@Test
	public void createsOnlyOneArchiveFileObjectPerArchiveID() {

		final List<File> filteredFiles = Arrays.asList(new File(SAMPLES_DIR, "simpleearR001.ear"), new File(
				SAMPLES_DIR, "anotherearR777.ear"));

		final ArchiveID targetArchive = new MavenArchiveID("dummy", "1.2.8", ArchiveType.JAR);

		final EarJarParser lister = new EarJarParser();
		final List<ArchiveFile> archiveFiles = lister.parseFiles(filteredFiles);

		assertEquals(2, archiveFiles.size());
		final ArchiveFile ear1 = archiveFiles.get(0);
		final ArchiveFile ear2 = archiveFiles.get(1);

		ArchiveFile found1 = null;
		for (final ArchiveFile contained : ear1.contains()) {
			if (contained.getArchiveID().equals(targetArchive)) {
				found1 = contained;
			}
		}
		assertNotNull(found1);

		assertTrue(ear2.contains().indexOf(found1) != 1);
		final ArchiveFile found2 = ear2.contains().get(ear2.contains().indexOf(found1));
		assertSame(found1, found2);
	}

	@Test
	public void knowsEnclosingArchives() {

		final List<File> filteredFiles = Arrays.asList(new File(SAMPLES_DIR, "simpleearR001.ear"), new File(
				SAMPLES_DIR, "anotherearR777.ear"));

		final ArchiveID targetArchive = new MavenArchiveID("dummy", "1.2.8", ArchiveType.JAR);

		final EarJarParser lister = new EarJarParser();
		final List<ArchiveFile> archiveFiles = lister.parseFiles(filteredFiles);

		assertEquals(2, archiveFiles.size());
		final ArchiveFile ear1 = archiveFiles.get(0);
		final ArchiveFile ear2 = archiveFiles.get(1);

		ArchiveFile found1 = null;
		for (final ArchiveFile contained : ear1.contains()) {
			if (contained.getArchiveID().equals(targetArchive)) {
				found1 = contained;
			}
		}
		assertNotNull(found1);

		assertTrue(found1.containedIn().contains(ear1));
		assertTrue(found1.containedIn().contains(ear2));
	}

}
