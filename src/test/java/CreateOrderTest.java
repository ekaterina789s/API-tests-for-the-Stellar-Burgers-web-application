import data.UserData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.LoginUserModel;
import user.UserModel;
import java.util.List;

import static data.OrderData.INVALID_HASH_FIRST_ING;
import static data.OrderData.INVALID_HASH_SECOND_ING;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;
import static steps.CreateOrderSteps.createOrderWithAuth;
import static steps.CreateOrderSteps.createOrderWithoutAuth;
import static steps.IngredientsSteps.getIngredients;
import static steps.LoginUserSteps.authorizationUser;
import static steps.UserSteps.createUniqueUser;
import static steps.UserSteps.deleteUser;

public class CreateOrderTest extends BaseApiTest {

    private UserModel userUnique;
    private LoginUserModel loginUser;
    private String email;
    private String password;
    private String name;
    private Response responseCreateUser;

    @Before
    public void init(){
        email = faker.internet().emailAddress();
        password = "12345";
        name = faker.name().fullName();
        userUnique = new UserModel(email, password, name);
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами и с авторизацией")
    @Description("Сервер должен успешно обработать запрос")
    public void testCreateOrderWithIngredientsAndAuthorization() {
        responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        //авторизуем пользователя
        loginUser = new LoginUserModel(email, password);

        var responseLoginUser = authorizationUser(loginUser);
        responseLoginUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "));


        String accessToken = responseCreateUser.jsonPath().getString("accessToken"); //извлекаем значение токена
        UserData.currentAccessToken = accessToken;

        //получаем хэш ингредиентов
        var responseIngredients = getIngredients();

        responseIngredients.then()
                .log().all()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("data", everyItem(hasKey("_id")))
                .body("data", everyItem(hasKey("name")))
                .body("data", everyItem(hasKey("type")))
                .body("data", everyItem(hasKey("proteins")))
                .body("data", everyItem(hasKey("fat")))
                .body("data", everyItem(hasKey("carbohydrates")))
                .body("data", everyItem(hasKey("calories")))
                .body("data", everyItem(hasKey("price")))
                .body("data", everyItem(hasKey("image")))
                .body("data", everyItem(hasKey("image_mobile")))
                .body("data", everyItem(hasKey("image_large")))
                .body("data", everyItem(hasKey("__v")));

        //помещаем хэши в список
        List<String> allIds = responseIngredients.jsonPath().getList("data._id");

        if (allIds.size() < 2) {
            throw new IllegalStateException("Для теста нужно минимум 2 ингредиента, а получено: " + allIds.size());
        }

        String hash_firstIngredient = allIds.get(0);
        String hash_secondIngredient = allIds.get(1);

        //помещаем в массив переменные с хэшем
        String[] idsArray = {hash_firstIngredient, hash_secondIngredient};

        //создаем заказ
        var responseCreateOrder = createOrderWithAuth(idsArray);

        responseCreateOrder.then()
                .log().all()
                .statusCode(SC_OK)
                .body("name", notNullValue())
                .body("order.number", notNullValue())
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов и с авторизацией")
    @Description("Сервер должен вернуть ошибку")
    public void testCreateOrderWithoutIngredientsAndWithAuthorization() {
        //создаем пользователя
        responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        //авторизуем пользователя
        loginUser = new LoginUserModel(email, password);
        var responseLoginUser = authorizationUser(loginUser);

        responseLoginUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "));

        //извлекаем токен и сохраняем его
        String accessToken = responseCreateUser.jsonPath().getString("accessToken");
        UserData.currentAccessToken = accessToken;

        //создаем пустой массив ингредиентов
        String[] emptyIdsArray = {};

        //создаем заказ без ингредиентов
        var responseCreateOrder = createOrderWithAuth(emptyIdsArray);

        responseCreateOrder.then()
                .log().all()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами и без авторизации")
    @Description("Сервер должен успешно обработать запрос")
    public void testCreateOrderWithIngredientsAndWithoutAuthorization(){
        responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        String accessToken = responseCreateUser.jsonPath().getString("accessToken"); //извлекаем значение токена
        UserData.currentAccessToken = accessToken;

        //получаем хэш ингредиентов
        var responseIngredients = getIngredients();

        responseIngredients.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", everyItem(hasKey("_id")))
                .body("data", everyItem(hasKey("name")))
                .body("data", everyItem(hasKey("type")))
                .body("data", everyItem(hasKey("proteins")))
                .body("data", everyItem(hasKey("fat")))
                .body("data", everyItem(hasKey("carbohydrates")))
                .body("data", everyItem(hasKey("calories")))
                .body("data", everyItem(hasKey("price")))
                .body("data", everyItem(hasKey("image")))
                .body("data", everyItem(hasKey("image_mobile")))
                .body("data", everyItem(hasKey("image_large")))
                .body("data", everyItem(hasKey("__v")));

        //помещаем хэши в список
        List<String> allIds = responseIngredients.jsonPath().getList("data._id");

        if (allIds.size() < 2) {
            throw new IllegalStateException("Для теста нужно минимум 2 ингредиента, а получено: " + allIds.size());
        }

        String hash_firstIngredient = allIds.get(0);
        String hash_secondIngredient = allIds.get(1);

        //помещаем в массив переменные с хэшем
        String[] idsArray = {hash_firstIngredient, hash_secondIngredient};

        //создаем заказ
        var responseCreateOrder = createOrderWithoutAuth(idsArray);

        responseCreateOrder
                .then()
                .log().all()
                .statusCode(SC_OK);
    }

    @Test
    @DisplayName("Создание заказа с неверным хэшем ингредиентов и с авторизацией")
    @Description("Сервер должен вернуть ошибку")
    public void testCreateOrderWithInvalidIngredientsAndWithAuthorization(){
        responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        //авторизуем пользователя
        loginUser = new LoginUserModel(email, password);

        var responseLoginUser = authorizationUser(loginUser);
        responseLoginUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "));


        String accessToken = responseCreateUser.jsonPath().getString("accessToken"); //извлекаем значение токена
        UserData.currentAccessToken = accessToken;

        //помещаем в массив неверный хэш ингредиентов
        String[] idsArray = {INVALID_HASH_FIRST_ING, INVALID_HASH_SECOND_ING};

        //создаем заказ
        createOrderWithAuth(idsArray)
                .then()
                .log().all()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @After
    public void tearDown() {
        if (UserData.currentAccessToken != null) {
            System.out.println("Удаляем пользователя с accessToken: " + UserData.currentAccessToken);
            deleteUser(userUnique);
        }
    }
}
