package tqs.backend.tqsbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.entity.ReportState;
import tqs.backend.tqsbackend.repository.ReportRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ReportRepository reportRepository;
    private final UserService userService;

    @Autowired
    public ReportService(ReportRepository reportRepository, UserService userService) {
        this.reportRepository = reportRepository;
        this.userService = userService;
    }

    public Report createReport(Long senderId, String title, String description) {
        if (senderId == null || senderId < 0 || userService.getUserById(senderId).isEmpty()) {
            logger.warn("Failed to create report: SenderId {} is invalid.", senderId);
            throw new IllegalArgumentException("Failed to create report: SenderId " + senderId + " is invalid.");
        }
        if (title == null || title.trim().isEmpty() || title.length() > 128) {
            logger.warn("Failed to create report: Title length {} is invalid (0-128).",
                    (title == null ? "null" : title.length()));
            throw new IllegalArgumentException("Failed to create report: Title is invalid.");
        }
        if (description == null || description.trim().isEmpty() || description.length() > 4096) {
            logger.warn("Failed to create report: Description length {} is invalid (0-4096).",
                    (description == null ? "null" : description.length()));
            throw new IllegalArgumentException("Failed to create report: Description is invalid.");
        }

        Report report = new Report(senderId, title, description);
        Report savedReport = reportRepository.save(report);
        logger.info("Report created successfully with ID {}", savedReport.getId());
        return savedReport;
    }

    public Optional<Report> getReportById(Long id) { return reportRepository.findById(id); }

    public List<Report> getReportsBySenderId(Long senderId) { return reportRepository.findBySenderId(senderId); }

    public List<Report> getAllReports() { return reportRepository.findAll(); }

    public List<Report> getReportsByState(ReportState state) { return reportRepository.findByState(state); }

    public boolean updateReportState(Long id) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            ReportState currentState = report.getState();
            ReportState nextState = null;

            switch (currentState) {
                case ReportState.NEW:
                    nextState = ReportState.IN_PROGRESS;
                    break;
                case IN_PROGRESS:
                    nextState = ReportState.CLOSED;
                    break;
                case CLOSED:
                    logger.info("Report {} is already CLOSED. No further state change applied.", id);
                    return false;
                default:
                    logger.warn("Unknown report state {} for report ID {}. No state change applied.", currentState, id);
                    return false;
            }

            if (nextState != null) {
                report.setState(nextState);
                reportRepository.save(report);
                logger.info("Updated report {} state from {} to {}", id, currentState, nextState);
                return true;
            }
        }
        logger.warn("Failed to update report state: Report with ID {} not found.", id);
        return false;
    }
}
