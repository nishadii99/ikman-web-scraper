import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lk.ijse.Main;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {
    static WebDriver driver;

    @BeforeAll
    static void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get("https://ikman.lk/en/ad/agricultural-tool-cultivator-for-sale-colombo-49");
    }

//    @BeforeEach
//    void setUp2() {
//        System.out.println("Hi");
//    }
//
//    @AfterEach
//    void tearDown() {
//        System.out.println("Tear down");
//    }
    @Test
    void testLoadAndSaveData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();
        System.out.println("tempFile: " + tempFile.getAbsolutePath());

        List<Map<String, Object>> original = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", "John");
        original.add(item);

        Main.saveData(tempFile, mapper, original);

        List<Map<String, Object>> loaded = Main.loadData(tempFile, mapper);
        assertEquals(1, loaded.size());
        assertEquals("John", loaded.get(0).get("name"));
    }

    @Test
    void testPhoneClassName () {
        driver.findElement(By.className("call-text--30D-J")).click();
        List<WebElement> elements = driver.findElements(By.className("phone-numbers--2COKR"));
        assertFalse(elements.isEmpty(), "Phone numbers should be present");

        for (WebElement el : elements) {
            String phone = el.getText();
            assertNotNull(phone);
            assertFalse(phone.isEmpty(), "Phone number should not be empty");
        }
    }

    @Test
    void testNameClassName () {
        String name = driver.findElement(By.className("contact-name--m97Sb")).getAttribute("innerHTML");
        assertNotNull(name, "Name should not be null");
        assertFalse(name.trim().isEmpty(), "Name should not be empty");
    }

    @Test
    void testUrlExists() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> item = new HashMap<>();
        item.put("item_link", "https://test.com");
        list.add(item);

        assertTrue(Main.urlExists("https://test.com", list));

        assertFalse(Main.urlExists("https://new.com", list));
    }

    @AfterAll
    static void teardown() {
        driver.quit();
    }
}