package tqs.backend.tqsbackend.controller.pict;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PICT-based Parameterized Tests for Rating Search
 * 
 * This test class uses PICT to generate test cases that cover all pairwise
 * combinations of rating search parameters.
 * 
 * PICT Model: tqsbackend/pict-models/rating_search.pict
 * Generated Cases: 13 test combinations
 * 
 * Parameters tested:
 * - sender: "NULL", "valid", "invalid"
 * - type: "NULL", "RENTER", "OWNER", "ITEM"
 * - rated: "NULL", "valid", "invalid"
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RatingSearchPICTTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * PICT Test: Tests all pairwise combinations of rating search parameters
     * 
     * CSV Format: sender,type,rated
     * Test count: 13 combinations
     */
    @ParameterizedTest(name = "#{index}: sender={0}, type={1}, rated={2}")
    @CsvFileSource(resources = "/pict/rating_search_cases.csv", numLinesToSkip = 1)
    public void testRatingSearchPICT(String sender, String type, String rated) throws Exception {
        // Build request
        var request = get("/api/ratings/search")
                .contentType(MediaType.APPLICATION_JSON);

        // Add parameters if not NULL
        if (!"NULL".equals(sender) && sender != null) {
            String cleanSender = sender.replace("\"", "");
            Long senderValue = "valid".equals(cleanSender) ? 1L : 99999L;
            request.param("sender", senderValue.toString());
        }
        if (!"NULL".equals(type) && type != null) {
            String cleanType = type.replace("\"", "");
            request.param("type", cleanType);
        }
        if (!"NULL".equals(rated) && rated != null) {
            String cleanRated = rated.replace("\"", "");
            Long ratedValue = "valid".equals(cleanRated) ? 1L : 99999L;
            request.param("rated", ratedValue.toString());
        }

        // Execute request and verify response
        mockMvc.perform(request)
                .andExpect(status().isOk());

        // Note: We don't check exact results as PICT is testing parameter combinations,
        // not business logic. The goal is to ensure no errors with various parameter
        // combos.
    }
}
