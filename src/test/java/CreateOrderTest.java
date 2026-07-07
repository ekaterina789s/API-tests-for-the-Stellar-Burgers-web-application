import data.UserData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;
import user.LoginUserModel;
import user.UserModel;
import java.util.List;
import static data.UserData.PASSWORD;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;
import static steps.CreateOrderSteps.createOrderWithAuth;
import static steps.IngredientsSteps.getIngredients;
import static steps.LoginUserSteps.authorizationUser;
import static steps.UserSteps.createUniqueUser;

public class CreateOrderTest extends BaseApiTest {

    private UserModel userUnique;

    @Test
    @DisplayName("Создание заказа с ингредиентами и с авторизацией")
    @Description("Сервер должен успешно обработать запрос")
    public void testCreateOrder_withIng_withAuthorization() {
        //сначала нужно создать пользователя
        String email = faker.internet().emailAddress();
        String name = faker.name().fullName();

        userUnique = new UserModel(email, PASSWORD, name);
        var responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(200)
                .body("success", equalTo(true));

        //авторизуем пользователя
        LoginUserModel loginUser_forCreateOrder = new LoginUserModel(userUnique.getEmail(), PASSWORD);

        var responseLoginUser = authorizationUser(loginUser_forCreateOrder);
        responseLoginUser.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "));


        String accessToken = responseLoginUser.jsonPath().getString("accessToken"); //извлекаем значение токена
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
        var responseCreateOrder = createOrderWithAuth(idsArray);

        responseCreateOrder.then()
                .log().all()
                .statusCode(200)
                .body("name", notNullValue())
                .body("order.number", notNullValue())
                .body("success", equalTo(true));
    }
}
