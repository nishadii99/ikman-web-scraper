package lk.ijse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;public class Main {

    private static final String DATA_FILE = "data.json";

    public static void main(String[] args) throws IOException, InterruptedException {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        ObjectMapper mapper = new ObjectMapper();

        File file = new File(DATA_FILE);
        List<Map<String, Object>> dataList = loadData(file, mapper);

        int pageCount = 2;
        try {
            for (int page = 1; page < pageCount; page++) {
                scrapePage(driver, page, dataList);
                saveData(file, mapper, dataList); // save after each page
            }
        } catch (Exception e) {
            takeScreenshot(driver);
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }

    public static List<Map<String, Object>> loadData(File file, ObjectMapper mapper) throws IOException {
        if (file.exists()) {
            return mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
        } else {
            return new ArrayList<>();
        }
    }

    public static void saveData(File file, ObjectMapper mapper, List<Map<String, Object>> dataList) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, dataList);
    }

    private static void scrapePage(WebDriver driver, int page, List<Map<String, Object>> dataList) {
        String url = "https://ikman.lk/en/ads?page=" + page;
        driver.get(url);

        System.out.println("Page: " + page + " | Title: " + driver.getTitle());

        List<WebElement> ads = driver.findElements(By.className("normal--2QYVk"));

        List<String> links = new ArrayList<>();

        for (WebElement ad : ads) {
            try {
                String link = ad.findElement(By.tagName("a")).getAttribute("href");
                System.out.println("Found link: " + link);

                links.add(link);
            } catch (Exception ignored) {}
        }
        for (String link : links) {
            System.out.println("Visiting: " + link);

            if (urlExists(link, dataList)) continue;
            driver.get(link);
            scrapeItem(driver, link, dataList);
        }
    }

    public static boolean urlExists(String targetLink, List<Map<String, Object>> dataList) {
        for (Map<String, Object> item : dataList) {
            if (targetLink.equals(item.get("item_link"))) {
                System.out.println("Link already exists: " + targetLink);
                return true;
            }
        }
        System.out.println("New link found: " + targetLink);
        return false;
    }

    private static void scrapeItem(WebDriver driver, String link, List<Map<String, Object>> dataList) {
        try {
            System.out.println("Scraping: " + link);

            if (!driver.findElements(By.className("title--3s1R8")).isEmpty()) {
                String itemName = driver.findElement(By.className("title--3s1R8")).getText();
                String name = driver.findElement(By.className("contact-name--m97Sb")).getText();


                List<WebElement> phoneButtons = driver.findElements(By.className("contact-number--jkttb"));
                if (!phoneButtons.isEmpty()) phoneButtons.get(0).click();


                List<WebElement> phoneElements = driver.findElements(By.className("phone-numbers--2COKR"));
                System.out.println(phoneElements.size());
                List<String> phones = new ArrayList<>();
                for (WebElement phoneEl : phoneElements) {
                    phones.add(phoneEl.getText());
                }

                Map<String, Object> newItem = new HashMap<>();
                newItem.put("name", name);
                newItem.put("item_name", itemName);
                newItem.put("item_link", link);
                newItem.put("phones", phones);

                dataList.add(newItem);
                System.out.println("Saved item: " + itemName);
            }

        } catch (Exception e) {
            System.err.println("Failed to scrape item: " + link);
        }
    }

    private static void takeScreenshot(WebDriver driver) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            String fileName = "error_" + System.currentTimeMillis() + ".png";
            FileHandler.copy(src, new File(fileName));
            System.out.println("Screenshot saved: " + fileName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}