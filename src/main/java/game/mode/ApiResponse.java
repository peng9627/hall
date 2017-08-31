package game.mode;

/**
 * Created by pengyi on 2016/4/15.
 */
public class ApiResponse<T> {

    private Integer code;     // API执行结果

    private T data;           //返回数据

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiResponse() {
    }

    public ApiResponse(Integer code) {
        this(code, null);
    }

    public ApiResponse(Integer code, T data) {
        this.code = code;
        this.data = data;
    }


}
