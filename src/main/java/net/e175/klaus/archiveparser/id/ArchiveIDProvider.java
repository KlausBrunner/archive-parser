package net.e175.klaus.archiveparser.id;

import java.io.File;

public interface ArchiveIDProvider {
	ArchiveID idForFile(File archiveFile);
}
