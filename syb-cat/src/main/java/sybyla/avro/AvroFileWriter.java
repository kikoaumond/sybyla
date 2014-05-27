package sybyla.avro;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: kiko
 * Date: 9/30/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class AvroFileWriter<T extends GenericContainer> {

  private static final Logger LOG = LoggerFactory.getLogger(AvroFileWriter.class);
  private DatumWriter<T> datumWriter ;
  private DataFileWriter<T> fileWriter;
  private int nRecords=0;
  private Class<T> clazz;
  private Schema schema;
  private File file;

  public AvroFileWriter(Class<T> clazz, String filename) throws IOException, InstantiationException, IllegalAccessException {

    if (clazz == null) throw new IllegalArgumentException("Class argument must not be null");
    if (filename == null|| filename.equals("")) {
      throw new IllegalArgumentException("File name  argument must not be null or empty. Filename:"+filename);
    }

    this.clazz =  clazz;
    this.file = new File(filename);
    init();
  }

    public AvroFileWriter(Schema schema, String filename) throws IOException, InstantiationException, IllegalAccessException {

      if (schema == null)  throw new IllegalArgumentException("Schema argument must not be null");
      if (filename == null|| filename.equals("")) {
        throw new IllegalArgumentException("File name  argument must not be null or empty. Filename:"+filename);
      }

      this.file = new File(filename);
      this.schema= schema;
      init();
    }


  public AvroFileWriter(Schema schema, File file) throws IOException, InstantiationException, IllegalAccessException {

    if (schema == null)  throw new IllegalArgumentException("Schema argument must not be null");
    if (file == null) {
      throw new IllegalArgumentException("File argument must not be null or empty. File:"+file);
    }

    this.file = file;
    this.schema= schema;
    init();
  }

    public void init() throws IOException, IllegalAccessException, InstantiationException {

      if (schema != null){
        datumWriter  = new SpecificDatumWriter(schema);
      }  else if (clazz != null){
        datumWriter = new SpecificDatumWriter<>(clazz);
      }

      fileWriter =  new DataFileWriter<T>(datumWriter);

      if (schema != null){
        fileWriter = fileWriter.create(schema, file);
      } else if (clazz != null )  {
        fileWriter = fileWriter.create(clazz.newInstance().getSchema(), file);
      }
    }

    public void write(T item) throws IOException {

        fileWriter.append(item);
        nRecords++;
    }

    public void close() throws IOException{

        fileWriter.close();
    }
}
