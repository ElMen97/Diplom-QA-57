package ru.netology.tests;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;
import org.testng.annotations.*;
import ru.netology.helpers.DataHelper;
import ru.netology.helpers.DbHelper;
import ru.netology.pages.TripCardPage;
import ru.netology.pages.TripFormPage;

import java.util.List;

import static com.codeborne.selenide.Selenide.open;
import static org.testng.AssertJUnit.*;

@Epic("Frontend тестирование функционала Путешествие дня")
@Feature("Покупка тура по карте")
public class PaymentUiTests {
    private static DataHelper.CardData cardData;
    private static TripCardPage tripCard;
    private static TripFormPage tripForm;
    private static List<DbHelper.PaymentEntity> payments;
    private static List<DbHelper.CreditRequestEntity> credits;
    private static List<DbHelper.OrderEntity> orders;

    @BeforeClass
    public void setupClass() {
        DbHelper.setDown();
        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true).savePageSource(true));
    }

    @BeforeMethod
    public void setupMethod() {
        open("http://localhost:8080/");
        tripCard = new TripCardPage();
    }

    @AfterMethod
    public void setDownMethod() {
        DbHelper.setDown();
    }

    @AfterClass
    public void setDownClass() {
        SelenideLogger.removeListener("allure");
    }

    @Story("HappyPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldHappyPath() {
        cardData = DataHelper.getValidApprovedCard();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();

        payments = DbHelper.getPayments();
        credits = DbHelper.getCreditsRequest();
        orders = DbHelper.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(tripCard.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("approved"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Story("SadPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldSadPath() {
        cardData = DataHelper.getValidDeclinedCard();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertBuyOperationWithErrorNotification();

        payments = DbHelper.getPayments();
        credits = DbHelper.getCreditsRequest();
        orders = DbHelper.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(tripCard.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Story("№2.1.1 Оставление поля пустым, остальные поля заполнены валидно")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyNumber() {
        cardData = DataHelper.getValidApprovedCard();
        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm("", cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue("", cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertNumberFieldIsEmptyValue();
    }

    @Story("№2.1.2 Заполнение поля 15 рандомными цифрами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith11DigitsInNumber() {
        cardData = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateInvalidCardNumberWith15Digits();
        var matchesNumber = number;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertNumberFieldIsInvalidValue();
    }

    @Story("№2.1.3 Заполнение поля 20 рандомными цифрами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith20DigitsInNumber() {
        cardData = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateInvalidCardNumberWith20Digits();
        var matchesNumber = number;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertNumberFieldIsInvalidValue();
    }

    @Story("№2.1.4 Заполнение поля 16 рандомными цифрами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldUnsuccessfulWith16DigitsInNumber() {
        cardData = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateValidCardNumberWith16Digits();
        var matchesNumber = number;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertBuyOperationWithErrorNotification();
    }

    @Story("№2.1.5 Заполнение поля 16 рандомными символами (не цифрами), остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithInvalidSymbolsInNumber() {
        cardData = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateInvalidCardNumberWith16RandomSymbols();
        var matchesNumber = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertNumberFieldIsEmptyValue();
    }

    @Story("№2.2.1 Оставление поля пустым, остальные поля заполнены валидно")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = "";
        var matchesMonth = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertMonthFieldIsEmptyValue();
    }

    @Story("№2.2.2 Заполнение поля номером от 1 до 9, остальные поля заполнены валидно")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldAddingNullInMonthWith1Digit() {
        cardData = DataHelper.getValidApprovedCard();
        var month = DataHelper.generateRandomOneDigit();
        var matchesMonth = "0" + month;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("№2.2.3 Заполнение поля 3 цифрами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldDeletingThirdDigitInMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = cardData.getMonth() + DataHelper.generateRandomOneDigit();
        var matchesMonth = cardData.getMonth();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("№2.2.4 Заполнение поля значением 00, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith00InMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = "00";
        var matchesMonth = month;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertMonthFieldIsInvalidValue();
    }

    @Story("№2.2.5 Заполнение поля значением 13, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith13InMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = "13";
        var matchesMonth = month;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertMonthFieldIsInvalidValue();
    }

    @Story("№2.2.6 Заполнение поля двумя рандомными символами(не цифрами), остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithInvalidSymbolsInMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = DataHelper.generateMonthWithRandomSymbols();
        var matchesMonth = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertMonthFieldIsEmptyValue();
    }

    @Story("№2.2.7 Заполнение поля Год текущим годом, а поля Месяц предыдущим месяцем, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithPrevMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = DataHelper.generateMonth(-1);
        var matchesMonth = month;
        var year = DataHelper.generateYear(0);
        var matchesYear = year;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, year, cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, matchesYear, cardData.getHolder(), cardData.getCvc());
        tripForm.assertMonthFieldIsInvalidValue();
    }

    @Story("№2.3.1 Оставление поля пустым")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyYear() {
        cardData = DataHelper.getValidApprovedCard();
        var year = "";
        var matchesYear = year;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), year, cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), matchesYear, cardData.getHolder(), cardData.getCvc());
        tripForm.assertYearFieldIsEmptyValue();
    }

    @Story("№2.3.2 Заполнение поля предыдущим годом, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithPrevYear() {
        cardData = DataHelper.getValidApprovedCard();
        var year = DataHelper.generateYear(-1);
        var matchesYear = year;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), year, cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), matchesYear, cardData.getHolder(), cardData.getCvc());
        tripForm.assertYearFieldIsInvalidValue();
    }

    @Story("№2.3.3 Заполнение поля следующим годом от текущего, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithNextYear() {
        cardData = DataHelper.getValidApprovedCard();
        var year = DataHelper.generateYear(+1);
        var matchesYear = year;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), year, cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), matchesYear, cardData.getHolder(), cardData.getCvc());
        tripForm.assertYearFieldIsInvalidValue();
    }

    @Story("№2.4.1 Оставление поля пустым, остальные поля заполнены валидно")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = "";
        var matchesHolder = holder;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertHolderFieldIsEmptyValue();
    }

    @Story("№2.4.2 Заполнение поля в нижнем регистре, остальные поля заполнены валидно")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldAutoUpperCaseInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = cardData.getHolder().toLowerCase();
        var matchesHolder = cardData.getHolder();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("№2.4.3 Заполнение поля с пробелами в начале и в конце, остальные поля заполнены валидно")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldAutoDeletingStartEndHyphenInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = " " + cardData.getHolder() + " ";
        var matchesHolder = cardData.getHolder();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("№2.4.4 Заполнение поля \"Владелец\" с дефисами в начале и в конце, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldAutoDeletingStartEndSpacebarInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = "-" + cardData.getHolder() + "-";
        var matchesHolder = cardData.getHolder();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("№2.4.5 Заполнение поля \"Владелец\" кириллицей, остальные поля заполнены валидно")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotificationWithCyrillicInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateInvalidHolderWithCyrillicSymbols();
        var matchesHolder = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertHolderFieldIsEmptyValue();
    }

    @Story("№2.4.6 Заполнение поля \"Владелец\" рандомными спецсимволами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithInvalidSymbolInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithInvalidSymbols();
        var matchesHolder = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertHolderFieldIsEmptyValue();
    }

    @Story("№2.5.1 Оставление поля пустым, остальные поля заполнены валидно")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyCVC() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = "";
        var matchesCvc = cvc;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertCvcFieldIsEmptyValue();
    }

    @Story("№2.5.2 Заполнение поля \"CVC/CVV\" 2 рандомными цифрами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith2DigitsInCVC() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateInvalidCVCWith2Digit();
        var matchesCvc = cvc;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertCvcFieldIsInvalidValue();
    }

    @Story("№2.5.3 Заполнение поля 4 рандомными цифрами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldSuccessfulWith4DigitsInCVC() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = cardData.getCvc() + DataHelper.generateRandomOneDigit();
        var matchesCvc = cardData.getCvc();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("№2.5.4/№2.5.5 Заполнение поля \"CVC/CVV\" 3 рандомными буквами/спецсимволами, остальные поля заполнены валидно")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithInvalidSymbolsInCVC() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateInvalidCVCWithRandomSymbols();
        var matchesCvc = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertCvcFieldIsEmptyValue();
    }
}