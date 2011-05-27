package ch.epfl.bbcf.gdv.utility.file;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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
	 * just test (if the file is gff) if there is score in the file
	 * TODO ALWAYS RETURN QUALITATIVE
	 * @param file
	 * @return 'qualitative' or 'quantitative'
	 * @throws ExtensionNotRecognizedException
	 * @throws IOException 
	 */
	public static String guessFileType(File file,Extension ext) throws ExtensionNotRecognizedException, IOException{
		switch(ext){
		case GFF:case BED:case GTF:case BEDGRAPH:case BAM:case SAM:
			return "qualitative";
		case  DB :
			ResultSet r = null;
			SQLiteAccess access = null;
			try {
				access = SQLiteAccess.getConnectionWithDatabase(file.getAbsolutePath());
				Map<String, Integer> chromosomes= access.getChromosomesAndLength();
				String firstChromosome = chromosomes.keySet().iterator().next();
				r = access.prepareQualitativeFeatures(firstChromosome);
				access.getNextQualitativeFeature(r);
				r.close();
				access.close();
				return "qualitative";
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				return "quantitative";
			} finally {
				try {
					r.close();
					access.close();
				} catch (SQLException e) {
				}
			}

			default :
				return "quantitative";
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