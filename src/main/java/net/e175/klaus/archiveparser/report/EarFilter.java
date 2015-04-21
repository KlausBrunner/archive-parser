package net.e175.klaus.archiveparser.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.e175.klaus.archiveparser.id.ArchiveID;
import net.e175.klaus.archiveparser.id.ArchiveType;
import net.e175.klaus.archiveparser.id.MavenArchiveID;

public final class EarFilter {
	public List<File> filterEarFiles(final File directory) {
		final List<String> fileNames = Arrays.asList(directory.list());

		final List<String> filteredFileNames = filterEarFilenames(fileNames);

		final List<File> fileList = new ArrayList<File>();
		for (final String fileName : filteredFileNames) {
			fileList.add(new File(directory, fileName));
		}
		return fileList;
	}

	public List<String> filterEarFilenames(final Collection<String> names) {
		final List<String> sortable = new ArrayList<String>();

		final MavenArchiveID id = new MavenArchiveID();
		for (final String name : names) {
			if (id.isApplicable(name)) {
				final ArchiveID currentEntry = id.parse(name);
				if (currentEntry.getType() == ArchiveType.EAR) {
					sortable.add(name);
				}
			}
		}

		Collections.sort(sortable);
		return sortable;
	}
}
