public class RdfFreemarkerCli implements Callable {

  private final static Logger LOGGER = LoggerFactory.getLogger(RdfFreemarkerCli.class);
  private String[] args;
  private Config typesafe;

  public static void main(String[] args) throws Exception {
    RdfFreemarkerCli cli = new RdfFreemarkerCli(args);
    try {
      cli.call();
    } catch( Exception e ) {
      LOGGER.error("Error", e);
      System.exit(1);
    }
    System.exit(0);
  }

  public RdfFreemarkerCli(String[] args) {
    ConfigFactory.invalidateCaches();
    this.args = args;
    this.typesafe = ConfigFactory.load();
  }

  public Boolean call() throws Exception {

    String baseDir;
    if (typesafe.hasPath("baseDir"))
      baseDir = typesafe.getString("baseDir");
    else
      baseDir = args[0];

    LOGGER.info("baseDir: " + baseDir);
    Path baseDirPath = Paths.get(baseDir);
    assert( Files.exists(baseDirPath) );
    assert( Files.isDirectory(baseDirPath) );

    String settingsFile;
    if (typesafe.hasPath("settingsFile"))
      settingsFile = typesafe.getString("settingsFile");
    else
      settingsFile = args[1];

    LOGGER.info("settingsFile: " + settingsFile);
    Path settingsFilePath = Paths.get(settingsFile);
    assert( Files.exists(settingsFilePath) );
    assert( !Files.isDirectory(settingsFilePath) );

    String sourceRoot;
    if (typesafe.hasPath("sourceRoot"))
      sourceRoot = typesafe.getString("sourceRoot");
    else
      sourceRoot = args[2];

    LOGGER.info("sourceRoot: " + sourceRoot);
    Path sourceRootPath = Paths.get(sourceRoot);
    assert( Files.exists(sourceRootPath) );
    assert( Files.isDirectory(sourceRootPath) );

    String dataRoot;
    if (typesafe.hasPath("dataRoot"))
      dataRoot = typesafe.getString("dataRoot");
    else
      dataRoot = args[3];

    LOGGER.info("dataRoot: " + dataRoot);
    Path dataRootPath = Paths.get(dataRoot);
    assert( Files.exists(dataRootPath) );
    assert( Files.isDirectory(dataRootPath) );

    String outputRoot;
    if (typesafe.hasPath("outputRoot"))
      outputRoot = typesafe.getString("outputRoot");
    else
      outputRoot = args[4];

    LOGGER.info("outputRoot: " + outputRoot);
    Path outputRootPath = Paths.get(outputRoot);
    assert( Files.exists(outputRootPath) );
    assert( Files.isDirectory(outputRootPath) );

    String namespace;
    if (typesafe.hasPath("namespace"))
      namespace = typesafe.getString("namespace");
    else
      namespace = args[5];

    String id;
    if (typesafe.hasPath("id"))
      id = typesafe.getString("id");
    else
      id = args[6];

    Settings settings = new Settings(baseDirPath.toFile());
    settings.load(settingsFilePath.toFile());
    settings.set(NAME_DATA_ROOT, dataRoot);
    settings.set(NAME_SOURCE_ROOT, sourceRoot);
    settings.set(NAME_OUTPUT_ROOT, outputRoot);

    Map<String, String> vars = new HashMap<>();
    vars.put("dataRoot", dataRoot);
    vars.put("id", id);
    vars.put("namespace", namespace);

    settings.set(NAME_DATA, vars);

//    settings.define("id", Settings.TYPE_STRING, true, true);
//    settings.set("id", id);
//    settings.define("namespace", Settings.TYPE_STRING, true, true);
//    settings.set("namespace", namespace);

    ConsoleProgressListener listener = new ConsoleProgressListener();
    settings.addProgressListener(listener);

    try {
      settings.execute();
    } catch( Exception ex ) {
      LOGGER.error("settings.execute() Exception", ex);
      return false;
    }
    LOGGER.info("settings.execute() Success");
    return true;
  }

  public String dropExtension(String path) {
    return path.substring(0, path.lastIndexOf('.'));
  }

}