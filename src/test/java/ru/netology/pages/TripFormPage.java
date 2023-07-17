package ru.netology.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;
import static org.testng.AssertJUnit.assertEquals;

public class TripFormPage {
    private static final SelenideElement dailyTripCard = $x("//div[@id='root']/div/div[contains(@class, 'card')]");

    private static final SelenideElement payButton = $x("//span[text()='Купить']//ancestor::button");
    private static final SelenideElement creditButton = $x("//span[text()='Купить в кредит']//ancestor::button");
    private static final SelenideElement form = $x("//form");
    private static final SelenideElement numberLabel = form.$x(".//span[text()='Номер карты']//ancestor::div/span");
    private static final SelenideElement numberInput = numberLabel.$x(".//ancestor::span//input");
    private static final SelenideElement monthLabel = form.$x(".//span[text()='Месяц']//ancestor::div/span/span[1]/span");
    private static final SelenideElement monthInput = monthLabel.$x(".//input");
    private static final SelenideElement yearLabel = form.$x(".//span[text()='Год']//ancestor::div/span/span[2]/span");
    private static final SelenideElement yearInput = yearLabel.$x(".//input");
    private static final SelenideElement holderLabel = form.$x(".//span[text()='Владелец']//ancestor::div/span/span[1]/span");
    private static final SelenideElement holderInput = holderLabel.$x(".//input");
    private static final SelenideElement cvcLabel = form.$x(".//span[text()='CVC/CVV']//ancestor::div/span/span[2]/span");
    private static final SelenideElement cvcInput = cvcLabel.$x(".//input");
    private static final SelenideElement continuousButton = form.$x(".//span[text()='Продолжить']//ancestor::button");
    private static final SelenideElement successNotification = $x("//div[contains(@class, 'notification_status_ok')]");
    private static final SelenideElement errorNotification = $x("//div[contains(@class, 'notification_status_error')]");

    public TripFormPage() {
        dailyTripCard.should(visible);
        payButton.should(visible);
        creditButton.should(visible);

        form.should(visible);
        successNotification.should(Condition.hidden);
        errorNotification.should(Condition.hidden);
    }

    public void insertingValueInForm(String number, String month, String year, String holder, String cvc) {
        numberLabel.click();
        numberInput.val(number);
        monthLabel.click();
        monthInput.val(month);
        yearLabel.click();
        yearInput.val(year);
        holderLabel.click();
        holderInput.val(holder);
        cvcLabel.click();
        cvcInput.val(cvc);
        continuousButton.click();
    }

    public void matchesByInsertValue(String number, String month, String year, String holder, String cvc) {
        assertEquals(number, numberInput.getValue());
        assertEquals(month, monthInput.getValue());
        assertEquals(year, yearInput.getValue());
        assertEquals(holder, holderInput.getValue());
        assertEquals(cvc, cvcInput.getValue());
    }

    public void BuyOperationIsSuccessful() {
        successNotification.shouldBe(visible, Duration.ofSeconds(11));
    }

    public void BuyOperationWithErrorNotification() {
        errorNotification.shouldBe(visible, Duration.ofSeconds(11));
    }

    public void assertNumberFieldIsEmptyValue() {
        numberLabel.should(Condition.cssClass("input_invalid")).shouldNot(Condition.cssClass("input_has-value"));
        numberLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Поле обязательно для заполнения"));
    }

    public void assertNumberFieldIsInvalidValue() {
        numberLabel.should(Condition.cssClass("input_invalid"), Condition.cssClass("input_has-value"));
        numberLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Неверный формат"));
    }

    public void assertMonthFieldIsEmptyValue() {
        monthLabel.should(Condition.cssClass("input_invalid")).shouldNot(Condition.cssClass("input_has-value"));
        monthLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Поле обязательно для заполнения"));
    }

    public void assertMonthFieldIsInvalidValue() {
        monthLabel.should(Condition.cssClass("input_invalid"), Condition.cssClass("input_has-value"));
        monthLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Неверно указан срок действия карты"));
    }

    public void assertYearFieldIsEmptyValue() {
        yearLabel.should(Condition.cssClass("input_invalid"));
        yearLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Поле обязательно для заполнения"));
    }

    public void assertYearFieldIsInvalidValue() {
        yearLabel.should(Condition.cssClass("input_invalid"), Condition.cssClass("input_has-value"));
        yearLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Истёк срок действия карты"));
    }

    public void assertHolderFieldIsEmptyValue() {
        holderLabel.should(Condition.cssClass("input_invalid")).shouldNot(Condition.cssClass("input_has-value"));
        holderLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Поле обязательно для заполнения"));
    }

    public void assertHolderFieldIsInvalidValue() {
        holderLabel.should(Condition.cssClass("input_invalid"), Condition.cssClass("input_has-value"));
        holderLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Неверный формат"));
    }

    public void assertCvcFieldIsEmptyValue() {
        cvcLabel.should(Condition.cssClass("input_invalid")).shouldNot(Condition.cssClass("input_has-value"));
        cvcLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Поле обязательно для заполнения"));
    }

    public void assertCvcFieldIsInvalidValue() {
        cvcLabel.should(Condition.cssClass("input_invalid"), Condition.cssClass("input_has-value"));
        cvcLabel.$x(".//span[@class='input__sub']").should(visible, Condition.text("Неверный формат"));
    }
}
