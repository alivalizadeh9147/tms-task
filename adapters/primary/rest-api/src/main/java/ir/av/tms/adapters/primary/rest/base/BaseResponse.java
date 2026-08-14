package ir.av.tms.adapters.primary.rest.base;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static java.util.Collections.unmodifiableList;

@Schema
public record BaseResponse<T>(boolean isSuccess, T data, List<ErrorItem> errors) {

    public static <T> BaseResponse<T> defaultSuccess() {
        return new BaseResponse<>(true, null, List.of());
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, data, List.of());
    }

    public static <T> BaseResponse<T> error(List<ErrorItem> errors) {
        return new BaseResponse<>(false, null, unmodifiableList(errors));
    }

    public static <T> BaseResponse<T> error(ErrorItem... errors) {
        return new BaseResponse<>(false, null, List.of(errors));
    }
}
