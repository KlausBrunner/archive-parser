package net.e175.klaus.archiveparser.id;

public enum ArchiveType {
	JAR(false, true), WAR(true, true), EAR(true, false), RAR(true, false), OTHER(false, true);

	private final boolean canContainArchives;
	private final boolean canBeContainedInArchives;

	ArchiveType(final boolean canContainArchives, final boolean canBeContainedInArchives) {
		this.canContainArchives = canContainArchives;
		this.canBeContainedInArchives = canBeContainedInArchives;
	}

	public boolean canContainArchives() {
		return canContainArchives;
	}

	public boolean canBeContainedInArchives() {
		return canBeContainedInArchives;
	}

}
