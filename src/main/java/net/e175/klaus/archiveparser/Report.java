package net.e175.klaus.archiveparser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.e175.klaus.archiveparser.report.EarFilter;
import net.e175.klaus.archiveparser.report.EarJarSpreadsheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Report {

	private static final Logger LOG = LoggerFactory.getLogger(Report.class);

	private Report() {
	}

	public static void main(final String[] args) throws IOException {
		if (args.length < 1 || args.length > 2) {
			LOG.error("usage: {} <eardirectory> [outputfile.xlsx]", Report.class);
			System.exit(1); // NOPMD
		}

		final String directory = args[0];
		final String target;
		if (args.length >= 2) {
			target = args[1];
		} else {
			final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			target = "ears_" + dateFormat.format(new Date()) + ".xlsx";
		}

		LOG.info("analyzing {} and writing report to XLSX file {} ", directory, target);

		final EarJarSpreadsheet spread = new EarJarSpreadsheet();
		spread.analyze(new File(directory), new EarFilter());
		spread.createWorkbook(new File(target));

		LOG.info("done.");
	}

}
