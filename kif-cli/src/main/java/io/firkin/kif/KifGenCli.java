/*
 * Copyright © 2021 Kif Contributors (https://kif.firkin.io/)
 * Copyright © 2021 Firkin•IO (https://firkin.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.firkin.kif;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.firkin.kif.utils.RecordInputStream;
import io.firkin.kif.utils.RecordOutputStream;
import io.firkin.kif.utils.RecordStreams;
import org.jline.builtins.Options;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.out;

public class KifGenCli {

  private static final String[] OPTIONS;
  private static final String[] DESCRIPTION;
  private static final String[] USAGE;

  private static Options options;
  private static boolean verbose;

  private enum RecordFormat {
    AVRO,      // bytes     --> https://avro.apache.org/
    RAW,       // bytes     --> How to delineate records safely?
    JSON,      // text      --> One object per line?
    PROTOBUF;   // bytes     --> https://developers.google.com/protocol-buffers

    private final String scheme;

    RecordFormat() {
      this.scheme = this.name().toLowerCase();
    }
  }

  private enum Opts {
    HELP("h", "help"),
    VERBOSE("v", "verbose"),
    QUIET("q", "quiet"),
    OUTPUT("o", "output"),
    INPUT("i", "input");

    String longName;
    String shortName;

    Opts(String shortName, String longName) {
      this.shortName = shortName;
      this.longName = longName;
    }

    public String toString() {
      return name().toLowerCase();
    }
  }

  static {
    List<String> descList = List.of(
        "kifkat -  KIF cli utility for generating or generate random data",
        "Usage:",
        "   kifkat [OPTIONS] [TOPIC]"
    );

    DESCRIPTION = descList.toArray(new String[0]);

    List<String> optList = List.of(
        "Options:",
        "  -v --verbose                 produce more verbose output",
        "  -q --quiet                   produce less verbose output",
        "  -h --help                    Show help",
        "  -o --output=FORMAT           Output data in the specified output format: avro, binary, base64, json, or protobuf",
        "  -i --input=FORMAT            Input data read in one of avro, binary, base64, json, or protobuf."
//      "  -s --schema=SCHEMAID         Schema for decoding or encoding records.",
//      "     --schemafile=FILE       Load schema from a file for reading/writing records.",
//      "     --stats                   Print stats",
//      "  -l --log=LEVEL:FILE      Log to a file at the desired level, one of:\n" +
//      "                               {(t)race,(d)ebug,(i)nfo,(w)arn,(e)rror}. Default level is \"info\".",
    );
    OPTIONS = optList.toArray(new String[0]);

    List<String> usageList = new ArrayList<>(DESCRIPTION.length + OPTIONS.length);
    usageList.addAll(descList);
    usageList.addAll(optList);
    USAGE = usageList.toArray(new String[0]);
  }


  public static void main(String[] argv) throws IOException, MalformedURLException {

    options = Options.compile(OPTIONS).parse(argv);
    if (options.isSet(Opts.VERBOSE.longName)) {
      verbose = true;
    }
    if (options.isSet(Opts.HELP.longName)) {
      printUsage();
      exit(1);
    }

    /*
     * To read data from a topic to a file:
     *
     * $> kifgen -i avro:kafka:topic://foobar avro:file://home/travis/sample.data
     * $> kifgen -i json:kafka:topic://foobar avro:file://home/travis/sample.data
     * $> kifgen -i protobuf:kafka:topic://foobar json:file://home/travis/sample.data
     *
     *
     * Read from / write to files.
     *
     * Using System In / System Out (default)  bytes in / bytes out
     * kifgen < file.json > file.avro
     *
     * # Read json from SysIn, write out to SysOut as avro
     * kifgen -i json -o avro < infile.json > outfile.avro
     * kifgen -i json:sys:in -o avro:sys:out
     *
     * # Read json from "infile.json", write avro to "outfile.avro"; both in current directory
     * kifgen -i json:infile.json -o avro:outfile.avro
     * kifgen -i json:./infile.json -o avro:./outfile.avro
     * kifgen -i json:file://./infile.json -o avro:file://./outfile.avro #TODO What is correct URI
     *
     * # Read json from absolute path, write avro to absolute path.
     * kifgen -i json:/usr/tmp/data/infile.json -o avro:/Users/thoffman/tmp/outfile.avro
     * kifgen -i json:file:///usr/tmp/data/infile.json -o avro:file:///Users/thoffman/tmp/outfile.avro #TODO What is correct URI
     *
     * # Read json from a remote API call
     * kifgen -i json:https://api.mockaroo.com/data/${generatorId}?count=10&foo=bar
     * kifgen -i json:kafka://{url-to-cluster}/topic/foo_bar_baz
     * kifgen -i json:kafka://{cluster-alias-from-config}[:9092]/topic/foo_bar_baz
     *
     * # Read objects from an data generator, via a plugin?
     * kifgen -i json:javafaker://{alias? type-seed}?       # Special builtin or plugin?
     * kifgen -i json:mockaroo://{alias? generatorid}?      # Special builtin or plugin?
     *
     */
    String inDescription = "";
    String inFormatStr = "";
    String inLocationStr = "";
    Path inLocalPath = null;
    RecordFormat inFormat = null;
    InputStream inStream = null;

    // --- Configure Input -------------------------------- >>

    String inOptValue = options.get("input");
    if (options.isSet("input")) {
      inFormat = getRecordFormat(inOptValue); // JSON, AVRO, PROTOBUF, RAW
      inFormatStr = inFormat.scheme;
      inLocationStr = getRecordLocation(inOptValue); // json:{something} -> {something}, {something} -> {something}
      if (isSystemStream(inLocationStr)) { // Handle system in pipe
        inStream = getInputStream(inLocationStr);
        inDescription = inFormatStr + ":sys:in";
      } else if (isLocalFile(inLocationStr)) { // Handle a file
        inLocalPath = getLocalPath(inLocationStr);
        inDescription = inLocalPath.toString();
      } else if (isBuiltInDataGenerator(inLocationStr)) { // Handle a data generator (possibly a plugin?)
//        DataGenerator dataGenerator;
//        inGenerator = getDataGenerator(inLocationStr);
//      } else if (isKafkaUri(inLocationStr)) { // Handle a kafka: url
        // -i json:kafka://pkc1234.confluent.cloud/{topic-id}
        // -b --bootstrap kafka://pkc1234.confluent.cloud/ (infers port 9092)
        // -r --registry (schema registry url)
//        inKafkaUrl = getKafkaUrl(inLocationStr);
//      } else if (isKnownUri(inLocationStr)) { // Handle
//        inUrl = getRecordUrl(inLocationStr);
      } else /* is a url? */ {
        // Unrecognized input source...
        err.println("Incorrect or Unsupported input source:");
//        System.exit(1);
      }
    } else { // If input parameter not defined, treat input as raw bytes.
      inFormat = RecordFormat.RAW;
      inStream = new BufferedInputStream(System.in);
      inDescription = "raw:sys:in";
    }

    // --- Configure Output ------------------------------- >>

    String outDescription = "";
    String outFormatStr = "";
    String outLocationStr = "";

    RecordFormat outFormat = null;
    OutputStream outStream = null;
    Path outLocalPath = null;

//    URL outUrl = null;

    String outOptValue = options.get("output");
    if (options.isSet("output")) {
      outFormat = getRecordFormat(outOptValue); // JSON, AVRO, PROTOBUF, RAW
      outFormatStr = outFormat.scheme;
      outLocationStr = getRecordLocation(outOptValue); // json:{something} -> {something}, {something} -> {something}
      if (isSystemStream(outLocationStr)) { // Handle system out/err pipe
        outStream = getOutputStream(outLocationStr);
        outDescription = "sys:out";
      } else if (isLocalFile(outLocationStr)) { // Handle a file
        outLocalPath = getLocalPath(outLocationStr);
        outDescription = outLocalPath.toString();
        /*
      } else if (isKafkaUri(inLocationStr)) { // Handle a kafka: url
        // -i json:kafka://pkc1234.confluent.cloud/{topic-id}
        // -b --bootstrap kafka://pkc1234.confluent.cloud/ (infers port 9092)
        // -r --registry (schema registry url)
        inKafkaUrl = getKafkaUrl(inLocationStr);
      } else if (isKnownUri(inLocationStr)) { // Handle
        inUrl = getRecordUrl(inLocationStr);
        */
      } else /* is a url? */ {
        // Unrecognized output source...
        err.println("Incorrect or Unsupported output sink");
      }
      err.println("  outOptValue="+outOptValue);
    } else { // No output defined, fall back to System.out
      outFormat = RecordFormat.RAW;
      outFormatStr = outFormat.scheme;
      outStream = new BufferedOutputStream(System.out);
      outDescription = "sys:out";
    }

    // TODO confirm we're not attempting to read AND write from the same local file.
    // TODO confirm we're not attempting to read from sys:out or sys:err
    // TODO confirm we're not attempting to write to sys:in

    err.printf("Reading %s from %s\n", inFormatStr, inDescription);
    if (verbose) {
      err.println("  inOptValue="+inOptValue);
      err.println("  inLocationStr="+inLocationStr);
      err.println("  inLocalPath=" + inLocalPath);
    }

    err.printf("Writing %s to %s\n", outFormatStr, outDescription);
    if (verbose) {
      err.println("  outOptValue="+outOptValue);
      err.println("  outLocationStr=" + outLocationStr);
      err.println("  outLocalPath=" + outLocalPath);
    }

    // TODO Determine the POJO Type(s) we are reading / writing; needed so that we can convert
    //      from one serialization format to another as needed.

    // TODO Initialize a Record Input Stream based on the configuration discovered so far.
    // Determine the type of the

    KafkaAvroSerializer     inAvroSerializer;
    AvroSchema inAvroSchema = null;
    KafkaJsonSerializer     inJsonSerializer;
    JsonSchema inJsonSchema = null;
    KafkaProtobufSerializer inProtobufSerializer;
    ProtobufSchema          inProtobufSchema = null;
//    KafkaRawSerializer      inRawSerializer;
//    RawSchema               inRawSchema = null;

    RecordStreams inBuilder = RecordStreams.stream();
    if (inLocalPath != null) { // Read from a file
      inBuilder.from(inLocalPath);
    } else if (inStream != null) { // Read from System.in
      inBuilder.from(inStream);
//    } else if (inUrl != null) {
//      inBuilder.from(inUrl);
//    } else if (inKafkaTopic != null) {
//    } else if (inDataGenerator != null) {
//    } else {
    }

    RecordInputStream recordInputStream = inBuilder.in();

    String inSchema = "";
    switch (inFormat) {
      case AVRO:
        if (inAvroSchema != null) inSchema = inAvroSchema.toString();
        inBuilder.avro(inSchema);
        break;
      case JSON:
        if (inJsonSchema != null) inSchema = inJsonSchema.toString();
        inBuilder.json(inSchema);
        break;
      case PROTOBUF:
        if (inProtobufSchema != null) inSchema = inProtobufSchema.toString();
        inBuilder.protobuf(inSchema);
        break;
      case RAW:
      default:
        //inSchema = null;
        //inBuilder.raw(); // actually do nothing
        break;
    }

    KafkaAvroDeserializer      outAvroDeserializer;
    AvroSchema                 outAvroSchema = null;
    KafkaJsonDeserializer      outJsonDeserializer;
    JsonSchema                 outJsonSchema = null;
    KafkaProtobufDeserializer  outProtobufSerializer;
    ProtobufSchema             outProtobufSchema = null;
//    KafkaRawDeserializer       outRawSerializer;
//    RawSchema                  outRawSchema = null;
    
    RecordStreams outBuilder = RecordStreams.stream();
    if (outLocalPath != null) { // Write to a file
      outBuilder.to(outLocalPath);
    } else if (outStream != null) { // Write to System.out or System.err
      outBuilder.to(outStream);
//    } else if (outUrl != null) {
//      streamBuilder.to(outUrl);
//    } else if (outKafkaTopic != null) {
//    } else if (outDataGenerator != null) {
//    } else {
    }

    String outSchema = "";
    switch (outFormat) {
      case AVRO:
        if (outAvroSchema != null) outSchema = outAvroSchema.toString();
        outBuilder.avro(outSchema);
        break;
      case JSON:
        if (outJsonSchema != null) outSchema = outJsonSchema.toString();
        outBuilder.json(outSchema);
        break;
      case PROTOBUF:
        if (outProtobufSchema != null) outSchema = outProtobufSchema.toString();
        outBuilder.protobuf(outSchema);
        break;
      case RAW:
      default:
        //outSchema = null;
        //outBuilder.raw(); // actually do nothing
        break;
    }

    RecordOutputStream recordOutputStream = outBuilder.out();

    /*
     * TODO Implement the primary read/transform/write loop, based on the configuration:
     *   1. Create a RecordInputStream which handles reading in records (one at a time) from the configured source:
     *      a. (stream)   e.g. System.in
     *      b. (file)     e.g. Local files like ./data.json, /var/data/file-1.avro, etc.
     *      c. (topic)    e.g. kafka-cluster:topic-a, lkc10c1.confluent.cloud, etc. via a kafka client.
     *      d. (datagen)  e.g. Mockaroo, JavaFaker, etc. (in memory or remote calls?)
     *      e. (url)      e.g. https://api.example.com/customers/ or maybe a REST listener?
     *      f. (connect)  e.g. Source Connectors?
     *   2. Create a RecordOutputStream which handles writing records (one at a time) to the configured destination:
     *      a. (stream)   e.g. System.out or System.err
     *      b. (file)     e.g. Local files like ./data.json, /var/data/file-1.avro, etc.
     *      c. (topic)    e.g. kafka-cluster:topic-a, lkc10c1.confluent.cloud, etc. via a kafka client.
     *      d. (connect)  e.g. Sink Connectors?
     *      e. (url)      e.g.
     *   3. Create Queues for inter-job coordination; might only need one in some cases?
     *      a. An Input Queue which is written to by the ReadJob and read from by the TransformJob.
     *      b. An Output Queue which is written to by the TransformJob and read from by the WriteJob.
     *   4. Create a ThreadExecutor pool for running a ReadJob, TransformJob, and WriteJob in parallel.
     *   5. Create a ReadJob ...
     *   6. Create a TranformJob which performs a transformation on input Records before outputting them.
     *      a. (kstream)  e.g. A KStream app which can take input from any of the above?
     *      b. (lambda)   e.g. An implementation of the interface Functional
     *      c. (script)   e.g. Longer term, implement a scriptable transformation (like SMTs? Gherkin?)
     *      d. (builtins) e.g. Serialization format: json->avro, avro->protobuf, Batching: 1->N, N->1, N->M
     *   7. Create a WriteJob ...
     *   8. Launch the Jobs ...
     *   9. Track Input and Terminal Signals to close/quit cleanly...
     *
     * NOTE Avoid implementing "too much", follow the principle of single-purpose unix commands. In short,
     *      this application should only provide a single read -> transform -> write workflow.
     */

    //Executors.newSingleThreadExecutor().submit(() -> )

    /*
     * TODO Write a system for storing / tracking configurations (~/.kif, ~/.ccloud, ~/.confluent) which can
     *      be easily referenced by aliases, tracks the default, etc. to make command lines as simple as
     *      possible for the "simple case".
     *
     * $ kifkat < data.json > data.avro
     * $ kifkat -i json:data.json -o avro:data.avro
     * $ kifkat -i mockaroo:a3crve71a -o json:test1.json
     * $ kifkat -i mockaroo:a3crve71a -o kafka://kafka-dev/test_topic
     */

    /*
     * TODO Think about error handling. Perhaps it's as simple as:
     *
     * $ kifkat -i json -o avro -e json < input.json 1> output.avro 2>error.json
     * $ kifkat -i json:input.json -o avro:output.avro -e json:error.json
     */


    // TODO Configure a shutdown handler for longer-running or interactive processes to enable a clean shutdown.
    System.exit(0);
  }

  public static void printUsage() {
    List.of(USAGE).forEach(out::println);
  }

  private static Pattern serdesFormatPattern = Pattern.compile("(json|avro|protobuf|raw):(.*)");

  public static boolean isFormatSpecified(String uriStr) {
    return serdesFormatPattern.matcher(uriStr).matches();
  }

  public static URL getRecordUrl(String uriStr) {
    /*
     * File URLs:
     *   json:file:///etc/hosts
     *   avro:file:./data/example1.avro
     *   raw:file://
     *
     * System In/Out URLs:
     *
     *   json:in
     *   avro:out
     *   raw:err
     *
     * Known services:
     *
     *   mockaroo:key@/api/3cqard
     *   https://foo@api.mockaroo.com/api/cx2roc3sar?count=1000
     */
    URI uri = URI.create(uriStr);

    if (uri.getScheme() == null) {
      // No scheme, assume file.
      uriStr = "file://" + uriStr;
    }
    err.println("uriStr=" + uriStr);
    try {
      URL url = new URL(uriStr);
      return url;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

//  public static RecordFormat getRecordFormat(String optValue) {
//    RecordFormat format = RecordFormat.RAW;
//    format = getFor
//    if (isFormatSpecified(optValue)) {
//      format =
//    }
//
//    RecordFormat format;
//    Pattern p = Pattern.compile("(json|avro|protobuf):(.*)");
//    Matcher m = p.matcher(optValue);
//
//    if (isFormatSpecified(optValue) && m.group(1).matches("(json|avro|protobuf)")) {
//      format = RecordFormat.valueOf(m.group(1).toUpperCase());
//    } else {
////      err.println("  Unsupported record format: \"" + optValue + "\"");
//      format = RecordFormat.RAW;
//    }
//    return format;
//  }

  public static InputStream getInputStream(String optValue) throws IOException {
//    String inOptValue = options.get("input");
    out.println("  Input=\"" + optValue + "\"");

    String urlValue = "";
    if (isFormatSpecified(optValue)) {
      urlValue = optValue.substring(optValue.indexOf(':') + 1);
    } else {
      urlValue = optValue;
    }
    out.println("urlValue=" + urlValue);
    return getRecordUrl(urlValue).openStream();
  }

  public static OutputStream getOutputStream(String optValue) {
    String outOptValue = options.get("output");
    out.println("  Output=\"" + outOptValue + "\"");

    Pattern p = Pattern.compile("(json|avro|protobuf):(.*)");
    Matcher m = p.matcher(outOptValue);
    out.println("    m.matches()=" + m.matches());
    out.println("    m.group(0)=\"" + m.group(0) + "\"");
    out.println("    m.group(1)=\"" + m.group(1) + "\"");
    out.println("    m.group(2)=\"" + m.group(2) + "\"");
    err.println("group(2): \"" + m.group(2) + "\"");
    OutputStream outStream = null;

    if (m.group(2) == null || m.group(2).isEmpty()) {
      // Input to be drawn from System.in
      err.println("Reading from System.in");
      outStream = new BufferedOutputStream(out);
    } else {
      URI uri = URI.create(m.group(2));
      String scheme = uri.getScheme();
      err.println("URI: " + uri);
      err.println("URI.scheme: " + uri.getScheme());
      err.println("URI.path: " + uri.getPath());
      if (scheme == null || scheme.equals("file")) {
        try {
          outStream = new BufferedOutputStream(new FileOutputStream(Path.of(uri.getPath()).toFile()));
        } catch (FileNotFoundException e) {
          err.println("Could not find input file: \"" + uri.getPath() + "\"");
          exit(1);
        }
      /*
      } else {
        // TODO Open and output stream using a REST Client, Kafka Producer, etc.
        try {
          outStream = new BufferedOutputStream(uri.toURL().openStream());
        } catch (MalformedURLException e) {
          err.println("Malformed URL: \"" + uri.toASCIIString() + "\"");
          exit(-1);
        } catch (InvalidPathException e) {
          err.println("Could not resolve Path: \"" + uri.getPath() + "\"");
        } catch (IOException e) {
          err.println("Could not open stream from \"" + uri.getPath() + "\"");
        }
       */
      }
    }
    return outStream;
  }

  // --- Private Implementation Methods ---------------------------------------------------------------------

  /**
   * Retrieves the Record Format from an input (or output) argument string.
   *
   * Examples:
   *   -i "json:file:///etc/foo.bar" -> "json" -> RecordFormat.JSON
   *   -o "avro:file:./foo.bar" -> "avro" -> RecordFormat.AVRO
   *   --input "file:./foo.bar" -> "" -> RecordFormat.RAW
   *
   * @param uriStr String containing the full
   * @return
   */
  static RecordFormat getRecordFormat(String uriStr) {
    RecordFormat format = RecordFormat.RAW;
    int colonIdx = uriStr.indexOf(':');
    if (colonIdx < 0) return format;
    String scheme = uriStr.substring(0, colonIdx).toUpperCase();
    switch (scheme) {
      case "JSON":      // RecordFormat.JSON.scheme:
      case "AVRO":      // RecordFormat.AVRO.scheme:
      case "PROTOBUF":  // RecordFormat.PROTOBUF.scheme:
      case "RAW":       // RecordFormat.RAW.scheme:
        format = RecordFormat.valueOf(scheme);
    }
    return format;
  }

  /**
   * Retrieves the Record Location from an input/output argument string.
   *
   * Examples:
   *   -i "json:file:///etc/foo.bar" -> "file:///etc/foo.bar"
   *   -o "avro:file:./foo.bar" -> "file:./foo.bar"
   *   --input "file:/etc/foo.bar" -> "file:/etc/foo.bar"
   *   --output "/etc/foo.bar" -> "/etc/foo.bar"
   *
   * @param uriStr String containing the full location
   * @return     non-null value
   */
  static String getRecordLocation(String uriStr) {
    if (uriStr == null) return "";
    uriStr = uriStr.trim();
    if (uriStr.isEmpty() || uriStr.isBlank()) return "";
    int colonIdx = uriStr.indexOf(':');
    if (colonIdx > 0) { // Treat it like a plain file path.
      String scheme = uriStr.substring(0, colonIdx).toUpperCase();
      switch (scheme) {
        case "JSON":      // RecordFormat.JSON.scheme:
        case "AVRO":      // RecordFormat.AVRO.scheme:
        case "PROTOBUF":  // RecordFormat.PROTOBUF.scheme:
        case "RAW":       // RecordFormat.RAW.scheme:
          uriStr = uriStr.substring(colonIdx+1);
      }
    }
    return uriStr;
  }

  /**
   * Extract the path part from a file path which may include the file:/// scheme, or
   * which may contain only the file path.
   *
   * @param pathStr
   * @return
   */
  static Path getLocalPath(String pathStr) {
    if (pathStr == null || pathStr.isEmpty() || pathStr.isBlank()) return null;
    if (pathStr.startsWith("file:///")) {
      pathStr = pathStr.substring(8);
    } else if (pathStr.startsWith("file://")) {
      return null; // TODO Indicates a remote file? -> file://example.com/foo/bar.json
    } else if (pathStr.startsWith("file:/")) {
      pathStr = pathStr.substring(6);
    } else if (pathStr.startsWith("file:")) {
      pathStr = pathStr.substring(5);
    }
    return Paths.get("").toAbsolutePath().normalize().resolve(Paths.get(pathStr)).toAbsolutePath().normalize();
  }

  /**
   * Determines if the string contains a known data generator. Currently. only "mockaroo://" and "javafaker://"
   * are supported. Mockaroo makes one or more external calls to https://api.mockaroo.com/. Java Faker uses a
   * java library which generates data from a random seed.
   *
   * Examples:
   *   -i json:mockaroo://{api-id}?count=100
   *   -i avro:javafaker://{seed}?count=100
   *
   *  --> isKnownDataGenerator("mockaroo://....");
   *  --> isKnownDataGenerator("javafaker://....");
   *
   * @param   uriStr   String which specifies the path to a resource.
   * @return  {@code true} if the uriStr begins with a scheme for a known Data Generator, {@code false} otherwise.
   *          Currently, only "mockaroo:" and "javafaker:" are supported.
   */
  static boolean isBuiltInDataGenerator(String uriStr) {
    if (uriStr == null || uriStr.isEmpty() || uriStr.isBlank()) return false;
    return (uriStr.startsWith("mockaroo://") || uriStr.startsWith("javafaker://"));
  }

  /**
   * Determines whether the -i/--input indicates System.in or -o/--output indicates System.out or System.err.
   *
   * Examples:
   *   optValue = null       (is null)
   *   optValue = ""         (is empty)
   *   optValue = "  \t\n"   (is blank)
   *   optValue = "sys:in"   (is sys:in)
   *
   * @param   optValue   String with the full value of the input option
   * @return             {@code true} if the value is {@code null}, {@code "empty"},
   *                     {@code "blank"}, or {@code "sys:in"}
   */
  static boolean isSystemStream(String optValue) {
    return optValue == null || optValue.isEmpty() || optValue.isBlank()
        || optValue.equals("sys:in") || optValue.equals("sys:out") || optValue.equals("sys:err");
  }

  /**
   * Determines whether the string is (probably) a local file (or directory) path. This does
   * not confirm whether the file exists, is readable, or any other qualifier. This does
   * handle file: URIs
   *
   * Examples:
   *   value = null                  (is null)                       --> false
   *   value = ""                    (is empty)                      --> false
   *   value = "   "                 (is blank)                      --> false
   *   value = "   .."               (trims to "..")                 --> true
   *   value = "./foo.bar"           (relative path)                 --> true
   *   value = "../foo.bar"          (relative path)                 --> true
   *
   * Url Examples:
   *   value = "file:///foo.bar"     ("///" looks absolute path)     --> true
   *   value = "file://foo.bar"      ("//" looks like server in url) --> false
   *   value = "file:/foo.bar"       ("/" looks absolute path)       --> true
   *   value = "file://./foo.bar"    ("//." looks relative path)     --> true
   *   value = "file:../foo.bar"     ("/" looks absolute path)       --> true
   *   value = "file:../foo.bar"     ("/" looks absolute path)       --> true
   *
   * Note: This does not currently support Windows paths.
   *   This does not handle complex urls for the file:
   *     value = "file://foo.com/foo.bar"     ("/" looks absolute path)     --> true
   *
   * @param    path   String which has been trimmed to file-like part of an in/out option
   * @return   {@code true} if the value looks like a local file path or a local file: url, {@code false} otherwise
   */
  static boolean isLocalFile(String path) {
    if (path == null) return false;
    path = path.trim();
    if (path.isEmpty() || path.isBlank()) return false;
    if (path.startsWith("..") || path.startsWith("."))
    if (path.startsWith("/")) return true; // "/foo.bar" --> Absolute path
    // TODO Handle file contents of a tar/zip/gzip/jar file?  e.g. file:jar://foo/bar/data.json
    if (path.startsWith("file:///")) {
      return isLocalFile(path.substring(7));
    } else if (path.startsWith("file://")) {
      return false; // looks like remote file://server.com/foo.bar
    } else if (path.startsWith("file:")) {
      return isLocalFile(path.substring(5));
    } else {
      return true;
    }
  }

  // --- Methods to Remove ----------------------------------------------------------------------------------

  private static void foo() {
    Pattern p = Pattern.compile("(json|avro|protobuf):(.*)");
    Matcher m = p.matcher("inOptValue");
    out.println("    m.matches()=" + m.matches());
    out.println("    m.group(0)=\"" + m.group(0) + "\"");
    out.println("    m.group(1)=\"" + m.group(1) + "\"");
    out.println("    m.group(2)=\"" + m.group(2) + "\"");
    err.println("group(2): \"" + m.group(2) + "\"");

    InputStream inStream = null;

    if (m.group(2) == null || m.group(2).isEmpty()) {
      // Input to be drawn from System.in
      err.println("Reading from System.in");
      inStream = new BufferedInputStream(in);
    } else {
      URI uri = URI.create(m.group(2));
      String scheme = uri.getScheme();
      err.println("URI: " + uri);
      err.println("URI.host: " + uri.getHost());
      err.println("URI.scheme: " + uri.getScheme());
      err.println("URI.scheme.specific: " + uri.getSchemeSpecificPart());
      err.println("URI.scheme.specific.raw: " + uri.getRawSchemeSpecificPart());
      err.println("URI.path: " + uri.getPath());
      err.println("URI.path.raw: " + uri.getRawPath());
      if (scheme == null || scheme.equals("file")) {
        try {
          inStream = new BufferedInputStream(new FileInputStream(Path.of(uri.getPath()).toFile()));
        } catch (FileNotFoundException e) {
          err.println("Could not find input file: \"" + uri.getPath() + "\"");
          exit(1);
        }
      } else {
        try {
          inStream = new BufferedInputStream(uri.toURL().openStream());
        } catch (MalformedURLException e) {
          err.println("Malformed URL: \"" + uri.toASCIIString() + "\"");
          exit(-1);
        } catch (InvalidPathException e) {
          err.println("Could not resolve Path: \"" + uri.getPath() + "\"");
        } catch (IOException e) {
          err.println("Could not open stream from \"" + uri.getPath() + "\"");
        }
      }
    }
  }

  private static void tmpMethod() {
    // TODO This assumes record-per-newline, text based input. It would
    //      be better to have reader/stream that'll intelligently read
    //      one entire record at a time. Configurability would be nice.
    // RecordReader reader;
    // RecordWriter writer;

    Reader reader = null;
    Writer writer = null;

    if (reader == null) {
      err.println("Reader could not be initialized.");
      exit(-1);
    }

    if (writer == null) {
      err.println("Writer could not be initialized.");
      exit(-1);
    }

    HttpClient httpClient = HttpClient.newHttpClient();
    String apiUrl = "https://api.mockaroo.com/api/";
    String apiId = "13a5dd70";
    int count = 10;
    String apiKey = "e0ca77c0";
//    String requestUrl = String.format("%s?count={}&key={}", apiUrl, count, apiKey);

    URI requestUri = URI.create(String.format("%s%s?count=%d&key=%s", apiUrl, apiId, count, apiKey));
    HttpRequest httpRequest = HttpRequest.newBuilder(requestUri).GET().build();
    try {
      HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      // Parse the body (json) just enough to split apart the array? response.body()

      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(response.body());
      if (parser.nextToken() != JsonToken.START_ARRAY) {
        throw new IllegalStateException("Expected an array");
      }
      int i = 1;
      while (parser.nextToken() == JsonToken.START_OBJECT) {
        // read everything from this START_OBJECT to the matching END_OBJECT
        // and return it as a tree model ObjectNode
        ObjectNode node = mapper.readTree(parser);

        // do whatever you need to do with this object
        out.println("Record " + i++ + ": " + node.toString());
      }
      parser.close();
    } catch (IOException e) {
      e.printStackTrace();
      exit(-1);
    } catch (InterruptedException e) {
      e.printStackTrace();
      exit(-1);
    }

    out.println("Goodbye from kif!");
    return;

  }
}
