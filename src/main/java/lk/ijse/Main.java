package lk.ijse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static boolean checUrlExist(String targetLink, List<Map<String, Object>> list) {
        boolean exists = false;

        for (Map<String, Object> item : list) {
            String link = (String) item.get("item_link");

            if (targetLink.equals(link)) {
                exists = true;
                break;
            }
        }

        if (exists) {
            System.out.println("Link already exists!");
        } else {
            System.out.println("New link, safe to add.");
        }
        return exists;
    }
    public static void main(String[] args) throws InterruptedException, IOException {

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("data.json");
        List<Map<String, Object>> list;
        int pageCount = 2;
        try {
            for  (int i = 1; i < pageCount; i++) {
                driver.get("https://ikman.lk/en/ads?page="+i);
                String title = driver.getTitle();
                System.out.printf("Title: %s\n", title);

                List<WebElement> elements = driver.findElements(By.className("gtm-normal-ad"));
                List<String> links = new ArrayList<>();
                for (WebElement element : elements) {
                    WebElement link = element.findElement(By.tagName("a"));
                    String href = link.getAttribute("href");
                    links.add(href);
                }
                for (String link : links) {
                    if (file.exists()) {
                        list = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {
                        });
                    } else {
                        list = new ArrayList<>();
                    }
                    if (checUrlExist(link, list)) continue;
                    driver.get(link);
                    System.out.println(link);

                    try {
                        driver.findElement(By.className("title--1ku7l"));
                        continue;
                    } catch (Exception e) {
                    }
                    System.out.println(driver.getTitle());
                    String item_name = driver.findElement(By.className("title--3s1R8")).getAttribute("innerHTML");
                    System.out.println(item_name);
                    driver.findElement(By.xpath("//*[@id=\"app-wrapper\"]/div[1]/div[2]/div[2]/div[3]/div[2]/div/div[2]/div/div/div[1]/div[2]/div[1]/button/div[2]")).click();

                    List<WebElement> elements1 = driver.findElements(By.className("phone-numbers--2COKR"));
                    List<String> phones = new ArrayList<>();
                    for (WebElement element : elements1) {
                        String phone = element.getAttribute("innerHTML");
                        System.out.println(phone);
                        phones.add(phone);
                    }
                    String name = driver.findElement(By.className("contact-name--m97Sb")).getAttribute("innerHTML");
                    System.out.println(name);
                    Map<String, Object> newItem = new HashMap<>();
                    newItem.put("name", name);
                    newItem.put("item_name", item_name);
                    newItem.put("item_link", link);
                    newItem.put("phones", phones);

                    list.add(newItem);
                    mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
                }
            }
        } catch (Exception e) {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);

            String fileName = "error" + System.currentTimeMillis() + ".png";

            File dest = new File(fileName);
            FileHandler.copy(src, dest);
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}