package tqs.backend.tqsbackend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.entity.ReportState;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    private Report r1;

    @BeforeEach
    void setUp() {
        r1 = new Report(1L, "Bug Found", "Description of bug");
        Report r2 = new Report(2L, "Other Issue", "Desc");

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.flush();
    }

    @Test
    void testFindById() {
        Optional<Report> found = reportRepository.findById(r1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Bug Found");
        assertThat(found.get().getState()).isEqualTo(ReportState.NEW);

        assertThat(reportRepository.findById(200L)).isNotPresent();
    }

    @Test
    void testFindBySenderId() {
        List<Report> found = reportRepository.findBySenderId(1L);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDescription()).isEqualTo("Description of bug");

        assertThat(reportRepository.findBySenderId(999L)).isEmpty();
    }
}
