package edu.kit.rocrate.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.DataSetEntity;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.writer.ROCrateWriter;
import edu.kit.crate.writer.ZipWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class ZipWriterTest {

  @Test
  void testWritingToZip(@TempDir Path tempDir) throws IOException {
    // create the RO_crate directory in the tempDir
    Path roDir = tempDir.resolve("ro_dir");
    FileUtils.forceMkdir(roDir.toFile());

    // the .json of our crate
    InputStream fileJson=
        ZipWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

    // fill the expected directory with files and dirs

    Path json = roDir.resolve("ro-crate-metadata.json");
    FileUtils.copyInputStreamToFile(fileJson, json.toFile());
    Path file1 = roDir.resolve("input.txt");
    FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
    Path dirInCrate = roDir.resolve("dir");
    FileUtils.forceMkdir(dirInCrate.toFile());
    FileUtils.writeStringToFile(dirInCrate.resolve("first.txt").toFile(),
        "content of first file in dir", Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("second.txt").toFile(),
        "content of second file in dir",
        Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("third.txt").toFile(),
        "content of third file in dir",
        Charset.defaultCharset());

    // create the RO_Crate including the files that should be present in it
    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("cp7glop.ai")
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .setLocation(file1.toFile())
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .setId("lots_of_little_files/")
                .addProperty("name", "Too many files")
                .addProperty("description",
                    "This directory contains many small files, that we're not going to describe in detail.")
                .setLocation(dirInCrate.toFile())
                .build()
        )
        .build();

    // safe the crate in the test.zip file
    Path test = tempDir.resolve("test.zip");
    // create a Writer for writing RoCrates to zip
    ROCrateWriter roCrateZipWriter = new ROCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, test.toString());
    Path res = tempDir.resolve("dest");
    new ZipFile(test.toFile()).extractAll(res.toString());

    assertTrue(FolderWriterTest.compareTwoDir(roDir.toFile(), res.toFile()));

    // just so we know the metadata is still valid
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode jsonROCrate = objectMapper.readTree(roCrate.getJsonMetadata());

    InputStream inputStream =
        ZipWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");
    JsonNode expectedJson = objectMapper.readTree(inputStream);

    assertEquals(jsonROCrate, expectedJson);
  }


  @Test
  void testWritingToZipFail(@TempDir Path tempDir) throws IOException {
    // create the RO_crate directory in the tempDir
    Path roDir = tempDir.resolve("ro_dir");
    FileUtils.forceMkdir(roDir.toFile());

    // the .json of our crate
    InputStream fileJson=
        ZipWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

    // fill the expected directory with files and dirs

    Path json = roDir.resolve("ro-crate-metadata.json");
    FileUtils.copyInputStreamToFile(fileJson, json.toFile());
    Path file1 = roDir.resolve("input.txt");
    FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
    Path dirInCrate = roDir.resolve("dir");
    FileUtils.forceMkdir(dirInCrate.toFile());
    FileUtils.writeStringToFile(dirInCrate.resolve("first.txt").toFile(),
        "content of first file in dir", Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("second.txt").toFile(),
        "content of second file in dir",
        Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("third.txt").toFile(),
        "content of third file in dir",
        Charset.defaultCharset());
    // false file, this test case should fal
    Path falseFile = tempDir.resolve("new");
    FileUtils.writeStringToFile(falseFile.toFile(), "this file contains something else", Charset.defaultCharset());
    // create the RO_Crate including the files that should be present in it
    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("cp7glop.ai")
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .setLocation(falseFile.toFile())
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .setId("lots_of_little_files/")
                .addProperty("name", "Too many files")
                .addProperty("description",
                    "This directory contains many small files, that we're not going to describe in detail.")
                .setLocation(dirInCrate.toFile())
                .build()
        )
        .build();

    // safe the crate in the test.zip file
    Path test = tempDir.resolve("test.zip");
    // create a Writer for writing RoCrates to zip
    ROCrateWriter roCrateZipWriter = new ROCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, test.toString());
    Path res = tempDir.resolve("dest");
    new ZipFile(test.toFile()).extractAll(res.toString());

    assertFalse(FolderWriterTest.compareTwoDir(roDir.toFile(), res.toFile()));

    // just so we know the metadata is still valid
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode jsonROCrate = objectMapper.readTree(roCrate.getJsonMetadata());

    InputStream inputStream =
        ZipWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");
    JsonNode expectedJson = objectMapper.readTree(inputStream);

    assertEquals(jsonROCrate, expectedJson);
  }
}