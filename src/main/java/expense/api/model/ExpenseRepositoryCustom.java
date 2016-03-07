package expense.api.model;

/**
 * Created by mbargeron on 3/5/16.
 */

import expense.api.ExpenseException;
import expense.api.dto.Expense;

import java.util.List;
import java.util.Map;

public interface ExpenseRepositoryCustom {

    public Expense updateExpense(String id, Map<String, String> paramMap) throws ExpenseException;
    public List<Expense> applyCustomFilter(Map<String, String> paramMap);
}