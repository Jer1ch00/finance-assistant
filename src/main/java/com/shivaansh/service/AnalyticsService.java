package com.shivaansh.service;


import com.shivaansh.entity.Transaction;
import com.shivaansh.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    private final TransactionRepository transactionRepository;

    // 1. FINANCIAL SUMMARY
    public Map<String, Object> getFinancialSummary() {
        log.debug("Calculating financial summary");

        double totalIncome = calculateTotalIncome();
        double totalExpense = calculateTotalExpense();
        double netBalance = totalIncome - totalExpense;
        long transactionCount = transactionRepository.count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netBalance", netBalance);
        summary.put("transactionCount", transactionCount);
        summary.put("savingsPercentage", totalIncome > 0 ? (netBalance / totalIncome * 100) : 0);

        return summary;
    }

    // 2. EXPENSE BY CATEGORY
    public Map<String, Double> getExpenseByCategoryBreakdown() {
        log.debug("Calculating expense by category");

        return transactionRepository.findByType("EXPENSE")
                .stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));
    }

    // 3. DAILY ANALYTICS
    public Map<String, Object> getDailyAnalytics(String dateString) {
        log.debug("Calculating daily analytics for date: {}", dateString);

        LocalDate date = dateString != null ?
                LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd")) :
                LocalDate.now();

        List<Transaction> dailyTransactions = transactionRepository.findAll()
                .stream()
                .filter(t -> t.getDate().equals(date))
                .collect(Collectors.toList());

        double dailyIncome = dailyTransactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        double dailyExpense = dailyTransactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        Map<String, Object> daily = new HashMap<>();
        daily.put("date", date);
        daily.put("income", dailyIncome);
        daily.put("expense", dailyExpense);
        daily.put("netDaily", dailyIncome - dailyExpense);
        daily.put("transactionCount", dailyTransactions.size());
        daily.put("transactions", dailyTransactions);

        return daily;
    }

    // 4. DATE RANGE ANALYTICS
    public Map<String, Object> getDateRangeAnalytics(String startDateStr, String endDateStr) {
        log.debug("Calculating analytics for range: {} to {}", startDateStr, endDateStr);

        LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<Transaction> transactions = transactionRepository.findByDateBetween(startDate, endDate);

        double rangeIncome = transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        double rangeExpense = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("startDate", startDate);
        dateRange.put("endDate", endDate);
        dateRange.put("income", rangeIncome);
        dateRange.put("expense", rangeExpense);
        dateRange.put("netBalance", rangeIncome - rangeExpense);
        dateRange.put("transactionCount", transactions.size());
        dateRange.put("averageDailyExpense", transactions.size() > 0 ? rangeExpense / getDaysBetween(startDate, endDate) : 0);

        return dateRange;
    }

    // 5. MONTHLY ANALYTICS
    public Map<String, Object> getMonthlyAnalytics(Integer year, Integer month) {
        log.debug("Calculating monthly analytics for: {}-{}", year, month);

        YearMonth yearMonth = year != null && month != null ?
                YearMonth.of(year, month) :
                YearMonth.now();

        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        List<Transaction> monthlyTransactions = transactionRepository.findByDateBetween(startOfMonth, endOfMonth);

        double monthlyIncome = monthlyTransactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        double monthlyExpense = monthlyTransactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        Map<String, Double> categoryBreakdown = monthlyTransactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));

        Map<String, Object> monthly = new HashMap<>();
        monthly.put("yearMonth", yearMonth.toString());
        monthly.put("income", monthlyIncome);
        monthly.put("expense", monthlyExpense);
        monthly.put("netSavings", monthlyIncome - monthlyExpense);
        monthly.put("categoryBreakdown", categoryBreakdown);
        monthly.put("transactionCount", monthlyTransactions.size());

        return monthly;
    }

    // 6. INCOME VS EXPENSE COMPARISON
    public Map<String, Object> getIncomeVsExpenseComparison() {
        log.debug("Calculating income vs expense comparison");

        double totalIncome = calculateTotalIncome();
        double totalExpense = calculateTotalExpense();

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("income", totalIncome);
        comparison.put("expense", totalExpense);
        comparison.put("balance", totalIncome - totalExpense);
        comparison.put("incomePercentage", totalIncome + totalExpense > 0 ? (totalIncome / (totalIncome + totalExpense) * 100) : 0);
        comparison.put("expensePercentage", totalIncome + totalExpense > 0 ? (totalExpense / (totalIncome + totalExpense) * 100) : 0);

        return comparison;
    }

    // 7. TOP EXPENSES
    public List<Transaction> getTopExpenses(int limit) {
        log.debug("Fetching top {} expenses", limit);

        return transactionRepository.findByType("EXPENSE")
                .stream()
                .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 8. SPENDING TRENDS
    public Map<String, Object> getSpendingTrends(String category) {
        log.debug("Calculating spending trends for category: {}", category);

        List<Transaction> expenses = category != null ?
                transactionRepository.findByCategory(category).stream()
                        .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                        .collect(Collectors.toList()) :
                transactionRepository.findByType("EXPENSE");

        double totalSpending = expenses.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        Map<String, Object> trends = new HashMap<>();
        trends.put("totalExpenses", totalSpending);
        trends.put("transactionCount", expenses.size());
        trends.put("averageExpense", expenses.size() > 0 ? totalSpending / expenses.size() : 0);
        trends.put("category", category != null ? category : "All Categories");

        return trends;
    }

    // 9. BUDGET CHECK
    public Map<String, Object> checkBudgetExceeded(Map<String, Double> budgets) {
        log.debug("Checking budget status");

        Map<String, Double> categoryExpenses = getExpenseByCategoryBreakdown();
        Map<String, Object> budgetStatus = new HashMap<>();
        Map<String, Object> categoryStatus = new HashMap<>();

        double totalBudget = 0;
        double totalActualSpend = 0;
        int categoriesExceeded = 0;

        for (String category : budgets.keySet()) {
            Double budgetAmount = budgets.get(category);
            Double actualSpend = categoryExpenses.getOrDefault(category, 0.0);

            totalBudget += budgetAmount;
            totalActualSpend += actualSpend;

            boolean exceeded = actualSpend > budgetAmount;
            if (exceeded) categoriesExceeded++;

            Map<String, Object> catStatus = new HashMap<>();
            catStatus.put("budget", budgetAmount);
            catStatus.put("actual", actualSpend);
            catStatus.put("remaining", budgetAmount - actualSpend);
            catStatus.put("percentage", budgetAmount > 0 ? (actualSpend / budgetAmount * 100) : 0);
            catStatus.put("exceeded", exceeded);

            categoryStatus.put(category, catStatus);
        }

        budgetStatus.put("categories", categoryStatus);
        budgetStatus.put("totalBudget", totalBudget);
        budgetStatus.put("totalActualSpend", totalActualSpend);
        budgetStatus.put("totalRemaining", totalBudget - totalActualSpend);
        budgetStatus.put("budgetUtilization", totalBudget > 0 ? (totalActualSpend / totalBudget * 100) : 0);
        budgetStatus.put("categoriesExceeded", categoriesExceeded);
        budgetStatus.put("onTrack", totalActualSpend <= totalBudget);

        return budgetStatus;
    }

    // 10. SAVINGS RATE
    public Map<String, Object> calculateSavingsRate() {
        log.debug("Calculating savings rate");

        double totalIncome = calculateTotalIncome();
        double totalExpense = calculateTotalExpense();
        double netSavings = totalIncome - totalExpense;

        double savingsRate = totalIncome > 0 ? (netSavings / totalIncome * 100) : 0;

        Map<String, Object> savingsData = new HashMap<>();
        savingsData.put("totalIncome", totalIncome);
        savingsData.put("totalExpense", totalExpense);
        savingsData.put("netSavings", netSavings);
        savingsData.put("savingsRate", savingsRate);
        savingsData.put("savingsRateCategory", getSavingsRateCategory(savingsRate));

        return savingsData;
    }

    // HELPER METHODS
    private double calculateTotalIncome() {
        return transactionRepository.findByType("INCOME")
                .stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }

    private double calculateTotalExpense() {
        return transactionRepository.findByType("EXPENSE")
                .stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }

    private long getDaysBetween(LocalDate startDate, LocalDate endDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private String getSavingsRateCategory(double savingsRate) {
        if (savingsRate >= 50) return "Excellent";
        if (savingsRate >= 30) return "Very Good";
        if (savingsRate >= 20) return "Good";
        if (savingsRate >= 10) return "Fair";
        if (savingsRate >= 0) return "Poor";
        return "Negative (Spending More Than Income)";
    }
}
