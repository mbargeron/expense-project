package expense.api.model;

/**
 * Created by mbargeron on 3/5/16.
 */

import org.springframework.data.mongodb.repository.MongoRepository;

import expense.api.dto.Expense;

public interface ExpenseRepository extends MongoRepository<Expense, String>, ExpenseRepositoryCustom {

    public Expense findById(String id);

}