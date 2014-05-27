package sybyla.avro;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: kiko
 * Date: 9/30/13
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class AvroFileReader<T extends GenericContainer> {

    private DatumReader<T> datumReader;
    private DataFileReader<T> fileReader;
    private Class<T> clazz;
    private Schema schema;
    private File file;
    private int read=0;

    public AvroFileReader(Class<T> clazz, String filename) throws IOException {

      if (clazz == null)  throw new IllegalArgumentException("Class argument must not be null");

      if (filename == null|| filename.equals("")) {
        throw new IllegalArgumentException("File name  argument must not be null or empty. Filename: "+filename);
      }

      this.clazz =  clazz;
      this.file =  new File(filename);

      init();
    }

  public AvroFileReader(Schema schema, File file) throws IOException {

    if (schema == null)  throw new IllegalArgumentException("Schema argument must not be null");

    if (file == null) {
      throw new IllegalArgumentException("File argument must not be null.");
    }

    this.schema = schema;
    this.file = file;

    init();
  }

  public AvroFileReader(Schema schema, String filename) throws IOException {

    if (schema == null)  throw new IllegalArgumentException("Schema argument must not be null");

    if (filename == null|| filename.equals("")) {
      throw new IllegalArgumentException("File name  argument must not be null or empty. Filename: "+filename);
    }

    this.schema =  schema;
    this.file =  new File(filename);

    init();
  }

    public void init() throws IOException {

        if (schema != null){
          datumReader = new SpecificDatumReader<>(schema);
        }  else if (clazz != null){
          datumReader = new SpecificDatumReader<>(clazz);
        }  else {
          datumReader =  new SpecificDatumReader<>();
        }
        fileReader =  new DataFileReader<>(file,datumReader);
    }

    public T readNext() {

      if (fileReader.hasNext()){
        read++;
        return fileReader.next();
      }   else {
        return null;
      }
    }

  public int getRead(){

    return read;
  }

    public void close() throws IOException{

      fileReader.close();
    }
}
