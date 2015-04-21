package net.e175.klaus.archiveparser.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.e175.klaus.archiveparser.id.ArchiveID;
import net.e175.klaus.archiveparser.id.ArchiveIDProvider;
import net.e175.klaus.archiveparser.id.ArchiveType;

public class UniqueJarFactory implements ArchiveFileFactory {

	private final Map<ArchiveID, ArchiveFile> jars = new HashMap<ArchiveID, ArchiveFile>();

	@Override
	public synchronized ArchiveFile getInstanceFor(final File file, final ArchiveIDProvider idProvider) {
		final ArchiveID id = idProvider.idForFile(file);
		ArchiveFile archiveInstance = null;

		if (id.getType().equals(ArchiveType.JAR)) {
			final ArchiveFile storedFile = jars.get(id);
			if (storedFile != null) {
				archiveInstance = storedFile;
			} else {
				archiveInstance = new ArchiveFile(file, idProvider, this);
				jars.put(id, archiveInstance);
			}
		} else {
			archiveInstance = new ArchiveFile(file, idProvider, this);
		}

		return archiveInstance;
	}

}
