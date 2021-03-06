package edu.kit.crate.reader;

import edu.kit.crate.Crate;
import edu.kit.crate.RoCrate;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.RoCrateWriter;
import edu.kit.crate.HelpFunctions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class FolderReaderTest {

  @Test
  void testReadingBasicCrate(@TempDir Path temp) throws IOException {
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .build();
    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    Crate res = roCrateFolderReader.readCrate(temp.toFile().toString());

    Path r = temp.resolve("output.txt");
    FileUtils.touch(r.toFile());
    FileUtils.writeStringToFile(r.toFile(), res.getJsonMetadata(), Charset.defaultCharset());
    assertTrue(FileUtils.contentEquals(f.toFile(), r.toFile()));
  }


  @Test
  void testWithFile(@TempDir Path temp) throws IOException {
    Path cvs = temp.resolve("survey-responses-2019.csv");
    FileUtils.touch(cvs.toFile());
    FileUtils.writeStringToFile(cvs.toFile(), "fkdjaflkjfla", Charset.defaultCharset());

    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setSource(cvs.toFile())
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();

    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    Crate res = roCrateFolderReader.readCrate(temp.toFile().toString());
    HelpFunctions.compareTwoCrateJson(roCrate, res);
  }

  @Test
  void TestWithFileWithLocation(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("survey-responses-2019.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setSource(file.toFile())
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    Path f = temp.resolve("ro-crate-metadata.json");

    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());

    Crate res = roCrateFolderReader.readCrate(locationSource.toFile().toString());

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());

    writer.save(res, destinationDir.toFile().toString());

    // that copies the directory locally to see its content
    //FileUtils.copyDirectory(locationSource.toFile(), new File("test"));
    assertTrue(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoCrateJson(roCrate, res);
  }


  @Test
  void TestWithFileWithLocationAddEntity(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("file.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setSource(file.toFile())
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    Path f = temp.resolve("ro-crate-metadata.json");

    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());

    Path newFile = temp.resolve("new_file");
    FileUtils.writeStringToFile(newFile.toFile(), "fkladjsl;fjasd;lfjda;lkf", Charset.defaultCharset());

    Crate res = roCrateFolderReader.readCrate(locationSource.toFile().toString());
    res.addDataEntity(new FileEntity.FileEntityBuilder()
        .setId("new_file")
        .setEncodingFormat("setnew")
        .setSource(newFile.toFile())
        .build(), true);

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());

    writer.save(res, destinationDir.toFile().toString());

    assertFalse(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoMetadataJsonNotEqual(roCrate, res);
  }
}
