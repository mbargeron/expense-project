package expense.api.controller;

/**
 * Created by mbargeron on 3/5/16.
 */

import expense.api.ExpenseException;
import expense.api.LogInterceptor;
import expense.api.dto.Expense;
import expense.api.dto.ExpenseResponse;
import expense.api.model.ExpenseRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
public class ExpenseController {

    @Autowired
    private ExpenseRepository repository;

    @Autowired
    private HttpServletRequest request;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    private static final String unknownExpenseId = "Unknown expense id ";
    private static final String undeletableExpense = "Expenses in status 'reimbursed' may not be deleted";

    @Bean
    public MappedInterceptor interceptor() {
        return new MappedInterceptor(null, null, new LogInterceptor());
    }


    @RequestMapping(value = "/expense", method = RequestMethod.POST)
    public ExpenseResponse create(@Valid Expense createExpense) throws Exception {
        repository.save(createExpense);
        Expense createResponse = new Expense(createExpense.getId());
        logResponse(createResponse, Level.INFO);
        return createResponse;
    }

    @RequestMapping(value = "/expense/{id}", method = RequestMethod.GET)
    public ExpenseResponse retrieve(@PathVariable(value = "id") String id) throws Exception {
        if(repository.exists(id)) {
            Expense expense = repository.findById(id);
            logResponse(expense, Level.INFO);
            return expense;
        } else {
            throw new ExpenseException(unknownExpenseId+id);
        }
    }

    @RequestMapping(value = "/expenses", method = RequestMethod.GET)
    public List<Expense> retrieveList(@RequestParam Map<String, String> requestParams) {
        List<Expense> expenseList = repository.applyCustomFilter(requestParams);
        return expenseList;
    }

    @RequestMapping(value = "/expense/{id}", method = RequestMethod.PUT)
    public ExpenseResponse update(@PathVariable(value = "id") String id,
                                  @RequestParam Map<String, String> paramMap) throws Exception {

        if(repository.exists(id)) {
            ExpenseResponse updateResponse = repository.updateExpense(id, paramMap);
            logResponse(updateResponse, Level.INFO);
            return updateResponse;
        } else {
            throw new ExpenseException(unknownExpenseId+id);
        }
    }

    @RequestMapping(value = "/expense/{id}", method = RequestMethod.DELETE)
    public ExpenseResponse delete(@PathVariable(value = "id") String id) throws Exception {
        if(repository.exists(id)) {
            Expense deleteExpense = repository.findById(id);
            if(!deleteExpense.getStatus().equals(Expense.Status.REIMBURSED)) {
                repository.delete(id);
                Expense deleteResponse = new Expense(id);
                deleteResponse.setStatus(Expense.Status.DELETED);
                logResponse(deleteResponse, Level.INFO);
                return deleteResponse;
            } else {
                throw new ExpenseException(undeletableExpense);
            }
        } else {
            throw new ExpenseException(unknownExpenseId+id);
        }
    }

    /** Gracefully handles any exception thrown when processing expense service data **/
    @ExceptionHandler
    @ResponseBody
    public ExpenseResponse handleException(final Exception e)
    {
        String errorString;
        String exceptionClass = e.getClass().getSimpleName();

        switch (exceptionClass) {
            case "ExpenseException":
            case "IllegalArgumentException":
            case "NullPointerException":
                errorString = e.getMessage();
                break;
            case "MissingServletRequestParameterException":
                errorString = "";
                MissingServletRequestParameterException mrpe = (MissingServletRequestParameterException) e;
                errorString += mrpe.getParameterName()+" is required and was not specified";
                break;
            case "BindException":
                errorString = "";
                BindException be = (BindException) e;
                for(ObjectError error : be.getAllErrors()) {
                    if(!errorString.isEmpty()) {
                        errorString += ", ";
                    }
                    if(error.getDefaultMessage().startsWith("expense")) {
                        errorString += error.getDefaultMessage();
                    } else if(error instanceof FieldError) {
                        FieldError fieldError = (FieldError) error;
                        errorString += error.getObjectName()+" '"+fieldError.getField()+"' contains an invalid value '"+fieldError.getRejectedValue()+"'";
                    } else {
                        errorString += error.getObjectName()+" "+error.getDefaultMessage();
                    }
                }
                break;
            default:
                System.out.println(e.toString());
                /** Log the entirety of any unrecognized error for troubleshooting **/
                errorString = exceptionClass+": An unknown error occurred. Contact an account representative for further investigation.";
                break;
        }

        ExpenseResponse errorResponse = new ExpenseResponse(errorString);
        logResponse(errorResponse, Level.SEVERE);
        return errorResponse;
    }

    private void logResponse(ExpenseResponse expenseResponse, Level logLevel) {

        String logHeader = request.getMethod() + " Response: " + request.getRequestURI() + ", ";
        if(logLevel == Level.INFO) {
            try {
                logger.info(logHeader + mapper.writeValueAsString(expenseResponse));
            } catch (Exception e) {
                logger.info(logHeader + "Failed to map response to JSON string");
            }
        } else if (logLevel == Level.SEVERE) {
            try {
                logger.error(logHeader +  mapper.writeValueAsString(expenseResponse));
            } catch (Exception e) {
                logger.error(logHeader + "Failed to map response to JSON string");
            }
        }
    }

    private void logResponse(String responseString, Level logLevel) {
        String logHeader = request.getMethod() + " Response: " + request.getRequestURI() + ", ";
        if(logLevel == Level.INFO) {
            logger.info(logHeader + responseString);
        } else if (logLevel == Level.SEVERE) {
            logger.error(logHeader + responseString);
        }
    }
}
