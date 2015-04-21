package net.e175.klaus.archiveparser.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.e175.klaus.archiveparser.core.ArchiveFile;
import net.e175.klaus.archiveparser.id.ArchiveID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class EarJarSpreadsheet {
	private Collection<ArchiveFile> archives;
	private Map<ArchiveID, List<ArchiveFile>> jarEarMap;

	public void analyze(final File sourceFolder, final EarFilter filter) {
		assert sourceFolder.isDirectory();

		final Collection<File> ears = filter.filterEarFiles(sourceFolder);

		final EarJarParser parser = new EarJarParser();
		archives = parser.parseFiles(ears);
		jarEarMap = parser.getJarEarIdMap(archives);
	}

	public void createWorkbook(final File targetFile) throws IOException {
		final Workbook book = new XSSFWorkbook();

		createEarJarSheet(book);
		createJarEarSheet(book);
		createJarUsageFrequencySheet(book);

		final FileOutputStream fileOut = new FileOutputStream(targetFile);
		book.write(fileOut);
		fileOut.close();
	}

	private void createEarJarSheet(final Workbook book) {
		final Sheet earSheet = book.createSheet("EARs with contained JARs");
		int rowCount = 0;
		int maxColumn = 0;

		for (final ArchiveFile archive : archives) {
			final Row row = earSheet.createRow(rowCount++);

			int cellCount = 0;
			Cell cell = row.createCell(cellCount++);
			String archiveName = archive.getArchiveID().toString();
			cell.setCellValue(archiveName);

			for (final ArchiveFile contained : archive.contains()) {
				cell = row.createCell(cellCount++);
				archiveName = contained.getArchiveID().toString();
				cell.setCellValue(archiveName);
				if (cellCount > maxColumn) {
					maxColumn = cellCount;
				}
			}
		}

		adjustColumns(earSheet, maxColumn);
	}

	private void createJarEarSheet(final Workbook book) {
		final Sheet jarSheet = book.createSheet("JARs with using EARs");

		int rowCount = 0;
		int maxColumn = 0;

		final ClientAnchor anchor = book.getCreationHelper().createClientAnchor();
		final Drawing drawing = jarSheet.createDrawingPatriarch();

		for (final Entry<ArchiveID, List<ArchiveFile>> jarEntry : jarEarMap.entrySet()) {
			final Row row = jarSheet.createRow(rowCount++);

			int cellCount = 0;
			final Cell jarCell = row.createCell(cellCount++);
			final ArchiveID jarID = jarEntry.getKey();
			final String archiveName = jarID.toString();
			jarCell.setCellValue(archiveName);

			for (final ArchiveFile ear : jarEntry.getValue()) {
				final Cell earCell = row.createCell(cellCount++);
				earCell.setCellValue(ear.toString());
				if (cellCount > maxColumn) {
					maxColumn = cellCount;
				}

				final Collection<ArchiveFile> concreteJars = getMatchingJarsInEar(jarID, ear);

				final StringBuilder commentString = new StringBuilder();
				for (final ArchiveFile concreteJar : concreteJars) {
					commentString.append(concreteJar).append(" ");
				}
				final Comment comment = drawing.createCellComment(anchor);
				final RichTextString str = book.getCreationHelper().createRichTextString(commentString.toString());
				comment.setString(str);
				earCell.setCellComment(comment);

			}
		}

		adjustColumns(jarSheet, maxColumn);
	}

	private void createJarUsageFrequencySheet(final Workbook book) {
		final Sheet jarSheet = book.createSheet("JAR usage frequency");

		int rowCount = 0;
		int maxColumn = 0;
		int cellCount = 0;

		final Row titleRow = jarSheet.createRow(rowCount++);
		final Cell jarTitle = titleRow.createCell(cellCount++);
		jarTitle.setCellValue("Archive");
		final Cell jarUsage = titleRow.createCell(cellCount++);
		jarUsage.setCellValue("Usage");

		for (final Entry<ArchiveID, List<ArchiveFile>> jarEntry : jarEarMap.entrySet()) {
			final Row row = jarSheet.createRow(rowCount++);

			cellCount = 0;
			final Cell jarCell = row.createCell(cellCount++);
			final ArchiveID jarID = jarEntry.getKey();
			final String archiveName = jarID.toString();
			jarCell.setCellValue(archiveName);

			final Cell usageCell = row.createCell(cellCount++);
			final int usageCount = jarEntry.getValue().size();
			usageCell.setCellValue(usageCount);

			if (cellCount > maxColumn) {
				maxColumn = cellCount;
			}
		}

		adjustColumns(jarSheet, maxColumn);
	}

	private Collection<ArchiveFile> getMatchingJarsInEar(final ArchiveID unversionedJarID, final ArchiveFile ear) {
		final List<ArchiveFile> concreteVersions = new ArrayList<ArchiveFile>();
		for (final ArchiveFile concreteJar : ear.contains()) {

			if (concreteJar.equalsExceptVersion(unversionedJarID)) {
				concreteVersions.add(concreteJar);
			}
		}
		return concreteVersions;
	}

	private void adjustColumns(final Sheet earSheet, final int maxColumn) {
		for (int i = 0; i < maxColumn; i++) {
			earSheet.autoSizeColumn(i);
		}
	}
}
