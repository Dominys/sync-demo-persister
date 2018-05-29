package com.sync.demo.syncdemo;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Pichkurov
 */
public class AjaxBooleanResponse<T> implements Serializable{

    private static final long serialVersionUID = 8656554580898847981L;
    private boolean success;
    private String message;
    private Long total;
    private T data;
    private long jobProcessingId = -1;

    public AjaxBooleanResponse() {
    }

    public AjaxBooleanResponse(boolean success) {
        this.success = success;
    }

    public AjaxBooleanResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AjaxBooleanResponse(boolean success, T result) {
        this.success = success;
        this.data = result;
    }

    public AjaxBooleanResponse(boolean success, T data, long total) {
        this.success = success;
        this.data = data;
        this.total = total;
    }

    public AjaxBooleanResponse(boolean success, T data, int total) {
        this.success = success;
        this.data = data;
        this.total = Long.valueOf(total);
    }

    public AjaxBooleanResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean sucess) {
        this.success = sucess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String error) {
        this.message = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T result) {
        this.data = result;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public long getJobProcessingId() {
        return jobProcessingId;
    }

    public void setJobProcessingId(long jobProcessingId) {
        this.jobProcessingId = jobProcessingId;
    }

    public Map toMap() {
        Map map = new HashMap();
        put(map, "success", success);
        put(map, "message", message);
        put(map, "total", total);
        put(map, "data", data);
        put(map, "jobProcessingId", jobProcessingId);
        return map;
    }

    private void put(Map map, String key, Object value) {
        map.put(key, value);
    }

}
