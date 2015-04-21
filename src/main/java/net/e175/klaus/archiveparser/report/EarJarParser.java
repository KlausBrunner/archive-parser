package net.e175.klaus.archiveparser.report;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.e175.klaus.archiveparser.core.ArchiveFile;
import net.e175.klaus.archiveparser.core.ArchiveFileFactory;
import net.e175.klaus.archiveparser.core.UniqueJarFactory;
import net.e175.klaus.archiveparser.id.ArchiveID;
import net.e175.klaus.archiveparser.id.ArchiveIDProvider;
import net.e175.klaus.archiveparser.id.MavenArchiveID;
import net.e175.klaus.archiveparser.id.MavenArchiveIDProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EarJarParser {

	private final ArchiveIDProvider defaultIDProvider = new MavenArchiveIDProvider();

	private static final class ArchiveIDComparator implements Comparator<ArchiveID>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(final ArchiveID o1, final ArchiveID o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(EarJarParser.class);

	public List<ArchiveFile> parseFiles(final Collection<File> files) {
		final List<ArchiveFile> archives = new ArrayList<ArchiveFile>();
		final ArchiveFileFactory archiveFactory = new UniqueJarFactory();

		return parseFiles(files, archives, archiveFactory);
	}

	public List<ArchiveFile> parseFiles(final Collection<File> files, final List<ArchiveFile> archives,
			final ArchiveFileFactory archiveFactory) {

		for (final File f : files) {
			parseFile(f, archives, archiveFactory);
		}

		return archives;
	}

	public List<ArchiveFile> parseFile(final File file, final List<ArchiveFile> archives,
			final ArchiveFileFactory archiveFactory) {
		assert file != null;
		assert archives != null;

		LOG.info("parsing {}", file);
		final ArchiveFile a = new ArchiveFile(file, defaultIDProvider, archiveFactory);
		archives.add(a);
		return archives;
	}

	public Map<ArchiveID, List<ArchiveFile>> getJarEarIdMap(final Collection<ArchiveFile> parsedArchives) {
		final Map<ArchiveID, List<ArchiveFile>> jarEarIdMap = new TreeMap<ArchiveID, List<ArchiveFile>>(
				new ArchiveIDComparator());

		for (final ArchiveFile ear : parsedArchives) {
			for (final ArchiveFile jar : ear.contains()) {
				final ArchiveID jarId = jar.getArchiveID();
				final ArchiveID unversionedJarId = new MavenArchiveID(jarId.getName(), "*", jarId.getType());

				List<ArchiveFile> ears = jarEarIdMap.get(unversionedJarId);
				if (ears == null) {
					ears = new ArrayList<ArchiveFile>();
				}
				if (!ears.contains(ear)) {
					ears.add(ear);
				}
				jarEarIdMap.put(unversionedJarId, ears);
			}
		}
		return jarEarIdMap;
	}

}
