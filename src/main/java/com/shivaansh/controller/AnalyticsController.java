package com.shivaansh.controller;

import com.shivaansh.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<?> getFinancialSummary() {
        try {
            log.info("GET /api/analytics/summary");
            Map<String, Object> summary = analyticsService.getFinancialSummary();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "summary", summary
            ));
        } catch (Exception e) {
            log.error("Error fetching financial summary", e);
            return buildError("Failed to fetch summary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-category")
    public ResponseEntity<?> getExpenseByCategory() {
        try {
            log.info("GET /api/analytics/by-category");
            Map<String, Double> breakdown = analyticsService.getExpenseByCategoryBreakdown();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "categoryBreakdown", breakdown
            ));
        } catch (Exception e) {
            log.error("Error fetching category breakdown", e);
            return buildError("Failed to fetch category breakdown", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<?> getDaily(@RequestParam(required = false) String date) {
        try {
            log.info("GET /api/analytics/daily, date={}", date);
            Map<String, Object> daily = analyticsService.getDailyAnalytics(date);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "daily", daily
            ));
        } catch (Exception e) {
            log.error("Error fetching daily analytics", e);
            return buildError("Failed to fetch daily analytics", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/range")
    public ResponseEntity<?> getRange(@RequestParam String startDate,
                                      @RequestParam String endDate) {
        try {
            log.info("GET /api/analytics/range, {} - {}", startDate, endDate);
            Map<String, Object> range = analyticsService.getDateRangeAnalytics(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "dateRange", range
            ));
        } catch (Exception e) {
            log.error("Error fetching date range analytics", e);
            return buildError("Failed to fetch date range analytics", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthly(@RequestParam(required = false) Integer year,
                                        @RequestParam(required = false) Integer month) {
        try {
            log.info("GET /api/analytics/monthly, year={}, month={}", year, month);
            Map<String, Object> monthly = analyticsService.getMonthlyAnalytics(year, month);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "monthly", monthly
            ));
        } catch (Exception e) {
            log.error("Error fetching monthly analytics", e);
            return buildError("Failed to fetch monthly analytics", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/comparison")
    public ResponseEntity<?> comparison() {
        try {
            log.info("GET /api/analytics/comparison");
            Map<String, Object> comparison = analyticsService.getIncomeVsExpenseComparison();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "comparison", comparison
            ));
        } catch (Exception e) {
            log.error("Error fetching comparison", e);
            return buildError("Failed to fetch comparison", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/top-expenses")
    public ResponseEntity<?> topExpenses(@RequestParam(defaultValue = "5") int limit) {
        try {
            log.info("GET /api/analytics/top-expenses, limit={}", limit);
            List<?> top = analyticsService.getTopExpenses(limit);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "count", top.size(),
                    "topExpenses", top
            ));
        } catch (Exception e) {
            log.error("Error fetching top expenses", e);
            return buildError("Failed to fetch top expenses", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trends")
    public ResponseEntity<?> trends(@RequestParam(required = false) String category) {
        try {
            log.info("GET /api/analytics/trends, category={}", category);
            Map<String, Object> trends = analyticsService.getSpendingTrends(category);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "trends", trends
            ));
        } catch (Exception e) {
            log.error("Error fetching trends", e);
            return buildError("Failed to fetch trends", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/budget-check")
    public ResponseEntity<?> budgetCheck(@RequestBody Map<String, Double> budget) {
        try {
            log.info("POST /api/analytics/budget-check, body={}", budget);
            Map<String, Object> result = analyticsService.checkBudgetExceeded(budget);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "budgetAnalysis", result
            ));
        } catch (Exception e) {
            log.error("Error in budget check", e);
            return buildError("Failed to check budget", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/savings-rate")
    public ResponseEntity<?> savingsRate() {
        try {
            log.info("GET /api/analytics/savings-rate");
            Map<String, Object> data = analyticsService.calculateSavingsRate();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "savingsRate", data
            ));
        } catch (Exception e) {
            log.error("Error calculating savings rate", e);
            return buildError("Failed to calculate savings rate", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> buildError(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "status", "error",
                "message", message
        ));
    }
}
