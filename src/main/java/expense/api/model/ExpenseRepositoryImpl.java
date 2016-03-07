package expense.api.model;

/**
 * Created by mbargeron on 3/5/16.
 */

import expense.api.ExpenseException;
import expense.api.dto.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseRepositoryImpl implements ExpenseRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Expense updateExpense(String id, Map<String, String> paramMap) throws ExpenseException {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Expense originalExpense = mongoTemplate.findOne(query, Expense.class);
        Expense updateResponse =  new Expense(id);

        if(!paramMap.isEmpty()) {
            boolean modified = false;
            boolean recognizedParameter = false;

            if(paramMap.get("amount") != null) {
                recognizedParameter = true;
                BigDecimal amount = new BigDecimal(paramMap.get("amount"));
                if(originalExpense.getAmount().compareTo(amount) != 0) {
                    modified = true;
                    originalExpense.setAmount(amount);
                    updateResponse.setAmount(amount);
                }
            }

            if(paramMap.get("merchant") != null && !paramMap.get("merchant").isEmpty()) {
                recognizedParameter = true;
                if(originalExpense.getMerchant().compareTo(paramMap.get("merchant")) != 0) {
                    modified = true;
                    originalExpense.setMerchant(paramMap.get("merchant"));
                    updateResponse.setMerchant(paramMap.get("merchant"));
                }
            }

            if(paramMap.get("datetime") != null && !paramMap.get("datetime").isEmpty()) {
                recognizedParameter = true;

                // You have to check dates as Java Dates because mongo changes the strings based in timezone
                String isoDateFormat = "yyyy-MM-dd'T'HH:mm:ssX";
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(isoDateFormat, Locale.US);
                    Date originalDate = sdf.parse(originalExpense.getDatetime());
                    Date updateDate = sdf.parse(paramMap.get("datetime"));

                    if(originalDate.compareTo(updateDate) != 0) {
                        modified = true;
                        originalExpense.setDatetime(paramMap.get("datetime"));
                        updateResponse.setDatetime(paramMap.get("datetime"));
                    }
                } catch (ParseException pe) {
                    throw new ExpenseException("'datetime' ("+isoDateFormat+") contains an invalid value '"+paramMap.get("datetime")+"'");
                }
            }

            if(paramMap.get("comments") != null && !paramMap.get("comments").isEmpty()) {
                modified = true;
                recognizedParameter = true;
                originalExpense.setComments(paramMap.get("comments"));
                updateResponse.setComments(paramMap.get("comments"));
            }

            if(paramMap.get("status") != null && !paramMap.get("status").isEmpty()) {
                recognizedParameter = true;
                if(originalExpense.getStatus().equals(Expense.Status.NEW)
                   && paramMap.get("status").equals(Expense.Status.REIMBURSED)) {
                    modified = true;
                    originalExpense.setStatus(paramMap.get("status"));
                    updateResponse.setStatus(paramMap.get("status"));
                } else {
                    throw new ExpenseException("Expense status may only be changed from 'new' to 'reimbursed'.");
                }
            }

            if(!recognizedParameter) {
                throw new ExpenseException("No updatable parameters detected.");
            } else if(modified) {
                mongoTemplate.save(originalExpense);
                return updateResponse;
            } else {
                throw new ExpenseException("No changes detected. Expense was not saved.");
            }
        } else {
            throw new ExpenseException("No parameters detected.");
        }
    }

    public List<Expense> applyCustomFilter(Map<String, String> paramMap) {
        if(paramMap.isEmpty()) {
            List<Expense> listAll = mongoTemplate.findAll(Expense.class);
            return listAll;
        } else {
            Criteria criteria = null;
            for (Field field : Expense.class.getDeclaredFields()) {
                String key = field.getName();
                String value = paramMap.get(key);
                if (value != null && !value.isEmpty()) {
                    if (criteria == null) {
                        criteria = Criteria.where(key).is(value);
                    } else {
                        criteria = criteria.andOperator(Criteria.where(key).is(value));
                    }
                }
            }
            Query query = Query.query(criteria);
            List<Expense> listFiltered = mongoTemplate.find(query, Expense.class);
            return listFiltered;
        }
    }

    private String toLikeRegex(String source) {
        return source.replaceAll("\\*", ".*");
    }
}