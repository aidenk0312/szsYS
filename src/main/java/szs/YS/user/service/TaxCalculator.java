package szs.YS.user.service;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

public class TaxCalculator {

    private static final Logger log = Logger.getLogger(TaxCalculator.class.getName());

    private static String formatNumber(double value) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.KOREA);
        formatter.applyPattern("#,##0.###"); // 소수점 세 자리까지만 표시
        return formatter.format(value);
    }

    public static JSONObject calculateTax(String scrapResult) {
        JSONObject responseJson = new JSONObject(scrapResult);
        JSONObject data = responseJson.getJSONObject("data").getJSONObject("jsonList");

        double calculatedTaxAmount = parseDouble(data.getString("산출세액"));
        double totalSalary = parseDouble(data.getJSONArray("급여").getJSONObject(0).getString("총지급액"));

        double insurancePremium = 0;
        double medicalExpenses = 0;
        double educationExpenses = 0;
        double donationAmount = 0;
        double pensionContribution = 0;

        for (int i = 0; i < data.getJSONArray("소득공제").length(); i++) {
            JSONObject deduction = data.getJSONArray("소득공제").getJSONObject(i);
            String type = deduction.getString("소득구분");
            switch (type) {
                case "보험료":
                    insurancePremium = parseDouble(deduction.getString("금액"));
                    break;
                case "교육비":
                    educationExpenses = parseDouble(deduction.getString("금액"));
                    break;
                case "기부금":
                    donationAmount = parseDouble(deduction.getString("금액"));
                    break;
                case "의료비":
                    medicalExpenses = parseDouble(deduction.getString("금액"));
                    break;
                case "퇴직연금":
                    pensionContribution = parseDouble(deduction.getString("총납임금액"));
                    break;
            }
        }

        double laborIncomeTaxCredit = calculatedTaxAmount * 0.55;
        double pensionTaxCredit = pensionContribution * 0.15;
        double insuranceDeduction = insurancePremium * 0.12;
        double medicalDeduction = Math.max((medicalExpenses - totalSalary * 0.03) * 0.15, 0);
        double educationDeduction = educationExpenses * 0.15;
        double donationDeduction = donationAmount * 0.15;

        double specialTaxCredit = insuranceDeduction + medicalDeduction + educationDeduction + donationDeduction;
        double standardTaxCredit = specialTaxCredit < 130000 ? 130000 : 0;
        if (standardTaxCredit == 130000) {
            specialTaxCredit = 0;
        }

        double finalTax = calculatedTaxAmount - laborIncomeTaxCredit - specialTaxCredit - standardTaxCredit - pensionTaxCredit;
        finalTax = Math.max(finalTax, 0);

        JSONObject taxResult = new JSONObject();
        taxResult.put("결정세액", formatNumber(finalTax));
        taxResult.put("퇴직연금세액공제", formatNumber(pensionTaxCredit));
        taxResult.put("근로소득세액공제금액", formatNumber(laborIncomeTaxCredit));
        taxResult.put("특별세액공제금액", formatNumber(specialTaxCredit));
        taxResult.put("산출세액", formatNumber(calculatedTaxAmount));
        taxResult.put("총지급액", formatNumber(totalSalary));
        taxResult.put("보험료", formatNumber(insurancePremium));
        taxResult.put("의료비", formatNumber(medicalExpenses));
        taxResult.put("교육비", formatNumber(educationExpenses));
        taxResult.put("기부금", formatNumber(donationAmount));
        taxResult.put("퇴직연금", formatNumber(pensionContribution));

        return taxResult;
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            log.warning("Unable to parse double from: " + value);
            return 0;
        }
    }
}
