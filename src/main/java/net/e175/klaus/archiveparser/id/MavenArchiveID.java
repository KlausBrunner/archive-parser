package net.e175.klaus.archiveparser.id;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MavenArchiveID implements ArchiveID {
	private static final Pattern MVN_PATTERN = Pattern.compile("(.*)\\.(j|w|e|r)ar", Pattern.CASE_INSENSITIVE);
	private static final Pattern NAME_VERSION_PATTERN = Pattern.compile("(.*)-(\\d{1}.*)", Pattern.CASE_INSENSITIVE);

	private String name;
	private String version;
	private ArchiveType type = ArchiveType.OTHER;

	public MavenArchiveID() {
	}

	public MavenArchiveID(final String name, final String version, final ArchiveType type) {
		this.name = name;
		this.version = version;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public ArchiveType getType() {
		return type;
	}

	@Override
	public ArchiveID parse(final String filename) {
		final MavenArchiveID id = new MavenArchiveID();

		Matcher matcher = MVN_PATTERN.matcher(filename);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("filename " + filename + " is not in valid format " + MVN_PATTERN);
		}

		id.name = matcher.group(1);
		id.version = UNKNOWN_VERSION;

		switch (matcher.group(2).toLowerCase().charAt(0)) {
		case 'j':
			id.type = ArchiveType.JAR;
			break;
		case 'w':
			id.type = ArchiveType.WAR;
			break;
		case 'e':
			id.type = ArchiveType.EAR;
			break;
		case 'r':
			id.type = ArchiveType.RAR;
			break;
		default:
			id.type = ArchiveType.OTHER;
		}

		matcher = NAME_VERSION_PATTERN.matcher(id.name);
		if (matcher.matches()) {
			id.name = matcher.group(1);
			id.version = matcher.group(2);
		}

		return id;
	}

	@Override
	public boolean isApplicable(final String filename) {
		final Matcher matcher = MVN_PATTERN.matcher(filename);
		return matcher.matches();
	}

	@Override
	public String toString() {
		return name + PART_SEPARATOR + version + PART_SEPARATOR + type;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof MavenArchiveID && toString().equals(obj.toString());
	}
}
