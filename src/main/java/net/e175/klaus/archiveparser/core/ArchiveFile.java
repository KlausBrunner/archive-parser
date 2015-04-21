package net.e175.klaus.archiveparser.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.e175.klaus.archiveparser.id.ArchiveID;
import net.e175.klaus.archiveparser.id.ArchiveIDProvider;
import net.e175.klaus.archiveparser.id.ArchiveType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.io.File;

public final class ArchiveFile implements Comparable<ArchiveFile> {
	static class DefaultFactory implements ArchiveFileFactory {
		@Override
		public ArchiveFile getInstanceFor(final java.io.File file, final ArchiveIDProvider idProvider) {
			return new ArchiveFile(file, idProvider, this);
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(ArchiveFile.class);

	private ArchiveID archiveId;
	private List<ArchiveFile> containedArchives;
	private List<ArchiveFile> containedInArchives;

	/** ignore WARs (treat them like directories) */
	public static final boolean TREAT_WARS_AS_FOLDERS = true; // TODO make this
																// configurable

	public ArchiveFile(final java.io.File file, final ArchiveIDProvider idProvider,
			final ArchiveFileFactory archiveFileFactory) {
		initialise(file, idProvider, archiveFileFactory == null ? new DefaultFactory() : archiveFileFactory);
	}

	public ArchiveFile(final java.io.File file, final ArchiveIDProvider idProvider) {
		final ArchiveFileFactory archiveFileFactory = new DefaultFactory();
		initialise(file, idProvider, archiveFileFactory);
	}

	private void initialise(final java.io.File file, final ArchiveIDProvider idProvider,
			final ArchiveFileFactory archiveFileFactory) {
		final File thisFile = new File(file);
		containedArchives = new LinkedList<ArchiveFile>();
		containedInArchives = new LinkedList<ArchiveFile>();
		archiveId = createArchiveID(thisFile, idProvider);

		parse(thisFile, idProvider, archiveFileFactory);
		Collections.sort(containedArchives);
	}

	private void parse(final File thisFile, final ArchiveIDProvider idProvider,
			final ArchiveFileFactory archiveFileFactory) {
		if (thisFile.exists() && thisFile.canRead()) {
			final List<File> containedArchiveFiles = getAllArchivesInAllSubfoldersButNotInArchives(thisFile,
					new LinkedList<File>());

			if (containedArchiveFiles.size() > 0 && !getArchiveID().getType().canContainArchives()) {
				LOG.warn(
						"Archive {} cannot contain archives according to type, but contained archives were found. Ignoring them.",
						this);
				return;
			}

			for (final File archiveFile : containedArchiveFiles) {
				final ArchiveFile contained = archiveFileFactory.getInstanceFor(archiveFile, idProvider);

				if (contained.getArchiveID().getName() == null) {
					LOG.warn("Ignoring unknown archive {} ({})", contained, archiveFile);
					continue;
				}

				updatedContainedInListFor(contained);

				if (TREAT_WARS_AS_FOLDERS && contained.getArchiveID().getType().equals(ArchiveType.WAR)) {
					// don't add the archive as such, just whatever it contains
					containedArchives.addAll(contained.contains());
					for (final ArchiveFile containedInWar : contained.contains()) {
						updatedContainedInListFor(containedInWar);
					}
				} else {
					containedArchives.add(contained);
				}
			}
		} else {
			throw new IllegalArgumentException("file does not exist or is not readable: " + thisFile);
		}
	}

	private void updatedContainedInListFor(final ArchiveFile contained) {
		if (contained.archiveId.getType().canBeContainedInArchives()) {
			contained.containedInArchives.add(this);
		} else {
			throw new IllegalStateException("Archive " + contained + " is contained in " + this
					+ ", but cannot be according to its type");
		}
	}

	private ArchiveID createArchiveID(final File thisFile, final ArchiveIDProvider idProvider) {
		if (idProvider != null) {
			return idProvider.idForFile(thisFile);
		} else {
			return null;
		}
	}

	private List<File> getAllArchivesInAllSubfoldersButNotInArchives(final File root, final List<File> collectedFiles) {
		final File archive = new File(root);

		final java.io.File[] files = archive.listFiles();
		if (files != null) {
			for (final java.io.File onefile : files) {
				final File f = new File(onefile);
				if (f.isArchive()) {
					collectedFiles.add(f);
				} else if (f.isDirectory()) {
					collectedFiles.addAll(getAllArchivesInAllSubfoldersButNotInArchives(f, new LinkedList<File>()));
				}
			}
		}

		return collectedFiles;
	}

	public List<ArchiveFile> contains() {
		return Collections.unmodifiableList(containedArchives);
	}

	public List<ArchiveFile> containedIn() {
		return Collections.unmodifiableList(containedInArchives);
	}

	@Override
	public String toString() {
		return archiveId.toString();
	}

	public ArchiveID getArchiveID() {
		return archiveId;
	}

	@Override
	public int compareTo(final ArchiveFile o) {
		assert o != null;
		return toString().compareTo(o.toString());
	}

	public boolean equalsExceptVersion(final ArchiveFile other) {
		return other != null && equalsExceptVersion(other.getArchiveID());
	}

	public boolean equalsExceptVersion(final ArchiveID other) {
		return other != null && archiveId != null && archiveId.getName().equals(other.getName()) && archiveId.getType().equals(other.getType());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ArchiveFile) {
			final ArchiveFile other = (ArchiveFile) obj;
			return compareTo(other) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getArchiveID().hashCode();
	}

}
