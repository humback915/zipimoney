package kr.zipimoney.domain.auth.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    Long id;
    String kakaoId;
    String nickname;
    String name;
    String profileImage;
    String email;
    String gender;
    String ageRange;
}
