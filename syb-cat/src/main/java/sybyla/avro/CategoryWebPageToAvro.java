package sybyla.avro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sybyla.generated.avro.CategoryWebPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kiko
 * Date: 10/8/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */

public class CategoryWebPageToAvro
{


  private static final Logger LOG = LoggerFactory.getLogger(CategoryWebPageToAvro.class);

  private int nRecordsPerFile = 2000;

  private File destinationPath;
  private String fileRoot;

  int nRecordsProcessed = 0;
  File currentOutputFile;

  List<File> files = new ArrayList<>();

  private AvroFileWriter writer;

  private static final String PART = ".part-";
  private static final String SUFFIX = ".avro";
  private static final int INDEX_LENGTH = 6;

  public CategoryWebPageToAvro(String destinationPath, String fileRoot){

    File dir  = new File(destinationPath);
    this.destinationPath = dir;
    this.fileRoot = fileRoot;
  }

  public void write(CategoryWebPage page) throws IOException, InstantiationException, IllegalAccessException {

    updateOutputFile();
    try {

        writer.write(page);
        nRecordsProcessed++;

        if (nRecordsProcessed%20 == 0){

         System.out.println(nRecordsProcessed + " records written to Avro files");
        }

    } catch (Exception e) {
      LOG.error("Error writing data to Avro file",e);
      writer.close();
    }
  }

  public void close(){
    if (writer != null){
      try {
        writer.close();
      } catch(IOException e){
        LOG.error("Error closing AvroFileWriter",e);
      }
      writer =  null;
    }

    nRecordsProcessed = 0;
    currentOutputFile = null;

    files = new ArrayList<>();
  }

  private void updateOutputFile() throws IllegalAccessException, IOException, InstantiationException {

    if (files.size() == 0  || (nRecordsProcessed % nRecordsPerFile == 0)){

      StringBuilder sb = new StringBuilder();

      String fileIndex = getFileIndex(files.size()+1);
      String path =  destinationPath.getPath();

      sb.append(path)
          .append(path.endsWith(File.separator) ? "" : File.separator)
          .append(fileRoot)
          .append(PART)
          .append(fileIndex)
          .append(SUFFIX);

      if (currentOutputFile != null) {
        LOG.info("Closing written file " + currentOutputFile);
        try {
          writer.close();
          writer =  null;
        } catch (IOException e) {
          LOG.error("Error closing file", e);
        }
      }

      currentOutputFile =  new File(sb.toString());
      if (writer ==  null){
         writer = new AvroFileWriter<CategoryWebPage>(CategoryWebPage.SCHEMA$, currentOutputFile);
      }
      files.add(currentOutputFile);

      LOG.info("Writing records to "+ currentOutputFile);
    }
  }

  public String getFileIndex(int n){

    StringBuilder sb = new StringBuilder();
    String nn = Integer.toString(n);
    long l = nn.length();

    for(int i = 1; i <= INDEX_LENGTH - l; i++){
        sb.append("0");
    }

    sb.append(nn);
    return sb.toString();
  }

  public int getnRecordsPerFile() {

    return nRecordsPerFile;
  }


  public void setnRecordsPerFile(int nRecordsPerFile) {

    this.nRecordsPerFile = nRecordsPerFile;
  }

  public List<File> getFiles() {
    return files;
  }
}
