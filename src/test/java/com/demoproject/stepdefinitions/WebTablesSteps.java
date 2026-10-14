package com.demoproject.stepdefinitions;

import com.demoproject.pages.WebTablesPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;

/**
 * Module 5 (tables) - course table and fixed-header employee table.
 */
public class WebTablesSteps extends BaseSteps {

    private WebTablesPage page;

    private WebTablesPage page() {
        if (page == null) {
            page = new WebTablesPage();
        }
        return page;
    }

    @When("user views the course table")
    public void userViewsTheCourseTable() {
        page().scrollToCourseTable();
    }

    @Then("the course table should have {string} rows")
    public void theCourseTableShouldHaveRows(String countKey) {
        Assert.assertEquals(page().getCourseRowCount(), dataInt(countKey), "Course rows");
    }

    @Then("the course table headers should be")
    public void theCourseTableHeadersShouldBe(List<String> headerKeys) {
        Assert.assertEquals(page().getCourseHeaders(), dataList(headerKeys), "Course table headers");
    }

    @Then("the price of course {string} should be {string}")
    public void thePriceOfCourseShouldBe(String courseKey, String priceKey) {
        Assert.assertEquals(page().getCoursePrice(data(courseKey)), dataInt(priceKey), "Price of " + data(courseKey));
    }

    @Then("the total of all course prices should be {string}")
    public void theTotalOfAllCoursePricesShouldBe(String totalKey) {
        Assert.assertEquals(page().getCoursePriceTotal(), dataInt(totalKey), "Total course price");
    }

    @Then("the displayed total amount should equal the sum of the amount column")
    public void theDisplayedTotalAmountShouldEqualTheSum() {
        Assert.assertEquals(page().getDisplayedTotalAmount(), page().getEmployeeAmountTotal(),
                "Displayed total vs. sum of Amount column");
    }

    @Then("the employees located in {string} should be")
    public void theEmployeesLocatedInShouldBe(String cityKey, List<String> nameKeys) {
        Assert.assertEquals(page().getEmployeesInCity(data(cityKey)), dataList(nameKeys), "Employees in " + data(cityKey));
    }
}
