package expense.api;

/**
 * Created by mbargeron on 3/5/16.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String input = request.getMethod()+" Request: " + request.getRequestURI();
        String paramString = getParametersAsString(request.getParameterMap());
        logger.info(input+", {"+paramString+"}");
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(	HttpServletRequest request, HttpServletResponse response,
                               Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

    private String getParametersAsString(Map paramMap) {
        String paramString = "";
        for (Object key: paramMap.keySet())
        {
            String keyStr = (String)key;
            String[] value = (String[])paramMap.get(keyStr);
            if(!paramString.isEmpty()) {
                paramString += ",";
            }
            paramString += "\"" + (String)key + "\":" + (value.length == 1 ? "\""+value[0]+"\"" : Arrays.toString(value));
        }
        return paramString;
    }
}