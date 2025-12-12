package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.entity.ReportState;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.ReportRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportService reportService;

    private Report validReport;
    private User sender;

    @BeforeEach
    void setUp() {
        sender = new User("Sender", "sender@ua.pt", "easter_egg_3", UserRoles.RENTER);
        sender.setId(1L);

        validReport = new Report(1L, "Bug Found", "Description of the bug");
        validReport.setId(10L);
    }

    @Test
    void testCreateReport() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(sender));
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        when(reportRepository.save(any(Report.class))).thenReturn(validReport);

        Report result = reportService.createReport(1L, "Bug Found", "Description of the bug");
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Bug Found");
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getState()).isEqualTo(ReportState.NEW);

        assertThrows(IllegalArgumentException.class, () -> reportService.createReport(99L, "Title", "Desc"));
        assertThrows(IllegalArgumentException.class, () -> reportService.createReport(1L, "", "Desc"));
        assertThrows(IllegalArgumentException.class, () -> reportService.createReport(1L, "Title", ""));
    }

    @Test
    void testGetters() {
        List<Report> reportList = List.of(validReport);

        when(reportRepository.findById(10L)).thenReturn(Optional.of(validReport));
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        when(reportRepository.findBySenderId(1L)).thenReturn(reportList);

        Optional<Report> foundById = reportService.getReportById(10L);
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getDescription()).isEqualTo("Description of the bug");

        Optional<Report> notFound = reportService.getReportById(99L);
        assertThat(notFound).isEmpty();

        List<Report> foundBySender = reportService.getReportsBySenderId(1L);
        assertThat(foundBySender).hasSize(1);
    }

    @Test
    void testUpdateReportState() {
        when(reportRepository.findById(10L)).thenReturn(Optional.of(validReport));
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenReturn(validReport);

        // Test transition NEW -> IN_PROGRESS
        validReport.setState(ReportState.NEW);
        boolean updateSuccess = reportService.updateReportState(10L);
        assertThat(updateSuccess).isTrue();
        assertThat(validReport.getState()).isEqualTo(ReportState.IN_PROGRESS);

        // Test transition IN_PROGRESS -> CLOSED
        boolean updateToClosed = reportService.updateReportState(10L);
        assertThat(updateToClosed).isTrue();
        assertThat(validReport.getState()).isEqualTo(ReportState.CLOSED);

        // Test transition CLOSED -> false
        boolean updateFromClosed = reportService.updateReportState(10L);
        assertThat(updateFromClosed).isFalse();
        assertThat(validReport.getState()).isEqualTo(ReportState.CLOSED);

        boolean updateFail = reportService.updateReportState(99L);
        assertThat(updateFail).isFalse();
    }
    @Test
    void testSearchReports() {
        Report newReport = new Report(1L, "New Report", "Desc");
        newReport.setState(ReportState.NEW);
        
        Report closedReport = new Report(1L, "Closed Report", "Desc");
        closedReport.setState(ReportState.CLOSED);

        when(reportRepository.findAll()).thenReturn(List.of(newReport, closedReport));
        when(reportRepository.findByState(ReportState.NEW)).thenReturn(List.of(newReport));

        // Test without filter (null state)
        List<Report> allReports = reportService.searchReports(null);
        assertThat(allReports).hasSize(2);

        // Test with filter
        List<Report> filteredReports = reportService.searchReports(ReportState.NEW);
        assertThat(filteredReports).hasSize(1);
        assertThat(filteredReports.get(0).getState()).isEqualTo(ReportState.NEW);
    }
}
