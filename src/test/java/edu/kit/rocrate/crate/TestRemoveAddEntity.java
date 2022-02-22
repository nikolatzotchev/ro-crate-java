package edu.kit.rocrate.crate;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.contextual.PlaceEntity;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.rocrate.HelpFunctions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestRemoveAddEntity {
  @Test
  void testAddRemoveEntity() throws IOException {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
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
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/onlyOneFile.json");

    // remove entity and check if equals to the basic crate
    roCrate.deleteEntityById("survey-responses-2019.csv");
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/simple.json");
  }

  @Test
  void withTwoFiles() throws IOException {
    PlaceEntity place = new PlaceEntity.PlaceEntityBuilder()
        .setId("http://sws.geonames.org/8152662/")
        .addProperty("name", "Catalina Park")
        .build();

    PersonEntity person = new PersonEntity.PersonEntityBuilder()
        .setId("#alice")
        .addProperty("name", "Alice")
        .addProperty("description", "One of hopefully many Contextual Entities")
        .build();

    FileEntity file = new FileEntity.FileEntityBuilder()
        .setId("data1.txt")
        .addProperty("description", "One of hopefully many Data Entities")
        .addIdProperty("author", person.getId())
        .setContentLocation(place.getId())
        .build();

    file.setAuthor(person.getId());

    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addContextualEntity(place)
        .addContextualEntity(person)
        .addDataEntity(file)
        .addDataEntity(
            new FileEntity.FileEntityBuilder().setId("data2.txt").build()
        )
        .build();

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/twoFiles.json");

    roCrate.deleteEntityById("data1.txt");
    roCrate.deleteEntityById("data2.txt");
    roCrate.deleteEntityById("#alice");
    roCrate.deleteEntityById("http://sws.geonames.org/8152662/");

    ROCrate roCrate2 = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity").build();
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, roCrate2);
  }
}