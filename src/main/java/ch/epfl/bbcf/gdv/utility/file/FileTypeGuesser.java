package ch.epfl.bbcf.gdv.utility.file;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import ch.epfl.bbcf.bbcfutils.parsing.SQLiteExtension;
import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.control.model.InputControl.Extension;
import ch.epfl.bbcf.gdv.control.model.InputControl.ZipExtension;

public class FileTypeGuesser {

	/**
	 * Guess the extension of the file.
	 * Support gff,gff3,wig,wiggle,zip,gzip,gz.
	 * @param file
	 * @return
	 * @throws ExtensionNotRecognizedException 
	 */
	public static Extension guessExtension(File file) throws ExtensionNotRecognizedException{
		//Application.debug("guess extension");
		String fileName = file.getName();
		String tab[] = fileName.split("\\.");
		Extension ext = processExtension(tab[tab.length-1]);
		//Application.debug(ext);
		return ext;
	}

	/**
	 * Guess if the file is qualitative or quantitative,
	 * because process of the file will be differents.
	 * @throws ExtensionNotRecognizedException
	 * @throws IOException 
	 */
	public static SQLiteExtension guessFileType(File file,Extension ext) throws ExtensionNotRecognizedException, IOException{
		switch(ext){
		case GFF:case BED:case BEDGRAPH:case BAM:case SAM:
			return SQLiteExtension.QUALITATIVE;
		case GTF: 
			return SQLiteExtension.QUALITATIVE_EXTENDED;
		case  DB :
			ResultSet r = null;
			SQLiteAccess access = null;
			String chr = null;
			try {
				access = SQLiteAccess.getConnectionWithDatabase(file.getAbsolutePath());
				Map<String, Integer> chromosomes= access.getChromosomesAndLength();
				chr = chromosomes.keySet().iterator().next();
				r = access.prepareFeatures(chr);
				access.getNextQualitativeFeature(r,chr);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				return SQLiteExtension.QUANTITATIVE;
			}
			try {
				access.getNextExtendedQualitativeFeature(r, chr);
			} catch (SQLException e) {
				return SQLiteExtension.QUALITATIVE;
			}  finally {
				try {
					r.close();
					access.close();
				} catch (SQLException e) {
					Application.error(e);
				}
			}
			return SQLiteExtension.QUALITATIVE_EXTENDED;
		default :
			return SQLiteExtension.QUANTITATIVE;
		}
	}





	private static Extension processExtension(String ext) throws ExtensionNotRecognizedException{
		Extension extension = null;
		if(ext.equalsIgnoreCase("gff")||ext.equalsIgnoreCase("gff3")){
			extension = Extension.GFF;
		}
		else if (ext.equalsIgnoreCase("wig")||ext.equalsIgnoreCase("wiggle")){
			extension = Extension.WIG;
		}
		else if (ext.equalsIgnoreCase("bed")){
			extension = Extension.BED;
		}
		else if(ext.equalsIgnoreCase("zip")){
			extension = Extension.ZIP;
		}
		else if(ext.equalsIgnoreCase("gzip")){
			extension = Extension.GZIP;
		}
		else if(ext.equalsIgnoreCase("gz")){
			extension = Extension.GZ;
		}
		else if(ext.equalsIgnoreCase("bam")){
			extension = Extension.BAM;
		}
		else if(ext.equalsIgnoreCase("bedgraph")){
			extension = Extension.BEDGRAPH;
		} else if(ext.equalsIgnoreCase("gtf")){
			extension = Extension.GTF;
		} else if(ext.equalsIgnoreCase("db")||ext.equalsIgnoreCase("sql")){
			extension = Extension.DB;
		}

		else {
			throw new ExtensionNotRecognizedException(ext);
		}
		return extension;
	}
}