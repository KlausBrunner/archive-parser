package net.e175.klaus.archiveparser.core;

import java.io.File;

import net.e175.klaus.archiveparser.id.ArchiveIDProvider;

public interface ArchiveFileFactory {

	ArchiveFile getInstanceFor(final File file, final ArchiveIDProvider idProvider);

}
