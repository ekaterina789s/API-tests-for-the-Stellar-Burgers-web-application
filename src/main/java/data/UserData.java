package data;

public class UserData {
    //константа для урл
    public static final String BASE_URI = "https://stellarburgers.education-services.ru/";

    //константа для ключа "password"
    public static final String PASSWORD = "password";

    //сюда будем сохранять accessToken
    public static String currentAccessToken = null;

    //эндпоинты
    public static final String CREATE_USER_PATH = "/api/auth/register";
    public static final String DELETE_USER_PATH = "/api/auth/user";
    public static final String AUTHORIZATION_PATH = "/api/auth/login";
}
