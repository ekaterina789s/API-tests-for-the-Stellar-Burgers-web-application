import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.junit.BeforeClass;

import static data.UserData.BASE_URI;

public class BaseApiTest {

    public final Faker faker = new Faker();

    @BeforeClass
    public static void setUp(){
        RestAssured.baseURI = BASE_URI;


    }
}
