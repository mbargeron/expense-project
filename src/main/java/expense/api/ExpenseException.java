package expense.api;

/**
 * Created by mbargeron on 3/5/16.
 */

public class ExpenseException extends Exception{

    public String message;

    public ExpenseException(String message){
        this.message = message;
    }

    // Overrides Exception's getMessage()
    @Override
    public String getMessage(){
        return message;
    }
}