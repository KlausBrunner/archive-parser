package net.e175.klaus.archiveparser.id;

public interface ArchiveID {
	String UNKNOWN_VERSION = "UNKNOWN";

	String PART_SEPARATOR = ":";

	String getName();

	String getVersion();

	ArchiveType getType();

	ArchiveID parse(String filename);

	boolean isApplicable(String filename);
}