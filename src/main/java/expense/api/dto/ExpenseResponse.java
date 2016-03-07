package expense.api.dto;

/**
 * Created by mbargeron on 3/5/16.
 */

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
public class ExpenseResponse {

    private String error;

    public ExpenseResponse() {
    }

    public ExpenseResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
