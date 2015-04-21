package net.e175.klaus.archiveparser.id;

import java.io.File;
import java.util.Locale;

public final class MavenArchiveIDProvider implements ArchiveIDProvider {

	@Override
	public ArchiveID idForFile(final File archiveFile) {
		final String name = archiveFile.getName().toLowerCase(Locale.ENGLISH);

		if (name.endsWith(".ear")) {
			return new MavenArchiveID().parse(name);
		} else if (name.endsWith(".war") || name.endsWith(".jar") || name.endsWith(".rar")) {
			return new MavenArchiveID().parse(name);
		} else {
			return new MavenArchiveID();
		}
	}

}
