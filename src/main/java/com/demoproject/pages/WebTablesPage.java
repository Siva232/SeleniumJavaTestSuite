package com.demoproject.pages;

import com.demoproject.exceptions.FrameworkException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * "Web Table Example" (courses) and "Web Table Fixed header" (employees) sections.
 */
public class WebTablesPage extends PracticePage {

    private static final By COURSE_TABLE = By.cssSelector("table[name='courses']");
    private static final By COURSE_HEADERS = By.cssSelector("table[name='courses'] th");
    private static final By COURSE_ROWS = By.xpath("//table[@name='courses']//tr[td]");
    private static final By EMPLOYEE_ROWS = By.cssSelector(".tableFixHead tbody tr");
    private static final By TOTAL_AMOUNT = By.cssSelector(".totalAmount");
    private static final Pattern NUMBER = Pattern.compile("(\\d+)");

    private static final int COURSE_COL_NAME = 1;
    private static final int COURSE_COL_PRICE = 2;
    private static final int EMPLOYEE_COL_NAME = 0;
    private static final int EMPLOYEE_COL_CITY = 2;
    private static final int EMPLOYEE_COL_AMOUNT = 3;

    /** Brings the course table into view (also useful for screenshots/videos). */
    public void scrollToCourseTable() {
        scrollIntoView(COURSE_TABLE);
    }

    /** @return number of course data rows (header excluded) */
    public int getCourseRowCount() {
        return courseRows().size();
    }

    /** @return course table column headers */
    public List<String> getCourseHeaders() {
        return findAll(COURSE_HEADERS).stream().map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    /**
     * @param courseName exact course name
     * @return its price
     */
    public int getCoursePrice(String courseName) {
        return courseRows().stream()
                .map(row -> row.findElements(By.tagName("td")))
                .filter(cells -> cells.get(COURSE_COL_NAME).getText().trim().equals(courseName))
                .map(cells -> Integer.parseInt(cells.get(COURSE_COL_PRICE).getText().trim()))
                .findFirst()
                .orElseThrow(() -> new FrameworkException("Course '" + courseName + "' not found in the course table"));
    }

    /** @return sum of the Price column */
    public int getCoursePriceTotal() {
        return courseRows().stream()
                .mapToInt(row -> Integer.parseInt(row.findElements(By.tagName("td")).get(COURSE_COL_PRICE)
                        .getText().trim()))
                .sum();
    }

    /** @return sum of the Amount column of the fixed-header table */
    public int getEmployeeAmountTotal() {
        return employeeRows().stream()
                .mapToInt(row -> Integer.parseInt(cellText(row, EMPLOYEE_COL_AMOUNT)))
                .sum();
    }

    /** @return the number shown in "Total Amount Collected: N" */
    public int getDisplayedTotalAmount() {
        scrollIntoView(TOTAL_AMOUNT);
        String text = getText(TOTAL_AMOUNT);
        Matcher matcher = NUMBER.matcher(text);
        if (!matcher.find()) {
            throw new FrameworkException("No amount found in '" + text + "'");
        }
        return Integer.parseInt(matcher.group(1));
    }

    /**
     * @param city city name
     * @return names of employees in that city, in table order
     */
    public List<String> getEmployeesInCity(String city) {
        return employeeRows().stream()
                .filter(row -> cellText(row, EMPLOYEE_COL_CITY).equals(city))
                .map(row -> cellText(row, EMPLOYEE_COL_NAME))
                .collect(Collectors.toList());
    }

    private List<WebElement> courseRows() {
        wait.present(COURSE_TABLE);
        return findAll(COURSE_ROWS);
    }

    /** The fixed-header table scrolls internally, so text is read via textContent. */
    private List<WebElement> employeeRows() {
        wait.present(EMPLOYEE_ROWS);
        return findAll(EMPLOYEE_ROWS);
    }

    private static String cellText(WebElement row, int column) {
        String text = row.findElements(By.tagName("td")).get(column).getDomProperty("textContent");
        return text == null ? "" : text.trim();
    }
}
