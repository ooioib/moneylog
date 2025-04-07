package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.query.CategoryExpense;
import org.codenova.moneylog.query.ExpenseWithCategory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Mapper
@Repository
public interface ExpenseRepository {
    public int save(Expense expense);

    public List<Expense> findByUserId(@Param("userId") int userId);

    public List<ExpenseWithCategory> findByUserIdAndDuration(@Param("userId") int userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    public List<ExpenseWithCategory> findWithCategoryByUserId(@Param("userId") int userId);

    public int weeklyTotalAmountExpense(@Param("userId") int userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    public List<Expense> WeeklyTop3Expense(@Param("userId") int userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    public List<CategoryExpense> WeeklyTopExpense(@Param("userId") int userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}

