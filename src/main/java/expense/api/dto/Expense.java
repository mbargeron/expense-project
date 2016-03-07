package expense.api.dto;

/**
 * Created by mbargeron on 3/5/16.
 */

import expense.api.ExpenseException;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;


public class Expense extends ExpenseResponse {

    @Id
    private String id;

    @NotEmpty(message = "{merchant.empty}")
    private String merchant;

    @NotNull(message = "{amount.null}")
    @DecimalMin(value = "0", message = "{amount.negative}")
    private BigDecimal amount;

    private Date datetime = null;

    private String status;

    private List<String> comments = new ArrayList<String>();

    public interface Status {
        String NEW = "new";
        String REIMBURSED = "reimbursed";
        String DELETED = "deleted";
    }

    public Expense() {
        this.status = Status.NEW;
    }

    public Expense(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getMerchant() {
        return this.merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) throws ExpenseException {
        if(amount.compareTo(BigDecimal.ZERO) >= 0) {
            this.amount = amount;
        } else {
            throw new ExpenseException("expense 'amount' cannot be negative");
        }
    }

    public String getDatetime() {
        if(this.datetime != null) {
            String isoDateFormat = "yyyy-MM-dd'T'HH:mm:ssX";
            SimpleDateFormat sdf = new SimpleDateFormat(isoDateFormat, Locale.US);
            return sdf.format(this.datetime);
        } else {
            return "";
        }
    }

    public void setDatetime(String datetime) throws ExpenseException {
        String isoDateFormat = "yyyy-MM-dd'T'HH:mm:ssX";
        try {
            /** Parse datetime to make sure the passed in value maps to an actual ISO 8601 date **/
            /** SimpleDateFormat is not re-entrant, so this may cause production issues under load **/
            SimpleDateFormat sdf = new SimpleDateFormat(isoDateFormat, Locale.US);
            this.datetime = sdf.parse(datetime);
        } catch (ParseException pe) {
            throw new ExpenseException("'datetime' ("+isoDateFormat+") contains an invalid value '"+datetime+"'");
        }
    }

    public List<String> getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments.add(comments);
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
