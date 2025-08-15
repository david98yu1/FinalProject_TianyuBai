
package com.example.commonlib.dto.account;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateAccountOnRegister {
    private Long authUserId;
    private String email;
    private String username;
}
