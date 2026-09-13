package kr.zipimoney.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    Error error;
    T data;

    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder().build();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().data(data).build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> fail(HttpStatus httpStatus, String errorCode, String errorMessage) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .error(new Error(errorCode, errorMessage))
                .build();
        return ResponseEntity.status(httpStatus).body(response);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(String errorCode, String errorMessage) {
    }
}
