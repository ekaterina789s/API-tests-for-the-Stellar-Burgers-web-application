package user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class UserModel {
    private String email;
    private String password;
    private String name;
}
