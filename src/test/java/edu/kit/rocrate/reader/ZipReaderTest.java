package edu.kit.rocrate.reader;

import edu.kit.crate.IROCrate;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.reader.ROCrateReader;
import edu.kit.crate.reader.ZipReader;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;
import edu.kit.crate.writer.ZipWriter;
import edu.kit.rocrate.HelpFunctions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ZipReaderTest {

  @Test
  void testReadingBasicCrate(@TempDir Path temp) throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .build();

    Path zipPath = temp.resolve("result.zip");

    ROCrateWriter roCrateZipWriter = new ROCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toString());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new ZipReader());
    IROCrate res = roCrateFolderReader.readCrate(zipPath.toFile().getAbsolutePath());
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, res);
  }


  @Test
  void testWithFile(@TempDir Path temp) throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();

    Path zipPath = temp.resolve("result.zip");

    ROCrateWriter roCrateZipWriter = new ROCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toFile().getAbsolutePath());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new ZipReader());
    IROCrate res = roCrateFolderReader.readCrate(zipPath.toFile().getAbsolutePath());

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, res);
  }

  @Test
  void TestWithFileWithLocation(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("survey-responses-2019.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setLocation(file.toFile())
                .build()
        )
        .build();

    Path zipPath = temp.resolve("result.zip");

    ROCrateWriter roCrateZipWriter = new ROCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toString());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new ZipReader());
    IROCrate res = roCrateFolderReader.readCrate(zipPath.toString());


    Path locationSource = temp.resolve("expected");
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(roCrate, locationSource.toString());

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());
    writer.save(res, destinationDir.toString());

    // that copies the directory locally to see its content
    assertTrue(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, res);
  }

  @Test
  void TestWithFileWithLocationAddEntity(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("file.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setLocation(file.toFile())
                .build()
        )
        .build();

    Path zipPath = temp.resolve("result.zip");

    ROCrateWriter roCrateZipWriter = new ROCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toString());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new ZipReader());
    IROCrate res = roCrateFolderReader.readCrate(zipPath.toFile().getAbsolutePath());


    Path locationSource = temp.resolve("expected");
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(roCrate, locationSource.toString());


    Path newFile = temp.resolve("new_file");
    FileUtils.writeStringToFile(newFile.toFile(), "fkladjsl;fjasd;lfjda;lkf", Charset.defaultCharset());

    res.addDataEntity(new FileEntity.FileEntityBuilder()
        .setId("new_file")
        .setEncodingFormat("setnew")
        .setLocation(newFile.toFile())
        .build(), true);

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());
    writer.save(res, destinationDir.toFile().toString());

    assertFalse(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoMetadataJsonNotEqual(roCrate, res);
  }
}
